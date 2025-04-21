package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.STRING;

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
public final class Jt808CtrlTerminalReplyPayload implements MessagePayload {

	/**
	 * 命令字
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer cmd;

	/**
	 * 命令参数
	 */
	@MessageField(order = 2, dataType = STRING)
	private String params;

}