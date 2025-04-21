package io.thingshub.cluster;

import lombok.Data;

@Data
public class ClusterNode {

	private String clusterId;

	private String nodeIp;

	private String endpoint;

}
