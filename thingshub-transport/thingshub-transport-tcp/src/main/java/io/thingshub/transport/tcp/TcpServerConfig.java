package io.thingshub.transport.tcp;

import io.thingshub.config.ServerConfig;
import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

@Config
public class TcpServerConfig extends ServerConfig {

	@Value("${thingshub.server.tcp.name: TCP}")
	@Getter
	private String name;

	@Value("${thingshub.server.tcp.host: 0.0.0.0}")
	@Getter
	private String host;

	@Value("${thingshub.server.tcp.port: 5000}")
	@Getter
	private Integer port;

	@Value("${thingshub.server.tcp.connect-timeout: 30}")
	@Getter
	private Integer connectTimeout;// 秒

	@Value("${thingshub.server.tcp.keep-alive: 300}")
	@Getter
	private Integer keepAlive;// 秒

}
