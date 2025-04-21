package io.thingshub.transport.mqtt.processor;

import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;

import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.PubAckMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQT PUBACK frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PubAckProcessor extends MqttProcessor<MqttChannelContext, PubAckMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PubAckMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client acknowledged server's packet");
		}

		Long cachedMsgId = (Long) ctx.pollOutboundMessage(msg.getPacketId());
		if (cachedMsgId == null) {
			log.error("MQTT client's QoS1 message with packet id {} is not exist in cache", msg.getPacketId());
		} else {
			final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
			ctx.addFgTask(CompletableFuture.runAsync(() -> {
				inboxService.ackDelivery(ctx.getClientId(), cachedMsgId);
				messageRetryTimer.cancel(ctx.channel().id().asLongText(), msg.getPacketId());
			}, ctx.executor()).exceptionally(e -> {
				ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
				log.error("", e);

				return null;
			}));
		}
	}

}
