package io.thingshub.transport.mqtt.handler.v5;

import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.AUTHENTICATION_DATA;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.AUTHENTICATION_METHOD;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.CONTENT_TYPE;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.CORRELATION_DATA;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.PAYLOAD_FORMAT_INDICATOR;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.REASON_STRING;
import static io.netty.handler.codec.mqtt.MqttProperties.MqttPropertyType.RESPONSE_TOPIC;

import java.util.Optional;

import io.netty.handler.codec.mqtt.MqttProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public class MQTT5MessageUtils {
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class MqttPropertiesBuilder {
		private final MqttProperties mqttProperties = new MqttProperties();

		public MqttPropertiesBuilder addSubscriptionIdentifier(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addReceiveMaximum(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.RECEIVE_MAXIMUM.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addTopicAliasMaximum(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addMaximumPacketSize(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.MAXIMUM_PACKET_SIZE.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addSessionExpiryInterval(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SESSION_EXPIRY_INTERVAL.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addServerKeepAlive(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SERVER_KEEP_ALIVE.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addServerReference(String value) {
			mqttProperties.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.SERVER_REFERENCE.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addTopicAlias(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addUserProperty(String key, String value) {
			mqttProperties.add(new MqttProperties.UserProperty(key, value));
			return this;
		}

		public MqttPropertiesBuilder addPayloadFormatIndicator(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(PAYLOAD_FORMAT_INDICATOR.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addContentType(String value) {
			mqttProperties.add(new MqttProperties.StringProperty(CONTENT_TYPE.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addResponseTopic(String value) {
			mqttProperties.add(new MqttProperties.StringProperty(RESPONSE_TOPIC.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addCorrelationData(String value) {
			mqttProperties.add(new MqttProperties.BinaryProperty(CORRELATION_DATA.value(), value.getBytes()));
			return this;
		}

		public MqttPropertiesBuilder addReasonString(String value) {
			mqttProperties.add(new MqttProperties.StringProperty(REASON_STRING.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addAuthMethod(String value) {
			mqttProperties.add(new MqttProperties.StringProperty(AUTHENTICATION_METHOD.value(), value));
			return this;
		}

		public MqttPropertiesBuilder addAuthData(String value) {
			mqttProperties.add(new MqttProperties.BinaryProperty(AUTHENTICATION_DATA.value(), value.getBytes()));
			return this;
		}

		public MqttPropertiesBuilder addRequestResponseInformation(boolean value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.REQUEST_RESPONSE_INFORMATION.value(), value ? 1 : 0));
			return this;
		}

		public MqttPropertiesBuilder addRequestProblemInformation(boolean value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.REQUEST_PROBLEM_INFORMATION.value(), value ? 1 : 0));
			return this;
		}

		public MqttPropertiesBuilder addMessageExpiryInterval(int value) {
			mqttProperties.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL.value(), value));
			return this;
		}

		public MqttProperties build() {
			return mqttProperties;
		}
	}

	public static MqttPropertiesBuilder mqttProps() {
		return new MqttPropertiesBuilder();
	}

	public static boolean isUTF8Payload(MqttProperties mqttProperties) {
		return packetFormatIndicator(mqttProperties).map(i -> i == 1).orElse(false);
	}

	public static Optional<Integer> receiveMaximum(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.RECEIVE_MAXIMUM);
	}

	public static Optional<Integer> topicAliasMaximum(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM);
	}

	public static Optional<Integer> subscriptionIdentifier(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER);
	}

	public static Optional<Integer> maximumPacketSize(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.MAXIMUM_PACKET_SIZE);
	}

	public static Optional<Integer> topicAlias(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.TOPIC_ALIAS);
	}

	static Optional<Integer> packetFormatIndicator(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, PAYLOAD_FORMAT_INDICATOR);
	}

	public static Optional<Integer> messageExpiryInterval(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.PUBLICATION_EXPIRY_INTERVAL);
	}

	public static Optional<String> contentType(MqttProperties mqttProperties) {
		return stringMqttProperty(mqttProperties, CONTENT_TYPE);
	}

	public static Optional<String> responseTopic(MqttProperties mqttProperties) {
		return stringMqttProperty(mqttProperties, RESPONSE_TOPIC);
	}

	public static Optional<String> authMethod(MqttProperties mqttProperties) {
		return stringMqttProperty(mqttProperties, AUTHENTICATION_METHOD);
	}

	public static boolean requestResponseInformation(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.REQUEST_RESPONSE_INFORMATION).orElse(0) == 1;
	}

	public static boolean requestProblemInformation(MqttProperties mqttProperties) {
		return integerMqttProperty(mqttProperties, MqttProperties.MqttPropertyType.REQUEST_PROBLEM_INFORMATION).orElse(1) == 1;
	}

	public static Optional<String> reasonString(MqttProperties mqttProperties) {
		return stringMqttProperty(mqttProperties, REASON_STRING);
	}

	static Optional<Integer> integerMqttProperty(MqttProperties mqttProperties, MqttProperties.MqttPropertyType type) {
		return Optional.ofNullable((MqttProperties.IntegerProperty) mqttProperties.getProperty(type.value())).map(MqttProperties.MqttProperty::value);
	}

	static Optional<String> stringMqttProperty(MqttProperties mqttProperties, MqttProperties.MqttPropertyType type) {
		return Optional.ofNullable((MqttProperties.StringProperty) mqttProperties.getProperty(type.value())).map(MqttProperties.MqttProperty::value);
	}
}
