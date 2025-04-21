package io.thingshub.transport.mqtt.handler;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_ACCEPTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE;
import static io.thingshub.transport.throttler.ORCondition.or;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.net.ssl.SSLPeerUnverifiedException;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.ssl.SslHandler;
import io.thingshub.Broker;
import io.thingshub.domain.Device;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.service.DeviceService;
import io.thingshub.service.InboxService;
import io.thingshub.service.ScriptInfoService;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.topic.TopicUtil;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.auth.AuthData;
import io.thingshub.transport.auth.AuthData.AuthDataBuilder;
import io.thingshub.transport.auth.AuthManager;
import io.thingshub.transport.auth.AuthResult;
import io.thingshub.transport.auth.AuthResult.ResultType;
import io.thingshub.transport.mqtt.MqttServerConfig;
import io.thingshub.transport.mqtt.codec.MqttTransformDecoder;
import io.thingshub.transport.mqtt.codec.MqttTransformEncoder;
import io.thingshub.transport.mqtt.handler.v3.MQTT3SessionHandler;
import io.thingshub.transport.mqtt.message.LastWillMessage;
import io.thingshub.transport.throttler.Condition;
import io.thingshub.transport.throttler.DirectMemPressureCondition;
import io.thingshub.transport.throttler.HeapMemPressureCondition;
import io.thingshub.transport.throttler.InboundResourceCondition;
import io.thingshub.transport.throttler.ResourceThrottler;
import io.thingshub.transport.throttler.ResourceType;
import io.thingshub.transport.throttler.SlowdownInboundHandler;
import io.thingshub.utils.TaskTracker;
import io.thingshub.utils.UTF8Util;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT connection handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class MQTTConnectionHandler extends ChannelDuplexHandler {

	private final TypeReference<JSONObject> LASTWILL_PAYLOAD_TYPE_REFERENCE = new TypeReference<JSONObject>() {
	};

	private static final int MAX_CLIENT_ID_LENGTH = 65535;

	private final MqttServerConfig mqttServerConfig;

	private final ResourceThrottler resourceThrottler;

	private final AuthManager authManager;

	private final ScriptInfoService scriptInfoService;

	private final ScriptEngineFactory scriptEngineFactory;

	private final DeviceService deviceService;

	private final InboxService inboxService;

	private final SubscriptionManager subscriptionManager;

	public MQTTConnectionHandler(MqttServerConfig mqttServerConfig) {
		this.mqttServerConfig = mqttServerConfig;

		this.authManager = Broker.getBean(AuthManager.class);
		this.resourceThrottler = Broker.getBean(ResourceThrottler.class);
		this.scriptInfoService = Broker.getBean(ScriptInfoService.class);
		this.scriptEngineFactory = Broker.getBean(ScriptEngineFactory.class);
		this.deviceService = Broker.getBean(DeviceService.class);
		this.inboxService = Broker.getBean(InboxService.class);
		this.subscriptionManager = Broker.getBean(SubscriptionManager.class);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		MqttMessage mqttMessage = (MqttMessage) msg;

		if (mqttMessage.fixedHeader().messageType() == MqttMessageType.CONNECT) {
			MqttConnectMessage connMsg = (MqttConnectMessage) msg;
			String clientId = connMsg.payload().clientIdentifier();

			if (StrUtil.isBlank(clientId)) {// 本平台要求Client提供不为空的client id
				closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_IDENTIFIER_REJECTED).build());

				return;

//				if (!connMsg.variableHeader().isCleanSession()) {
//					// If the Client supplies a zero-byte ClientId, the Client MUST also set CleanSession to 1
//					log.error("protocol error: client id is empty but clean session is set to false");
//					closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_IDENTIFIER_REJECTED).build());
//					return;
//				}
			}

			if (!UTF8Util.isWellFormed(clientId, false)) {
				log.error("protocol error: include unacceptable chars in client id");
				closeChannel(ctx, null);
				return;
			}

			if (clientId.length() > MAX_CLIENT_ID_LENGTH) {
				log.error("protocol error: client id exceeds max length");
				closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_IDENTIFIER_REJECTED).build());
				return;
			}

			if (connMsg.variableHeader().hasUserName() && !UTF8Util.isWellFormed(connMsg.payload().userName(), false)) {
				log.error("protocol error: include unacceptable chars in user name");
				closeChannel(ctx, null);
				return;
			}
			if (connMsg.variableHeader().isWillFlag() && !UTF8Util.isWellFormed(connMsg.payload().willTopic(), false)) {
				log.error("protocol error: include unacceptable chars in will topic");
				closeChannel(ctx, null);
				return;
			}

			if (StrUtil.isBlank(connMsg.payload().userName()) || connMsg.payload().passwordInBytes() == null) {
				log.error("MQTT client's user name and password can not be empty");

				closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD).build());
				return;
			}

			AuthData authData = buildAuthData(ctx.channel(), connMsg);
			TaskTracker.getInstance().track(CompletableFuture.supplyAsync(() -> authManager.authenticate(authData))).exceptionally(e -> {
				log.error("Unexpected error from auth manager, error: {}", e);

				return AuthResult.builder().clientId(authData.getClientId()).result(ResultType.ERROR.value()).build();
			}).thenAccept(authResult -> {
				switch (authResult.getType()) {
				case OK:
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalConnections)) {
						closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_SERVER_UNAVAILABLE).build());
						return;
					}
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalSessionMemoryBytes)) {
						closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_SERVER_UNAVAILABLE).build());
						return;
					}
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalConnectPerSecond)) {
						closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_SERVER_UNAVAILABLE).build());
						return;
					}

					ValidateError error = validate(connMsg);
					if (error != null) {
						closeChannel(ctx, error.getFarewell());
						return;
					}

					ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).set(clientId);
					Device device = deviceService.getBySn(clientId);
					ScriptInfo scriptInfo = null;
					if (device != null) {
						ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).set(device.getProductCode());

						scriptInfo = scriptInfoService.getScriptInfo(device.getProductCode());
						if (scriptInfo != null) {
							ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).set(scriptInfo.getLang());
						}
					}

					Condition slowdownCondition = or(DirectMemPressureCondition.INSTANCE, HeapMemPressureCondition.INSTANCE,
							new InboundResourceCondition(resourceThrottler, authResult.getTenant()));
					SlowdownInboundHandler slowdownHandler = new SlowdownInboundHandler(slowdownCondition);
					ctx.pipeline().addFirst(ctx.executor(), SlowdownInboundHandler.class.getSimpleName(), slowdownHandler);

					MqttTransformDecoder mqttTransformDecoder = Broker.getBean(MqttTransformDecoder.class);
					ctx.pipeline().addAfter(ctx.executor(), MqttDecoder.class.getSimpleName(), MqttTransformDecoder.class.getSimpleName(),
							mqttTransformDecoder);
					MqttTransformEncoder mqttTransformEncoder = Broker.getBean(MqttTransformEncoder.class);
					ctx.pipeline().addBefore(ctx.executor(), this.getClass().getSimpleName(), MqttTransformEncoder.class.getSimpleName(), mqttTransformEncoder);

					// TODO isClearSession (MQTT 3 and MQTT 5)
					boolean isCleanSession = connMsg.variableHeader().isCleanSession();
					if (isCleanSession) {// clean state data
						inboxService.cleanUnackedDeliveries(clientId);
						subscriptionManager.cleanClientSubscriptions(clientId);
					}

					/**
					 * build last will message
					 */
					LastWillMessage lwm = null;
					if (connMsg.variableHeader().isWillFlag()) {
						JSONObject payload = null;
						byte[] willMsgInBytes = connMsg.payload().willMessageInBytes();

						if (device != null) {
							if (scriptInfo != null) {
								ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine(scriptInfo.getLang());
								payload = scriptEngine.invoke(device.getProductCode(), LASTWILL_PAYLOAD_TYPE_REFERENCE, "lastWill", willMsgInBytes);
							} else {
								payload = JSON.parseObject(willMsgInBytes);
							}
						} else {
							payload = JSON.parseObject(willMsgInBytes);
						}

						lwm = LastWillMessage.builder().rawData(new String(willMsgInBytes)).payload(payload).retain(connMsg.variableHeader().isWillRetain())
								.topic(connMsg.payload().willTopic()).qos(connMsg.variableHeader().willQos()).build();
					}

					/**
					 * set keep alive
					 */
					int keepAlive = connMsg.variableHeader().keepAliveTimeSeconds();
					if (keepAlive == 0) {
						keepAlive = mqttServerConfig.getKeepAlive();
					}
					keepAlive = Math.max(5, keepAlive);
					keepAlive = Math.min(keepAlive, 2 * 60 * 60);// max keep alive

					// TODO getSessionExpiryInterval (MQTT 3 and MQTT 5)
					int sessionExpiryInterval = 0;
					if (!isCleanSession) {
						sessionExpiryInterval = 24 * 60 * 60;// default session expiry interval
					}
					// TODO isSessionPresent (MQTT 3 and MQTT 5)
					final boolean sessionPresent = isCleanSession ? false : (sessionExpiryInterval == 0 ? false : true);

					// TODO buildSessionHandler
					MQTT3SessionHandler sessionHandler = new MQTT3SessionHandler(authResult.getTenant(), clientId, keepAlive, sessionPresent,
							sessionExpiryInterval, lwm);
					sessionHandler.onInitialized(v -> {
						log.info("MQTT client [{}] connected", clientId);

						// TODO buildConnAck (MQTT 3 and MQTT 5)
						MqttConnAckMessage connAckMsg = MqttMessageBuilders.connAck().sessionPresent(sessionPresent).returnCode(CONNECTION_ACCEPTED).build();
						final AtomicReference<Boolean> connAckedRef = new AtomicReference<>(Boolean.FALSE);
						ctx.writeAndFlush(connAckMsg).addListener(new ChannelFutureListener() {

							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								if (!future.isSuccess()) {
									log.error("Server failed to sending ConnAck message. Client ID: {}", clientId);
									if (future.cause() != null) {
										log.error("", future.cause());
									}
								} else {
									connAckedRef.set(Boolean.TRUE);
								}
							}

						}).awaitUninterruptibly(15, TimeUnit.SECONDS);

						return connAckedRef.get();
					});

					ctx.pipeline().replace(this, MQTTSessionHandler.class.getSimpleName(), sessionHandler);

					return;
				case BAD_PASS:
					closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD).build());
					return;
				case NOT_AUTHORIZED:
					closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_NOT_AUTHORIZED).build());
					return;
				default:
					log.error("Unexpected error from auth manager, auth result: {}", authResult.getType().name());

					closeChannel(ctx, MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_SERVER_UNAVAILABLE).build());
					return;
				}
			});
		} else {
			if (mqttMessage.decoderResult().isSuccess()) {
				// TODO MQTT 5 处理 Auth消息
			} else {
				log.error(mqttMessage.decoderResult().cause().getMessage());
				closeChannel(ctx, null);
			}
		}
	}

	@Builder
	@Getter
	public static class ValidateError {

		private String clientId;

		private MqttMessage farewell;

		private String reason;

	};

	private AuthData buildAuthData(Channel channel, MqttConnectMessage msg) {
		AuthDataBuilder builder = AuthData.builder();
		SslHandler sslHandler = channel.pipeline().get(SslHandler.class);

		if (sslHandler != null) {
			try {
				Certificate[] certChains = sslHandler.engine().getSession().getPeerCertificates();
				if (certChains != null && certChains.length != 0) {
					X509Certificate cert = (X509Certificate) certChains[0];

					builder.cert(Base64.getEncoder().encode(cert.getEncoded()));
				}
			} catch (SSLPeerUnverifiedException | CertificateEncodingException ex) {
			}
		}

		builder.clientId(msg.payload().clientIdentifier()).username(msg.payload().userName()).password(new String(msg.payload().passwordInBytes()));

		InetSocketAddress remoteAddr = (InetSocketAddress) channel.remoteAddress();
		if (remoteAddr != null) {
			builder.remotePort(remoteAddr.getPort());
			InetAddress ip = remoteAddr.getAddress();
			if (remoteAddr.getAddress() != null) {
				builder.remoteAddr(ip.getHostAddress());
			}
		}

		return builder.build();
	}

	private void closeChannel(@Nullable ChannelHandlerContext ctx, @Nullable MqttMessage farewell) {
		assert ctx.executor().inEventLoop();

		if (!ctx.channel().isActive()) {
			return;
		}

		ctx.executor().schedule(() -> {
			if (farewell != null) {
				ctx.writeAndFlush(farewell).addListener(ChannelFutureListener.CLOSE);
			} else {
				ctx.channel().close();
			}
		}, ThreadLocalRandom.current().nextInt(100, 5000), TimeUnit.MILLISECONDS);
	}

	protected ValidateError validate(MqttConnectMessage message
//			,TenantSettings settings, ClientInfo clientInfo
	) {
//		if (message.variableHeader().version() == 3
//		// && !settings.mqtt3Enabled
//		) {
//			return MqttValidateError.builder().farewell(MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION).build())
//					.reason("protocol violation: MQTT3.1 not enabled").build();
////			return new GoAway(MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION).build(),
////					getLocal(ProtocolViolation.class).statement("MQTT3.1 not enabled").clientInfo(clientInfo));
//		}
//		if (message.variableHeader().version() == 4
//		// && !settings.mqtt4Enabled
//		) {
//			return MqttValidateError.builder().farewell(MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION).build())
//					.reason("protocol violation: MQTT3.1.1 not enabled").build();
//		}
		if (message.variableHeader().isWillFlag()) {
			if (!TopicUtil.isValidTopic(message.payload().willTopic(), 40, 16, 255
			// , settings.maxTopicLevelLength, settings.maxTopicLevels, settings.maxTopicLength
			)) {
				return ValidateError.builder().reason(StrFormatter.format("invalid topic: {}", message.payload().willTopic())).build();
			}
//			if (message.variableHeader().isWillRetain()
//			// && !settings.retainEnabled
//			) {
//				return MqttValidateError.builder().reason("protocol violation: retain not supported").build();
//			}
			if (message.variableHeader().willQos() > 2
			// settings.maxQoS.getNumber()
			) {
				return ValidateError.builder().reason("protocol violation: Will QoS not supported").build();
			}
		}

		return null;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.warn("ctx: {}, cause:", ctx, cause);
		ctx.channel().close();
	}

}