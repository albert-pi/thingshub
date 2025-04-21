package io.thingshub.transport.mqtt.codec;

import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.utils.MessageUtils;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public final class MqttTransformEncoder extends MessageToMessageEncoder<MqttMessage> {

	private ScriptEngineFactory scriptEngineFactory;

	@Inject
	public MqttTransformEncoder(ScriptEngineFactory scriptEngineFactory) {
		this.scriptEngineFactory = scriptEngineFactory;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, MqttMessage msg, List<Object> out) throws Exception {
		switch (msg.fixedHeader().messageType()) {
		case PUBLISH:
			MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
			publishMessage.retain();

			try {
				String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
				String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();
				if (productCode != null && scriptLang != null) {
					ScriptEngine scriptEngine = scriptEngineFactory.getScriptEngine(scriptLang);

					byte[] thingMessageInBytes = MessageUtils.readByteBuf(publishMessage.payload());
					JSONObject payloadObj = JSON.parseObject(thingMessageInBytes);
					byte[] payloadInBytes = scriptEngine.invoke(productCode, ScriptEngine.BYTEARRAY_TYPE, "encode", payloadObj);
					ByteBuf payloadBuf = Unpooled.copiedBuffer(payloadInBytes);
					out.add(publishMessage.replace(payloadBuf));
					publishMessage.release();
				} else {
					out.add(publishMessage);
				}
			} catch (Exception e) {
				log.error("", e);
			}

			break;
//			case SUBACK:
		// 重新构建MqttSubscribeMessage，转换topic
//				MqttSubscribeMessage subscribeMsg = (MqttSubscribeMessage) msg;
		//
//				break;
//			case UNSUBACK:
		// 重新构建MqttUnsubscribeMessage，转换topic
//					MqttSubscribeMessage subscribeMsg = (MqttSubscribeMessage) msg;
		//
//					break;
		default:
			out.add(msg);
			break;
		}
	}

}
