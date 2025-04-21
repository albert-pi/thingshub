package io.thingshub.transport;

import lombok.Getter;
import lombok.experimental.Accessors;

public enum TransportType {
	TCP("TCP"), UDP("UDP"), HTTP("HTTP"), MQTT("MQTT"), MQTT_WS("MQTT over Web Socket");

	@Accessors(fluent = true)
	@Getter
	private final String desc;

	TransportType(String desc) {
		this.desc = desc;
	}

	public static TransportType of(String name) {
		switch (name) {
		case "TCP":
			return TCP;
		case "UDP":
			return UDP;
		case "HTTP":
			return HTTP;
		case "MQTT":
			return MQTT;
		case "MQTT_WS":
			return MQTT_WS;
		default:
			return null;
		}
	}
}