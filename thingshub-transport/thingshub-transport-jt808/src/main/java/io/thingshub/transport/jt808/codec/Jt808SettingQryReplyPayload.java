package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.WORD;

import java.util.List;

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
public final class Jt808SettingQryReplyPayload implements MessagePayload {

	/**
	 * 应答流水号
	 */
	@MessageField(order = 1, dataType = WORD)
	private Integer messageSeq;

	/**
	 * 参数总数
	 */
	@MessageField(order = 1, dataType = BYTE)
	private Integer total;

	/**
	 * 参数项列表
	 */
	private List<Jt808TerminalParamItem> params;

}