package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 设备通信协议转换脚本基本信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ProductScript implements Serializable {

	private static final long serialVersionUID = 6431036327985940723L;

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
	 * 协议版本
	 */
	@QuerySqlField(index = true, name = "protocol_version", notNull = true)
	private String protocolVersion;

	/**
	 * 脚本编号
	 */
	@QuerySqlField(index = true, name = "script_id", notNull = true)
	private String scriptId;

	/**
	 * 脚本名称
	 */
	@QuerySqlField(notNull = true)
	private String scriptName;

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