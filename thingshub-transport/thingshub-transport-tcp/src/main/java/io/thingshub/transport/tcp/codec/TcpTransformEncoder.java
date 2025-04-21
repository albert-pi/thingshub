package io.thingshub.transport.tcp.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.codec.ThingMessage;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public final class TcpTransformEncoder extends MessageToMessageEncoder<ThingMessage> {

	private ScriptEngineFactory scriptEngineFactory;

	@Inject
	public TcpTransformEncoder(ScriptEngineFactory scriptEngineFactory) {
		this.scriptEngineFactory = scriptEngineFactory;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ThingMessage replyMsg, List<Object> out) throws Exception {
		out.add(doEncode(ctx, replyMsg));
	}

	private ByteBuf doEncode(ChannelHandlerContext ctx, ThingMessage replyMsg) {
		String deviceSn = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).get();

		try {
			String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
			String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();

			ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine(scriptLang);
			byte[] bytes = scriptEngine.invoke(productCode, ScriptEngine.BYTEARRAY_TYPE, "encode", replyMsg);
			ByteBuf buf = ctx.alloc().buffer(bytes.length);
			buf.writeBytes(bytes);

			return buf;
		} catch (Throwable e) {
			log.error("Failed to encode TCP transport message. Devcie SN: {}, Error: ", deviceSn, e);
			throw e;
		}
	}

}
