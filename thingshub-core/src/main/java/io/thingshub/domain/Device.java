package io.thingshub.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 设备信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class Device implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 设备编号
	 */
	@QuerySqlField(index = true, notNull = true)
	private String sn;

	/**
	 * 设备用户名
	 */
	@QuerySqlField(name = "user_name")
	private String userName;

	/**
	 * 设备秘钥
	 */
	@QuerySqlField
	private String secret;

	/**
	 * 分组编号
	 */
	@QuerySqlField(index = true, name = "group_id", notNull = true)
	private Long groupId;

	/**
	 * 分组名称
	 */
	@QuerySqlField(index = true, name = "group_name", notNull = true)
	private String groupName;

	/**
	 * 产品编码
	 */
	@QuerySqlField(index = true, name = "product_code", notNull = true)
	private String productCode;

	/**
	 * 产品名称
	 */
	@QuerySqlField(name = "product_name", notNull = true)
	private String productName;

	/**
	 * 协议版本
	 */
	@QuerySqlField(name = "protocol_version", notNull = true)
	private String protocolVersion;

	/**
	 * 设备所在地区行政编码
	 */
	@QuerySqlField(index = true, notNull = true)
	private String area;

	/**
	 * 设备当前的详细地址
	 */
	@QuerySqlField
	private String address;

	/**
	 * 设备当前的纬度
	 */
	@QuerySqlField
	private BigDecimal lat;

	/**
	 * 设备当前的经度
	 */
	@QuerySqlField
	private BigDecimal lng;

	/**
	 * 设备二维码
	 */
	@QuerySqlField(name = "qr_code")
	private String qrCode;

	/**
	 * 所属租户编号
	 */
	@QuerySqlField(index = true, name = "tenant_code", notNull = true)
	private String tenantCode;

	/**
	 * 所属租户名称
	 */
	@QuerySqlField(name = "tenant_name", notNull = true)
	private String tenantName;

	/**
	 * 所属商户编号
	 */
	@QuerySqlField(index = true, name = "merchant_code")
	private String merchantCode;

	/**
	 * 所属商户名称
	 */
	@QuerySqlField(name = "merchant_name")
	private String merchantName;

	/**
	 * 连接状态。0-离线；1-在线；
	 */
	@QuerySqlField(name = "connect_state", notNull = true)
	private Integer connectState;

	/**
	 * 故障状态。0-正常；1-故障；
	 */
	@QuerySqlField(name = "fault_state", notNull = true)
	private Integer faultState;

	/**
	 * 设备最后上报时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	@QuerySqlField(name = "report_time")
	private Date reportTime;

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