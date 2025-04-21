package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;

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
public final class Jt808UpgradeResultPayload implements MessagePayload {

	/**
	 * 升级类型
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer type;

	/**
	 * 升级结果
	 */
	@MessageField(order = 2, dataType = BYTE)
	private Integer result;

}