package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;

public class DisconnectMessage extends TransportMessage {

	public DisconnectMessage() {
		this.packetId = 0;
		this.msgType = MqttMessageType.DISCONNECT.value();
		this.msgName = MqttMessageType.DISCONNECT.name();
		this.timestamp = DateUtil.current();
	}

}
