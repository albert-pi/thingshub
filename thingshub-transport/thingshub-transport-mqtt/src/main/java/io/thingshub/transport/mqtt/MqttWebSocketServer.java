package io.thingshub.transport.mqtt;

import io.thingshub.transport.TransportServer;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;

public class MqttWebSocketServer implements TransportServer {

	private MqttServerConfig mqttServerConfig;

	private MqttTransport mqttTransport;

	private DisposableServer disposableServer;

	@Inject
	public MqttWebSocketServer(MqttServerConfig mqttServerConfig, MqttTransport mqttTransport) {
		this.mqttServerConfig = mqttServerConfig;
		this.mqttTransport = mqttTransport;
	}

	@Override
	public String getName() {
		return mqttServerConfig.getName();
	}

	@Override
	public Mono<TransportServer> start() {
		// TODO Auto-generated method stub
		return Mono.empty();
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

}