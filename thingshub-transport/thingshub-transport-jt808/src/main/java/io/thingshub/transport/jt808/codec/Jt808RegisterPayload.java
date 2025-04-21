package io.thingshub.transport.jt808.codec;

import static io.thingshub.transport.codec.MessageDataType.BYTE;
import static io.thingshub.transport.codec.MessageDataType.BYTES;
import static io.thingshub.transport.codec.MessageDataType.STRING;
import static io.thingshub.transport.codec.MessageDataType.WORD;

import cn.hutool.core.util.HexUtil;
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
public final class Jt808RegisterPayload implements MessagePayload {

	@MessageField(order = 1, dataType = WORD)
	private int provinceId;

	@MessageField(order = 2, dataType = WORD)
	private int cityId;

	@MessageField(order = 3, dataType = BYTES, length = 5)
	private String manufacturerId;

	@MessageField(order = 4, dataType = BYTES, length = 20)
	private String model;

	@MessageField(order = 5, dataType = BYTES, length = 7)
	private String terminalId;

	@MessageField(order = 6, dataType = BYTE)
	private int plateColor;

	@MessageField(order = 7, dataType = STRING)
	private String licensePlate;

	public static void main(String[] args) {
		Jt808RegisterPayload registerPayload = Jt808RegisterPayload.builder().provinceId(11).cityId(0).manufacturerId("12359").model("12345678901234567890")
				.terminalId("id12345").plateColor(1).licensePlate("13160466666").build();
		System.out.println(HexUtil.encodeHex(registerPayload.toBytes()));
	}

}