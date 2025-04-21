package io.thingshub.transport.mqtt.handler.v5;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.topic.TopicUtil;
import io.thingshub.transport.mqtt.handler.MQTTSessionHandler;
import io.thingshub.transport.mqtt.message.LastWillMessage;
import io.thingshub.utils.UTF8Util;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT5 session handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class MQTT5SessionHandler extends MQTTSessionHandler {

	public MQTT5SessionHandler(String tenant, String clientId, int keepAlive, boolean sessionPresent, int sessionExpiryInterval, LastWillMessage lwm) {
		super(tenant, clientId, keepAlive, sessionPresent, sessionExpiryInterval, lwm);
	}

	public ProtocolValidationResult validatePubMessage(MqttPublishMessage message) {
		MqttPublishMessage publishMsg = (MqttPublishMessage) message;

		String topic = publishMsg.variableHeader().topicName();
		int qos = publishMsg.fixedHeader().qosLevel().value();
		boolean isDup = publishMsg.fixedHeader().isDup();
		boolean isRetain = publishMsg.fixedHeader().isRetain();

		if (!UTF8Util.isWellFormed(topic, false)) {
			log.error("Include unacceptable chars in topic [{}]", topic);

			MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
		} else if (!TopicUtil.isValidTopic(topic, 40, 16, 255)) {
			log.error("Invalid topic [{}]", topic);
		} else if (qos == MqttQoS.AT_MOST_ONCE.value() && isDup) {// TODO farewell
			log.error("DUP flag MUST be set to 0 for all QoS 0 messages");
		}

		return null;
	}

}