package io.thingshub.transport.jt808.codec;

import io.thingshub.transport.codec.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@ToString
public final class Jt808LocationPayload implements MessagePayload {

	/**
	 * 报警标志
	 */
	private Integer alarm;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 纬度。以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
	 */
	private Integer lat;

	/**
	 * 经度。以度为单位的经度值乘以 10 的 6 次方，精确到百万分之一度
	 */
	private Integer lng;

	/**
	 * 高程。海拔高度，单位为米（m）
	 */
	private Integer elevation;

	/**
	 * 速度。1/10km/h
	 */
	private Integer speed;

	/**
	 * 方向。0-359，正北为 0，顺时针
	 */
	private Integer angle;

	/**
	 * 时间。YY-MM-DD-hh-mm-ss(GMT+8时间，本标准之后涉及的时间均采用此时区）
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

}