package io.thingshub.transport.jt808.codec;

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
public final class Jt808GenericReplyPayload implements MessagePayload {

	/**
	 * 如果是终端应答，对应的是平台消息的流水号；如果是平台应答，对应的是终端消息的流水号
	 */
	@MessageField(order = 1, dataType = WORD)
	private int messageSeq;

	/**
	 * 如果是终端应答，对应的是平台消息的 ID；如果是平台应答，对应的是终端消息的 ID
	 */
	@MessageField(order = 2, dataType = WORD)
	private final int messageId;

	/**
	 * 0：成功/确认；1：失败；2：消息有误；3：不支持；4：报警处理确认；
	 */
	@MessageField(order = 3, dataType = BYTE)
	private int result;

}