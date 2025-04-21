package io.thingshub.transport.tcp.event;

import io.thingshub.transport.event.CloseSessionEvent;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.tcp.TcpChannelContext;
import io.thingshub.transport.tcp.TcpTransport;
import io.thingshub.transport.tcp.handler.TcpSessionHandler;

public class CloseTcpSessionListener extends TransportEventListener<CloseSessionEvent, TcpTransport> {

	@Override
	public void onEvent(CloseSessionEvent event) {
		TcpChannelContext ctx = TcpSessionHandler.CHANNEL_CONTEXTS.get(event.getClientId());
		if (ctx == null) {
			return;
		}

		ctx.close();
	}

}
