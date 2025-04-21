package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808AuthPayload;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class AuthMessage extends TransportMessage {

	private String code;

	public AuthMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.AUTH.getId();
		this.msgName = Jt808MessageType.AUTH.name();
		this.timestamp = DateUtil.current();

		Jt808AuthPayload payload = (Jt808AuthPayload) message.getPayload();
		this.code = payload.getCode();
	}

}