package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;

public class HeartBeatMessage extends TransportMessage {

	public HeartBeatMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.HEARTBEAT.getId();
		this.msgName = Jt808MessageType.HEARTBEAT.name();
		this.timestamp = DateUtil.current();
	}

}
