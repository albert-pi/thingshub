package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BCD;
import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.DWORD;
import static io.thingshub.transport.codec.MessageDataType.WORD;

import java.util.List;

import io.thingshub.transport.codec.MessageField;
import io.thingshub.transport.codec.MessagePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@Getter
@ToString
public final class Jt808TransmissionPayload implements MessagePayload {

	/**
	 * 透传类型
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer type;

	// ================== 驾驶行程数据 ==================

	/**
	 * 开始时间
	 */
	@MessageField(order = 101, dataType = BCD, length = 6)
	private String startTime;

	/**
	 * 结束时间
	 */
	@MessageField(order = 102, dataType = BCD, length = 6)
	private String endTime;

	/**
	 * 起点纬度
	 */
	@MessageField(order = 103, dataType = BCD, length = 4)
	private Integer startLat;

	/**
	 * 起点经度
	 */
	@MessageField(order = 104, dataType = BCD, length = 4)
	private Integer startLng;

	/**
	 * 终点纬度
	 */
	@MessageField(order = 105, dataType = BCD, length = 4)
	private Integer endLat;

	/**
	 * 终点经度
	 */
	@MessageField(order = 106, dataType = BCD, length = 4)
	private Integer endLng;

	/**
	 * 驾驶循环标签
	 */
	@MessageField(order = 107, dataType = WORD)
	private Integer drivingId;

	/**
	 * 一个驾驶循环总里程类型
	 */
	@MessageField(order = 108, dataType = BYTE)
	private Integer mileageType;

	/**
	 * 一个驾驶循环总里程，单位米
	 */
	@MessageField(order = 109, dataType = BCD, length = 4)
	private Integer mileage;

	/**
	 * 一个驾驶循环总耗油，单位毫升(ml)
	 */
	@MessageField(order = 110, dataType = BCD, length = 4)
	private Integer fuel;

	/**
	 * 一个驾驶循环总时长，单位秒
	 */
	@MessageField(order = 111, dataType = BCD, length = 4)
	private Integer duration;

	/**
	 * 一个驾驶循环超速累计时长，单位秒
	 */
	@MessageField(order = 112, dataType = WORD)
	private Integer durationInOverspeed;

	/**
	 * 一个驾驶循环超速次数，单位次
	 */
	@MessageField(order = 113, dataType = WORD)
	private Integer timesInOverspeed;

	/**
	 * 一个驾驶循环平均车速，单位KM/H
	 */
	@MessageField(order = 114, dataType = BYTE)
	private Integer avgSpeed;

	/**
	 * 一个驾驶循环最大车速，单位KM/H
	 */
	@MessageField(order = 115, dataType = BYTE)
	private Integer maxSpeed;

	/**
	 * 一个驾驶循环怠速时长，单位秒
	 */
	@MessageField(order = 116, dataType = BCD, length = 4)
	private Integer durationInIdling;

	/**
	 * 一个驾驶循环脚刹次数支持与否，1为支持
	 */
	@MessageField(order = 117, dataType = BYTE)
	private Integer supportFootbraking;

	/**
	 * 一个驾驶循环脚刹总次数，单位次
	 */
	@MessageField(order = 118, dataType = WORD)
	private Integer footbrakingTimes;

	/**
	 * 一个驾驶循环急加速次数
	 */
	@MessageField(order = 119, dataType = BCD, length = 4)
	private Integer rapidSpeedupTimes;

	/**
	 * 一个驾驶循环急减速次数
	 */
	@MessageField(order = 120, dataType = BCD, length = 4)
	private Integer rapidSlowdownTimes;

	/**
	 * 一个驾驶循环急转弯次数
	 */
	@MessageField(order = 121, dataType = BCD, length = 4)
	private Integer rapidTurningTimes;

	/**
	 * 速度为-20Km/H的里程,单位:m
	 */
	@MessageField(order = 122, dataType = BCD, length = 4)
	private Integer mileage4Speed20;

	/**
	 * 速度为20-40Km/H的里程,单位:m
	 */
	@MessageField(order = 123, dataType = BCD, length = 4)
	private Integer mileage4Speed2040;

	/**
	 * 速度为40-60Km/H的里程,单位:m
	 */
	@MessageField(order = 124, dataType = BCD, length = 4)
	private Integer mileage4Speed4060;

	/**
	 * 速度为60-80Km/H的里程,单位:m
	 */
	@MessageField(order = 125, dataType = BCD, length = 4)
	private Integer mileage4Speed6080;

	/**
	 * 速度为80-100Km/H的里程,单位:m
	 */
	@MessageField(order = 126, dataType = BCD, length = 4)
	private Integer mileage4Speed80100;

	/**
	 * 速度为100-120Km/H的里程,单位:m
	 */
	@MessageField(order = 127, dataType = BCD, length = 4)
	private Integer mileage4Speed100120;

	/**
	 * 速度为120Km/H以上的里程,单位:m
	 */
	@MessageField(order = 128, dataType = BCD, length = 4)
	private Integer mileage4Speed120;

	/**
	 * 一个行程中的怠速油耗值,单位:ML
	 */
	@MessageField(order = 129, dataType = BCD, length = 4)
	private Integer fuleInIdling;

	// ================== 故障码数据 ==================

	/**
	 * 时间
	 */
	@MessageField(order = 201, dataType = BCD, length = 6)
	private String faultTime;

	/**
	 * 纬度
	 */
	@MessageField(order = 202, dataType = DWORD)
	private Integer faultLat;

	/**
	 * 经度
	 */
	@MessageField(order = 203, dataType = DWORD)
	private Integer faultLng;

	/**
	 * 为0表示无故障码，非0为故障码个数
	 */
	@MessageField(order = 204, dataType = BYTE)
	private Integer faultCount;

	/**
	 * 故障码ID列表。数量由faultCount确定
	 */
	private List<Integer> faultIds;

	// ================== 休眠状态数据 ==================

	/**
	 * 休眠进入时间，年月日时分秒
	 */
	@MessageField(order = 301, dataType = BCD, length = 6)
	private String sleepingTime;

	// ================== 休眠被唤醒数据 ==================

	/**
	 * 休眠被时间，年月日时分秒
	 */
	@MessageField(order = 401, dataType = BCD, length = 6)
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
	@MessageField(order = 402, dataType = BYTE)
	private Integer wakeningType;

	/**
	 * 总线电压
	 */
	@MessageField(order = 403, dataType = WORD)
	private Integer voltage;

	/**
	 * 震动加速度值
	 */
	@MessageField(order = 404, dataType = WORD)
	private Integer acceleration;

}