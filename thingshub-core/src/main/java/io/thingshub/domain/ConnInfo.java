package io.thingshub.domain;

import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.DataRegion;
import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 连接信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Data
@DataRegion(name = "transport_connection", persistent = false, local = true)
public class ConnInfo {

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 租户ID
	 */
	@QuerySqlField(index = true, name = "tenant_id", notNull = true)
	private String tenantId;

	/**
	 * 客户端ID
	 */
	@QuerySqlField(index = true, name = "client_id", notNull = true)
	private String clientId;

	/**
	 * 通道ID
	 */
	@QuerySqlField(name = "channel_id", notNull = true)
	private String channelId;

	/**
	 * 客户端地址
	 */
	@QuerySqlField(name = "client_addr", notNull = true)
	private String clientAddr;

	/**
	 * 服务器地址
	 */
	@QuerySqlField(index = true, name = "server_addr", notNull = true)
	private String serverAddr;

	/**
	 * 连接时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(name = "connect_time", notNull = true)
	private Date connectTime;

	/**
	 * 传输协议
	 */
	@QuerySqlField
	private String transport;

}
