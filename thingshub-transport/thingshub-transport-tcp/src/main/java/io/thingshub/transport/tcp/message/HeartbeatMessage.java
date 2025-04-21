package io.thingshub.transport.tcp.message;

import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.codec.ThingMethod;
import lombok.Getter;

public class HeartbeatMessage extends TransportMessage {

	@Getter
	private ThingMessage pingMessage;

	public HeartbeatMessage(ThingMessage pingMessage) {
		this.packetId = 0;
		this.msgName = ThingMethod.HEARTBEAT.name();
		this.timestamp = pingMessage.getTimestamp();

		this.pingMessage = pingMessage;
	}

}