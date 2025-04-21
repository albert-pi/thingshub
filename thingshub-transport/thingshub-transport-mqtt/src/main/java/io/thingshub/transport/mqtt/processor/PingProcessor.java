package io.thingshub.transport.mqtt.processor;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.PingMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT PINGREQ frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PingProcessor extends MqttProcessor<MqttChannelContext, PingMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PingMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client [{}] send ping", ctx.getClientId());
		}

		ctx.writeAndFlush(MqttMessage.PINGRESP);
	}

}
