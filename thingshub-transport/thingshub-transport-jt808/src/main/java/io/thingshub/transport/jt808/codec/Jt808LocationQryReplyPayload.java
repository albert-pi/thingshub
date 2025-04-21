package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BCD;
import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.DWORD;
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
public final class Jt808LocationQryReplyPayload implements MessagePayload {

	/**
	 * 应答流水号
	 */
	@MessageField(order = 1, dataType = WORD)
	private Integer messageSeq;

	/**
	 * 参数总数
	 */
	@MessageField(order = 2, dataType = BYTE)
	private Integer total;

	/**
	 * 报警标志
	 */
	@MessageField(order = 3, dataType = DWORD)
	private Integer alarm;

	/**
	 * 状态
	 */
	@MessageField(order = 4, dataType = DWORD)
	private Integer status;

	/**
	 * 纬度。以度为单位的纬度值乘以 10 的 6 次方，精确到百万分之一度
	 */
	@MessageField(order = 5, dataType = DWORD)
	private Integer lat;

	/**
	 * 经度。以度为单位的经度值乘以 10 的 6 次方，精确到百万分之一度
	 */
	@MessageField(order = 6, dataType = DWORD)
	private Integer lng;

	/**
	 * 高程。海拔高度，单位为米（m）
	 */
	@MessageField(order = 7, dataType = WORD)
	private Integer elevation;

	/**
	 * 速度。1/10km/h
	 */
	@MessageField(order = 8, dataType = WORD)
	private Integer speed;

	/**
	 * 方向。0-359，正北为 0，顺时针
	 */
	@MessageField(order = 9, dataType = WORD)
	private Integer angle;

	/**
	 * 时间。YY-MM-DD-hh-mm-ss(GMT+8时间，本标准之后涉及的时间均采用此时区）
	 */
	@MessageField(order = 10, dataType = BCD, length = 6)
	private String time;

}