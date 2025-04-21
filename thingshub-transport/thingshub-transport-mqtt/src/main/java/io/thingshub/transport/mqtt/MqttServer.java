package io.thingshub.transport.mqtt;

import io.thingshub.transport.TransportServer;
import io.thingshub.transport.TransportType;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

/**
 * <p>
 * MQTT Server
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class MqttServer implements TransportServer {

	private MqttServerConfig mqttServerConfig;

	private MqttTransport mqttTransport;

	private DisposableServer disposableServer;

	@Inject
	public MqttServer(MqttServerConfig mqttServerConfig, MqttTransport mqttTransport) {
		this.mqttServerConfig = mqttServerConfig;
		this.mqttTransport = mqttTransport;
	}

	@Override
	public String getName() {
		return mqttServerConfig.getName();
	}

	@Override
	public Mono<TransportServer> start() {
		log.info("Starting MQTT Server...");

		return mqttTransport.bind() //
				.doOnNext(server -> {
					disposableServer = server;
					Runtime.getRuntime().addShutdownHook(new Thread(() -> {
						if (!disposableServer.isDisposed())
							disposableServer.dispose();
					}));
				}) //
				.thenReturn(this) //
				.doOnSuccess(s -> log.info("MQTT Server is listening on port {}", mqttServerConfig.getPort())) //
				.doOnError(e -> log.error("Failed to start MQTT Server. Error: ", e)) //
				.cast(TransportServer.class) //
				.contextWrite(context -> context.put(TransportType.class, TransportType.MQTT));
	}

	@Override
	@PreDestroy
	public void shutdown() {
		if (!disposableServer.isDisposed())
			disposableServer.dispose();
	}

}
