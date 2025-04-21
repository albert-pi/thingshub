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
 * 消息收件箱
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class Inbox implements Serializable {

	private static final long serialVersionUID = 7103406945700321832L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 消息接收者的client ID
	 */
	@QuerySqlField(index = true, notNull = true)
	private String recipient;

	/**
	 * 消息发送者的client ID或SN
	 */
	@QuerySqlField(index = true, notNull = true)
	private String publisher;

	/**
	 * 消息发送记录ID
	 */
	@QuerySqlField(index = true, name = "message_seq", notNull = true)
	private Long messageSeq;

	/**
	 * 投递时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(index = true, name = "deliver_time", notNull = true)
	private Date deliverTime;

	/**
	 * 是否确认。0-未确认；1-已确认；
	 */
	@QuerySqlField(index = true, notNull = true)
	private boolean acked;

	/**
	 * 确认时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(name = "ack_time")
	private Date ackTime;

}
