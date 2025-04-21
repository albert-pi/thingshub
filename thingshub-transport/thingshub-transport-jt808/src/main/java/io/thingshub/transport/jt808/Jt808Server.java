package io.thingshub.transport.jt808;

import io.thingshub.transport.TransportServer;
import io.thingshub.transport.TransportType;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

/**
 * <p>
 * JT808服务器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class Jt808Server implements TransportServer {

	private Jt808ServerConfig jt808ServerConfig;

	private Jt808Transport jt808Transport;

//	private SubscriptionManager subscriptioManager;

	private DisposableServer disposableServer;

	@Inject
	public Jt808Server(Jt808ServerConfig jt808ServerConfig, Jt808Transport jt808Transport // , SubscriptionManager subscriptionManager
	) {
		this.jt808ServerConfig = jt808ServerConfig;
		this.jt808Transport = jt808Transport;
//		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public String getName() {
		return "JT808";
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting JT808 Server...");

		return jt808Transport.bind() //
				.doOnNext(server -> {
					disposableServer = server;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!disposableServer.isDisposed())
							disposableServer.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(t -> log.info("JT808 Server is listening on port {}", jt808ServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start JT808 Server. Error: ", e)) //
				.cast(TransportServer.class) //
				.contextWrite(context -> context.put(TransportType.class, TransportType.TCP));
	}

	@Override
	@PreDestroy
	public void shutdown() {
		if (!disposableServer.isDisposed())
			disposableServer.dispose();
	}

}
