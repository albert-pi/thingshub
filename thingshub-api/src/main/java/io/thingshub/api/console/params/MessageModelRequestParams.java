package io.thingshub.api.console.params;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * <p>
 * 产品消息模型管理的请求参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
public class MessageModelRequestParams {

	@Data
	public static class MessageModelFormParams {

		private Long id;

		/**
		 * 产品编号
		 */
		@NotBlank(message = "产品编号不能为空")
		protected String productCode;

		/**
		 * 标题，消息显示名称
		 */
		@NotBlank(message = "标题不能为空")
		private String title;

		/**
		 * 消息名称（消息标识符，由字符、数字或下划线组成）
		 */
		@NotNull(message = "名称不能为空")
		private String name;

		/**
		 * 参数类别。PROPERTY-属性参数；SERVICE-服务调用参数；EVENT-事件参数；
		 */
		@NotBlank(message = "参数类别不能为空")
		private String paramType;

		/**
		 * 消息流向。UP-上行（设备发送）；DOWN-下行（设备接收）；
		 */
		@NotBlank(message = "消息流向不能为空")
		private String streamDirection;

		/**
		 * 是否为回复确认消息（消息接收者对消息发送者的回复确认）。0-不是；1-是；
		 */
		@NotNull(message = "回复确认标记不能为空")
		private Integer ackFlag;

		/**
		 * 消息发布的原始topic
		 */
		private String rawTopic;

		/**
		 * 描述
		 */
		private String description;

	}

}
