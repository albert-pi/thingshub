package io.thingshub.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

import com.alibaba.fastjson2.annotation.JSONField;

import io.thingshub.service.base.Key;
import lombok.Data;

/**
 * <p>
 * 产品信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class Product implements Serializable {

	private static final long serialVersionUID = 5565453824453010319L;

	/**
	 * ID
	 */
	@Key
	@QuerySqlField(index = true, notNull = true)
	private Long id;

	/**
	 * 产品编号（或产品型号）
	 */
	@QuerySqlField(index = true, notNull = true)
	private String code;

	/**
	 * 产品名称
	 */
	@QuerySqlField(notNull = true)
	private String name;

	/**
	 * 产品品类ID
	 */
	@QuerySqlField(index = true, notNull = true)
	private Long catId;

	/**
	 * 产品品类名称
	 */
	@QuerySqlField(notNull = true)
	private String catName;

	/**
	 * 节点类型。1-直连设备；2-网关设备；3-网关子设备；4-监控设备；
	 */
	@QuerySqlField(notNull = true)
	private Integer nodeType;

	/**
	 * 联网方式。1-通过以太网连接；2-通过蜂窝数据网连接(2G/3G/4G/5G)；3-通过WIFI连接；9-其他；
	 */
	@QuerySqlField(name = "net_mode", notNull = true)
	private Integer netMode;

	/**
	 * 通信协议。MQTT, TCP, UDP, HTTP, MQTT_WS
	 */
	@QuerySqlField
	private String transport;

	/**
	 * 产品图片地址列表
	 */
	@QuerySqlField
	private List<String> imgs;

	/**
	 * 当前的固件包ID
	 */
	@QuerySqlField(name = "firmware_id")
	private Long firmwareId;

	/**
	 * 当前的软件包ID
	 */
	@QuerySqlField(name = "software_id")
	private Long softwareId;

	/**
	 * 产品Key
	 */
	@QuerySqlField(name = "access_key")
	private String accessKey;

	/**
	 * 产品秘钥
	 */
	@QuerySqlField(name = "access_secret")
	private String accessSecret;

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
	@QuerySqlField(index = true, name = "create_time", notNull = true)
	private Date createTime;

	/**
	 * 创建者账号名称
	 */
	@QuerySqlField(name = "create_by", notNull = true)
	private String createBy;

}