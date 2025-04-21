package io.thingshub.transport.codec;

/**
 * <p>
 * 消息类型接口
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public interface MessageType {

	/**
	 * 获取消息类型ID
	 * 
	 * @return
	 */
	int getId();

	/**
	 * 获取消息类型名称
	 * 
	 * @return
	 */
	String getName();

	/**
	 * 获取消息类型的描述
	 * 
	 * @return
	 */
	String getDesc();

}