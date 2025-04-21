package io.thingshub.service.model;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 物模型中的数据类型
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ThingDataType implements Serializable {

	private static final long serialVersionUID = -8340400368305760888L;

	/**
	 * 名称
	 */
	private String type;

	/**
	 * 值规范
	 */
	private ThingDataTypeSpecs specs;

}