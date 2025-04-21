package io.thingshub.transport.mqtt.processor;

import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.PubCompMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTTT PUBCOMP frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class PubCompProcessor extends MqttProcessor<MqttChannelContext, PubCompMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull PubCompMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("MQTT client completed receiving server's packet");
		}
	}

}
