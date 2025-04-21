package io.thingshub.domain;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import io.thingshub.service.base.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 原始数据包内容
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class RawPacket implements Serializable {

	private static final long serialVersionUID = 7433413039128565398L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 消息发送者ID
	 */
	@QuerySqlField(index = true, name = "client_id", notNull = true)
	private String clientId;

	/**
	 * 消息发送时间戳
	 */
	@QuerySqlField(index = true, notNull = true)
	private Long timestamp;

	/**
	 * 消息名称
	 */
	@QuerySqlField(notNull = true)
	private String msgName;

	/**
	 * transport名称
	 */
	@QuerySqlField(index = true, name = "transport_name", notNull = true)
	private String transportName;

	/**
	 * 消息原始内容
	 */
	@QuerySqlField(notNull = true)
	private String content;

}
