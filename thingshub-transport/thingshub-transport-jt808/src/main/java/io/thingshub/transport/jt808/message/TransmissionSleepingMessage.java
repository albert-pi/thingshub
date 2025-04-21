package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.codec.Jt808TransmissionPayload;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class TransmissionSleepingMessage extends TransportMessage {

	/**
	 * 休眠时间
	 */
	private String sleepingTime;

	public TransmissionSleepingMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.TRANSMISSION.getId();
		this.msgName = Jt808MessageType.TRANSMISSION.name();
		this.timestamp = DateUtil.current();

		Jt808TransmissionPayload payload = (Jt808TransmissionPayload) message.getPayload();
		this.sleepingTime = payload.getSleepingTime();
	}

}