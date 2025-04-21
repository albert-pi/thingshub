package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class UnregisterMessage extends TransportMessage {

	public UnregisterMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.UNREGISTER.getId();
		this.msgName = Jt808MessageType.UNREGISTER.name();
		this.timestamp = DateUtil.current();
	}

}