package io.thingshub.domain;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import io.thingshub.service.base.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 消息记录
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class Message implements Serializable {

	private static final long serialVersionUID = 7433413039128565398L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 消息投递Topic
	 */
	@QuerySqlField(index = true, notNull = true)
	private String topic;

	/**
	 * 消息发送者ID
	 */
	@QuerySqlField(index = true, name = "publisher_id", notNull = true)
	private String publisherId;

	/**
	 * 消息的附加属性。JSON格式的字符串（消息头中定义的属性等）
	 */
	@QuerySqlField
	private String props;

	/**
	 * 消息载荷
	 */
	@QuerySqlField
	private String payload;

	/**
	 * 消息时间戳
	 */
	@QuerySqlField(index = true, notNull = true)
	private Long timestamp;

}
