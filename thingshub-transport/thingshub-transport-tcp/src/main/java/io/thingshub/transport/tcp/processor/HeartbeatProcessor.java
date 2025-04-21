package io.thingshub.transport.tcp.processor;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.thingshub.transport.Processor;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.codec.ThingMessageCode;
import io.thingshub.transport.codec.ThingMethod;
import io.thingshub.transport.tcp.TcpChannelContext;
import io.thingshub.transport.tcp.message.HeartbeatMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 心跳消息处理器
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j(topic = "io.thingshub.transport.processor")
public class HeartbeatProcessor extends Processor<TcpChannelContext, HeartbeatMessage> {

	@Override
	public void process(@NonNull TcpChannelContext ctx, @NonNull HeartbeatMessage msg) {
		if (log.isDebugEnabled()) {
			log.debug("TCP client [{}] send heartbeat", ctx.getClientId());
		}

		ThingMessage pongMessage = ThingMessage.builder() //
				.id(msg.getPingMessage().getId()) //
				.version(msg.getPingMessage().getVersion()) //
				.method(ThingMethod.HEARTBEAT_REPLY.code()) //
				.code(ThingMessageCode.SUCCESS.code()) //
				.message(ThingMessageCode.SUCCESS.desc()) //
				.data(msg.getPingMessage().getParams()).build();

		ctx.writeAndFlush(pongMessage).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					log.error("Server failed to send the pong message: {}", pongMessage);
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});
	}

}