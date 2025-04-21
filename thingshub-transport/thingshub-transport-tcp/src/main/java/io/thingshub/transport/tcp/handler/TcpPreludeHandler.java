package io.thingshub.transport.tcp.handler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.TypeReference;

import cn.hutool.core.util.StrUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.thingshub.Broker;
import io.thingshub.domain.Device;
import io.thingshub.domain.RawPacket;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.service.DeviceService;
import io.thingshub.service.RawPacketService;
import io.thingshub.service.ScriptInfoService;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.codec.ThingMethod;
import io.thingshub.transport.tcp.TcpServerConfig;
import io.thingshub.transport.tcp.TcpTransport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpPreludeHandler extends ChannelDuplexHandler {

	private final TypeReference<ThingMessage> THING_MESSAGE_TYPE = new TypeReference<ThingMessage>() {
	};

	private final TcpServerConfig tcpServerConfig;

	private ChannelHandlerContext ctx;

	private ScheduledFuture<?> timeoutCloseTask;

	private ScheduledFuture<?> closeConnectionTask;

	private final RawPacketService rawPacketService;

	public TcpPreludeHandler(TcpServerConfig tcpServerConfig) {
		this.tcpServerConfig = tcpServerConfig;

		this.rawPacketService = Broker.getBean(RawPacketService.class);
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.ctx = ctx;

		timeoutCloseTask = ctx.executor().schedule(() -> {
			ctx.channel().close();
		}, tcpServerConfig.getConnectTimeout(), TimeUnit.SECONDS);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ctx.channel().config().setAutoRead(false);
		timeoutCloseTask.cancel(true);

		if (msg instanceof ByteBuf) {
			final ByteBuf buf = (ByteBuf) msg;
			try {
				if (buf.readableBytes() <= 0) {
					return;
				}

				final byte[] msgBytes = new byte[buf.readableBytes()];
				buf.readBytes(msgBytes);

				String protocolScriptId = "TCP";// TODO
				ScriptInfo scriptInfo = Broker.getBean(ScriptInfoService.class).getScriptInfo(protocolScriptId);
				ScriptEngine scriptEngine = Broker.getBean(ScriptEngineFactory.class).getScriptEngine(scriptInfo.getLang());
				String deviceSn = scriptEngine.invoke(protocolScriptId, ScriptEngine.STRING_TYPE, "decode", msgBytes);
				if (!StrUtil.isBlank(deviceSn)) {
					Device device = Broker.getBean(DeviceService.class).getBySn(deviceSn);
					if (device != null) {
						long ts = System.currentTimeMillis();

						ThingMessage inboundMessage = scriptEngine.invoke(device.getProductCode(), THING_MESSAGE_TYPE, "decode", msgBytes);
						if (inboundMessage != null) {
							ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).set(deviceSn);
							ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).set(device.getProductCode());
							ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).set(scriptInfo.getLang());

							ThingMethod method = ThingMethod.of(inboundMessage.getMethod());
							switch (method) {
							case REGISTER:
								// TODO register device and get auth code or secret
								break;
							case AUTH:
								inboundMessage.setTimestamp(ts);

								ctx.pipeline().addAfter(ctx.executor(), this.getClass().getSimpleName(), TcpAuthenticateHandler.class.getSimpleName(),
										new TcpAuthenticateHandler(tcpServerConfig));
								ctx.fireChannelRead(inboundMessage);
								ctx.pipeline().remove(this.getClass().getSimpleName());

								break;
							default:
								log.error("TCP transport first packet must be a register packet or an auth packet. Device SN: {}, raw data: {}", deviceSn,
										new String(msgBytes, StandardCharsets.UTF_8));
								closeChannel(null);

								break;
							}
						} else {
							log.error("Failed to parse TCP transport first packet into thing model message. Device SN: {}, raw data: {}", deviceSn,
									new String(msgBytes, StandardCharsets.UTF_8));
							closeChannel(null);
						}

						RawPacket rawPacket = new RawPacket();
						rawPacket.setClientId(deviceSn);
						rawPacket.setTimestamp(ts);
						rawPacket.setTransportName(TcpTransport.class.getSimpleName());
						rawPacket.setContent(new String(msgBytes, StandardCharsets.UTF_8));
						rawPacket.setMsgName(inboundMessage.getMethod().split("\\.")[2]);
						rawPacketService.save(rawPacket);
					} else {
						log.error("Invalid device SN in TCP transport packet. Device SN: {}, raw data: {}", deviceSn,
								new String(msgBytes, StandardCharsets.UTF_8));
						closeChannel(null);
					}
				} else {
					log.error("Failed to parse device SN from TCP transport first packet. Raw data: {}", new String(msgBytes, StandardCharsets.UTF_8));
					closeChannel(null);
				}
			} catch (Exception e) {
				log.error("Failed to parse TCP transport first packet. Error: ", e);
				closeChannel(null);
			} finally {
				ReferenceCountUtil.release(buf);
			}
		} else {
			closeChannel(null);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("", cause);
		ctx.channel().close();
	}

	private void closeChannel(ThingMessage farewell) {
		assert ctx.executor().inEventLoop();

		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}

		if (!ctx.channel().isActive()) {
			return;
		}

		assert closeConnectionTask == null;
		closeConnectionTask = ctx.executor().schedule(() -> {
			if (farewell != null) {
				ctx.writeAndFlush(farewell).addListener(ChannelFutureListener.CLOSE);
			} else {
				ctx.channel().close();
			}
		}, ThreadLocalRandom.current().nextInt(100, 3000), TimeUnit.MILLISECONDS);
	}

}