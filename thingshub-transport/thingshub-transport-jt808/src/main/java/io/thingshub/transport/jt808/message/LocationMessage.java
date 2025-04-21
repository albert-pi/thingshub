package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808LocationPayload;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.codec.LocationAdditionalAlarm;
import io.thingshub.transport.jt808.codec.LocationAdditionalBasic;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class LocationMessage extends TransportMessage {

	/**
	 * 报警标志
	 */
	private Integer alarm;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 纬度
	 */
	private Double lat;

	/**
	 * 经度
	 */
	private Double lng;

	/**
	 * 高程。海拔高度，单位为米（m）
	 */
	private Integer elevation;

	/**
	 * 速度。km/h
	 */
	private Double speed;

	/**
	 * 方向。0-359，正北为 0，顺时针
	 */
	private Integer angle;

	/**
	 * 时间。YYYY-MM-DD hh-mm-ss(GMT+8时间，本标准之后涉及的时间均采用此时区）
	 */
	private String time;

	/**
	 * 长消息时的报警信息
	 */
	private LocationAdditionalAlarm additionalAlarm;

	/**
	 * 长消息时的OBD基础数据
	 */
	private LocationAdditionalBasic additionalBasic;

	public LocationMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.LOCATION.getId();
		this.msgName = Jt808MessageType.LOCATION.name();
		this.timestamp = DateUtil.current();

		Jt808LocationPayload payload = (Jt808LocationPayload) message.getPayload();

		this.alarm = payload.getAlarm();
		this.status = payload.getStatus();
		this.lat = NumberUtil.div(payload.getLat().toString(), "1000000", 6).doubleValue();
		this.lng = NumberUtil.div(payload.getLng().toString(), "1000000", 6).doubleValue();
		this.elevation = payload.getElevation();
		this.speed = NumberUtil.div(payload.getSpeed().toString(), "10", 2).doubleValue();
		this.angle = payload.getAngle();
		this.time = DateUtil.format(DateUtil.parse("20" + payload.getTime()), DatePattern.NORM_DATETIME_FORMATTER);

		this.additionalAlarm = payload.getAdditionalAlarm();
		this.additionalBasic = payload.getAdditionalBasic();
	}

}