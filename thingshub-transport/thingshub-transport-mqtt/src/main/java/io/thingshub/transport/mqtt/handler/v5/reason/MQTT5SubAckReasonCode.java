package io.thingshub.transport.mqtt.handler.v5.reason;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum MQTT5SubAckReasonCode {
	GrantedQoS0(0), GrantedQoS1(1), GrantedQoS2(2), UnspecifiedError(0x80), ImplementationSpecificError(0x83), NotAuthorized(0x87), TopicFilterInvalid(0x8F),
	PacketIdentifierInUse(0x91), QuotaExceeded(0x97), SharedSubscriptionsNotSupported(0x9E), SubscriptionIdentifierNotSupported(0xA1),
	WildcardSubscriptionsNotSupported(0xA2);

	@Accessors(fluent = true)
	@Getter
	private final int value;

	MQTT5SubAckReasonCode(int value) {
		this.value = value;
	}

	public static MQTT5SubAckReasonCode of(int value) {
		switch (value) {
		case 0:
			return GrantedQoS0;
		case 1:
			return GrantedQoS1;
		case 2:
			return GrantedQoS2;
		case 0x80:
			return UnspecifiedError;
		case 0x83:
			return ImplementationSpecificError;
		case 0x87:
			return NotAuthorized;
		case 0x8F:
			return TopicFilterInvalid;
		case 0x91:
			return PacketIdentifierInUse;
		case 0x97:
			return QuotaExceeded;
		case 0x9E:
			return SharedSubscriptionsNotSupported;
		case 0xA1:
			return SubscriptionIdentifierNotSupported;
		case 0xA2:
			return WildcardSubscriptionsNotSupported;
		default:
			throw new IllegalArgumentException("Invalid SubAck ReasonCode: " + value);
		}
	}

}
