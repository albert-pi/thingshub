package io.thingshub.transport.jt808.message;

import java.util.List;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.codec.Jt808TransmissionPayload;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class TransmissionFaultMessage extends TransportMessage {

	/**
	 * 时间
	 */
	private String faultTime;

	/**
	 * 纬度
	 */
	private Double faultLat;

	/**
	 * 经度
	 */
	private Double faultLng;

	/**
	 * 故障码ID列表
	 */
	private List<Integer> faultIds;

	public TransmissionFaultMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.TRANSMISSION.getId();
		this.msgName = Jt808MessageType.TRANSMISSION.name();
		this.timestamp = DateUtil.current();

		Jt808TransmissionPayload payload = (Jt808TransmissionPayload) message.getPayload();

		this.faultTime = payload.getFaultTime();
		this.faultLat = NumberUtil.div(payload.getFaultLat().toString(), "1000000", 6).doubleValue();
		this.faultLng = NumberUtil.div(payload.getFaultLng().toString(), "1000000", 6).doubleValue();
		this.faultIds = payload.getFaultIds();
	}

}