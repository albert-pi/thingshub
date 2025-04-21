package io.thingshub.domain;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 脚本信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ScriptInfo implements Serializable {

	private static final long serialVersionUID = 1495703771784177384L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 脚本编号
	 */
	@QuerySqlField(index = true, notNull = true)
	private String code;

	/**
	 * 脚本语言。javascript; python；
	 */
	@QuerySqlField(index = true, notNull = true)
	private String lang;

	/**
	 * 脚本内容
	 */
	@QuerySqlField(notNull = true)
	private String content;

	/**
	 * 删除状态。0-未删除；1-已删除；
	 */
	@JSONField(serialize = false)
	@QuerySqlField(name = "deleted_status", notNull = true)
	private Integer deletedStatus;

	/**
	 * 备注或说明
	 */
	@QuerySqlField
	private String remark;

}