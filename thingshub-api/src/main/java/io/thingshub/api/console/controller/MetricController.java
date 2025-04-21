package io.thingshub.api.console.controller;

import java.util.Collection;
import java.util.Map;

import io.thingshub.cluster.ClusterManager;
import io.thingshub.cluster.ClusterNode;
import io.thingshub.monitor.MetricManagerHolder;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestMapping;
import jakarta.inject.Inject;

/**
 * <p>
 * 系统监控管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class MetricController {

	@Inject
	private ClusterManager clusterManager;

	@RequestMapping(path = "/metric/cluster")
	public Collection<ClusterNode> queryProduct() {
		return clusterManager.getClusterNodes();
	}

	@RequestMapping(path = "/metric/counters")
	public Map<String, Object> getCounters() {
		return MetricManagerHolder.getMetricManager().getCounters();
	}

	@RequestMapping(path = "/metric/cpu")
	public Map<String, Object> getCpuMetric() {
		return MetricManagerHolder.getMetricManager().getCpuMetric();
	}

	@RequestMapping(path = "/metric/jvm")
	public Map<String, Object> getJvmMetric() {
		return MetricManagerHolder.getMetricManager().getJvmMetric();
	}

	@RequestMapping(path = "/metric/event")
	public Map<String, Object> getEventMetric() {
		return MetricManagerHolder.getMetricManager().getEventMetric();
	}

}