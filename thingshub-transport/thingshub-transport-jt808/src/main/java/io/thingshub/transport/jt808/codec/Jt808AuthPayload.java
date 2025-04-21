package io.thingshub.transport.jt808.codec;

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
public final class Jt808AuthPayload implements MessagePayload {

	@MessageField(order = 1, dataType = STRING)
	private String code;

}