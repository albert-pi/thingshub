package io.thingshub.transport.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.thingshub.transport.BaseChannelContext;

public class TcpChannelContext extends BaseChannelContext {

	public TcpChannelContext(ChannelHandlerContext ctx, String tenant, String clientId, String clientAddr, int keepalive) {
		super(ctx, tenant, clientId, clientAddr, keepalive);
	}

}
