package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * retain消息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class Retain implements Serializable {

	private static final long serialVersionUID = -8952257700697171258L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 主题
	 */
	@QuerySqlField(index = true, notNull = true)
	private String topic;

	/**
	 * 消息记录ID
	 */
	@QuerySqlField(index = true, name = "message_id", notNull = true)
	private Long messageId;

	/**
	 * 是否为活动状态。每个topic只有一个retain是有效的
	 */
	@QuerySqlField(index = true, notNull = true)
	private boolean active;

	/**
	 * retain消息创建时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(index = true, name = "create_time", notNull = true)
	private Date createTime;

}
