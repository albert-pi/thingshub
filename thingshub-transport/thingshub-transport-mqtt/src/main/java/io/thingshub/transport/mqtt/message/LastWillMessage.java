package io.thingshub.transport.mqtt.message;

import java.io.Serializable;

import com.alibaba.fastjson2.JSONObject;

import io.netty.handler.codec.mqtt.MqttProperties;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LastWillMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean retain;

	private String topic;

	private Integer qos;

	private JSONObject payload;

	private String rawData;

	// MQTT 5
	private long messageExpiryInterval;

//    private final Mqtt5PayloadFormatIndicator payloadFormatIndicator;

	private String contentType;

	private String responseTopic;

	private byte[] correlationData;

	private MqttProperties userProperties;

	private long delayInterval;

}
