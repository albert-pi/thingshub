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
public class TransmissionWakeningMessage extends TransportMessage {

	/**
	 * 休眠被唤醒时间，年月日时分秒
	 */
	private String wakeningTime;

	/**
	 * 唤醒类型
	 * <ul>
	 * <li>心跳 0X01</li>
	 * <li>CAN1 0X02</li>
	 * <li>CAN2 0X04</li>
	 * <li>G-SENSOR 0X08</li>
	 * <li>电压变化 0X10</li>
	 * <li>GSM 0X20</li>
	 * </ul>
	 */
	private Integer wakeningType;

	/**
	 * 总线电压
	 */
	private Integer voltage;

	/**
	 * 震动加速度值
	 */
	private Integer acceleration;

	public TransmissionWakeningMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.TRANSMISSION.getId();
		this.msgName = Jt808MessageType.TRANSMISSION.name();
		this.timestamp = DateUtil.current();

		Jt808TransmissionPayload payload = (Jt808TransmissionPayload) message.getPayload();
		this.wakeningTime = payload.getWakeningTime();
		this.wakeningType = payload.getWakeningType();
		this.voltage = payload.getVoltage();
		this.acceleration = payload.getAcceleration();
	}

}