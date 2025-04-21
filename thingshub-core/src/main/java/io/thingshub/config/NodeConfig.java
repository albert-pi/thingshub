package io.thingshub.config;

import java.util.List;

import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

/**
 * <p>
 * 节点配置
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Config
public class NodeConfig {

	@Value("${thingshub.node-id}")
	@Getter
	private String nodeId;

	@Value("${thingshub.attributes}")
	@Getter
	private List<String> attributes;

	@Value("${thingshub.data-dir}")
	@Getter
	private String dataDir;

}
