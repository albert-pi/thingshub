package io.thingshub.service.model;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 物模型中的输入输出参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */
@Data
public class ThingInOutData implements Serializable {

	private static final long serialVersionUID = -1904714138551473008L;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 标识符
	 */
	private String identifier;

	/**
	 * 数据类型
	 */
	private ThingDataType dataType;

}