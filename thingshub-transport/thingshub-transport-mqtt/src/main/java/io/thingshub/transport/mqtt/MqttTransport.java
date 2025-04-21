package io.thingshub.transport.mqtt;

import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.thingshub.config.ServerConfig;
import io.thingshub.transport.BaseTransport;
import io.thingshub.transport.mqtt.handler.MQTTPreludeHandler;
import jakarta.inject.Inject;
import reactor.netty.Connection;

/**
 * <p>
 * 绑定TCP Server，添加MQTT协议解码器、编码器及各种handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class MqttTransport extends BaseTransport {

	private MqttServerConfig mqttServerConfig;

	@Inject
	public MqttTransport(MqttServerConfig mqttServerConfig) {
		this.mqttServerConfig = mqttServerConfig;
	}

	@Override
	protected void attachHandlers(Connection connection) {
		connection.addHandlerLast(MqttDecoder.class.getSimpleName(), new MqttDecoder(mqttServerConfig.getMaxBytesInMessage()));
		connection.addHandlerLast(MqttEncoder.class.getSimpleName(), MqttEncoder.INSTANCE);
		connection.addHandlerLast(MQTTPreludeHandler.class.getSimpleName(), new MQTTPreludeHandler(mqttServerConfig));
	}

	@Override
	protected ServerConfig getServiceConfig() {
		return mqttServerConfig;
	}

}
