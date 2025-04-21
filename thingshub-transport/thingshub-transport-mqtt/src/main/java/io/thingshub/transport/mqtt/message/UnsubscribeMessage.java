package io.thingshub.transport.mqtt.message;

import java.util.List;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.topic.Unsubscription;
import io.thingshub.transport.TransportMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeMessage extends TransportMessage {

	private List<Unsubscription> unsubscriptions;

	public UnsubscribeMessage(int packetId, List<Unsubscription> unsubscriptions) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.UNSUBSCRIBE.value();
		this.msgName = MqttMessageType.UNSUBSCRIBE.name();
		this.timestamp = DateUtil.current();

		this.unsubscriptions = unsubscriptions;
	}
}
