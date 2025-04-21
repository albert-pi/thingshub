package io.thingshub.transport.codec;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum ThingMessageCode {
	SUCCESS(200, "success"), //
	PROTOCOL_ERROR(4000, "protocol error"), //
	NOT_AUTHORIZED(4001, "client not found"), //
	BAD_USER_NAME_OR_PASSWORD(4002, "bad user name or password"), //
	UNSUPPORTED_PROTOCOL_VERSION(4005, "unsupported protocol version"), //
	PACKET_TOO_LARGE(4013, "packet too large"), //
	CONNECTION_QUOTA_EXCEEDED(5001, "connection quota exceeded"), //
	CONNECTION_RATE_EXCEEDED(5002, "connection rate exceeded"), //
	RESOURCE_QUOTA_EXCEEDED(5003, "resource quota exceeded"), //
	SERVICE_UNAVAILABLE(5004, "service not implemented"), //
	SERVER_BUSY(5009, "server busy"); //

	@Accessors(fluent = true)
	@Getter
	private final int code;

	@Accessors(fluent = true)
	@Getter
	private final String desc;

	ThingMessageCode(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ThingMessageCode of(int code) {
		switch (code) {
		case 200:
			return SUCCESS;
		case 4000:
			return PROTOCOL_ERROR;
		case 4001:
			return NOT_AUTHORIZED;
		case 4002:
			return BAD_USER_NAME_OR_PASSWORD;
		case 4005:
			return UNSUPPORTED_PROTOCOL_VERSION;
		case 4013:
			return PACKET_TOO_LARGE;
		case 5001:
			return CONNECTION_QUOTA_EXCEEDED;
		case 5002:
			return CONNECTION_RATE_EXCEEDED;
		case 5003:
			return RESOURCE_QUOTA_EXCEEDED;
		case 5004:
			return SERVICE_UNAVAILABLE;
		case 5009:
			return SERVER_BUSY;
		default:
			return null;
		}
	}

}