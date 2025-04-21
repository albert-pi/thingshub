package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * Transport Server的协议转换脚本，用于transport server的客户端在发送第一个数据包（通常是connection、register或auth）时进行调用，以获得客户端的ID等基本信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ServerScript implements Serializable {

	private static final long serialVersionUID = -231374398413786774L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * Server名称
	 */
	@QuerySqlField(index = true, name = "server_name", notNull = true)
	private String serverName;

	/**
	 * 备注或说明
	 */
	@QuerySqlField
	private String remark;

	/**
	 * 状态。0-正常；1-禁用；
	 */
	@QuerySqlField(notNull = true)
	private Integer status;

	/**
	 * 删除状态。0-未删除；1-已删除；
	 */
	@JSONField(serialize = false)
	@QuerySqlField(name = "deleted_status", notNull = true)
	private Integer deletedStatus;

	/**
	 * 创建时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(name = "create_time", notNull = true)
	private Date createTime;

	/**
	 * 创建者账号名称
	 */
	@QuerySqlField(name = "create_by", notNull = true)
	private String createBy;

	/**
	 * 最后修改时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(index = true, name = "update_time", notNull = true)
	private Date updateTime;

	/**
	 * 修改者账号名称
	 */
	@QuerySqlField(name = "update_by", notNull = true)
	private String updateBy;

}