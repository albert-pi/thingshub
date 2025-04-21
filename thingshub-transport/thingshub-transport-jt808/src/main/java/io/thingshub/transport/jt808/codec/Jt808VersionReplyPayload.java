package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BCD;
import static io.thingshub.transport.codec.MessageDataType.BYTE;
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
public final class Jt808VersionReplyPayload implements MessagePayload {

	/**
	 * 平台当前时间
	 */
	@MessageField(order = 1, dataType = BCD, length = 6)
	private String time;

	/**
	 * 车型ID
	 */
	@MessageField(order = 2, dataType = WORD)
	private Integer vehicleType;

	/**
	 * 排量
	 */
	@MessageField(order = 3, dataType = BYTE)
	private Integer capacity;

	/**
	 * 是否升级
	 */
	@MessageField(order = 10, dataType = BYTE)
	private Integer upgraded;

}