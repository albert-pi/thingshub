package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class PubRecMessage extends TransportMessage {

	public PubRecMessage(int packetId) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.PUBREC.value();
		this.msgName = MqttMessageType.PUBREC.name();
		this.timestamp = DateUtil.current();
	}

}
