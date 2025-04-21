package io.thingshub.api.console.params;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.thingshub.service.model.ThingDataType;
import io.thingshub.service.model.ThingInOutData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 物模型管理的请求参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
public class ThingModelRequestParams {

	@Data
	public static abstract class ThingModelFormParams {

		protected Long id;

		/**
		 * 产品编号
		 */
		@NotBlank(message = "产品编号不能为空")
		protected String productCode;

		/**
		 * 名称
		 */
		@NotBlank(message = "名称不能为空")
		protected String name;

		/**
		 * 标识符
		 */
		@NotBlank(message = "标识符不能为空")
		protected String identifier;

		/**
		 * 描述
		 */
		protected String description;

	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class ThingPropertyFormParams extends ThingModelFormParams {

		/**
		 * 数据类型
		 */
		@NotNull(message = "数据类型不能为空")
		private ThingDataType dataType;

		/**
		 * 读写模式。r-只读；rw-读写；
		 */
		@NotBlank(message = "读写模式不能为空")
		private String accessMode;

	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class ThingServiceFormParams extends ThingModelFormParams {

		/**
		 * 输入参数
		 */
		private List<ThingInOutData> input;

		/**
		 * 输出参数
		 */
		private List<ThingInOutData> output;

	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class ThingEventFormParams extends ThingModelFormParams {

		/**
		 * 输出参数
		 */
		private List<ThingInOutData> output;

	}

}
