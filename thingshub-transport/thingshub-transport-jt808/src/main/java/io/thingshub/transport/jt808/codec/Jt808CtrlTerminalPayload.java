package io.thingshub.transport.jt808.codec;

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
public final class Jt808CtrlTerminalPayload implements MessagePayload {

	/**
	 * 应答流水号
	 */
	@MessageField(order = 1, dataType = WORD)
	private Integer messageSeq;

	/**
	 * 命令参数
	 */
	@MessageField(order = 2, dataType = STRING)
	private String params;

}