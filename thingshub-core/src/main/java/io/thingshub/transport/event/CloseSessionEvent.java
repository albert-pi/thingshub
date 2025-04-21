package io.thingshub.transport.event;

import lombok.Getter;

@Getter
public class CloseSessionEvent extends TransportEvent {

	private final String clientId;

	public CloseSessionEvent(String clientId, String onTransport) {
		this.clientId = clientId;
		this.onTransport = onTransport;
	}

}
