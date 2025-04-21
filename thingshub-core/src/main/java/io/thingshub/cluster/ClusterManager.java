package io.thingshub.cluster;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;

import io.thingshub.Broker;
import io.thingshub.cluster.compute.CallableJob;
import io.thingshub.cluster.compute.ClosureJob;
import io.thingshub.cluster.compute.JobExecutor;
import io.thingshub.cluster.compute.RunnableJob;
import jakarta.inject.Inject;

/**
 * <p>
 * 集群管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ClusterManager {

	private Ignite ignite;

	private JobExecutor jobExecutor;

	@Inject
	public ClusterManager(Ignite ignite) {
		this.ignite = ignite;
		this.jobExecutor = new JobExecutor(ignite);
	}

	public Collection<ClusterNode> getClusterNodes() {
		return jobExecutor.execute(new CallableJob<ClusterNode>() {

			private static final long serialVersionUID = 872929868018943945L;

			@Override
			public ClusterNode call() throws Exception {
				org.apache.ignite.cluster.ClusterNode igniteClusterNode = ignite.cluster().localNode();
				ClusterNode clusterNode = new ClusterNode();
				clusterNode.setClusterId(igniteClusterNode.consistentId().toString());
				clusterNode.setNodeIp(igniteClusterNode.addresses().stream().findAny().orElse(null));
				clusterNode.setEndpoint(Broker.currentNode);

				return clusterNode;
			}

			@Override
			public String getName() {
				return "cluster_nodes_task";
			}

			@Override
			public Boolean isBroadcasted() {
				return true;
			}

		}, true);
	}

	public Set<String> getOtherClusterNodes() {
		return ignite.cluster().nodes().stream().filter(clusterNode -> clusterNode != ignite.cluster().localNode())
				.map(clusterNode -> clusterNode.consistentId().toString()).collect(Collectors.toSet());
	}

	public String getLocalNode() {
		return ignite.cluster().localNode().addresses().stream().findFirst().orElse(Broker.currentNode);
	}

	public void executeJob(RunnableJob job, boolean includeLocal) {
		jobExecutor.execute(job, includeLocal);
	}

	public void executeJobAsync(RunnableJob job, boolean includeLocal) {
		jobExecutor.executeAsync(job, includeLocal);
	}

	public <INPUT, OUTPUT> Collection<OUTPUT> executeJob(ClosureJob<INPUT, OUTPUT> job, INPUT input, boolean includeLocal) {
		return jobExecutor.execute(job, input, includeLocal);
	}

}
