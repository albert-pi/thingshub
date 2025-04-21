package io.thingshub.config;

import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

/**
 * <p>
 * Log配置
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Config
public class LogConfig {

	@Value("${thingshub.log.level: INFO}")
	@Getter
	private String level;

}
