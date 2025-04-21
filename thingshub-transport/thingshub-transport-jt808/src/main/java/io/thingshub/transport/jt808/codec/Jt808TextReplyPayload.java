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
public final class Jt808TextReplyPayload implements MessagePayload {

	/**
	 * 标志。'0'代表TXT_BG2312,'1'为 TXT_UNICODE
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer encoding;

	/**
	 * 标志符号。默认为"*提示*",占用6个字节
	 */
	@MessageField(order = 2, dataType = STRING, length = 6)
	private String prompt;

	/**
	 * 文本信息。最长为1024字节,经GBK编码
	 */
	@MessageField(order = 3, dataType = STRING)
	private String content;

}