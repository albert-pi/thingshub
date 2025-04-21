package io.thingshub.config;

import java.util.List;

import io.thingshub.ioc.Config;
import io.thingshub.ioc.Value;
import lombok.Getter;

/**
 * <p>
 * 集群配置信息
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Config
public class ClusterConfig {

	@Value("${thingshub.cluster.local-address}")
	@Getter
	private String localAddress;

	@Value("${thingshub.cluster.addresses}")
	@Getter
	private List<String> addresses;

	@Value("${thingshub.cluster.multicast-group}")
	@Getter
	private String multicastGroup;

	@Value("${thingshub.cluster.multicast-port}")
	@Getter
	private Integer multicastPort;

}
