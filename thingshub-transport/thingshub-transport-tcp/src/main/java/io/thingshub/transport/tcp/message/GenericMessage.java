package io.thingshub.transport.tcp.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.codec.ThingMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class GenericMessage extends TransportMessage {

	private ThingMessage thingMessage;

	public GenericMessage(ThingMessage thingMessage) {
		this.packetId = Integer.parseInt(thingMessage.getId());// ???
//		this.msgType = msgType;
		this.msgName = thingMessage.getMethod().split("\\.")[2];
		this.timestamp = DateUtil.current();
		this.thingMessage = thingMessage;
	}

}
