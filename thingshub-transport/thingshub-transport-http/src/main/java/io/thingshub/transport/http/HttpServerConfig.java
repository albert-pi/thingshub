package io.thingshub.transport.http;

import io.thingshub.config.ServerConfig;
import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

@Config
public class HttpServerConfig extends ServerConfig {

	@Value("${thingshub.server.http.name: HTTP}")
	@Getter
	private String name;

	@Value("${thingshub.server.http.host: 0.0.0.0}")
	@Getter
	private String host;

	@Value("${thingshub.server.http.port: 9280}")
	@Getter
	private Integer port;

	@Value("${thingshub.server.http.access-log: true}")
	@Getter
	private boolean accessLog;

}
