package io.thingshub.transport.mqtt.event;

import io.thingshub.transport.event.CloseSessionEvent;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.MqttTransport;
import io.thingshub.transport.mqtt.handler.MQTTSessionHandler;

public class CloseMqttSessionListener extends TransportEventListener<CloseSessionEvent, MqttTransport> {

	@Override
	public void onEvent(CloseSessionEvent event) {
		MqttChannelContext ctx = MQTTSessionHandler.CHANNEL_CONTEXTS.get(event.getClientId());
		if (ctx == null) {
			return;
		}

		ctx.close();
	}

}
