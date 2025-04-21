package io.thingshub.api.console.params;

import javax.validation.constraints.NotBlank;

import io.thingshub.commons.PageParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 协议转换脚本管理的请求参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
public class ScriptRequestParams {

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class QueryServerScriptParams extends PageParams {

	}

	@Data
	public static class ServerScriptFormParams {

		/**
		 * ID
		 */
		private Long id;

		/**
		 * Server名称
		 */
		@NotBlank(message = "Server名称不能为空")
		private String serverName;

		/**
		 * 脚本语言
		 */
		@NotBlank(message = "脚本语言不能为空")
		private String scriptLang;

		/**
		 * 脚本内容
		 */
		@NotBlank(message = "脚本内容不能为空")
		private String scriptContent;

		/**
		 * 备注或说明
		 */
		private String remark;

	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	public static class QueryProductScriptParams extends PageParams {

		/**
		 * 产品编号
		 */
		@NotBlank(message = "产品编号不能为空")
		private String productCode;

	}

	@Data
	public static class ProductScriptFormParams {

		/**
		 * ID
		 */
		private Long id;

		/**
		 * 产品编号
		 */
		@NotBlank(message = "产品编号不能为空")
		private String productCode;

		/**
		 * 协议版本
		 */
		@NotBlank(message = "协议版本不能为空")
		private String protocolVersion;

		/**
		 * 脚本编号
		 */
		@NotBlank(message = "脚本编号不能为空")
		private String scriptId;

		/**
		 * 脚本名称
		 */
		@NotBlank(message = "脚本名称不能为空")
		private String scriptName;

		/**
		 * 脚本语言
		 */
		@NotBlank(message = "脚本语言不能为空")
		private String scriptLang;

		/**
		 * 脚本内容
		 */
		@NotBlank(message = "脚本内容不能为空")
		private String scriptContent;

		/**
		 * 备注或说明
		 */
		private String remark;

	}

}
