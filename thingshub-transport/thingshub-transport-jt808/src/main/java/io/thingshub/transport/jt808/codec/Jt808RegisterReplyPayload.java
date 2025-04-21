package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;
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
public final class Jt808RegisterReplyPayload implements MessagePayload {

	@MessageField(order = 1, dataType = WORD)
	private int msgSeq;

	@MessageField(order = 2, dataType = BYTE)
	private int result;

	@MessageField(order = 3, dataType = STRING)
	private String code;

}