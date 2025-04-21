package io.thingshub.transport.mqtt;

import java.time.Duration;
import java.util.Set;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.netty.channel.ChannelHandlerContext;
import io.thingshub.transport.BaseChannelContext;

public class MqttChannelContext extends BaseChannelContext {

	private final Set<Integer> packetIdsInUsing = new ConcurrentHashSet<>();

	private final Cache<Integer, Object> inboundMessages;

	private final Cache<Integer, Object> outboundMessages;

	private int receivingCount;

	public MqttChannelContext(ChannelHandlerContext ctx, String tenant, String clientId, String clientAddr, int keepalive) {
		super(ctx, tenant, clientId, clientAddr, keepalive);

		this.inboundMessages = Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(keepalive * 1500)).build();
		this.outboundMessages = Caffeine.newBuilder().expireAfterWrite(Duration.ofMillis(keepalive * 1500)).build();
	}

	public boolean isPacketIdUsed(int packetId) {
		return packetIdsInUsing.contains(packetId);
	}

	public void addUsingPacketId(int packetId) {
		receivingCount++;
		packetIdsInUsing.add(packetId);
	}

	public void removeUsingPacketId(int packetId) {
		packetIdsInUsing.remove(packetId);
		receivingCount = Math.max(receivingCount - 1, 0);
	}

	public void addInboundMessage(int packetId, Object inboundMsg) {
		inboundMessages.put(packetId, inboundMsg);
	}

	public Object pollInboundMessage(int packetId) {
		Object val = inboundMessages.getIfPresent(packetId);
		inboundMessages.invalidate(packetId);

		return val;
	}

	public void addOutboundMessage(int packetId, Object outboundMsg) {
		outboundMessages.put(packetId, outboundMsg);
	}

	public Object pollOutboundMessage(int packetId) {
		Object val = outboundMessages.getIfPresent(packetId);
		outboundMessages.invalidate(packetId);

		return val;
	}

}
