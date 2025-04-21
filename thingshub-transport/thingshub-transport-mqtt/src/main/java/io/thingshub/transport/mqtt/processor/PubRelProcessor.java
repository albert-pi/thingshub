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
import io.thingshub.transport.mqtt.message.PubRelMessage;
import io.thingshub.transport.mqtt.message.PublishMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT PUBREL frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PubRelProcessor extends MqttProcessor<MqttChannelContext, PubRelMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PubRelMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client released QoS2 message");
		}

		PublishMessage cachedPubMsg = (PublishMessage) ctx.pollInboundMessage(msg.getPacketId());
		if (cachedPubMsg == null) {
			log.error("MQTT client's QoS2 message with packet id {} is not exist in cache", msg.getPacketId());

			ctx.addFgTask(CompletableFuture.runAsync(() -> completePublish(ctx, msg), ctx.executor()));
		} else {
			final Map<String, String> parentMdc = MDC.getCopyOfContextMap();

			ctx.addFgTask(CompletableFuture.supplyAsync(() -> storeAsMessage(ctx, cachedPubMsg), ctx.executor()) //
					.thenAccept(this::deliverMessage) //
					.thenRun(() -> completePublish(ctx, msg)) //
					.exceptionally(e -> {
						ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
						log.error("", e);

						return null;
					}));
		}
	}

	private void completePublish(MqttChannelContext ctx, PubRelMessage msg) {
		ctx.removeUsingPacketId(msg.getPacketId());

		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.getPacketId());
		ctx.writeAndFlush(MqttMessageFactory.newMessage(mqttFixedHeader, variableHeader, null)).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					log.error("Server failed to sending PUBCOMP message. Message id: {}", msg.getPacketId());
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});
	}

}
