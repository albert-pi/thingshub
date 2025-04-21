package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTES;
import static io.thingshub.transport.codec.MessageDataType.DWORD;
import static io.thingshub.transport.codec.MessageDataType.STRING;
import static io.thingshub.transport.codec.MessageDataType.WORD;

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
public final class Jt808VersionPayload implements MessagePayload {

	/**
	 * 终端软件版本号。比如HBM430_V403633
	 */
	@MessageField(order = 1, dataType = STRING, length = 14)
	private String versionName;

	/**
	 * 终端软件版本日期。比如2020/12/7
	 */
	@MessageField(order = 2, dataType = STRING, length = 10)
	private String versionDate;

	/**
	 * CPU ID
	 */
	@MessageField(order = 3, dataType = BYTES, length = 12)
	private String cpuId;

	/**
	 * GSM型号
	 */
	@MessageField(order = 4, dataType = STRING, length = 15)
	private String gsmModel;

	/**
	 * GSM IMEI
	 */
	@MessageField(order = 5, dataType = STRING, length = 15)
	private String imei;

	/**
	 * SIM卡 IMSI号
	 */
	@MessageField(order = 6, dataType = STRING, length = 15)
	private String imsi;

	/**
	 * SIM卡 ICCID
	 */
	@MessageField(order = 6, dataType = STRING, length = 20)
	private String iccid;

	/**
	 * 车系车型ID
	 */
	@MessageField(order = 7, dataType = WORD)
	private Integer vehicleType;

	/**
	 * 车辆VIN码
	 */
	@MessageField(order = 8, dataType = STRING, length = 17)
	private String vin;

	/**
	 * 总里程。装上终端后车辆累计总里程或车辆仪表里程(m)
	 */
	@MessageField(order = 9, dataType = DWORD)
	private Integer mileage;

	/**
	 * 总耗油量。装上终端后车辆累计总耗油量(ml)
	 */
	@MessageField(order = 10, dataType = DWORD)
	private Integer fuel;

}