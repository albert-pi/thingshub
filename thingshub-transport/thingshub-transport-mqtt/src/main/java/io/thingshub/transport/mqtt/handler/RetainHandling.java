package io.thingshub.transport.mqtt.handler;

import lombok.Getter;

@Getter
public enum RetainHandling {

	SEND_AT_SUBSCRIBE(0), //

	SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS(1), //

	DONT_SEND_AT_SUBSCRIBE(2), //

	UNRECOGNIZED(-1);

	public static final int SEND_AT_SUBSCRIBE_VALUE = 0;

	public static final int SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS_VALUE = 1;

	public static final int DONT_SEND_AT_SUBSCRIBE_VALUE = 2;

	private final int value;

	private RetainHandling(int val) {
		this.value = val;
	}

}