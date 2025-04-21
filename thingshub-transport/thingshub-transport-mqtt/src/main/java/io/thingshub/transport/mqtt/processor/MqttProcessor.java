package io.thingshub.transport.mqtt.processor;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import cn.hutool.core.collection.CollUtil;
import io.netty.channel.ChannelHandlerContext;
import io.thingshub.acl.AclManager;
import io.thingshub.cluster.ClusterManager;
import io.thingshub.domain.Message;
import io.thingshub.service.InboxService;
import io.thingshub.service.MessageService;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.topic.TopicTreeNodeMember;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.DistributeJob;
import io.thingshub.transport.MessageRetryTimer;
import io.thingshub.transport.Processor;
import io.thingshub.transport.TransportMessage;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.RetainManager;
import io.thingshub.transport.mqtt.message.PublishMessage;
import io.thingshub.transport.throttler.ResourceThrottler;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class MqttProcessor<Ctx extends ChannelHandlerContext, M extends TransportMessage> extends Processor<Ctx, M> {

	enum PubResult {
		NOT_AUTHORIZED, OK, NO_MATCH, ERROR
	}

	enum RetainResult {
		RETAINED, CLEARED, EXCEED_LIMIT, ERROR
	}

	enum SubResult {
		OK, EXISTS, NO_INBOX, EXCEED_LIMIT, NOT_AUTHORIZED, TOPIC_FILTER_INVALID, WILDCARD_NOT_SUPPORTED, SHARED_SUBSCRIPTION_NOT_SUPPORTED,
		SUBSCRIPTION_IDENTIFIER_NOT_SUPPORTED, BACK_PRESSURE_REJECTED, ERROR;
	};

	enum UnsubResult {
		OK, NO_SUB, NO_INBOX, NOT_AUTHORIZED, TOPIC_FILTER_INVALID, EXCEED_LIMIT, ERROR;
	}

	@AllArgsConstructor
	@Getter
	private static class PersistResult {

		private Long pubId;

		private Long retainId;

	}

	@Inject
	protected AclManager aclManager;

	@Inject
	protected ResourceThrottler resourceThrottler;

	@Inject
	protected ClusterManager clusterManager;

	@Inject
	protected MessageService messageService;

	@Inject
	protected RetainManager retainManager;

	@Inject
	protected InboxService inboxService;

	@Inject
	protected SubscriptionManager subscriptionManager;

	@Inject
	protected MessageRetryTimer messageRetryTimer;

	protected Message storeAsMessage(MqttChannelContext ctx, PublishMessage pubMsg) {
		Map<String, Object> props = new HashMap<>();
		props.put("qos", pubMsg.getQos());

		long timestamp = System.currentTimeMillis();
		String topic = pubMsg.getTopic();
		String payload = new String(pubMsg.getPayload(), StandardCharsets.UTF_8);
		String scriptLang = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_SCRIPT_LANG).get();
		if (scriptLang != null) {
			String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
			JSONObject payloadObj = JSON.parseObject(payload);
			timestamp = payloadObj.getLongValue("timestamp");

			topic = "devices/".concat(productCode).concat("/").concat(ctx.getClientId()).concat("/")
					.concat(payloadObj.getString("method").replaceAll("\\.", "/"));
		}

		Message message = new Message();
		message.setPublisherId(ctx.getClientId());
		message.setTopic(topic);
		message.setProps(JSON.toJSONString(props));
		message.setPayload(payload);
		message.setTimestamp(timestamp);
		messageService.save(message);
		Long messageId = message.getId();

		if (pubMsg.isRetain()) {
			if (pubMsg.getPayload() == null) {
				retainManager.cleanRetain(pubMsg.getTopic());
			} else {
				retainManager.renewRetain(pubMsg.getTopic(), messageId);
			}
		}

		return message;
	}

	protected void deliverMessage(Message message) {
		Set<TopicTreeNodeMember> nodeMembers = subscriptionManager.match(message.getTopic());
		if (CollUtil.isNotEmpty(nodeMembers)) {
			nodeMembers.parallelStream().forEach(member -> {
				// TODO 投递到离线的共享订阅者？？？
				inboxService.deliverMessage(message.getPublisherId(), member.getSubscriberId(), message.getId());
			});
		}

		clusterManager.executeJob(new DistributeJob(message.getId()), true);
	}

}
