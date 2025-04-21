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
public final class Jt808TextPayload implements MessagePayload {

	/**
	 * 标志
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer type;

	/**
	 * 文本信息
	 */
	@MessageField(order = 2, dataType = STRING)
	private String content;

}