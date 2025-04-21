package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class PubAckMessage extends TransportMessage {

	public PubAckMessage(int packetId) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.PUBACK.value();
		this.msgName = MqttMessageType.PUBACK.name();
		this.timestamp = DateUtil.current();
	}

}
