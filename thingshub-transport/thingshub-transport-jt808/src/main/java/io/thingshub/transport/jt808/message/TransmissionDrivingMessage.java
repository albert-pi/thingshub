package io.thingshub.transport.jt808.message;

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
public class TransmissionDrivingMessage extends TransportMessage {

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;

	/**
	 * 起点纬度
	 */
	private Double startLat;

	/**
	 * 起点经度
	 */
	private Double startLng;

	/**
	 * 终点纬度
	 */
	private Double endLat;

	/**
	 * 终点经度
	 */
	private Double endLng;

	/**
	 * 驾驶循环标签
	 */
	private Integer drivingId;

	/**
	 * 一个驾驶循环总里程类型
	 */
	private Integer mileageType;

	/**
	 * 一个驾驶循环总里程，单位Km
	 */
	private Double mileage;

	/**
	 * 一个驾驶循环总耗油，单位L
	 */
	private Double fuel;

	/**
	 * 一个驾驶循环总时长，单位H
	 */
	private Double duration;

	/**
	 * 一个驾驶循环超速累计时长，单位H
	 */
	private Double durationInOverspeed;

	/**
	 * 一个驾驶循环超速次数，单位次
	 */
	private Integer timesInOverspeed;

	/**
	 * 一个驾驶循环平均车速，单位KM/H
	 */
	private Double avgSpeed;

	/**
	 * 一个驾驶循环最大车速，单位KM/H
	 */
	private Double maxSpeed;

	/**
	 * 一个驾驶循环怠速时长，单位H
	 */
	private Double durationInIdling;

	/**
	 * 一个驾驶循环脚刹次数支持与否，1为支持
	 */
	private Integer supportFootbraking;

	/**
	 * 一个驾驶循环脚刹总次数，单位次
	 */
	private Integer footbrakingTimes;

	/**
	 * 一个驾驶循环急加速次数
	 */
	private Integer rapidSpeedupTimes;

	/**
	 * 一个驾驶循环急减速次数
	 */
	private Integer rapidSlowdownTimes;

	/**
	 * 一个驾驶循环急转弯次数
	 */
	private Integer rapidTurningTimes;

	/**
	 * 速度为-20Km/H的里程,单位Km
	 */
	private Double mileage4Speed20;

	/**
	 * 速度为20-40Km/H的里程,单位Km
	 */
	private Double mileage4Speed2040;

	/**
	 * 速度为40-60Km/H的里程,单位Km
	 */
	private Double mileage4Speed4060;

	/**
	 * 速度为60-80Km/H的里程,单位Km
	 */
	private Double mileage4Speed6080;

	/**
	 * 速度为80-100Km/H的里程,单位Km
	 */
	private Double mileage4Speed80100;

	/**
	 * 速度为100-120Km/H的里程,单位Km
	 */
	private Double mileage4Speed100120;

	/**
	 * 速度为120Km/H以上的里程,单位Km
	 */
	private Double mileage4Speed120;

	/**
	 * 一个行程中的怠速油耗值,单位L
	 */
	private Double fuleInIdling;

	public TransmissionDrivingMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.TRANSMISSION.getId();
		this.msgName = Jt808MessageType.TRANSMISSION.name();
		this.timestamp = DateUtil.current();

		Jt808TransmissionPayload payload = (Jt808TransmissionPayload) message.getPayload();

		this.startTime = payload.getStartTime();
		this.endTime = payload.getEndTime();
		this.startLat = NumberUtil.div(payload.getStartLat().toString(), "1000000", 6).doubleValue();
		this.startLng = NumberUtil.div(payload.getStartLng().toString(), "1000000", 6).doubleValue();
		this.endLat = NumberUtil.div(payload.getEndLat().toString(), "1000000", 6).doubleValue();
		this.endLng = NumberUtil.div(payload.getEndLng().toString(), "1000000", 6).doubleValue();
		this.drivingId = payload.getDrivingId();
		this.mileageType = payload.getMileageType();
		this.mileage = NumberUtil.div(payload.getMileage().toString(), "1000", 2).doubleValue();
		this.fuel = NumberUtil.div(payload.getFuel().toString(), "1000", 2).doubleValue();
		this.duration = NumberUtil.div(payload.getDuration().toString(), "3600", 2).doubleValue();
		this.durationInOverspeed = NumberUtil.div(payload.getDurationInOverspeed().toString(), "3600", 2).doubleValue();
		this.timesInOverspeed = payload.getTimesInOverspeed();
		this.avgSpeed = NumberUtil.toDouble(payload.getAvgSpeed());
		this.maxSpeed = NumberUtil.toDouble(payload.getMaxSpeed());
		this.durationInIdling = NumberUtil.div(payload.getDurationInIdling().toString(), "3600", 2).doubleValue();
		this.supportFootbraking = payload.getSupportFootbraking();
		this.footbrakingTimes = payload.getFootbrakingTimes();
		this.rapidSpeedupTimes = payload.getRapidSpeedupTimes();
		this.rapidSlowdownTimes = payload.getRapidSlowdownTimes();
		this.rapidTurningTimes = payload.getRapidTurningTimes();

		this.mileage4Speed20 = NumberUtil.div(payload.getMileage4Speed20().toString(), "1000", 2).doubleValue();
		this.mileage4Speed2040 = NumberUtil.div(payload.getMileage4Speed2040().toString(), "1000", 2).doubleValue();
		this.mileage4Speed4060 = NumberUtil.div(payload.getMileage4Speed4060().toString(), "1000", 2).doubleValue();
		this.mileage4Speed6080 = NumberUtil.div(payload.getMileage4Speed6080().toString(), "1000", 2).doubleValue();
		this.mileage4Speed80100 = NumberUtil.div(payload.getMileage4Speed80100().toString(), "1000", 2).doubleValue();
		this.mileage4Speed100120 = NumberUtil.div(payload.getMileage4Speed100120().toString(), "1000", 2).doubleValue();
		this.mileage4Speed120 = NumberUtil.div(payload.getMileage4Speed120().toString(), "1000", 2).doubleValue();
		this.fuleInIdling = NumberUtil.div(payload.getFuleInIdling().toString(), "1000", 2).doubleValue();
	}

}