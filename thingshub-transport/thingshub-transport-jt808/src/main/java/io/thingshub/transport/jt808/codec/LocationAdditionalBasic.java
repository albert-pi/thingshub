package io.thingshub.transport.jt808.codec;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@ToString
public final class LocationAdditionalBasic {

	/**
	 * 总里程（米）
	 */
	private Integer mileage;

	/**
	 * 总油耗（毫升）
	 */
	private Integer fuel;

	/**
	 * 车辆运行累计总时长（秒）
	 */
	private Integer duration;

	/**
	 * 车辆熄火累计总时长（秒）
	 */
	private Integer durationInStalling;

	/**
	 * 车辆怠速累计总时长（秒）
	 */
	private Integer durationInIdling;

	/**
	 * 加速度信息最近1秒内采集点个数
	 */
	private Integer accelerationMetrics;

	/**
	 * 加速度信息最近1秒内采集点间隔（毫秒）
	 */
	private Integer accelerationMetricInterval;

	/**
	 * 每个采集点平均加速度。数量由accelerationMetrics决定
	 */
	private List<Integer> avgAccelerations;

	/**
	 * 最大加速度（mg）
	 */
	private Integer maxAcceleration;

	/**
	 * 车辆电压（0.1V）
	 */
	private Integer voltage;

	/**
	 * 终端内置电池电压（0.1V）
	 */
	private Integer batteryVoltage;

	/**
	 * 网络信号强度
	 */
	private Integer csq;

	/**
	 * 车型ID
	 */
	private Integer vehicleType;

	/**
	 * OBD协议类型
	 */
	private Integer obdProtocol;

	/**
	 * 驾驶循环标签
	 */
	private Integer drivingId;

	/**
	 * GPS定位收星数
	 */
	private Integer gpsNum;

	/**
	 * GPS位置精度（0.01）
	 */
	private Integer gpsPrecision;

	/**
	 * GPS天线状态。0:天线正常 1:天线开路 2:天线短路(需模块支持)
	 */
	private Integer gpsAntenna;

	/**
	 * 设备拔出状态 0x02: 设备拔出 或者 设备上电后第一次定位前；非0x02: 其他
	 */
	private Integer devicePulled;

	/**
	 * 累计里程
	 */
	private Integer totalMileage;

	/**
	 * 点火类型
	 * <ul>
	 * <li>BIT0: 1:ACC线点火</li>
	 * <li>BIT1: 1:安防监听点火</li>
	 * <li>BIT2: 1:GPS速度</li>
	 * <li>BIT3: 1:电压</li>
	 * <li>BIT4: 1:发动机车速转速</li>
	 * <li>BIT5: 1:ACC中断点火</li>
	 * </ul>
	 */
	private Integer ignitionType;

}