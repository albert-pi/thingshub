package io.thingshub.service.model;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 产品联网模式
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public enum ProductNetMode {

	ETHERNET(1, "通过以太网连接"), CELL(2, "通过蜂窝数据网连接"), WIFI(3, "通过WIFI连接"), OTHER(9, "其他");

	@Accessors(fluent = true)
	@Getter
	private final int type;

	@Accessors(fluent = true)
	@Getter
	private final String title;

	ProductNetMode(int type, String title) {
		this.type = type;
		this.title = title;
	}

	public static ProductNetMode of(int type) {
		switch (type) {
		case 1:
			return ETHERNET;
		case 2:
			return CELL;
		case 3:
			return WIFI;
		case 9:
			return OTHER;
		default:
			return null;
		}
	}

}
