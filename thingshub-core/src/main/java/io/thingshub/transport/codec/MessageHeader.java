package io.thingshub.transport.codec;

/**
 * <p>
 * 消息头接口
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public interface MessageHeader {

	/**
	 * 获取消息类型
	 * 
	 * @return
	 */
	MessageType getMessageType();

	/**
	 * 获取消息序号
	 * 
	 * @return
	 */
	int getPacketId();

	/**
	 * 获取设备号
	 * 
	 * @return
	 */
	String getDeviceSn();

	/**
	 * 获取消息payload的字节序列长度
	 * 
	 * @return
	 */
	int getPayloadLength();

	/**
	 * 获取消息包数量（消息过大的情况下，分成多个包发送）
	 * 
	 * @return
	 */
	default int getTotalPackages() {
		return 1;
	};

	/**
	 * 获取消息包的序号
	 * 
	 * @return
	 */
	default int getPackageSeq() {
		return 1;
	};

	/**
	 * 转换成字节数组
	 * 
	 * @return
	 */
	byte[] toBytes();

}