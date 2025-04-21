package io.thingshub.service.model;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 参数数据类型的值规范
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ThingDataTypeSpecs implements Serializable {

	private static final long serialVersionUID = 2189702262923781468L;

	/**
	 * 最小值。int、float、double类型特有
	 */
	private String min;

	/**
	 * 最大值。int、float、double类型特有
	 */
	private String max;

	/**
	 * 单位。int、float、double类型特有
	 */
	private String unit;

	/**
	 * 单位名称。int、float、double类型特有
	 */
	private String unitName;

	/**
	 * 数组元素的个数。array类型特有
	 */
	private Integer size;

	/**
	 * 数据长度。text类型特有
	 */
	private Integer length;

	/**
	 * 0的值。bool类型特有
	 */
	private String _0;

	/**
	 * 1的值。bool类型特有
	 */
	private String _1;

}