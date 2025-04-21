package io.thingshub.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.netty.channel.ChannelHandlerContext;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 消息路由分发
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class MessageDispatcher {

	private final Map<Class<TransportMessage>, Processor<ChannelHandlerContext, TransportMessage>> messageProcessors = new HashMap<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Inject
	public MessageDispatcher(Set<Processor> processors) {
		processors.forEach(processor -> messageProcessors.put(processor.getMessageClass(), processor));
	}

	public void dispatch(@NonNull ChannelHandlerContext ctx, @NonNull TransportMessage message) {
		try {
			Optional.ofNullable(messageProcessors.get(message.getClass())).ifPresent(processor -> processor.process(ctx, message));
		} catch (Exception e) {
			log.error("Failed to dispatch channel's message. Error: ", e);
		}
	}

}
