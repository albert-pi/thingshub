package io.thingshub.transport.gb28181;

import io.thingshub.config.ServerConfig;
import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

@Config
public class Gb28181ServerConfig extends ServerConfig {

	@Value("${thingshub.server.gb28181.host: 0.0.0.0}")
	@Getter
	private String host;

	@Value("${thingshub.server.gb28181.port: 5060}")
	@Getter
	private Integer port;

}
