package io.thingshub.transport.mqtt.processor;

import static io.thingshub.topic.TopicUtil.isSharedSubscription;
import static io.thingshub.transport.mqtt.handler.RetainHandling.SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS_VALUE;
import static io.thingshub.transport.mqtt.handler.RetainHandling.SEND_AT_SUBSCRIBE_VALUE;
import static io.thingshub.transport.throttler.ResourceType.TotalPersistentSubscribePerSecond;
import static io.thingshub.transport.throttler.ResourceType.TotalPersistentSubscriptions;
import static io.thingshub.transport.throttler.ResourceType.TotalRetainMatchBytesPerSecond;
import static io.thingshub.transport.throttler.ResourceType.TotalRetainMatchPerSeconds;
import static io.thingshub.transport.throttler.ResourceType.TotalSharedSubscriptions;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import com.alibaba.fastjson2.JSON;

import cn.hutool.core.collection.CollUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.thingshub.acl.AclAction;
import io.thingshub.domain.Message;
import io.thingshub.domain.Retain;
import io.thingshub.topic.Subscription;
import io.thingshub.topic.TopicUtil;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.SubscribeMessage;
import io.thingshub.utils.UTF8Util;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT SUBSCRIBE frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class SubscribeProcessor extends MqttProcessor<MqttChannelContext, SubscribeMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull SubscribeMessage msg) {
		log.info("MQTT client {} subscribe topics {}", ctx.getClientId(), msg.getSubscriptions());

		if (msg.getSubscriptions().isEmpty()) {
			log.error("protocol violation: SUBSCRIBE packet must contain at least one topic filter / QoS pair");
			ctx.goAway(false);

			return;
		}
		if (msg.getSubscriptions().size() > 10) {// TenantSettings maxTopicFiltersPerSub 0-100
			log.error("tenant limit: exceeded max topic filters limit per subscription");
			ctx.goAway(false);

			return;
		}

		if (ctx.isPacketIdUsed(msg.getPacketId())) {// MQTT5
			// TODO
		}

		final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
		ctx.addUsingPacketId(msg.getPacketId());
		List<CompletableFuture<SubResult>> subFutures = msg.getSubscriptions().stream().map(sub -> {
			return ctx.addFgTask(CompletableFuture.supplyAsync(() -> checkSubValidity(ctx, sub), ctx.executor())//
					.thenApply(result -> {
						if (result == SubResult.OK) {
							subscriptionManager.subscribe(sub);

							return SubResult.OK;
						} else {
							return result;
						}
					}).thenApplyAsync(result -> {
						if (result == SubResult.OK) {
							publishRetain(ctx, sub);
						}

						return result;
					}, ctx.executor()).exceptionally(e -> {
						ofNullable(parentMdc).ifPresent(pMdc -> MDC.setContextMap(pMdc));
						log.error("failed to subscribe topic filter {}. Error: ", sub.getTopicFilter(), e);

						return SubResult.ERROR;
					}));
		}).collect(Collectors.toList());

		CompletableFuture.allOf(subFutures.stream().toArray(CompletableFuture[]::new)).thenApplyAsync(v -> {
			List<SubResult> subResults = subFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());

			ofNullable(parentMdc).ifPresent(pMdc -> MDC.setContextMap(pMdc));
			List<MqttQoS> grantedQoSs = new ArrayList<>(subResults.size());
			for (int i = 0; i < subResults.size(); i++) {
				switch (subResults.get(i)) {
				case NOT_AUTHORIZED:
					log.error("Subscription not authorized");
					break;
				case ERROR:
					log.error("Subscription error");
					grantedQoSs.add(MqttQoS.FAILURE);
					break;
				case EXCEED_LIMIT:
					log.error("Subscription resource limited");
					grantedQoSs.add(MqttQoS.FAILURE);
					break;
				case OK:
				case EXISTS:
					Integer qos = (Integer) msg.getSubscriptions().get(i).getProps().get("qos");
					grantedQoSs.add(MqttQoS.valueOf(qos));
					break;
				default:
					grantedQoSs.add(MqttQoS.FAILURE);
					break;
				}
			}

			return MqttMessageBuilders.subAck().packetId(msg.getPacketId()).addGrantedQoses(grantedQoSs.toArray(new MqttQoS[0])).build();
		}, ctx.executor()).thenAccept(subAckMsg -> {
			ctx.writeAndFlush(subAckMsg).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						log.error("Server failed to sending SUBACK message. Message id: {}, granted QoS list: {}", msg.getPacketId(), subAckMsg.payload());
						if (future.cause() != null) {
							log.error("", future.cause());
						}
					}
				}

			});

			ctx.removeUsingPacketId(msg.getPacketId());
		});
	}

	private SubResult checkSubValidity(MqttChannelContext ctx, Subscription subscription) {
		if (!UTF8Util.isWellFormed(subscription.getTopicFilter(), false)) {
			return SubResult.TOPIC_FILTER_INVALID;
		}

		if (!TopicUtil.isValidTopicFilter(subscription.getTopicFilter(), 40, 16, 255)) {
			return SubResult.TOPIC_FILTER_INVALID;
		}

		if (!aclManager.check(subscription.getSubscriberId(), subscription.getTopicFilter(), AclAction.SUBSCRIBE)) {
			return SubResult.NOT_AUTHORIZED;
		}

		if (isSharedSubscription(subscription.getTopicFilter()) && !resourceThrottler.hasResource(ctx.getTenant(), TotalSharedSubscriptions)) {
			return SubResult.EXCEED_LIMIT;
		}
		if (!resourceThrottler.hasResource(ctx.getTenant(), TotalPersistentSubscriptions)) {
			return SubResult.EXCEED_LIMIT;
		}
		if (!resourceThrottler.hasResource(ctx.getTenant(), TotalPersistentSubscribePerSecond)) {
			return SubResult.EXCEED_LIMIT;
		}

		Integer retainHandling = (Integer) subscription.getProps().get("retainHandling");
		if (!isSharedSubscription(subscription.getTopicFilter())
				&& (retainHandling == SEND_AT_SUBSCRIBE_VALUE || retainHandling == SEND_AT_SUBSCRIBE_IF_NOT_YET_EXISTS_VALUE)) {
			if (!resourceThrottler.hasResource(ctx.getTenant(), TotalRetainMatchPerSeconds)) {
				return SubResult.EXCEED_LIMIT;
			}
			if (!resourceThrottler.hasResource(ctx.getTenant(), TotalRetainMatchBytesPerSecond)) {
				return SubResult.EXCEED_LIMIT;
			}
		}

		return SubResult.OK;
	}

	private void publishRetain(MqttChannelContext ctx, Subscription subscription) {
		final List<Retain> retains4Subcriber = new ArrayList<>();
		if (subscription.getTopicFilter().indexOf(TopicUtil.SINGLE_WILDCARD) >= 0) {
			String prefixOfTopicFilter = subscription.getTopicFilter().substring(0, subscription.getTopicFilter().indexOf(TopicUtil.SINGLE_WILDCARD));
			retains4Subcriber.addAll(retainManager.getRetainsByPrefix(prefixOfTopicFilter));
		} else if (subscription.getTopicFilter().endsWith(TopicUtil.MULTI_WILDCARD)) {
			String prefixOfTopicFilter = subscription.getTopicFilter().substring(0, subscription.getTopicFilter().indexOf(TopicUtil.MULTI_WILDCARD));
			retains4Subcriber.addAll(retainManager.getRetainsByPrefix(prefixOfTopicFilter));
		} else {
			retains4Subcriber.add(retainManager.getRetain(subscription.getTopicFilter()));
		}

		retains4Subcriber.parallelStream().filter(retain -> CollUtil.isNotEmpty(subscriptionManager.match(retain.getTopic()))).peek(retain -> {
			Message theMsg = messageService.getById(retain.getMessageId());
			if (theMsg != null) {
				if (inboxService.deliverMessage(theMsg.getPublisherId(), ctx.getClientId(), theMsg.getId())) {
					int packetId = ctx.nextPacketId();
					byte[] payloadInBytes = theMsg.getPayload().getBytes();

					Integer qosOfMsg = JSON.parseObject(theMsg.getProps()).getInteger("qos");
					Integer qosOfSub = (Integer) subscription.getProps().get("qos");
					MqttQoS theMqttQos = MqttQoS.valueOf(Math.min(qosOfMsg, qosOfSub));

					switch (theMqttQos) {
					case AT_MOST_ONCE:
						MqttPublishMessage pubMsg = MqttMessageBuilders.publish().messageId(0).qos(theMqttQos).retained(false).topicName(retain.getTopic())
								.payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
						ctx.writeAndFlush(pubMsg).addListener(new ChannelFutureListener() {

							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								if (future.isSuccess()) {
									inboxService.ackDelivery(ctx.getClientId(), theMsg.getId());
								} else {
									log.error("Server failed to sending retain message after subscribing. Message id: {}, QoS: {}, payload: {}", packetId,
											theMqttQos.value(), theMsg.getPayload());
									if (future.cause() != null) {
										log.error("", future.cause());
									}
								}
							}
						});

						break;
					case EXACTLY_ONCE:
					case AT_LEAST_ONCE:
					default:
						ctx.addOutboundMessage(packetId, retain.getMessageId());

						MqttPublishMessage retryingPubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false)
								.topicName(retain.getTopic()).payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
						messageRetryTimer.doRetry(ctx, packetId, retryingPubMsg, 5, 2);

						MqttPublishMessage immediatePubMsg = MqttMessageBuilders.publish().messageId(packetId).qos(theMqttQos).retained(false)
								.topicName(retain.getTopic()).payload(Unpooled.wrappedBuffer(payloadInBytes)).build();
						ctx.writeAndFlush(immediatePubMsg).addListener(new ChannelFutureListener() {

							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								if (!future.isSuccess()) {
									log.error("Server failed to sending retain message after subscribing. Message id: {}, QoS: {}, payload: {}", packetId,
											theMqttQos.value(), theMsg.getPayload());
									if (future.cause() != null) {
										log.error("", future.cause());
									}
								}
							}

						});

						break;
					}
				}
			}
		});
	}

}
