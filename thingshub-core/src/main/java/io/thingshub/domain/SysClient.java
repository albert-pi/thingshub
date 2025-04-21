package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 系统客户端
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class SysClient implements Serializable {

	private static final long serialVersionUID = -8225088207420924112L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 用户名
	 */
	@QuerySqlField(index = true, notNull = true)
	private String name;

	/**
	 * 密码
	 */
	@QuerySqlField(notNull = true)
	private String password;

	/**
	 * 业务系统名称
	 */
	@QuerySqlField(name = "biz_name")
	private String bizName;

	/**
	 * 备注
	 */
	@QuerySqlField
	private String remark;

	/**
	 * 状态。0-正常；1-禁用；
	 */
	@QuerySqlField(notNull = true)
	private Integer status;

	/**
	 * 删除状态。0-未删除；1-已删除
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
	@QuerySqlField(name = "create_by", notNull = true)
	private String createBy;

}