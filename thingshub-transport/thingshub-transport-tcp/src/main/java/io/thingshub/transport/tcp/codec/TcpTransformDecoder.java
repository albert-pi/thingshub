package io.thingshub.transport.tcp.codec;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.TypeReference;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.thingshub.domain.RawPacket;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.service.RawPacketService;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.tcp.TcpTransport;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class TcpTransformDecoder extends ChannelInboundHandlerAdapter {

	private final TypeReference<ThingMessage> THING_MESSAGE_TYPE = new TypeReference<ThingMessage>() {
	};

	private final ScriptEngineFactory scriptEngineFactory;

	private final RawPacketService rawPacketService;

	@Inject
	public TcpTransformDecoder(ScriptEngineFactory scriptEngineFactory, RawPacketService rawPacketService) {
		this.scriptEngineFactory = scriptEngineFactory;
		this.rawPacketService = rawPacketService;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof ByteBuf) {
			final ByteBuf buf = (ByteBuf) msg;
			try {
				if (buf.readableBytes() <= 0) {
					return;
				}

				String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
				String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();
				ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine(scriptLang);
				final byte[] msgBytes = new byte[buf.readableBytes()];
				buf.readBytes(msgBytes);

				long ts = System.currentTimeMillis();
				String deviceSn = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).get();

				ThingMessage requestMsg = scriptEngine.invoke(productCode, THING_MESSAGE_TYPE, "decode", msgBytes);
				requestMsg.setTimestamp(ts);

				RawPacket rawPacket = new RawPacket();
				rawPacket.setClientId(deviceSn);
				rawPacket.setTimestamp(ts);
				rawPacket.setTransportName(TcpTransport.class.getSimpleName());
				rawPacket.setContent(new String(msgBytes, StandardCharsets.UTF_8));
				rawPacket.setMsgName(requestMsg.getMethod().split("\\.")[2]);
				rawPacketService.save(rawPacket);

				ctx.fireChannelRead(requestMsg);
			} catch (Exception e) {
				log.error("", e);

				ctx.executor().schedule(() -> ctx.channel().close(), ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);
			} finally {
				ReferenceCountUtil.release(buf);
			}
		} else {
			ctx.executor().schedule(() -> ctx.channel().close(), ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);
		}
	}

}
