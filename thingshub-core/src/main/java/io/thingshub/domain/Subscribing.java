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
 * 客户端订阅信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class Subscribing implements Serializable {

	private static final long serialVersionUID = -4734745094213474253L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 订阅者client id
	 */
	@QuerySqlField(index = true, name = "client_id", notNull = true)
	private String clientId;

	/**
	 * 订阅分组
	 */
	@QuerySqlField(index = true, name = "sub_group")
	private String subGroup;

	/**
	 * 订阅TOPIC
	 */
	@QuerySqlField(index = true, notNull = true)
	private String topic;

	/**
	 * 原始订阅TOPIC
	 */
	@QuerySqlField(index = true, name = "raw_topic")
	private String rawTopic;

	/**
	 * 扩展属性。json格式的字符串
	 */
	@QuerySqlField
	private String props;

	/**
	 * 通哪个transport进行订阅
	 */
	@QuerySqlField(name = "on_transport", notNull = true)
	private String onTransport;

	/**
	 * 订阅时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(index = true, name = "sub_time", notNull = true)
	private Date subTime;

}
