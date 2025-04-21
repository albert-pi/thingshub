package io.thingshub.transport.mqtt.handler.v5.reason;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum MQTT5PubRecReasonCode {
	Success((byte) 0x00), NoMatchingSubscribers((byte) 0x10), UnspecifiedError((byte) 0x80), ImplementationSpecificError((byte) 0x83),
	NotAuthorized((byte) 0x87), TopicNameInvalid((byte) 0x90), PacketIdentifierInUse((byte) 0x91), QuotaExceeded((byte) 0x97),
	PayloadFormatInvalid((byte) 0x99);

	@Accessors(fluent = true)
	@Getter
	private final byte value;

	MQTT5PubRecReasonCode(byte value) {
		this.value = value;
	}

	public static MQTT5PubRecReasonCode of(byte value) {
		switch (value) {
		case (byte) 0x00:
			return Success;
		case (byte) 0x10:
			return NoMatchingSubscribers;
		case (byte) 0x80:
			return UnspecifiedError;
		case (byte) 0x83:
			return ImplementationSpecificError;
		case (byte) 0x87:
			return NotAuthorized;
		case (byte) 0x90:
			return TopicNameInvalid;
		case (byte) 0x91:
			return PacketIdentifierInUse;
		case (byte) 0x97:
			return QuotaExceeded;
		case (byte) 0x99:
			return PayloadFormatInvalid;
		default:
			throw new IllegalArgumentException("Invalid PubRec ReasonCode: " + value);
		}
	}
}
