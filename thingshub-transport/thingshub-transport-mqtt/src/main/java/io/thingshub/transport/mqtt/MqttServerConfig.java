package io.thingshub.transport.mqtt;

import io.thingshub.config.ServerConfig;
import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

@Config
public class MqttServerConfig extends ServerConfig {

	@Value("${thingshub.server.mqtt.name: MQTT}")
	@Getter
	private String name;

	@Value("${thingshub.server.mqtt.host: 0.0.0.0}")
	@Getter
	private String host;

	@Value("${thingshub.server.mqtt.port: 1883}")
	@Getter
	private Integer port;

	@Value("${thingshub.server.mqtt.connect-timeout: 30}")
	@Getter
	private Integer connectTimeout;// 秒

	@Value("${thingshub.server.mqtt.keep-alive: 300}")
	@Getter
	private Integer keepAlive;// 秒

}
