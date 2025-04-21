package io.thingshub.transport.mqtt.handler.v5.reason;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum MQTT5DisconnectReasonCode {
	Normal((byte) 0), DisconnectWithWillMessage((byte) 0x04), UnspecifiedError((byte) 0x80), MalformedPacket((byte) 0x81), ProtocolError((byte) 0x82),
	ImplementationSpecificError((byte) 0x83), NotAuthorized((byte) 0x87), ServerBusy((byte) 0x89), ServerShuttingDown((byte) 0x8B),
	KeepAliveTimeout((byte) 0x8D), SessionTakenOver((byte) 0x8E), TopicFilterInvalid((byte) 0x8F), TopicNameInvalid((byte) 0x90),
	ReceiveMaximumExceeded((byte) 0x93), TopicAliasInvalid((byte) 0x94), PacketTooLarge((byte) 0x95), MessageRateToHigh((byte) 0x96),
	QuotaExceeded((byte) 0x97), AdministrativeAction((byte) 0x98), PayloadFormatInvalid((byte) 0x99), RetainNotSupported((byte) 0x9A),
	QoSNotSupported((byte) 0x9B), UseAnotherServer((byte) 0x9B), ServerMoved((byte) 0x9D), SharedSubscriptionsNotSupported((byte) 0x9E),
	ConnectionRateExceeded((byte) 0x9F), MaximumConnectTime((byte) 0xA0), SubscriptionIdentifierNotSupported((byte) 0xA1),
	WildcardSubscriptionsNotSupported((byte) 0xA2);

	@Accessors(fluent = true)
	@Getter
	private final byte value;

	MQTT5DisconnectReasonCode(byte value) {
		this.value = value;
	}

	public static MQTT5DisconnectReasonCode of(byte value) {
		switch (value) {
		case (byte) 0:
			return Normal;
		case (byte) 0x04:
			return DisconnectWithWillMessage;
		case (byte) 0x80:
			return UnspecifiedError;
		case (byte) 0x81:
			return MalformedPacket;
		case (byte) 0x82:
			return ProtocolError;
		case (byte) 0x83:
			return ImplementationSpecificError;
		case (byte) 0x87:
			return NotAuthorized;
		case (byte) 0x89:
			return ServerBusy;
		case (byte) 0x8B:
			return ServerShuttingDown;
		case (byte) 0x8D:
			return KeepAliveTimeout;
		case (byte) 0x8E:
			return SessionTakenOver;
		case (byte) 0x8F:
			return TopicFilterInvalid;
		case (byte) 0x90:
			return TopicNameInvalid;
		case (byte) 0x93:
			return ReceiveMaximumExceeded;
		case (byte) 0x94:
			return TopicAliasInvalid;
		case (byte) 0x95:
			return PacketTooLarge;
		case (byte) 0x96:
			return MessageRateToHigh;
		case (byte) 0x97:
			return QuotaExceeded;
		case (byte) 0x98:
			return AdministrativeAction;
		case (byte) 0x99:
			return PayloadFormatInvalid;
		case (byte) 0x9A:
			return RetainNotSupported;
		case (byte) 0x9B:
			return QoSNotSupported;
		case (byte) 0x9C:
			return UseAnotherServer;
		case (byte) 0x9D:
			return ServerMoved;
		case (byte) 0x9E:
			return SharedSubscriptionsNotSupported;
		case (byte) 0x9F:
			return ConnectionRateExceeded;
		case (byte) 0xA0:
			return MaximumConnectTime;
		case (byte) 0xA1:
			return SubscriptionIdentifierNotSupported;
		case (byte) 0xA2:
			return WildcardSubscriptionsNotSupported;
		default:
			throw new IllegalArgumentException("Invalid Disconnect ReasonCode: " + value);
		}
	}
}
