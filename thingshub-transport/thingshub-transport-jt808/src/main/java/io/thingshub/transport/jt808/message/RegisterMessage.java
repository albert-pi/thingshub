package io.thingshub.transport.jt808.message;

import cn.hutool.core.date.DateUtil;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.jt808.codec.Jt808Message;
import io.thingshub.transport.jt808.codec.Jt808MessageType;
import io.thingshub.transport.jt808.codec.Jt808RegisterPayload;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class RegisterMessage extends TransportMessage {

	private int provinceId;

	private int cityId;

	private String manufacturerId;

	private String model;

	private String terminalId;

	private int plateColor;

	private String licensePlate;

	public RegisterMessage(Jt808Message message) {
		this.packetId = message.getHeader().getPacketId();
		this.msgType = Jt808MessageType.REGISTER.getId();
		this.msgName = Jt808MessageType.REGISTER.name();
		this.timestamp = DateUtil.current();

		Jt808RegisterPayload payload = (Jt808RegisterPayload) message.getPayload();

		this.provinceId = payload.getProvinceId();
		this.cityId = payload.getCityId();
		this.manufacturerId = payload.getManufacturerId();
		this.model = payload.getModel();
		this.terminalId = payload.getTerminalId();
		this.plateColor = payload.getPlateColor();
		this.licensePlate = payload.getLicensePlate();
	}

}