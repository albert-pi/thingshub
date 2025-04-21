package io.thingshub.transport.mqtt.processor;

import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.DisconnectMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT DISCONNECT frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class DisconnectProcessor extends MqttProcessor<MqttChannelContext, DisconnectMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull DisconnectMessage msg) {
		log.info("MQTT client [{}] actively close connection", ctx.getClientId());

		// TODO 根据session的sessionExpiryInterval处理inbox消息过期

		ctx.channel().config().setAutoRead(false);
		ctx.close();
	}

}
