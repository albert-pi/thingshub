package io.thingshub.service.model;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 产品节点类型
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public enum ProductNodeType {

	DIRECT(1, "直连设备"), GATEWAY(2, "网关设备"), GATEWAY_SUB(3, "网关子设备"), MONITOR(4, "监控设备");

	@Accessors(fluent = true)
	@Getter
	private final int type;

	@Accessors(fluent = true)
	@Getter
	private final String title;

	ProductNodeType(int type, String title) {
		this.type = type;
		this.title = title;
	}

	public static ProductNodeType of(int type) {
		switch (type) {
		case 1:
			return DIRECT;
		case 2:
			return GATEWAY;
		case 3:
			return GATEWAY_SUB;
		case 4:
			return MONITOR;
		default:
			return null;
		}
	}

}
