package io.thingshub.transport.tcp.processor;

import static java.util.Optional.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;

import com.alibaba.fastjson2.JSON;

import cn.hutool.core.collection.CollUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.cluster.ClusterManager;
import io.thingshub.domain.Message;
import io.thingshub.service.InboxService;
import io.thingshub.service.MessageService;
import io.thingshub.service.RawPacketService;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.topic.TopicTreeNodeMember;
import io.thingshub.transport.BaseChannelContext;
import io.thingshub.transport.DistributeJob;
import io.thingshub.transport.Processor;
import io.thingshub.transport.tcp.TcpChannelContext;
import io.thingshub.transport.tcp.message.GenericMessage;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "io.thingshub.transport.processor")
public class GenericProcessor extends Processor<TcpChannelContext, GenericMessage> {

	private ClusterManager clusterManager;

	protected MessageService messageService;

	protected InboxService inboxService;

	protected SubscriptionManager subscriptionManager;

	@Inject
	public GenericProcessor(//
			ClusterManager clusterManager, //
			MessageService messageService, //
			RawPacketService messageRawService, //
			InboxService inboxService, //
			SubscriptionManager subscriptionManager) {
		this.clusterManager = clusterManager;
		this.messageService = messageService;
		this.inboxService = inboxService;
		this.subscriptionManager = subscriptionManager;
	}

	@Override
	public void process(@NonNull TcpChannelContext ctx, @NonNull GenericMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("TCP client [{}] send message. method: {}", ctx.getClientId(), msg.getThingMessage().getMethod().split("\\.")[2]);
		}

		final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
		ctx.addFgTask(CompletableFuture.supplyAsync(() -> storeAsMessage(ctx, msg), ctx.executor()).thenAccept(this::deliverMessage)).exceptionally(e -> {
			ofNullable(parentMdc).ifPresent(pmdc -> MDC.setContextMap(pmdc));
			log.error("", e);

			return null;
		});
	}

	private Message storeAsMessage(TcpChannelContext ctx, GenericMessage msg) {
		Map<String, Object> props = new HashMap<>();
		props.put("qos", MqttQoS.EXACTLY_ONCE.value());

		String productCode = ctx.channel().attr(BaseChannelContext.ATTRIBUTE_PRODUCT_CODE).get();
		String topic = "devices/".concat(productCode).concat("/").concat(ctx.getClientId()).concat("/")
				.concat(msg.getThingMessage().getMethod().replaceAll("\\.", "/"));

		Message message = new Message();
		message.setPublisherId(ctx.getClientId());
		message.setTopic(topic);
		message.setProps(JSON.toJSONString(props));
		message.setPayload(JSON.toJSONString(msg.getThingMessage()));
		message.setTimestamp(msg.getThingMessage().getTimestamp());
		messageService.save(message);

		return message;
	}

	private void deliverMessage(Message message) {
		Set<TopicTreeNodeMember> nodeMembers = subscriptionManager.match(message.getTopic());
		if (CollUtil.isNotEmpty(nodeMembers)) {
			nodeMembers.parallelStream().forEach(member -> {
				inboxService.deliverMessage(message.getPublisherId(), member.getSubscriberId(), message.getId());
			});
		}

		clusterManager.executeJob(new DistributeJob(message.getId()), true);
	}

}
