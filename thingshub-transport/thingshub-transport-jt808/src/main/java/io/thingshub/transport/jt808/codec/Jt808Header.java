package io.thingshub.transport.jt808.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

import io.thingshub.transport.codec.MessageHeader;
import io.thingshub.transport.codec.MessageType;
import io.thingshub.transport.jt808.Jt808Exception;
import io.thingshub.utils.BcdOps;
import io.thingshub.utils.IntOps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public final class Jt808Header implements MessageHeader {

	/**
	 * 消息流水号
	 */
	private final int packetId;

	/**
	 * 消息类型（消息ID及描述）
	 */
	private final MessageType messageType;

	/**
	 * 消息体属性
	 */
	private final PayloadProps payloadProps;

	/**
	 * 终端手机号（即设备ID）
	 */
	private final String deviceSn;

	private final int totalPackages;

	private final int packageSeq;

	public Jt808Header(MessageType messageType, PayloadProps payloadProps, String terminalId, int packetId) {
		this(messageType, payloadProps, terminalId, packetId, 1, 1);
	}

	public Jt808Header(MessageType messageType, PayloadProps payloadProps, String deviceSn, int packetId, int totalPackages, int packageSeq) {
		this.messageType = messageType;
		this.payloadProps = payloadProps;
		this.deviceSn = deviceSn;
		this.packetId = packetId;
		this.totalPackages = totalPackages;
		this.packageSeq = packageSeq;
	}

	public byte[] toBytes() {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			// 1. 消息ID word(16)
			baos.write(IntOps.intTo2Bytes(messageType.getId()));
			// 2. 消息体属性 word(16)
			baos.write(IntOps.intTo2Bytes(payloadProps.value()));
			// 3. 终端手机号 bcd[6]
			baos.write(BcdOps.strToBcd(deviceSn));
			// 4. 消息流水号 word(16),按发送顺序从 0 开始循环累加
			baos.write(IntOps.intTo2Bytes(packetId));
			// 消息包封装项 此处不予考虑
			return baos.toByteArray();
		} catch (IOException e) {
			throw new Jt808Exception(e);
		}
	}

	@Getter
	public static class PayloadProps {
		/**
		 * bit[0-9] 消息体长度
		 */
		@Setter
		private int payloadLength;

		/**
		 * bit[10-12] 数据加密方式
		 * 
		 * 三位都为 0，表示消息体不加密 第 10 位为 1，表示消息体经过 RSA 算法加密
		 * 
		 * 其它位保留
		 */
		private final int encryptionType;

		/**
		 * bit[13] 是否分包
		 * 
		 * 1：消息体为长消息，进行分包发送处理，具体分包信息由消息包封装项决定 0：则消息头中无消息包封装项字段
		 */
		private final boolean isSubPackage;

		/**
		 * bit[14-15] 保留位
		 */
		private final int reversed;

		public PayloadProps(int payloadLength, int encryptionType, boolean isSubPackage, int reversed) {
			this.payloadLength = payloadLength;
			this.encryptionType = encryptionType;
			this.isSubPackage = isSubPackage;
			this.reversed = reversed;
		}

		public int value() {
			int props = (this.payloadLength & 0x3FF)
					// [10-12] 0001,1100,0000,0000(1C00)(加密类型)
					| ((this.encryptionType << 10) & 0x1C00)
					// [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
					| (((this.isSubPackage ? 1 : 0) << 13) & 0x2000)
					// [14-15] 1100,0000,0000,0000(C000)(保留位)
					| ((this.reversed << 14) & 0xC000);
			return props & 0xFFFF;
		}
	}

	public static class PayloadPropsBuilder {
		/**
		 * bit[0-9] 消息体长度
		 */
		private int payloadLength = -1;

		/**
		 * bit[10-12] 数据加密方式
		 * 
		 * 三位都为 0，表示消息体不加密 第 10 位为 1，表示消息体经过 RSA 算法加密
		 * 
		 * 其它位保留
		 */
		private int encryptionType = 0b000;

		/**
		 * bit[13] 是否分包
		 * 
		 * 1：消息体为长消息，进行分包发送处理，具体分包信息由消息包封装项决定 0：则消息头中无消息包封装项字段
		 */
		private boolean isSubPackage = false;

		/**
		 * bit[14-15] 保留位
		 */
		private int reversed = 0b00;

		private PayloadPropsBuilder() {

		}

		public static PayloadPropsBuilder builder() {
			return new PayloadPropsBuilder();
		}

		public PayloadPropsBuilder withEncryptionType(int encryptionType) {
			this.encryptionType = encryptionType;
			return this;
		}

		public PayloadPropsBuilder withSubPackage(boolean isSubPackage) {
			this.isSubPackage = isSubPackage;
			return this;
		}

		public PayloadPropsBuilder withReversed(int reversed) {
			this.reversed = reversed;
			return this;
		}

		public PayloadProps build() {
			return new PayloadProps(payloadLength, encryptionType, isSubPackage, reversed);
		}

	}

	public static class Jt808HeaderBuilder {
		/**
		 * byte[0-1] 消息ID
		 */
		private MessageType messageType;

		/**
		 * byte[2-3] 消息体属性
		 */
		private PayloadProps payloadProps;

		/**
		 * byte[4-9] 终端手机号或设备ID bcd[6]
		 */
		private String deviceId;

		/**
		 * byte[10-11] 消息流水号，按发送顺序从 0 开始循环累加
		 */
		private int messageSeq;

		// 5. byte[12-15] 消息包封装项
		// --> byte[0-1] 消息包总数(word(16)) : 该消息分包后得总包数
		// --> byte[2-3] 包序号(word(16)): 从 1 开始, 如果消息体属性中相关标识位确定消息分包处理,则该项有内容,否则无该项

		private int totalPackages = -1;

		private int packageSeq = 1;

		private Jt808HeaderBuilder() {
		}

		public static Jt808HeaderBuilder builder() {
			return new Jt808HeaderBuilder();
		}

		public Jt808HeaderBuilder withMessageType(Jt808MessageType messageType) {
			this.messageType = messageType;
			return this;
		}

		public Jt808HeaderBuilder withMessageType(int messageId) {
			this.messageType = Jt808MessageType.valueOf(messageId);
			return this;
		}

		public Jt808HeaderBuilder withPayloadProps(PayloadProps payloadProps) {
			this.payloadProps = payloadProps;
			return this;
		}

		public Jt808HeaderBuilder withPayloadProps(Function<PayloadPropsBuilder, PayloadProps> builder) {
			this.payloadProps = builder.apply(new PayloadPropsBuilder());
			return this;
		}

		public Jt808HeaderBuilder withDeviceId(String deviceId) {
			this.deviceId = deviceId;
			return this;
		}

		public Jt808HeaderBuilder withMessageSeq(int messageSeq) {
			this.messageSeq = messageSeq;
			return this;
		}

		public Jt808HeaderBuilder withTotalPackages(int totalPackages) {
			if (totalPackages > 0) {
				this.totalPackages = totalPackages;
			}
			return this;
		}

		public Jt808HeaderBuilder withPackageSeq(int packageSeq) {
			if (packageSeq > 0) {
				this.packageSeq = packageSeq;
			}
			return this;
		}

		public Jt808Header build() {
			return new Jt808Header(messageType, payloadProps, deviceId, messageSeq, totalPackages, packageSeq);
		}
	}

	@Override
	public int getPacketId() {
		return packetId;
	}

	@Override
	public int getPayloadLength() {
		return payloadProps.getPayloadLength();
	}

}