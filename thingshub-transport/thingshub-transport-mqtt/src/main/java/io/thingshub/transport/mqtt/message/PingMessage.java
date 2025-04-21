package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class PingMessage extends TransportMessage {

	public PingMessage() {
		this.packetId = 0;
		this.msgType = MqttMessageType.PINGREQ.value();
		this.msgName = MqttMessageType.PINGREQ.name();
		this.timestamp = DateUtil.current();
	}

}
