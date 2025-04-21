package io.thingshub.transport.mqtt.processor;

import static io.thingshub.transport.throttler.ResourceType.TotalPersistentUnsubscribePerSecond;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.MDC;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.thingshub.acl.AclAction;
import io.thingshub.topic.TopicUtil;
import io.thingshub.topic.Unsubscription;
import io.thingshub.transport.mqtt.MqttChannelContext;
import io.thingshub.transport.mqtt.message.UnsubscribeMessage;
import io.thingshub.utils.UTF8Util;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * MQTT UNSUBSCRIBE frame processor
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class UnsubscribeProcessor extends MqttProcessor<MqttChannelContext, UnsubscribeMessage> {

	@Override
	public void process(@NonNull MqttChannelContext ctx, @NonNull UnsubscribeMessage msg) {
		log.info("MQTT client {} unsubscribe topics: {}", ctx.getClientId(), msg.getUnsubscriptions());

		if (msg.getUnsubscriptions().isEmpty()) {
			log.error("Protocol violation: UNSUBSCRIBE packet must contain at least one Topic Filter");
			ctx.goAway(false);

			return;
		}
		if (msg.getUnsubscriptions().size() > 10) {// TenantSettings maxTopicFiltersPerSub 0-100
			log.error("Exceeded max topic filters limit per subscription");
			ctx.goAway(false);

			return;
		}

		for (Unsubscription unsubscription : msg.getUnsubscriptions()) {
			if (!UTF8Util.isWellFormed(unsubscription.getTopicFilter(), false)) {
				log.error("protocol error: include unacceptable chars in topic filter [{}]", unsubscription.getTopicFilter());
				ctx.goAway(false);

				return;
			}
		}

		if (ctx.isPacketIdUsed(msg.getPacketId())) {// MQTT5
			// TODO
		}

		final Map<String, String> parentMdc = MDC.getCopyOfContextMap();
		ctx.addUsingPacketId(msg.getPacketId());
		List<CompletableFuture<UnsubResult>> unsubFutures = msg.getUnsubscriptions().stream().map(unsub -> {
			return ctx.addFgTask(CompletableFuture.supplyAsync(() -> checkUnsubValidity(ctx, unsub), ctx.executor())//
					.thenApply(result -> {
						if (result == UnsubResult.OK) {
							subscriptionManager.unsubscribe(unsub);
						}

						return result;
					}).exceptionally(e -> {
						ofNullable(parentMdc).ifPresent(pMdc -> MDC.setContextMap(pMdc));
						log.error("MQTT client {} failed to unsubscribe topic [{}]. Error: {}", ctx.getClientId(), unsub.getTopicFilter(), e);

						return UnsubResult.ERROR;
					}));
		}).collect(Collectors.toList());

		CompletableFuture.allOf(unsubFutures.stream().toArray(CompletableFuture[]::new)).thenApplyAsync(v -> {
			List<UnsubResult> unsubResults = unsubFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());

			ofNullable(parentMdc).ifPresent(pMdc -> MDC.setContextMap(pMdc));
			for (int i = 0; i < unsubResults.size(); i++) {
				switch (unsubResults.get(i)) {
				case NOT_AUTHORIZED:
					log.error("Unsubscription not authorized");
					break;
				case EXCEED_LIMIT:
					log.error("Unsubscription resource limited");
					break;
				case ERROR:
					log.error("Unsubscription error");
					break;
				case OK:
				default:
					break;
				}
			}

			return MqttMessageBuilders.unsubAck().packetId(msg.getPacketId()).build();
		}, ctx.executor()).thenAccept(unsubAck -> {
			ctx.writeAndFlush(unsubAck).addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isSuccess()) {
						log.error("Server failed to sending UNSUBACK message. Message id: {}", msg.getPacketId());
						if (future.cause() != null) {
							log.error("", future.cause());
						}
					}
				}

			});

			ctx.removeUsingPacketId(msg.getPacketId());
		});
	}

	private UnsubResult checkUnsubValidity(MqttChannelContext ctx, Unsubscription unsubscription) {
		if (!TopicUtil.isValidTopicFilter(unsubscription.getTopicFilter(), 40, 16, 255)) {// TenantSettings maxTopicLevelLength, maxTopicLevels, maxTopicLength
			return UnsubResult.TOPIC_FILTER_INVALID;
		}

		if (!aclManager.check(unsubscription.getClientId(), unsubscription.getTopicFilter(), AclAction.UNSUBSCRIBE)) {
			return UnsubResult.NOT_AUTHORIZED;
		}

		if (!resourceThrottler.hasResource(ctx.getTenant(), TotalPersistentUnsubscribePerSecond)) {
			return UnsubResult.EXCEED_LIMIT;
		}

		return UnsubResult.OK;
	}

}
