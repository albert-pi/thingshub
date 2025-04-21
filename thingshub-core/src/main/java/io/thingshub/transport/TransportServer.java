package io.thingshub.transport;

import reactor.core.publisher.Mono;

/**
 * <p>
 * Transport Server
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public interface TransportServer {

	String getName();

	Mono<TransportServer> start();

	void shutdown();

}
