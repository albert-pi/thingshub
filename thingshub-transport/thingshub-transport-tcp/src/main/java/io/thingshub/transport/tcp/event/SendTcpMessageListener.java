package io.thingshub.transport.tcp.event;

import com.alibaba.fastjson2.JSON;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.thingshub.domain.Message;
import io.thingshub.service.InboxService;
import io.thingshub.transport.MessageRetryTimer;
import io.thingshub.transport.codec.ThingMessage;
import io.thingshub.transport.event.DistributeEvent;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.tcp.TcpChannelContext;
import io.thingshub.transport.tcp.TcpTransport;
import io.thingshub.transport.tcp.handler.TcpSessionHandler;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendTcpMessageListener extends TransportEventListener<DistributeEvent, TcpTransport> {

	private MessageRetryTimer messageRetryTimer;

	private InboxService inboxService;

	@Inject
	public SendTcpMessageListener(MessageRetryTimer messageRetryTimer, InboxService inboxService) {
		this.messageRetryTimer = messageRetryTimer;
		this.inboxService = inboxService;
	}

	@Override
	public void onEvent(DistributeEvent event) {
		TcpChannelContext ctx = TcpSessionHandler.CHANNEL_CONTEXTS.get(event.getRecipientId());
		if (ctx == null) {
			return;
		}

		Message theMsg = event.getMessage();
		ThingMessage thingMessage = JSON.parseObject(theMsg.getPayload(), ThingMessage.class);

		messageRetryTimer.doRetry(ctx, Integer.parseInt(thingMessage.getId()), thingMessage, 5, 6);

		ctx.writeAndFlush(thingMessage).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					inboxService.ackDelivery(ctx.getClientId(), theMsg.getId());
					messageRetryTimer.cancel(ctx.channel().id().asLongText(), Integer.parseInt(thingMessage.getId()));
				} else {
					log.error("TCP transport Server failed to distribute message. payload: {}", theMsg.getPayload());
					if (future.cause() != null) {
						log.error("", future.cause());
					}
				}
			}

		});

	}

}
