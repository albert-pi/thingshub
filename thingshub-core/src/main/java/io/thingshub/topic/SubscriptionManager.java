package io.thingshub.topic;

import static io.thingshub.topic.TopicUtil.THING_EVENT_POST_REPLY_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_PROPERTY_POST_REPLY_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_PROPERTY_SET_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_SERVICE_CALL_TOPIC_FORMAT;
import static io.thingshub.topic.TopicUtil.THING_SERVICE_REQUEST_REPLY_TOPIC_FORMAT;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.configuration.CacheConfiguration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.thingshub.domain.Device;
import io.thingshub.domain.MessageModel;
import io.thingshub.domain.Subscribing;
import io.thingshub.service.DeviceService;
import io.thingshub.service.MessageModelService;
import io.thingshub.service.SubscribingService;
import io.thingshub.service.base.DataRegion;
import io.thingshub.service.model.ThingModelType;
import jakarta.inject.Inject;

/**
 * <p>
 * 订阅管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class SubscriptionManager {

	@DataRegion(name = "group_message_region", persistent = false, local = true, initSize = 16L * 1024 * 1024, maxSize = 128L * 1024 * 1024)
	public class GroupMessageRegion {
	}

	private final IgniteCache<String, Long> groupMessageCache;

	private final Map<String, Map<String, TopicTreeNodeMember>> groupedNodeMembers;

	private final TopicTree<TopicTreeNodeMember> topicTree;

	private final SubscribingService subscribingService;

	private final DeviceService deviceService;

	private final MessageModelService messageModelService;

	@Inject
	public SubscriptionManager(Ignite ignite, SubscribingService subscribingService, DeviceService deviceService, MessageModelService messageModelService) {
		this.subscribingService = subscribingService;
		this.deviceService = deviceService;
		this.messageModelService = messageModelService;

		this.topicTree = new TopicTree<>();
		this.groupedNodeMembers = new ConcurrentHashMap<>();

		CacheConfiguration<String, Long> cfg = new CacheConfiguration<String, Long>() //
				.setName("group_message_cache") //
				.setCacheMode(CacheMode.REPLICATED) //
				.setDataRegionName("group_message_region") //
				.setIndexedTypes(new Class[] { String.class, Long.class }) // TODO
				.setRebalanceMode(CacheRebalanceMode.ASYNC);
		// TODO 设置过期策略
		this.groupMessageCache = ignite.getOrCreateCache(cfg);

		// TODO 全量加载订阅信息（异步？）
		// clientSubService.list()//分页载入
	}

	public void loadClientSubscriptions(String clientId) {
		List<Subscribing> clientSubscribings = subscribingService.getSubscribings(clientId);
		if (CollUtil.isNotEmpty(clientSubscribings)) {
			clientSubscribings.forEach(sub -> {
				JSONObject props = JSON.parseObject(sub.getProps());
				TopicTreeNodeMember nodeMember = new TopicTreeNodeMember(sub.getClientId(), props, sub.getSubGroup(), sub.getOnTransport());
				topicTree.add(sub.getTopic(), nodeMember);

				if (sub.getSubGroup() != null) {
					groupedNodeMembers.computeIfAbsent(sub.getSubGroup(), grp -> Maps.newConcurrentMap()).put(sub.getClientId(), nodeMember);
				}
			});
		}
	}

	public void unloadClientSubscriptions(String clientId) {
		List<Subscribing> clientSubscribings = subscribingService.getSubscribings(clientId);
		if (CollUtil.isNotEmpty(clientSubscribings)) {
			clientSubscribings.forEach(sub -> {
				JSONObject props = JSON.parseObject(sub.getProps());
				topicTree.remove(sub.getTopic(), new TopicTreeNodeMember(sub.getClientId(), props, sub.getSubGroup(), sub.getOnTransport()));

				if (sub.getSubGroup() != null) {
					groupedNodeMembers.computeIfPresent(sub.getSubGroup(), (grp, membersInGroup) -> {
						membersInGroup.remove(sub.getClientId());
						return membersInGroup;
					});
				}
			});
		}
	}

	public Set<TopicTreeNodeMember> match(String topic) {
		return topicTree.match(topic);
	}

	public void subscribe(Subscription sub) {
		List<String> topicFilters = Lists.newArrayList(sub.getTopicFilter());

		Device device = deviceService.getBySn(sub.getSubscriberId());
		if (device != null) {
			String productCode = device.getProductCode();
			String subscriberId = sub.getSubscriberId();

			List<MessageModel> downMessageModels = messageModelService.getDownMessageModels(device.getProductCode());
			if (CollUtil.isNotEmpty(downMessageModels)) {
				downMessageModels.forEach(model -> {
					if (StrUtil.isNotBlank(model.getRawTopic()) && sub.getTopicFilter().equals(model.getRawTopic())) {
						String topicFilter = null;
						switch (ThingModelType.valueOf(model.getParamType())) {
						case PROPERTY:
							if (model.getAckFlag() == 1) {
								topicFilter = String.format(THING_PROPERTY_POST_REPLY_TOPIC_FORMAT, productCode, subscriberId, model.getName());
							} else {
								topicFilter = String.format(THING_PROPERTY_SET_TOPIC_FORMAT, productCode, subscriberId, model.getName());
							}

							break;
						case SERVICE:
							if (model.getAckFlag() == 1) {
								topicFilter = String.format(THING_SERVICE_REQUEST_REPLY_TOPIC_FORMAT, productCode, subscriberId, model.getName());
							} else {
								topicFilter = String.format(THING_SERVICE_CALL_TOPIC_FORMAT, productCode, subscriberId, model.getName());
							}

							break;
						case EVENT:
							if (model.getAckFlag() == 1) {
								topicFilter = String.format(THING_EVENT_POST_REPLY_TOPIC_FORMAT, productCode, subscriberId, model.getName());
							}

							break;
						}

						if (StrUtil.isNotBlank(topicFilter)) {
							topicFilters.add(topicFilter);
						}
					}
				});
			}
		}

		topicFilters.forEach(tf -> {
			Subscribing subscribing = new Subscribing();
			subscribing.setClientId(sub.getSubscriberId());
			subscribing.setSubGroup(sub.getGroup());
			subscribing.setTopic(tf);
			subscribing.setProps(JSON.toJSONString(sub.getProps()));
			subscribing.setOnTransport(sub.getOnTransport());
			subscribingService.saveSubscribing(subscribing);
		});

		TopicTreeNodeMember nodeMember = new TopicTreeNodeMember(sub.getSubscriberId(), sub.getProps(), sub.getGroup(), sub.getOnTransport());
		topicTree.add(sub.getTopicFilter(), nodeMember);

		if (sub.getGroup() != null) {
			groupedNodeMembers.computeIfAbsent(sub.getGroup(), grp -> Maps.newConcurrentMap()).put(sub.getSubscriberId(), nodeMember);
		}
	}

	public void unsubscribe(Unsubscription unsub) {
		Subscribing subscribing = subscribingService.getSubscribing(unsub.getClientId(), unsub.getGroup(), unsub.getTopicFilter());

		JSONObject props = JSON.parseObject(subscribing.getProps());
		TopicTreeNodeMember nodeMember = new TopicTreeNodeMember(subscribing.getClientId(), props, subscribing.getSubGroup(), unsub.getOnTransport());
		topicTree.remove(subscribing.getTopic(), nodeMember);
		subscribingService.removeById(subscribing.getId());

		if (subscribing.getSubGroup() != null) {
			groupedNodeMembers.computeIfPresent(subscribing.getSubGroup(), (grp, membersInGroup) -> {
				membersInGroup.remove(unsub.getClientId());
				return membersInGroup;
			});
		}
	}

	public void cleanClientSubscriptions(String clientId) {
		subscribingService.removeSubscribings(clientId);
	}

	public List<TopicTreeNodeMember> getMembersInGroup(String group) {
		Map<String, TopicTreeNodeMember> membersInGroup = groupedNodeMembers.computeIfPresent(group, (grp, members) -> members);

		return Lists.newArrayList(membersInGroup.values().toArray(new TopicTreeNodeMember[0]));
	}

	public void putGroupMessageId(String key, Long messageId) {
		groupMessageCache.putIfAbsent(key, messageId);
	}

	public Long pollGroupMessageId(String key) {
		return groupMessageCache.getAndRemove(key);
	}

}
