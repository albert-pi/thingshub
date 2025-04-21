package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 产品消息模型
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class MessageModel implements Serializable {

	private static final long serialVersionUID = 7720699406836904572L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 产品编号
	 */
	@QuerySqlField(index = true, name = "product_code", notNull = true)
	private String productCode;

	/**
	 * 消息显示名称
	 */
	@QuerySqlField(notNull = true)
	private String title;

	/**
	 * 消息名称（消息标识符，由字符、数字或下划线组成）
	 */
	@QuerySqlField(index = true, notNull = true)
	private String name;

	/**
	 * 参数的类别。PROPERTY-物模型属性参数；SERVICE-物模型服务输入输出参数；EVENT-事件输出参数；
	 */
	@QuerySqlField(notNull = true)
	private String paramType;

	/**
	 * 消息流向。UP-上行（设备发送）；DOWN-下行（设备接收）；
	 */
	@QuerySqlField(name = "stream_direction", notNull = true)
	private String streamDirection;

	/**
	 * 是否为回复确认消息（消息接收者对消息发送者的回复确认）。0-不是；1-是；
	 */
	@QuerySqlField(name = "ack_flag", notNull = true)
	private Integer ackFlag;

	/**
	 * 消息发布的原始topic
	 */
	@QuerySqlField(name = "raw_topic")
	private String rawTopic;

	/**
	 * 描述
	 */
	@QuerySqlField
	private String description;

	/**
	 * 删除状态，默认为0。0-未删除；1-已删除；
	 */
	@JSONField(serialize = false)
	@QuerySqlField(name = "deleted_status", notNull = true)
	private Integer deletedStatus;

	/**
	 * 创建时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(index = true, name = "create_time", notNull = true)
	private Date createTime;

	/**
	 * 创建者账号名称
	 */
	@QuerySqlField(notNull = true)
	private String createBy;

}