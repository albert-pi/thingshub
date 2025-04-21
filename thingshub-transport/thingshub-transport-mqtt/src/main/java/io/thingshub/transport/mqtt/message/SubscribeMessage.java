package io.thingshub.transport.mqtt.message;

import java.util.List;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.topic.Subscription;
import io.thingshub.transport.TransportMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class SubscribeMessage extends TransportMessage {

	private List<Subscription> subscriptions;

	public SubscribeMessage(int packetId, List<Subscription> subscriptions) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.SUBSCRIBE.value();
		this.msgName = MqttMessageType.SUBSCRIBE.name();
		this.timestamp = DateUtil.current();

		this.subscriptions = subscriptions;
	}
}
