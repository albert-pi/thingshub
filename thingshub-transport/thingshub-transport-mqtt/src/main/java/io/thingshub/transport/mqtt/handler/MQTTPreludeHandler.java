package io.thingshub.transport.mqtt.handler;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_MALFORMED_PACKET;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_PACKET_TOO_LARGE;
import static io.netty.handler.codec.mqtt.MqttMessageType.CONNECT;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import io.thingshub.transport.mqtt.MqttServerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Prepare for a new MQTT connection
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public class MQTTPreludeHandler extends ChannelDuplexHandler {

	private MqttServerConfig mqttServerConfig;

	private ChannelHandlerContext ctx;

	private ScheduledFuture<?> timeoutCloseTask;

	private ScheduledFuture<?> closeConnectionTask;

	public MQTTPreludeHandler(MqttServerConfig mqttServerConfig) {
		this.mqttServerConfig = mqttServerConfig;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		this.ctx = ctx;

		timeoutCloseTask = ctx.executor().schedule(() -> {
			ctx.channel().close();
		}, mqttServerConfig.getConnectTimeout(), TimeUnit.SECONDS);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) {
		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		assert msg instanceof MqttMessage;

		ctx.channel().config().setAutoRead(false);
		timeoutCloseTask.cancel(true);

		MqttMessage message = (MqttMessage) msg;
		if (!message.decoderResult().isSuccess()) {
			Throwable cause = message.decoderResult().cause();
			if (cause instanceof MqttUnacceptableProtocolVersionException) {
				log.error("protocol error: unaccepted protocol version", cause);
				closeChannelWithDelay();
				return;
			}
			if (message.fixedHeader() != null && message.fixedHeader().messageType() != CONNECT) {
				log.error("protocol error: the first Packet must be a CONNECT Packet");
				closeChannelWithDelay();
				return;
			}

			Object varHeader = message.variableHeader();
			if (varHeader instanceof MqttConnectVariableHeader) {
				MqttConnectVariableHeader connVarHeader = (MqttConnectVariableHeader) varHeader;

				switch (connVarHeader.version()) {
				case 3:
				case 4:
					if (cause instanceof TooLongFrameException) {
						log.error("protocol error: too large packet", cause);
						closeChannelWithDelay();
					} else if (cause instanceof MqttIdentifierRejectedException) {
						log.error("protocol error: identifier rejected", cause);

						closeChannelWithDelay(MqttMessageBuilders.connAck().returnCode(CONNECTION_REFUSED_IDENTIFIER_REJECTED).build());
					} else {
						log.error("", cause);
						closeChannelWithDelay();
					}
					return;
				case 5:
				default:
					MqttProperties props = new MqttProperties();
					if (cause.getMessage() != null) {
						props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), cause.getMessage()));
					}

					if (cause instanceof TooLongFrameException) {
						log.error("protocol error: too large packet", cause);
						closeChannelWithDelay(MqttMessageBuilders.connAck().properties(props).returnCode(CONNECTION_REFUSED_PACKET_TOO_LARGE).build());
					} else if (cause instanceof MqttIdentifierRejectedException) {
						log.error("protocol error: client identifier not valid", cause);
						closeChannelWithDelay(
								MqttMessageBuilders.connAck().properties(props).returnCode(CONNECTION_REFUSED_CLIENT_IDENTIFIER_NOT_VALID).build());
					} else {
						log.error("protocol error: malformed packet", cause);
						closeChannelWithDelay(MqttMessageBuilders.connAck().properties(props).returnCode(CONNECTION_REFUSED_MALFORMED_PACKET).build());
					}
					return;
				}
			} else {
				log.error("protocol error: ", cause);
				closeChannelWithDelay();// other
				return;
			}
		} else if (!(message instanceof MqttConnectMessage)) {
			log.error("protocol error: the first Packet must be a CONNECT Packet");
			closeChannelWithDelay();
			return;
		}

		MqttConnectMessage connectMessage = (MqttConnectMessage) message;
		switch (connectMessage.variableHeader().version()) {
		case 3:
		case 4:
			ctx.pipeline().addAfter(ctx.executor(), this.getClass().getSimpleName(), MQTTConnectionHandler.class.getSimpleName(),
					new MQTTConnectionHandler(mqttServerConfig));
			ctx.fireChannelRead(connectMessage);
			ctx.pipeline().remove(this.getClass().getSimpleName());
			break;
		case 5:// TODO MQTT 5 handler
			ctx.pipeline().addAfter(ctx.executor(), this.getClass().getSimpleName(), MQTTConnectionHandler.class.getSimpleName(),
					new MQTTConnectionHandler(mqttServerConfig));
			ctx.fireChannelRead(connectMessage);
			ctx.pipeline().remove(this.getClass().getSimpleName());
			break;
		default:
			log.warn("Unsupported protocol version: {}", connectMessage.variableHeader().version());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("", cause);
		ctx.channel().close();
	}

	private void closeChannelWithDelay() {
		closeChannelWithDelay(null);
	}

	private void closeChannelWithDelay(@Nullable MqttMessage farewell) {
		if (timeoutCloseTask != null) {
			timeoutCloseTask.cancel(true);
		}

		if (!ctx.channel().isActive()) {
			return;
		}

		assert closeConnectionTask == null;
		closeConnectionTask = ctx.executor().schedule(() -> {
			if (farewell != null) {
				ctx.writeAndFlush(farewell).addListener(ChannelFutureListener.CLOSE);
			} else {
				ctx.channel().close();
			}
		}, ThreadLocalRandom.current().nextInt(3000), TimeUnit.MILLISECONDS);
	}

}