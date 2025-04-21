package io.thingshub.api.console.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.thingshub.commons.PageParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 产品消息模型管理的请求参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
public class SysClientRequestParams {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class QuerySysClientParams extends PageParams {

	}

	@Data
	public static class SysClientFormParams {

		private Long id;

		/**
		 * 客户端名称
		 */
		@NotBlank(message = "客户端用户名称不能为空")
		protected String name;

		/**
		 * 客户端密码
		 */
		@NotBlank(message = "客户端密码不能为空")
		private String password;

		/**
		 * 业务系统名称
		 */
		@NotNull(message = "业务系统名称不能为空")
		private String bizName;

		/**
		 * 备注说明
		 */
		private String remark;

	}

}
