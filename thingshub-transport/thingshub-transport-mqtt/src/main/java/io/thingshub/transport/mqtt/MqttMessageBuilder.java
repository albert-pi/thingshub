package io.thingshub.transport.mqtt;

import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;

public class MqttMessageBuilder {

	private static final MqttMessage PING_MESSAGE = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0));

	private static final MqttMessage PONG_MESSAGE = new MqttMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0));

	public static MqttPublishMessage buildPub(boolean isDup, MqttQoS qoS, int packetId, String topic, byte[] payloadInBytes) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, isDup, qoS, false, 0);
		MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, packetId);
		MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, Unpooled.wrappedBuffer(payloadInBytes));
		return mqttPublishMessage;
	}

	public static MqttPublishMessage buildPub(boolean isDup, MqttQoS qoS, boolean isRetain, int packetId, String topic, byte[] payloadInBytes) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, isDup, qoS, isRetain, 0);
		MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader(topic, packetId);
		return new MqttPublishMessage(mqttFixedHeader, mqttPublishVariableHeader, Unpooled.wrappedBuffer(payloadInBytes));
	}

	public static MqttPubAckMessage buildPubAck(int packetId) {
		return buildAckMessage(MqttMessageType.PUBACK, packetId, false);
	}

	public static MqttPubAckMessage buildPubRec(int packetId) {
		return buildAckMessage(MqttMessageType.PUBREC, packetId, false);
	}

	public static MqttPubAckMessage buildPubRel(int packetId) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(packetId);
		return new MqttPubAckMessage(mqttFixedHeader, from);
	}

	public static MqttPubAckMessage buildPubComp(int packetId) {
		return buildAckMessage(MqttMessageType.PUBCOMP, packetId, false);

	}

	private static MqttPubAckMessage buildAckMessage(MqttMessageType mqttMessageType, int packetId, boolean isRetain) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(mqttMessageType, false, MqttQoS.AT_MOST_ONCE, isRetain, 0x02);
		MqttMessageIdVariableHeader from = MqttMessageIdVariableHeader.from(packetId);
		return new MqttPubAckMessage(mqttFixedHeader, from);
	}

	public static MqttSubAckMessage buildSubAck(int packetId, List<Integer> qos) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(packetId);
		MqttSubAckPayload payload = new MqttSubAckPayload(qos);
		return new MqttSubAckMessage(mqttFixedHeader, variableHeader, payload);
	}

	public static MqttUnsubAckMessage buildUnsubAck(int packetId) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(packetId);
		return new MqttUnsubAckMessage(mqttFixedHeader, variableHeader);
	}

	public static MqttConnAckMessage buildConnectAck(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {
		MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(connectReturnCode, sessionPresent);
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0X02);
		return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
	}

	public static MqttSubscribeMessage buildSub(int packetId, List<MqttTopicSubscription> topicSubscriptions) {
		MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(topicSubscriptions);
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
		MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(packetId);
		return new MqttSubscribeMessage(mqttFixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
	}

	public static MqttUnsubscribeMessage buildUnsub(int packetId, List<String> topics) {
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0x02);
		MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(packetId);
		MqttUnsubscribePayload MqttUnsubscribeMessage = new MqttUnsubscribePayload(topics);
		return new MqttUnsubscribeMessage(mqttFixedHeader, variableHeader, MqttUnsubscribeMessage);
	}

	public static MqttConnectMessage buildConnect(String clientId, String willTopic, String willMessage, String username, String password, boolean isUsername,
			boolean isPassword, boolean isWill, int willQos, int heart) {
		MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(),
				MqttVersion.MQTT_3_1_1.protocolLevel(), isUsername, isPassword, false, willQos, isWill, false, heart);
		MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(clientId, willTopic, isWill ? willMessage.getBytes() : null, username,
				isPassword ? password.getBytes() : null);
		MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 10);
		return new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
	}

	public static MqttMessage buildPingMessage() {
		return PING_MESSAGE;
	}

	public static MqttMessage buildPongMessage() {
		return PONG_MESSAGE;

	}
}
