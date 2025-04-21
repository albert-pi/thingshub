package io.thingshub.transport.mqtt.message;

import cn.hutool.core.date.DateUtil;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.thingshub.transport.TransportMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class PublishMessage extends TransportMessage {

	private String topic;

	private Integer qos;

	private boolean isRetain;

	private byte[] payload;

	private boolean isDup;

	public PublishMessage(int packetId, String topic, int qos, boolean isRetain, boolean dup, byte[] payload) {
		this.packetId = packetId;
		this.msgType = MqttMessageType.PUBLISH.value();
		this.msgName = MqttMessageType.PUBLISH.name();
		this.timestamp = DateUtil.current();

		this.topic = topic;
		this.qos = qos;
		this.isRetain = isRetain;
		this.isDup = dup;
		this.payload = payload;
	}

}
