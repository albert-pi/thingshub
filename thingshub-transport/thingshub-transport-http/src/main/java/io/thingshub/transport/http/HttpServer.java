package io.thingshub.transport.http;

import io.thingshub.transport.TransportServer;
import io.thingshub.transport.TransportType;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

/**
 * <p>
 * HTTP服务器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class HttpServer implements TransportServer {

	private HttpServerConfig httpServerConfig;

	private HttpTransport httpTransport;

	private DisposableServer disposableServer;

	@Inject
	public HttpServer(HttpServerConfig httpServerConfig, HttpTransport httpTransport) {
		this.httpServerConfig = httpServerConfig;
		this.httpTransport = httpTransport;
	}

	@Override
	public String getName() {
		return httpServerConfig.getName();
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting HTTP Server...");

		return httpTransport.bind() //
				.doOnNext(server -> {
					disposableServer = server;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!disposableServer.isDisposed())
							disposableServer.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(t -> log.info("HTTP Server is listening on port {}", httpServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start HTTP Server. Error: ", e)) //
				.cast(TransportServer.class) //
				.contextWrite(context -> context.put(TransportType.class, TransportType.HTTP));
	}

	@Override
	public void shutdown() {
		if (!this.disposableServer.isDisposed())
			this.disposableServer.dispose();
	}

}
