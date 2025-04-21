package io.thingshub.service.model;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.Data;

/**
 * <p>
 * Transport Server协议转换脚本详细信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ServerScriptDetails implements Serializable {

	private static final long serialVersionUID = -6605172642976519620L;

	/**
	 * ID
	 */
	private Long id;

	/**
	 * Server名称
	 */
	private String serverName;

	/**
	 * 脚本语言
	 */
	private String scriptLang;

	/**
	 * 脚本内容
	 */
	private String scriptContent;

	/**
	 * 备注或说明
	 */
	private String remark;

	/**
	 * 状态。0-正常；1-禁用；
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 创建者账号名称
	 */
	private String createBy;

	/**
	 * 最后修改时间
	 */
	@JSONField(format = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	/**
	 * 修改者账号名称
	 */
	private String updateBy;

}