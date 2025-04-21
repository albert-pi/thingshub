package io.thingshub.transport.jt808.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

import io.thingshub.transport.codec.MessagePayload;
import io.thingshub.transport.jt808.Jt808Exception;
import io.thingshub.utils.ByteOps;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Jt808Message {

	public static final Charset JT808_STRING_ENCODING = Charset.forName("GBK");

	public static final int DELIMITER = 0x7e;

	private static final Function<byte[], Byte> DEFAULT_CHECK_SUM_CALCULATOR = bytes -> calculateCheckSum(bytes, 0, bytes.length);

	private static final Function<byte[], byte[]> DEFAULT_ESCAPE_OUTBOUND_FUNCTION = bytes -> escapeOutboundMessage(bytes, 0, bytes.length - 1);

	public static byte calculateCheckSum(byte[] bytes, int start, int end) {
		byte checksum = bytes[start];
		for (int i = start + 1; i < end; i++) {
			checksum ^= bytes[i];
		}

		return checksum;
	}

	private static byte[] escapeOutboundMessage(byte[] bytes, int start, int end) {
		if (start < 0 || end > bytes.length) {
			throw new Jt808Exception("byte index out of bounds(start=" + start + ",end=" + end + ",bytes length=" + bytes.length + ")");
		}

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			for (int i = 0; i < start; i++) {
				outputStream.write(bytes[i]);
			}

			for (int i = start; i <= end; i++) {
				if (bytes[i] == 0x7e) {
					outputStream.write(0x7d);
					outputStream.write(0x02);
				} else if (bytes[i] == 0x7d) {
					outputStream.write(0x7d);
					outputStream.write(0x01);
				} else {
					outputStream.write(bytes[i]);
				}
			}
			for (int i = end + 1; i < bytes.length; i++) {
				outputStream.write(bytes[i]);
			}
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new Jt808Exception(e);
		}
	}

	@Getter
	private final Jt808Header header;

	@Getter
	private final MessagePayload payload;

	public Jt808Message(Jt808Header header) {
		this(header, null);
	}

	public Jt808Message(Jt808Header header, MessagePayload payload) {
		this.header = header;
		this.payload = payload;
	}

	public byte[] encode() {
		return this.encode(DEFAULT_CHECK_SUM_CALCULATOR, DEFAULT_ESCAPE_OUTBOUND_FUNCTION);
	}

	public byte[] encode(Function<byte[], Byte> checksumCalculator, Function<byte[], byte[]> escapeFunction) {
		byte[] payloadBytes = this.payload.toBytes();
		this.header.getPayloadProps().setPayloadLength(payloadBytes.length);

		final byte[] headerBytes = this.header.toBytes();
		byte[] headerPayloadBytes = ByteOps.concatAll(headerBytes, payloadBytes);
		byte checkSum = checksumCalculator.apply(headerPayloadBytes);

		if (escapeFunction == null) {
			return ByteOps.concatAll(new byte[] { DELIMITER }, headerPayloadBytes, new byte[] { checkSum }, new byte[] { DELIMITER });
		}

		final byte[] unescapedBytes = ByteOps.concatAll(headerPayloadBytes, new byte[] { checkSum });

		final byte[] escaped = escapeFunction.apply(unescapedBytes);

		return ByteOps.concatAll(new byte[] { DELIMITER }, escaped, new byte[] { DELIMITER });
	}

}