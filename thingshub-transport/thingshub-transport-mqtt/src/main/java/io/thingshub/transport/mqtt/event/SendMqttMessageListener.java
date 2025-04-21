package io.thingshub.transport.mqtt.event;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.domain.Message;
import io.thingshub.domain.MessageModel;
import io.thingshub.service.InboxService;
import io.thingshub.service.MessageModelService;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.MessageRetryTimer;
import io.thingshub.transport.event.DistributeEvent;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.MqttTransport;
import io.thingshub.transport.mqtt.handler.MQTTSessionHandler;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMqttMessageListener extends TransportEventListener<DistributeEvent, MqttTransport> {

	private MessageRetryTimer messageRetryTimer;

	private InboxService inboxService;

	private MessageModelService messageModelService;

	@Inject
	public SendMqttMessageListener(MessageRetryTimer messageRetryTimer, InboxService inboxService, MessageModelService messageModelService) {
		this.messageRetryTimer = messageRetryTimer;
		this.inboxService = inboxService;
		this.messageModelService = messageModelService;
	}

	@Override
	public void onEvent(DistributeEvent event) {
		MqttChannelContext ctx = MQTTSessionHandler.CHANNEL_CONTEXTS.get(event.getRecipientId());
		if (ctx == null) {
			return;
		}

		Message theMsg = event.getMessage();

		Integer pubQos = JSON.parseObject(theMsg.getProps()).getInteger("qos");
		Integer subQos = (Integer) event.getRecipientProps().get("qos");
		MqttQoS theMqttQos = MqttQoS.valueOf(Math.min(pubQos, subQos));

		int packetId = ctx.nextPacketId();
		byte[] payloadInBytes = theMsg.getPayload().getBytes();
		String topic = theMsg.getTopic();

		String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();
		if (scriptLang != null) {
			String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
			JSONObject payloadObj = JSON.parseObject(payloadInBytes);
			String command = payloadObj.getString("method").split("\\.")[2];
			MessageModel messageModel = messageModelService.getMessageModel(productCode, command);
			if (messageModel.getRawTopic() != null) {
				topic = messageModel.getRawTopic();
			}
		}

		switch (theMqttQos) {
		case AT_MOST_ONCE:
			MqttPublishMessage pubMsg = MqttMessageBuilders.publish().messageId(0).qos(theMqttQos).retained(false).topicName(topic)
					.payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
			ctx.writeAndFlush(pubMsg).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						inboxService.ackDelivery(ctx.getClientId(), theMsg.getId());
					} else {
						log.error("Server failed to distribute message. QoS: {}, payload: {}", theMqttQos.value(), theMsg.getPayload());
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
			ctx.addOutboundMessage(packetId, theMsg.getId());

			// TODO 重试任务优化，抽象，与业务无关
			MqttPublishMessage retryingPubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false).topicName(topic)
					.payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
			messageRetryTimer.doRetry(ctx, packetId, retryingPubMsg, 5, 6);

			MqttPublishMessage immediatePubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false).topicName(topic)
					.payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
			ctx.writeAndFlush(immediatePubMsg).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						log.error("MQTT transport Server failed to distribute message. QoS: {}, payload: {}", theMqttQos.value(), theMsg.getPayload());
						if (future.cause() != null) {
							log.error("", future.cause());
						}
					}
				}

			});

			break;
		}
	}

}
