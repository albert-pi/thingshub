package io.thingshub.transport.mqtt.handler;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Ticker;

import cn.hutool.core.collection.CollUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.util.ReferenceCountUtil;
import io.thingshub.Broker;
import io.thingshub.cluster.ClusterManager;
import io.thingshub.domain.Inbox;
import io.thingshub.domain.Message;
import io.thingshub.domain.RawPacket;
import io.thingshub.service.ConnInfoService;
import io.thingshub.service.InboxService;
import io.thingshub.service.MessageService;
import io.thingshub.service.RawPacketService;
import io.thingshub.service.SessionService;
import io.thingshub.topic.Subscription;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.topic.TopicTreeNodeMember;
import io.thingshub.topic.TopicUtil;
import io.thingshub.topic.Unsubscription;
import io.thingshub.transport.CloseSessionJob;
import io.thingshub.transport.DistributeJob;
import io.thingshub.transport.MessageDispatcher;
import io.thingshub.transport.MessageRetryTimer;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.MqttTransport;
import io.thingshub.transport.mqtt.RetainManager;
import io.thingshub.transport.mqtt.message.DisconnectMessage;
import io.thingshub.transport.mqtt.message.LastWillMessage;
import io.thingshub.transport.mqtt.message.PingMessage;
import io.thingshub.transport.mqtt.message.PubAckMessage;
import io.thingshub.transport.mqtt.message.PubCompMessage;
import io.thingshub.transport.mqtt.message.PubRecMessage;
import io.thingshub.transport.mqtt.message.PubRelMessage;
import io.thingshub.transport.mqtt.message.PublishMessage;
import io.thingshub.transport.mqtt.message.SubscribeMessage;
import io.thingshub.transport.mqtt.message.UnsubscribeMessage;
import io.thingshub.utils.MessageUtils;
import io.thingshub.utils.TaskTracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT session handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public abstract class MQTTSessionHandler extends ChannelDuplexHandler {

	public static final Map<String, MqttChannelContext> CHANNEL_CONTEXTS = new ConcurrentHashMap<>();

	private final ConnInfoService connInfoService;

	private final SessionService sessionService;

	private final MessageDispatcher messageDispatcher;

	private final MessageService messageService;

	private final RawPacketService rawPacketService;

	private final MessageRetryTimer messageRetryTimer;

	private final RetainManager retainManager;

	private final InboxService inboxService;

	private final SubscriptionManager subscriptionManager;

	private final ClusterManager clusterManager;

	private final String tenant;

	private final String clientId;

	// 租户内存使用数

	private final int keepAlive;

	private final int sessionExpiryInterval;

	private final boolean sessionPresent;

	private final long idleTimeout;

	private MqttChannelContext mqttChannelContext;

	private LastWillMessage lwm;

	private long lastActiveAt;

	private ScheduledFuture<?> idleTimeoutTask;

	private Function<Void, Boolean> replyAfterInit;

	public MQTTSessionHandler(String tenant, String clientId, int keepAlive, boolean sessionPresent, int sessionExpiryInterval, LastWillMessage lwm) {
		this.tenant = tenant;
		this.clientId = clientId;
		this.keepAlive = keepAlive;
		this.sessionPresent = sessionPresent;
		this.sessionExpiryInterval = sessionExpiryInterval;
		this.lwm = lwm;
		this.idleTimeout = Duration.ofMillis(keepAlive * 1500L).toNanos();

		this.connInfoService = Broker.getBean(ConnInfoService.class);
		this.sessionService = Broker.getBean(SessionService.class);
		this.messageDispatcher = Broker.getBean(MessageDispatcher.class);
		this.retainManager = Broker.getBean(RetainManager.class);
		this.inboxService = Broker.getBean(InboxService.class);
		this.subscriptionManager = Broker.getBean(SubscriptionManager.class);
		this.clusterManager = Broker.getBean(ClusterManager.class);
		this.messageService = Broker.getBean(MessageService.class);
		this.rawPacketService = Broker.getBean(RawPacketService.class);
		this.messageRetryTimer = Broker.getBean(MessageRetryTimer.class);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		clusterManager.executeJob(new CloseSessionJob(clientId, MqttTransport.class.getSimpleName()), false);// kick

		this.idleTimeoutTask = ctx.executor().scheduleAtFixedRate(this::checkIdle, idleTimeout, idleTimeout, TimeUnit.NANOSECONDS);
		this.lastActiveAt = Ticker.systemTicker().read();

		InetSocketAddress remoteAddr = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientAddr = null;
		if (remoteAddr != null) {
			InetAddress ip = remoteAddr.getAddress();
			if (remoteAddr.getAddress() != null) {
				clientAddr = ip.getHostAddress();
			}
		}
		this.mqttChannelContext = new MqttChannelContext(ctx, tenant, clientId, clientAddr, keepAlive);
		CHANNEL_CONTEXTS.put(clientId, this.mqttChannelContext);
		connInfoService.reg(tenant, clientId, ctx.channel().id().asLongText(), clientAddr, "MQTT");

		// TODO CleanSession和sessionPresent组合的逻辑处理
		if (sessionPresent) {
			subscriptionManager.loadClientSubscriptions(clientId);

		} else {
			if (this.sessionExpiryInterval > 0) {
				sessionService.create(clientAddr, keepAlive, sessionExpiryInterval);
			} else {
				// TODO remove session
			}
		}

		if (this.replyAfterInit != null) {
			Boolean isReplySuccessful = this.replyAfterInit.apply(null);
			if (isReplySuccessful) {
				TaskTracker.getInstance().track(CompletableFuture.runAsync(this::sendUnackedMessages, ctx.executor()));
			}
		} else {
			TaskTracker.getInstance().track(CompletableFuture.runAsync(this::sendUnackedMessages, ctx.executor()));
		}

		ctx.channel().config().setAutoRead(true);
		ctx.read();

		// TODO
//		ChannelAttrs.trafficShaper(ctx).setReadLimit(settings.inboundBandwidth);
//        ChannelAttrs.trafficShaper(ctx).setWriteLimit(settings.outboundBandwidth);
//        ChannelAttrs.trafficShaper(ctx).setMaxWriteSize(settings.outboundBandwidth);
//        ChannelAttrs.setMaxPayload(settings.maxPacketSize, ctx);
//        memUsage.addAndGet(estMemSize());
	}

	private void checkIdle() {
		if ((Ticker.systemTicker().read() - lastActiveAt) > idleTimeout) {
			log.warn("MQTT client [{}] idle timeout", clientId);

			idleTimeoutTask.cancel(true);
			mqttChannelContext.channel().config().setAutoRead(false);
			mqttChannelContext.close();
		}
	}

	private void sendUnackedMessages() {
		List<Inbox> deliveries = inboxService.queryUnackedDeliveries(clientId);
		if (CollUtil.isEmpty(deliveries)) {
			return;
		}

		deliveries.parallelStream().forEach(delivery -> {
			Message theMsg = messageService.getById(delivery.getMessageSeq());
			if (theMsg == null) {
				return;
			}

			int packetId = mqttChannelContext.nextPacketId();
			byte[] payloadInBytes = theMsg.getPayload().getBytes();

			Integer qosOfMsg = JSON.parseObject(theMsg.getProps()).getInteger("qos");
			MqttQoS theMqttQos = MqttQoS.valueOf(qosOfMsg);

			switch (theMqttQos) {
			case AT_MOST_ONCE:
				MqttPublishMessage pubMsg = MqttMessageBuilders.publish().messageId(0).qos(theMqttQos).retained(false).topicName(theMsg.getTopic())
						.payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
				mqttChannelContext.writeAndFlush(pubMsg).addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							inboxService.ackDelivery(mqttChannelContext.getClientId(), theMsg.getId());
						} else {
							log.error("Server failed to sending unacked message after connection. Message id: 0, QoS: {}, payload: {}", theMqttQos.value(),
									theMsg.getPayload());
							if (future.cause() != null) {
								log.error("", future.cause());
							}
						}
					}

				});

				break;
			case EXACTLY_ONCE:
			case AT_LEAST_ONCE:
			default:
				mqttChannelContext.addOutboundMessage(packetId, theMsg.getId());

				// TODO 重试任务优化，抽象，与业务无关
				MqttPublishMessage retryingPubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false)
						.topicName(theMsg.getTopic()).payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
				messageRetryTimer.doRetry(mqttChannelContext, packetId, retryingPubMsg, 5, 2);

				MqttPublishMessage immediatePubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false)
						.topicName(theMsg.getTopic()).payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
				mqttChannelContext.writeAndFlush(immediatePubMsg).addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							log.error("Server failed to sending unacked message after connection. Message id: {}, QoS: {}, payload: {}", packetId,
									theMqttQos.value(), theMsg.getPayload());
							if (future.cause() != null) {
								log.error("", future.cause());
							}
						}
					}

				});

				break;
			}

		});
	}

	public final void onInitialized(Function<Void, Boolean> reply) {
		this.replyAfterInit = reply;
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		if (mqttChannelContext != null) {
			mqttChannelContext.flushIfNeeded();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("MQTT client [{}] channel throwed exception. ctx: {}, cause:", clientId, ctx, cause);

		mqttChannelContext.flushIfNeeded();

		ctx.channel().config().setAutoRead(false);
		ctx.channel().close();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) {
		if (!ctx.channel().isWritable()) {
			mqttChannelContext.flushIfNeeded();
		}
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.info("MQTT client [{}] disconnected", clientId);

		if (idleTimeoutTask != null) {
			idleTimeoutTask.cancel(true);
		}

		if (lwm != null) {
			CompletableFuture.runAsync(this::pubLastWill, ctx.executor());
		}

		connInfoService.unreg(clientId);
		mqttChannelContext.getFgTasks().forEach(t -> t.cancel(true));

		// TODO CleanSession和sessionPresent组合的逻辑处理
		if (sessionExpiryInterval == 0) {

		}
		subscriptionManager.unloadClientSubscriptions(this.clientId);

		CHANNEL_CONTEXTS.remove(clientId);

//        Sets.newHashSet(ctxWrapper.getFgTasks()).forEach(t -> t.cancel(true));
//        sessionRegister.stop();
//        tenantMeter.recordCount(MqttDisconnectCount);
//        memUsage.addAndGet(-estMemSize());

		ctx.fireChannelInactive();
	}

	private void pubLastWill() {
		Map<String, Object> props = new HashMap<>();
		props.put("qos", this.lwm.getQos());

		long ts = System.currentTimeMillis();
		Message message = new Message();
		message.setPublisherId(clientId);
		message.setTopic(this.lwm.getTopic());
		message.setProps(JSON.toJSONString(props));
		message.setPayload(JSON.toJSONString(this.lwm.getPayload()));
		message.setTimestamp(ts);
		messageService.save(message);
		Long messageSeq = message.getId();

		RawPacket rawPacket = new RawPacket();
		rawPacket.setClientId(clientId);
		rawPacket.setTimestamp(ts);
		rawPacket.setTransportName(MqttTransport.class.getSimpleName());
		rawPacket.setContent(this.lwm.getRawData());
		rawPacket.setMsgName(this.lwm.getPayload().getString("method").split("\\.")[2]);
		rawPacketService.save(rawPacket);

		if (this.lwm.isRetain()) {
			if (this.lwm.getPayload() == null) {
				retainManager.cleanRetain(this.lwm.getTopic());
			} else {
				retainManager.renewRetain(this.lwm.getTopic(), messageSeq);
			}
		}

		Set<TopicTreeNodeMember> nodeMembers = subscriptionManager.match(this.lwm.getTopic());
		if (CollUtil.isNotEmpty(nodeMembers)) {
			nodeMembers.parallelStream().forEach(m -> {
				// TODO 投递到离线的共享订阅者？？？
				inboxService.deliverMessage(clientId, m.getSubscriberId(), messageSeq);
			});
		} else {
			// TODO TOPIC TREE已全量加载，不需要做什么？
		}

		clusterManager.executeJob(new DistributeJob(messageSeq), true);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		assert msg instanceof MqttMessage;

		MqttMessage mqttMessage = (MqttMessage) msg;
		if (mqttMessage.decoderResult().isSuccess()) {
			lastActiveAt = Ticker.systemTicker().read();

			Integer packetId = null;
			if (mqttMessage.variableHeader() instanceof MqttMessageIdVariableHeader) {
				packetId = ((MqttMessageIdVariableHeader) mqttMessage.variableHeader()).messageId();
			} else if (mqttMessage.variableHeader() instanceof MqttPublishVariableHeader) {
				packetId = ((MqttPublishVariableHeader) mqttMessage.variableHeader()).packetId();
			}

			MDC.put("clientId", mqttChannelContext.getClientId());
			MDC.put("clientAddr", mqttChannelContext.getClientAddr());
			MDC.put("protocol", "MQTT");
			MDC.put("serverAddr", Broker.currentNode);
			MDC.put("packetId", packetId != null ? packetId.toString() : "---");
			MDC.put("msgName", mqttMessage.fixedHeader().messageType().name());

			TransportMessage theMessage = null;
			switch (mqttMessage.fixedHeader().messageType()) {
			case CONNECT:
				log.error("protocol violation: client can't send duplicate CONNECT packet after connected"); // MQTT3-3.1.0-2

				ctx.channel().config().setAutoRead(false);
				ctx.executor().schedule(() -> ctx.close(), ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);

				theMessage = TransportMessage.EMPTY_MESSAGE;
				break;
			case SUBSCRIBE:
				MqttSubscribeMessage subscribeMsg = (MqttSubscribeMessage) msg;

				// TODO 校验
				List<Subscription> subs = subscribeMsg.payload().topicSubscriptions().stream().map(sub -> {
					Map<String, Object> properties = new HashMap<>();
					properties.put("qos", sub.qualityOfService().value());
					properties.put("retainHandling", RetainHandling.SEND_AT_SUBSCRIBE.getValue());

					String[] groupAndTopicFilter = TopicUtil.decode(sub.topicName());

					return new Subscription(mqttChannelContext.getClientId(), groupAndTopicFilter[0], groupAndTopicFilter[1], properties,
							MqttTransport.class.getSimpleName());
				}).collect(Collectors.toList());

				theMessage = new SubscribeMessage(packetId, subs);
				break;
			case PUBLISH:
//				if (isExceedReceivingMaximum()) {//接收消息最大值
//				
//				}
//				if (!throttler.pass()) {//pub速率
//				
//				}

				MqttPublishMessage publishMsg = (MqttPublishMessage) msg;
				String topic = publishMsg.variableHeader().topicName();
				int qos = publishMsg.fixedHeader().qosLevel().value();
				boolean isDup = publishMsg.fixedHeader().isDup();
				boolean isRetain = publishMsg.fixedHeader().isRetain();

				ProtocolValidationResult validationResult = validatePubMessage(publishMsg);
				if (validationResult.isValid()) {
					byte[] payloadInBytes = MessageUtils.readByteBuf(publishMsg.payload());
					theMessage = new PublishMessage(packetId, topic, qos, isRetain, isDup, payloadInBytes);
					ReferenceCountUtil.release(publishMsg);
				} else {
					mqttChannelContext.goAwayWithFarewell(validationResult.getFarewell(), false);
					theMessage = TransportMessage.EMPTY_MESSAGE;
				}

				break;
			case PUBACK:
				theMessage = new PubAckMessage(packetId);
				break;
			case PUBREC:
				theMessage = new PubRecMessage(packetId);
				break;
			case PUBREL:
				theMessage = new PubRelMessage(packetId);
				break;
			case PINGREQ:
				theMessage = new PingMessage();
				break;
			case PUBCOMP:
				theMessage = new PubCompMessage(packetId);
				break;
			case UNSUBSCRIBE:
				// TODO 校验
				MqttUnsubscribeMessage unsubscribeMsg = (MqttUnsubscribeMessage) msg;
				List<Unsubscription> unsubscriptions = unsubscribeMsg.payload().topics().stream().map(topicFilter -> {
					String[] groupAndTopicFilter = TopicUtil.decode(topicFilter);

					return new Unsubscription(mqttChannelContext.getClientId(), groupAndTopicFilter[0], groupAndTopicFilter[1],
							MqttTransport.class.getSimpleName());
				}).collect(Collectors.toList());

				theMessage = new UnsubscribeMessage(packetId, unsubscriptions);
				break;
			case DISCONNECT:
				lwm = null;
				theMessage = new DisconnectMessage();
				break;
			default:
				theMessage = TransportMessage.EMPTY_MESSAGE;
				break;
			}

			messageDispatcher.dispatch(mqttChannelContext, theMessage);
		} else {
			ctx.channel().config().setAutoRead(false);
			ctx.executor().schedule(() -> ctx.close(), ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);
		}
	}

	@Getter
	@AllArgsConstructor
	public static class ProtocolValidationResult {

		private boolean isValid;

		private MqttMessage farewell;

	};

	protected abstract ProtocolValidationResult validatePubMessage(MqttPublishMessage message);

}