package io.thingshub.transport.mqtt.handler.v5.reason;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum MQTT5AuthReasonCode {
	Success((byte) 0x00), Continue((byte) 0x18), ReAuth((byte) 0x19);

	@Accessors(fluent = true)
	@Getter
	private final byte value;

	MQTT5AuthReasonCode(byte value) {
		this.value = value;
	}

	public static MQTT5AuthReasonCode of(byte value) {
		switch (value) {
		case 0x00:
			return Success;
		case 0x18:
			return Continue;
		case 0x19:
			return ReAuth;
		default:
			throw new IllegalArgumentException("Invalid Auth ReasonCode: " + value);
		}
	}
}
