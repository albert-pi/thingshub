package io.thingshub.transport.codec;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum ThingMethod {
	REGISTER("thing.event.register.post"), //
	REGISTER_REPLY("thing.event.register.post_reply"), //
	AUTH("thing.service.auth.request"), //
	AUTH_REPLY("thing.service.auth.request_reply"), //
	HEARTBEAT("thing.event.heartbeat.post"), //
	HEARTBEAT_REPLY("thing.event.heartbeat.post_reply"), //
	OTHER("");

	@Accessors(fluent = true)
	@Getter
	private final String code;

	ThingMethod(String code) {
		this.code = code;
	}

	public static ThingMethod of(String code) {
		switch (code) {
		case "thing.event.register.post":
			return REGISTER;
		case "thing.event.register.post_reply":
			return REGISTER_REPLY;
		case "thing.event.auth.post":
			return AUTH;
		case "thing.event.auth.post_reply":
			return AUTH_REPLY;
		case "thing.event.heartbeat.post":
			return HEARTBEAT;
		case "thing.event.heartbeat.post_reply":
			return HEARTBEAT_REPLY;
		default:
			return OTHER;
		}
	}
}