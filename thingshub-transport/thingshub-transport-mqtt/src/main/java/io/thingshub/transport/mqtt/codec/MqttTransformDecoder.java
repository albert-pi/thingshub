package io.thingshub.transport.mqtt.codec;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.thingshub.domain.RawPacket;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.service.RawPacketService;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.mqtt.MqttTransport;
import io.thingshub.utils.MessageUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Transform MQTT payload by script
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
@ChannelHandler.Sharable
public class MqttTransformDecoder extends MessageToMessageDecoder<MqttMessage> {

	private final ScriptEngineFactory scriptEngineFactory;

	private final RawPacketService rawPacketService;

	@Inject
	public MqttTransformDecoder(ScriptEngineFactory scriptEngineFactory, RawPacketService rawPacketService) {
		this.scriptEngineFactory = scriptEngineFactory;
		this.rawPacketService = rawPacketService;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, MqttMessage msg, List<Object> out) throws Exception {
		switch (msg.fixedHeader().messageType()) {
		case PUBLISH:
			MqttPublishMessage pubMsg = (MqttPublishMessage) msg;
			pubMsg.retain();

			try {
				String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
				String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();
				if (productCode != null && scriptLang != null) {
					long ts = System.currentTimeMillis();
					byte[] payloadInBytes = MessageUtils.readByteBuf(pubMsg.payload());

					ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine(scriptLang);
					JSONObject payloadObj = scriptEngine.invoke(productCode, ScriptEngine.JSONOBJECT_TYPE, "decode", payloadInBytes);
					payloadObj.put("timestamp", ts);

					ByteBuf payloadBuf = Unpooled.copiedBuffer(JSON.toJSONString(payloadObj).getBytes(StandardCharsets.UTF_8));
					out.add(pubMsg.replace(payloadBuf));
					pubMsg.release();

					String clientId = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_CLIENT_ID).get();

					RawPacket rawPacket = new RawPacket();
					rawPacket.setClientId(clientId);
					rawPacket.setTimestamp(ts);
					rawPacket.setTransportName(MqttTransport.class.getSimpleName());
					rawPacket.setContent(new String(payloadInBytes, StandardCharsets.UTF_8));
					rawPacket.setMsgName(payloadObj.getString("method").split("\\.")[2]);
					rawPacketService.save(rawPacket);
				} else {
					out.add(pubMsg);
				}
			} catch (Exception e) {
				log.error("", e);
			}

			break;
		default:
			out.add(msg);
			break;
		}
	}

}
