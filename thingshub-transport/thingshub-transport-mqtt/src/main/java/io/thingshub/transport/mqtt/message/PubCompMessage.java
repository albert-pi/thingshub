package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class PubCompMessage extends TransportMessage {

	public PubCompMessage(int packetId) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.PUBCOMP.value();
		this.msgName = MqttMessageType.PUBCOMP.name();
		this.timestamp = DateUtil.current();
	}

}
