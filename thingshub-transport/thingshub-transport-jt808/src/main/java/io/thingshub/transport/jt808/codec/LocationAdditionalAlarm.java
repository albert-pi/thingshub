package io.thingshub.transport.jt808.codec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@ToString
public final class LocationAdditionalAlarm {

	/**
	 * 报警ID
	 */
	private Integer alarmId;

	// 以下信息只有在报警类型为怠速过长报警（0x0106）、超速报警（0x0107）、疲劳驾驶报警（0x0108）、水温报警（0x0109）时才有

	/**
	 * 报警解除或触发。0x00-报警解除，有以下数据项；0x01-触发报警，没有以下数据项；
	 */
	private Integer status;

	/**
	 * 持续时长（秒）
	 */
	private Integer duration;

	/**
	 * 怠速过长报警数据 - 怠速耗油量（ML）
	 */
	private Integer fuelInIdling;

	/**
	 * 怠速过长报警数据 - 怠速转速最大值（RPM）
	 */
	private Integer maxIdlingRotatingSpeed;

	/**
	 * 怠速过长报警数据 - 怠速转速最小值（RPM）
	 */
	private Integer minIdlingRotatingSpeed;

	/**
	 * 超速报警数据 - 超速最大速度（0.1KM/H）
	 */
	private Integer maxSpeed;

	/**
	 * 超速报警数据 - 平均速度（0.1KM/H）
	 */
	private Integer avgSpeed;

	/**
	 * 超速报警数据 - 超速行驶距离（米）
	 */
	private Integer mileageInOverSpeed;

	/**
	 * 水温过高报警数据 - 最高温度（0.1度）
	 */
	private Integer maxWaterTemp;

	/**
	 * 水温过高报警数据 - 平均温度（0.1度）
	 */
	private Integer avgWaterTemp;

}