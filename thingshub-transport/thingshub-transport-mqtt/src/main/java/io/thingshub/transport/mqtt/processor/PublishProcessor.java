package io.thingshub.transport.mqtt.processor;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.acl.AclAction;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.PublishMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT PUBLISH frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PublishProcessor extends MqttProcessor<MqttChannelContext, PublishMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PublishMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client publish message to topic {}", msg.getTopic());
		}

		if (!aclManager.check(ctx.getClientId(), msg.getTopic(), AclAction.PUBLISH)) {
			log.warn("MQTT client is not authorized to publish message to topic: {}", msg.getTopic());
			ctx.goAway(false);

			return;
		}

		final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
		MqttQoS qos = MqttQoS.valueOf(msg.getQos());
		switch (qos) {
		case AT_MOST_ONCE:
			ctx.addFgTask(CompletableFuture.supplyAsync(() -> storeAsMessage(ctx, msg), ctx.executor()).thenAccept(this::deliverMessage)).exceptionally(e -> {
				ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
				log.error("", e);

				return null;
			});

			break;
		case AT_LEAST_ONCE:
			if (!ctx.isPacketIdUsed(msg.getPacketId())) {
				ctx.addFgTask(CompletableFuture.runAsync(() -> ctx.addUsingPacketId(msg.getPacketId()), ctx.executor()) //
						.thenApply(v -> storeAsMessage(ctx, msg)) //
						.thenAccept(this::deliverMessage) //
						.thenRun(() -> ackPublish(ctx, msg)) //
						.exceptionally(e -> {
							ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
							log.error("", e);

							return null;
						}));
			}

			break;
		case EXACTLY_ONCE:
			if (ctx.isPacketIdUsed(msg.getPacketId())) {
				ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
				log.error("protocol violation: packet id is in using");

				ctx.goAway(false);
			} else {
				ctx.addFgTask(CompletableFuture.runAsync(() -> receivedPublish(ctx, msg), ctx.executor()));
			}

			break;
		default:
			log.warn("Invalid MQTT qos: {}", qos.value());
			ctx.goAway(false);

			break;
		}
	}

	private void ackPublish(MqttChannelContext ctx, PublishMessage msg) {
		ctx.removeUsingPacketId(msg.getPacketId());

		MqttMessage pubMsg = MqttMessageBuilders.pubAck().packetId(msg.getPacketId()).build();
		ctx.writeAndFlush(pubMsg).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					log.error("Server failed to sending PUBACK message. Message id: {}, QoS: AT_LEAST_ONCE", msg.getPacketId());
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});
	}

	private void receivedPublish(MqttChannelContext ctx, PublishMessage msg) {
		ctx.addUsingPacketId(msg.getPacketId());
		ctx.addInboundMessage(msg.getPacketId(), msg);

		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.getPacketId());
		ctx.writeAndFlush(MqttMessageFactory.newMessage(mqttFixedHeader, variableHeader, null)).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					log.error("Server failed to sending PUBREC message. Message id: {}, QoS: EXACTLY_ONCE", msg.getPacketId());
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});
	}

}
