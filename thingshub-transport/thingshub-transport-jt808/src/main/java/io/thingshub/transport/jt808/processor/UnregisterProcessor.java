package io.thingshub.transport.jt808.processor;

import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.Processor;
import io.thingshub.transport.codec.MessagePayload;
import io.thingshub.transport.jt808.codec.Jt808GenericReplyPayload;
import io.thingshub.transport.jt808.codec.Jt808Header;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.message.UnregisterMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnregisterProcessor extends Processor<BaseChannelContext, UnregisterMessage> {

	@Override
	public void process(@NonNull BaseChannelContext ctx, @NonNull UnregisterMessage msg) {
		log.info("JT808 client unregister - {} | {} | {}", ctx.getClientAddr(), ctx.getClientId(), "unregister");

		Jt808Header.PayloadProps payloadProps = new Jt808Header.PayloadProps(5, 0, false, 0);
		Jt808Header header = new Jt808Header(Jt808MessageType.UNREGISTER, payloadProps, ctx.getClientId(), ctx.nextPacketId());
		MessagePayload payload = Jt808GenericReplyPayload.builder().messageSeq(msg.getPacketId()).messageId(Jt808MessageType.UNREGISTER.getId()).result(0)
				.build();
		Jt808Message replyMessage = new Jt808Message(header, payload);
		ctx.writeAndFlush(replyMessage);
	}

}
