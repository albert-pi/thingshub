package io.thingshub.transport.mqtt.processor;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.PubRecMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT PUBREC frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PubRecProcessor extends MqttProcessor<MqttChannelContext, PubRecMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PubRecMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client received server's QoS2 message");
		}

		final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
		Long messageSeq = (Long) ctx.pollOutboundMessage(msg.getPacketId());
		if (messageSeq == null) {
			log.error("Server's QoS2 message with packet id {} is not exist in cache", msg.getPacketId());
		}

		ctx.addFgTask(CompletableFuture.runAsync(() -> ackDelivery(ctx, messageSeq, msg), ctx.executor()) //
				.thenAccept(v -> releasePublish(ctx, msg)) //
				.exceptionally(e -> {
					ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
					log.error("", e);

					return null;
				}));
	}

	private void ackDelivery(MqttChannelContext ctx, Long msgIdInSys, PubRecMessage msg) {
		if (msgIdInSys != null) {
			inboxService.ackDelivery(ctx.getClientId(), msgIdInSys);
		}
		messageRetryTimer.cancel(ctx.channel().id().asLongText(), msg.getPacketId());
	}

	private void releasePublish(MqttChannelContext ctx, PubRecMessage msg) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.getPacketId());
		ctx.writeAndFlush(MqttMessageFactory.newMessage(mqttFixedHeader, variableHeader, null)).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					log.error("Server failed to sending PUBREL message. Message id: {}", msg.getPacketId());
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});
	}

}
