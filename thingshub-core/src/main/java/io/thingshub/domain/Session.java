package io.thingshub.domain;

import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.DataRegion;
import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 会话信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@DataRegion(name = "transport_session", persistent = true, local = true)
public class Session {

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 客户端ID
	 */
	@QuerySqlField(index = true, name = "client_id", notNull = true)
	private String clientId;

	/**
	 * 保活时间。单位：秒
	 */
	@QuerySqlField(name = "keep_alive", notNull = true)
	private int keepAlive;

	/**
	 * 会话保存时间。单位：秒
	 */
	@QuerySqlField(name = "session_expiry_interval", notNull = true)
	private int sessionExpiryInterval;

	/**
	 * 最后激活时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(name = "last_active_time", notNull = true)
	private Date lastActiveTime;

	/**
	 * 状态。0-离线；1-在线；
	 */
	@QuerySqlField(notNull = true)
	private int status;

}
