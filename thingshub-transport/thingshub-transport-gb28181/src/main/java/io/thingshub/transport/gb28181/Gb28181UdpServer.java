package io.thingshub.transport.gb28181;

import io.thingshub.transport.TransportServer;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * <p>
 * GB28181 UDP服务器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class Gb28181UdpServer implements TransportServer {

	private Gb28181ServerConfig gb28181ServerConfig;

	private Gb28181UdpTransport gb28181UdpTransport;

	private Connection connection;

	@Inject
	public Gb28181UdpServer(Gb28181ServerConfig gb28181ServerConfig, Gb28181UdpTransport gb28181UdpTransport) {
		this.gb28181ServerConfig = gb28181ServerConfig;
		this.gb28181UdpTransport = gb28181UdpTransport;
	}

	@Override
	public String getName() {
		return "GB28181";
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting GB28181 UDP Server...");

		return gb28181UdpTransport.bind() //
				.doOnNext(conn -> {
					connection = conn;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!connection.isDisposed())
							connection.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(t -> log.info("GB28181 UDP Server is listening on port {}", gb28181ServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start GB28181 UDP Server. Error: ", e)) //
				.cast(TransportServer.class);
	}

	@Override
	@PreDestroy
	public void shutdown() {
		if (!this.connection.isDisposed())
			this.connection.dispose();
	}

}
