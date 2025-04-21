package io.thingshub.transport.tcp.handler;

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

import javax.net.ssl.SSLPeerUnverifiedException;

import com.alibaba.fastjson2.JSONObject;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.SslHandler;
import io.thingshub.Broker;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.auth.AuthData;
import io.thingshub.transport.auth.AuthData.AuthDataBuilder;
import io.thingshub.transport.auth.AuthManager;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.codec.ThingMessageCode;
import io.thingshub.transport.codec.ThingMethod;
import io.thingshub.transport.tcp.TcpServerConfig;
import io.thingshub.transport.tcp.codec.TcpTransformDecoder;
import io.thingshub.transport.tcp.codec.TcpTransformEncoder;
import io.thingshub.transport.throttler.Condition;
import io.thingshub.transport.throttler.DirectMemPressureCondition;
import io.thingshub.transport.throttler.HeapMemPressureCondition;
import io.thingshub.transport.throttler.InboundResourceCondition;
import io.thingshub.transport.throttler.ResourceThrottler;
import io.thingshub.transport.throttler.ResourceType;
import io.thingshub.transport.throttler.SlowdownInboundHandler;
import io.thingshub.utils.TaskTracker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpAuthenticateHandler extends ChannelDuplexHandler {

	private final TcpServerConfig tcpServerConfig;

	private final ResourceThrottler resourceThrottler;

	private final AuthManager authManager;

	private ChannelHandlerContext ctx;

	public TcpAuthenticateHandler(TcpServerConfig tcpServerConfig) {
		this.tcpServerConfig = tcpServerConfig;

		this.authManager = Broker.getBean(AuthManager.class);
		this.resourceThrottler = Broker.getBean(ResourceThrottler.class);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ThingMessage inboundMessage = (ThingMessage) msg;
		String deviceSn = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).get();

		if (ThingMethod.AUTH.code() == inboundMessage.getMethod()) {
			String username = null;
			String password = null;

			JSONObject params = inboundMessage.getParams();
			if (params != null) {
				username = params.getString("username");
				password = params.getString("password");
			}

			AuthData authData = buildAuthData(ctx.channel(), deviceSn, username, password);
			TaskTracker.getInstance().track(CompletableFuture.supplyAsync(() -> authManager.authenticate(authData))).thenAccept(authResult -> {
				switch (authResult.getType()) {
				case OK:
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalConnections)) {
						log.error("Tenant's resource quota exceeded. tenant: {}, resource type: {}", authResult.getTenant(), ResourceType.TotalConnections);

						ThingMessage farewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
								.code(ThingMessageCode.CONNECTION_QUOTA_EXCEEDED.code()).message(ThingMessageCode.CONNECTION_QUOTA_EXCEEDED.desc()).build();
						closeChannel(farewell);
						return;
					}
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalSessionMemoryBytes)) {
						log.error("Tenant's resource quota exceeded. tenant: {}, resource type: {}", authResult.getTenant(),
								ResourceType.TotalSessionMemoryBytes);

						ThingMessage farewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
								.code(ThingMessageCode.RESOURCE_QUOTA_EXCEEDED.code()).message(ThingMessageCode.RESOURCE_QUOTA_EXCEEDED.desc()).build();
						closeChannel(farewell);
						return;
					}
					if (!resourceThrottler.hasResource(authResult.getTenant(), ResourceType.TotalConnectPerSecond)) {
						log.error("Tenant's resource quota exceeded. tenant: {}, resource type: {}", authResult.getTenant(),
								ResourceType.TotalConnectPerSecond);

						ThingMessage farewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
								.code(ThingMessageCode.CONNECTION_RATE_EXCEEDED.code()).message(ThingMessageCode.CONNECTION_RATE_EXCEEDED.desc()).build();
						closeChannel(farewell);
						return;
					}

					Condition slowdownCondition = or(DirectMemPressureCondition.INSTANCE, HeapMemPressureCondition.INSTANCE,
							new InboundResourceCondition(resourceThrottler, authResult.getTenant()));
					SlowdownInboundHandler slowdownHandler = new SlowdownInboundHandler(slowdownCondition);
					ctx.pipeline().addFirst(ctx.executor(), SlowdownInboundHandler.class.getSimpleName(), slowdownHandler);

					TcpTransformDecoder tcpTransformDecoder = Broker.getBean(TcpTransformDecoder.class);
					ctx.pipeline().addBefore(ctx.executor(), this.getClass().getSimpleName(), TcpTransformDecoder.class.getSimpleName(), tcpTransformDecoder);
					TcpTransformEncoder tcpTransformEncoder = Broker.getBean(TcpTransformEncoder.class);
					ctx.pipeline().addBefore(ctx.executor(), this.getClass().getSimpleName(), TcpTransformEncoder.class.getSimpleName(), tcpTransformEncoder);

					// TODO 获取设备的保活时间（由终端上传参数或在设备管理进行设置）
					int keepAlive = tcpServerConfig.getKeepAlive();
					keepAlive = Math.max(5, keepAlive);
					keepAlive = Math.min(keepAlive, 2 * 60 * 60);

					TcpSessionHandler sessionHandler = new TcpSessionHandler(authResult.getTenant(), deviceSn, keepAlive);
					sessionHandler.onInitialized(v -> {
						log.info("Device [{}] connected", deviceSn);

						ThingMessage authReply = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
								.code(ThingMessageCode.SUCCESS.code()).message(ThingMessageCode.SUCCESS.desc()).build();
						ctx.writeAndFlush(authReply).addListener(new ChannelFutureListener() {

							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								if (!future.isSuccess()) {
									log.error("Server has authenticated device [{}] but failed to send the reply to device");
									if (future.cause() != null) {
										log.error("", future.cause());
									}
								}
							}

						});
					});
					ctx.pipeline().replace(this, TcpSessionHandler.class.getSimpleName(), sessionHandler);

					return;
				case BAD_PASS:
					ThingMessage badPassFarewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
							.code(ThingMessageCode.BAD_USER_NAME_OR_PASSWORD.code()).message(ThingMessageCode.BAD_USER_NAME_OR_PASSWORD.desc()).build();
					closeChannel(badPassFarewell);
					return;
				case NOT_AUTHORIZED:
					ThingMessage notAuthorizedFarewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
							.code(ThingMessageCode.NOT_AUTHORIZED.code()).message(ThingMessageCode.NOT_AUTHORIZED.desc()).build();
					closeChannel(notAuthorizedFarewell);
					return;
				default:
					log.error("Unexpected error from auth manager, auth result: {}", authResult.getType().name());

					ThingMessage farewell = ThingMessage.builder().id(inboundMessage.getId()).version("1.0").method(ThingMethod.AUTH_REPLY.code())
							.code(ThingMessageCode.SERVICE_UNAVAILABLE.code()).message(ThingMessageCode.SERVICE_UNAVAILABLE.desc()).build();
					closeChannel(farewell);
					return;
				}
			});
		} else {
			log.error("Before establishing a connection, device must be authenticated. Device SN: {},  request method: {}", deviceSn,
					inboundMessage.getMethod());
			closeChannel(null);
		}
	}

	private AuthData buildAuthData(Channel channel, String clientId, String username, String password) {
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

		builder.clientId(clientId).username(username).password(password);

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

	private void closeChannel(ThingMessage farewell) {
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
		}, ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.warn("ctx: {}, cause:", ctx, cause);
		ctx.channel().close();
	}

}