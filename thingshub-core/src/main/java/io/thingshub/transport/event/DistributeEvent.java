package io.thingshub.transport.event;

import java.util.Map;

import io.thingshub.domain.Message;
import lombok.Getter;

@Getter
public class DistributeEvent extends TransportEvent {

	private final Message message;

	private final String recipientId;

	private final Map<String, Object> recipientProps;

	public DistributeEvent(Message message, String recipientId, Map<String, Object> recipientProps, String onTransport) {
		this.message = message;
		this.recipientId = recipientId;
		this.recipientProps = recipientProps;
		this.onTransport = onTransport;
	}

}
