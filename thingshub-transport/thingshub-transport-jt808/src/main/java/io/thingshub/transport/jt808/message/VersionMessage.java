package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.codec.Jt808VersionPayload;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class VersionMessage extends TransportMessage {

	/**
	 * 终端软件版本号。比如HBM430_V403633
	 */
	private String versionName;

	/**
	 * 终端软件版本日期。比如2020/12/7
	 */
	private String versionDate;

	/**
	 * CPU ID
	 */
	private String cpuId;

	/**
	 * GSM型号
	 */
	private String gsmModel;

	/**
	 * GSM IMEI
	 */
	private String imei;

	/**
	 * SIM卡 IMSI号
	 */
	private String imsi;

	/**
	 * SIM卡 ICCID
	 */
	private String iccid;

	/**
	 * 车系车型ID
	 */
	private Integer vehicleType;

	/**
	 * 车辆VIN码
	 */
	private String vin;

	/**
	 * 总里程。装上终端后车辆累计总里程或车辆仪表里程(m)
	 */
	private Double mileage;

	/**
	 * 总耗油量。装上终端后车辆累计总耗油量(ml)
	 */
	private Double fuel;

	public VersionMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.VERSION.getId();
		this.msgName = Jt808MessageType.VERSION.name();
		this.timestamp = DateUtil.current();

		Jt808VersionPayload payload = (Jt808VersionPayload) message.getPayload();

		this.versionName = payload.getVersionName();
		this.versionDate = payload.getVersionDate();
		this.cpuId = payload.getCpuId();
		this.gsmModel = payload.getGsmModel();
		this.imei = payload.getImei();
		this.imsi = payload.getImsi();
		this.iccid = payload.getIccid();
		this.vehicleType = payload.getVehicleType();
		this.vin = payload.getVin();
		this.mileage = NumberUtil.div(payload.getMileage().toString(), "1000", 2).doubleValue();
		this.fuel = NumberUtil.div(payload.getFuel().toString(), "1000", 2).doubleValue();
	}

}