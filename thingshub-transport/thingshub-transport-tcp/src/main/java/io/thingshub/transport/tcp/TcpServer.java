package io.thingshub.transport.tcp;

import io.thingshub.transport.TransportServer;
import io.thingshub.transport.TransportType;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

/**
 * <p>
 * TCP服务器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class TcpServer implements TransportServer {

	private TcpServerConfig tcpServerConfig;

	private TcpTransport tcpTransport;

	private DisposableServer disposableServer;

	@Inject
	public TcpServer(TcpServerConfig tcpServerConfig, TcpTransport tcpTransport) {
		this.tcpServerConfig = tcpServerConfig;
		this.tcpTransport = tcpTransport;
	}

	@Override
	public String getName() {
		return tcpServerConfig.getName();
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting TCP Server...");

		return this.tcpTransport.bind() //
				.doOnNext(server -> {
					disposableServer = server;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!disposableServer.isDisposed())
							disposableServer.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(s -> log.info("TCP Server is listening on port {}", tcpServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start TCP Server. Error: ", e)) //
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
