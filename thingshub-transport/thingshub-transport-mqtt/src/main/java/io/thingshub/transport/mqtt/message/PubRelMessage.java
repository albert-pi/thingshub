package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class PubRelMessage extends TransportMessage {

	public PubRelMessage(int packetId) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.PUBREL.value();
		this.msgName = MqttMessageType.PUBREL.name();
		this.timestamp = DateUtil.current();
	}

}
