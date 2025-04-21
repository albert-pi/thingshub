package io.thingshub.transport.gb28181;

import io.thingshub.transport.TransportServer;
import io.thingshub.transport.TransportType;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

/**
 * <p>
 * Gb28181信令服务器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class Gb28181TcpServer implements TransportServer {

	private Gb28181ServerConfig gb28181ServerConfig;

	private Gb28181Transport gb28181Transport;

	private DisposableServer disposableServer;

	@Inject
	public Gb28181TcpServer(Gb28181ServerConfig gb28181ServerConfig, Gb28181Transport gb28181Transport) {
		this.gb28181ServerConfig = gb28181ServerConfig;
		this.gb28181Transport = gb28181Transport;
	}

	@Override
	public String getName() {
		return "GB28181";
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting GB28181 TCP Server...");

		return gb28181Transport.bind() //
				.doOnNext(server -> {
					disposableServer = server;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!disposableServer.isDisposed())
							disposableServer.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(t -> log.info("GB28181 TCP Server is listening on port {}", gb28181ServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start GB28181 TCP Server. Error: ", e)) //
				.cast(TransportServer.class) //
				.contextWrite(context -> context.put(TransportType.class, TransportType.TCP));
	}

	@Override
	@PreDestroy
	public void shutdown() {
		if (!this.disposableServer.isDisposed())
			this.disposableServer.dispose();
	}

}
