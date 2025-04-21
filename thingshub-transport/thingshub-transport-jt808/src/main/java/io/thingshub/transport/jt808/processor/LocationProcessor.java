package io.thingshub.transport.jt808.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.codec.MessagePayload;
import io.thingshub.transport.jt808.codec.Jt808GenericReplyPayload;
import io.thingshub.transport.jt808.codec.Jt808Header;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.message.LocationMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationProcessor extends Processor<BaseChannelContext, LocationMessage> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull LocationMessage msg) {

		log.info("JT808 client location data - {} | {} | {}", ctx.getClientAddr(), ctx.getClientId(), "location");

		// TODO 发送Publish消息到MQTT（物模型中定义的采集量消息）

		// TODO 业务系统的处理：1.保存采集量信息；2.通过MQTT下发通用应答

		Jt808Header.PayloadProps payloadProps = new Jt808Header.PayloadProps(5, 0, false, 0);
		Jt808Header header = new Jt808Header(Jt808MessageType.LOCATION, payloadProps, ctx.getClientId(), ctx.nextPacketId());
		MessagePayload payload = Jt808GenericReplyPayload.builder().messageSeq(msg.getPacketId()).messageId(Jt808MessageType.LOCATION.getId()).result(0)
				.build();
		Jt808Message replyMessage = new Jt808Message(header, payload);
		ctx.writeAndFlush(replyMessage);
	}

}
