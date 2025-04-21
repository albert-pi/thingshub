package io.thingshub.transport.mqtt.handler.v5.reason;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum MQTT5PubCompReasonCode {
	Success((byte) 0x00), PacketIdentifierNotFound((byte) 0x92);

	@Accessors(fluent = true)
	@Getter
	private final byte value;

	MQTT5PubCompReasonCode(byte value) {
		this.value = value;
	}

	public static MQTT5PubCompReasonCode of(byte value) {
		switch (value) {
		case (byte) 0x00:
			return Success;
		case (byte) 0x92:
			return PacketIdentifierNotFound;
		default:
			throw new IllegalArgumentException("Invalid PubComp ReasonCode: " + value);
		}
	}
}
