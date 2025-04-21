package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 产品物模型定义
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ThingModel implements Serializable {

	private static final long serialVersionUID = 1L;

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
	 * 名称
	 */
	@QuerySqlField(notNull = true)
	private String name;

	/**
	 * 标识符
	 */
	@QuerySqlField(index = true, notNull = true)
	private String identifier;

	/**
	 * 类别。PROPERTY-属性；SERVICE-服务；EVENT-事件；
	 */
	private String type;

	/**
	 * 属性读写类型：r-只读；rw-读写；
	 */
	@QuerySqlField(name = "access_mode")
	private String accessMode;

	/**
	 * 属性数据类型。ThingDataType对象的JSON字串
	 */
	@QuerySqlField(name = "data_type", notNull = true)
	private String dataType;

	/**
	 * 描述
	 */
	@QuerySqlField
	private String description;

	/**
	 * 服务输入参数。ThingInOutData对象数组的JSON字串
	 */
	@QuerySqlField
	private String input;

	/**
	 * 服务或事件输出参数。ThingInOutData对象数组的JSON字串
	 */
	@QuerySqlField
	private String output;

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
	@QuerySqlField(index = true, name = "create_time", notNull = true)
	private Date createTime;

	/**
	 * 创建者账号名称
	 */
	@QuerySqlField(name = "create_by", notNull = true)
	private String createBy;

}