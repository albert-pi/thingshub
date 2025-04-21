package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.DWORD;
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
public final class Jt808TerminalParamItem implements MessagePayload {

	/**
	 * 参数 ID
	 */
	@MessageField(order = 1, dataType = DWORD)
	private Integer paramId;

	/**
	 * 参数长度
	 */
	@MessageField(order = 2, dataType = BYTE)
	private Integer paramLen;

	/**
	 * 参数值。数据类型为DWORD或STRING，视参数ID而定
	 */
	@MessageField(order = 3, dataType = STRING)
	private Object paramValue;

}