package io.thingshub.transport.jt808.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.thingshub.transport.jt808.Jt808Exception;

@ChannelHandler.Sharable
public final class Jt808Encoder extends MessageToMessageEncoder<Jt808Message> {

	public static final Jt808Encoder INSTANCE = new Jt808Encoder();

	@Override
	protected void encode(ChannelHandlerContext ctx, Jt808Message msg, List<Object> out) throws Exception {
		out.add(doEncode(ctx, msg));
	}

	static ByteBuf doEncode(ChannelHandlerContext ctx, Jt808Message message) {
		if (Jt808MessageType.valueOf(message.getHeader().getMessageType().getId()) == null) {
			throw new Jt808Exception("Unknown message ID: " + message.getHeader().getMessageType());
		}

		byte[] messageBytes = message.encode();
		ByteBuf buf = ctx.alloc().buffer(messageBytes.length);
		buf.writeBytes(messageBytes);

		return buf;
	}

}
