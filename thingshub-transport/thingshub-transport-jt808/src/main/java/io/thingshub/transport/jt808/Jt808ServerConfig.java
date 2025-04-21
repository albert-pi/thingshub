package io.thingshub.transport.jt808;

import io.thingshub.config.ServerConfig;
import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

@Config
public class Jt808ServerConfig extends ServerConfig {

	@Value("${thingshub.server.jt808.host: 0.0.0.0}")
	@Getter
	private String host;

	@Value("${thingshub.server.jt808.port: 6808}")
	@Getter
	private Integer port;

}
