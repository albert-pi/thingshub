package io.thingshub.transport.tcp.handler;

import static io.thingshub.topic.TopicUtil.THING_EVENT_POST_REPLY_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_PROPERTY_POST_REPLY_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_PROPERTY_SET_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_SERVICE_CALL_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_SERVICE_REQUEST_REPLY_TOPIC_FORMAT;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.MDC;

import com.google.common.base.Ticker;

import cn.hutool.core.collection.CollUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.thingshub.Broker;
import io.thingshub.cluster.ClusterManager;
import io.thingshub.domain.MessageModel;
import io.thingshub.service.ConnInfoService;
import io.thingshub.service.MessageModelService;
import io.thingshub.service.model.ThingModelType;
import io.thingshub.topic.Subscription;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.CloseSessionJob;
import io.thingshub.transport.MessageDispatcher;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.codec.ThingMethod;
import io.thingshub.transport.tcp.TcpChannelContext;
import io.thingshub.transport.tcp.TcpTransport;
import io.thingshub.transport.tcp.message.GenericMessage;
import io.thingshub.transport.tcp.message.HeartbeatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpSessionHandler extends ChannelDuplexHandler {

	public static final Map<String, TcpChannelContext> CHANNEL_CONTEXTS = new ConcurrentHashMap<>();

	private final String tenant;

	private final String clientId;

	private final int keepAlive;

	private final long idleTimeout;

	private final ConnInfoService connInfoService;

	private final MessageDispatcher messageDispatcher;

	private final ClusterManager clusterManager;

	private final MessageModelService messageModelService;

	private final SubscriptionManager subscriptionManager;

	private TcpChannelContext tcpChannelContext;

	private long lastActiveAt;

	private ScheduledFuture<?> idleTimeoutTask;

	private Consumer<Void> actionAfterInit;

	public TcpSessionHandler(String tenant, String clientId, int keepAlive) {
		this.tenant = tenant;
		this.clientId = clientId;
		this.keepAlive = keepAlive;
		this.idleTimeout = Duration.ofMillis(keepAlive * 1500L).toNanos();

		this.connInfoService = Broker.getBean(ConnInfoService.class);
		this.messageDispatcher = Broker.getBean(MessageDispatcher.class);
		this.clusterManager = Broker.getBean(ClusterManager.class);
		this.messageModelService = Broker.getBean(MessageModelService.class);
		this.subscriptionManager = Broker.getBean(SubscriptionManager.class);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		clusterManager.executeJob(new CloseSessionJob(clientId, TcpTransport.class.getSimpleName()), false);// kick

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

		this.tcpChannelContext = new TcpChannelContext(ctx, tenant, clientId, clientAddr, keepAlive);
		CHANNEL_CONTEXTS.put(clientId, this.tcpChannelContext);
		connInfoService.reg(tenant, clientId, ctx.channel().id().asLongText(), clientAddr, "TCP");

		String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
		List<MessageModel> downMessageModels = messageModelService.getDownMessageModels(productCode);
		if (CollUtil.isNotEmpty(downMessageModels)) {
			downMessageModels.forEach(messageModel -> {
				String topicFilter = buildTopicFilterByDownMessageModel(productCode, messageModel);

				if (topicFilter != null) {
					Subscription subscription = new Subscription(clientId, null, topicFilter, null, TcpTransport.class.getSimpleName());
					subscriptionManager.subscribe(subscription);
				}
			});
		}

		if (this.actionAfterInit != null) {
			this.actionAfterInit.accept(null);
		}

		ctx.channel().config().setAutoRead(true);
		ctx.read();
	}

	private String buildTopicFilterByDownMessageModel(String productCode, MessageModel downMessageModel) {
		String topicFilter = null;
		switch (ThingModelType.valueOf(downMessageModel.getParamType())) {
		case PROPERTY:
			if (downMessageModel.getAckFlag() == 1) {
				topicFilter = String.format(THING_PROPERTY_POST_REPLY_TOPIC_FORMAT, productCode, clientId, downMessageModel.getName());
			} else {
				topicFilter = String.format(THING_PROPERTY_SET_TOPIC_FORMAT, productCode, clientId, downMessageModel.getName());
			}

			break;
		case SERVICE:
			if (downMessageModel.getAckFlag() == 1) {
				topicFilter = String.format(THING_SERVICE_REQUEST_REPLY_TOPIC_FORMAT, productCode, clientId, downMessageModel.getName());
			} else {
				topicFilter = String.format(THING_SERVICE_CALL_TOPIC_FORMAT, productCode, clientId, downMessageModel.getName());
			}

			break;
		case EVENT:
			if (downMessageModel.getAckFlag() == 1) {
				topicFilter = String.format(THING_EVENT_POST_REPLY_TOPIC_FORMAT, productCode, clientId, downMessageModel.getName());
			}

			break;
		}
		return topicFilter;
	}

	private void checkIdle() {
		if ((Ticker.systemTicker().read() - lastActiveAt) > idleTimeout) {
			log.warn("TCP client [{}] idle timeout", clientId);

			idleTimeoutTask.cancel(true);
			tcpChannelContext.channel().config().setAutoRead(false);
			tcpChannelContext.close();
		}
	}

	public final void onInitialized(Consumer<Void> action) {
		this.actionAfterInit = action;
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		if (tcpChannelContext != null) {
			tcpChannelContext.flushIfNeeded();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("TCP client [{}] channel throwed exception. ctx: {}, cause:", clientId, ctx, cause);

		tcpChannelContext.flushIfNeeded();

		ctx.channel().config().setAutoRead(false);
		ctx.channel().close();
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext ctx) {
		if (!ctx.channel().isWritable()) {
			tcpChannelContext.flushIfNeeded();
		}
		ctx.fireChannelWritabilityChanged();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.info("MQTT client [{}] disconnected", clientId);

		if (idleTimeoutTask != null) {
			idleTimeoutTask.cancel(true);
		}

		connInfoService.unreg(clientId);
		tcpChannelContext.getFgTasks().forEach(t -> t.cancel(true));
		subscriptionManager.unloadClientSubscriptions(this.clientId);

		CHANNEL_CONTEXTS.remove(clientId);

//        Sets.newHashSet(ctxWrapper.getFgTasks()).forEach(t -> t.cancel(true));
//        sessionRegister.stop();
//        tenantMeter.recordCount(MqttDisconnectCount);
//        memUsage.addAndGet(-estMemSize());

		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		assert msg instanceof ThingMessage;

		ThingMessage requestMsg = (ThingMessage) msg;
		lastActiveAt = Ticker.systemTicker().read();

		String reqId = requestMsg.getId();
		String method = requestMsg.getMethod();

		MDC.put("clientId", tcpChannelContext.getClientId());
		MDC.put("clientAddr", tcpChannelContext.getClientAddr());
		MDC.put("protocol", "TCP");
		MDC.put("serverAddr", Broker.currentNode);
		MDC.put("packetId", reqId != null ? reqId.toString() : "---");
		MDC.put("msgName", method.split("\\.")[2]);

		TransportMessage theMessage = null;
		switch (ThingMethod.of(method)) {
		case AUTH:
			log.error("Can't send duplicate authenticate packet after completing authentication. Device SN: {}", tcpChannelContext.getClientId());

			ctx.channel().config().setAutoRead(false);
			ctx.executor().schedule(() -> ctx.close(), ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);

			theMessage = TransportMessage.EMPTY_MESSAGE;
			break;
		case HEARTBEAT:
			theMessage = new HeartbeatMessage(requestMsg);
			break;
		default:
			theMessage = new GenericMessage(requestMsg);
			break;
		}

		messageDispatcher.dispatch(tcpChannelContext, theMessage);
	}

}