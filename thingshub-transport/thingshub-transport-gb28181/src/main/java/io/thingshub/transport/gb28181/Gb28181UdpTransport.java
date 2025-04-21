package io.thingshub.transport.gb28181;

import io.netty.channel.ChannelOption;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.sipstack.netty.codec.sip.SipMessageDatagramDecoder;
import io.sipstack.netty.codec.sip.SipMessageEncoder;
import io.thingshub.transport.throttler.MessageDebounceHandler;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.resources.LoopResources;
import reactor.netty.udp.UdpServer;

/**
 * <p>
 * 绑定UDP Server，添加SIP协议编解码handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class Gb28181UdpTransport {

	private LoopResources loopResources;

	private Gb28181ServerConfig gb28181ServerConfig;

	@Inject
	public Gb28181UdpTransport(Gb28181ServerConfig gb28181ServerConfig) {
		this.gb28181ServerConfig = gb28181ServerConfig;
	}

	public Mono<Connection> bind() {
		return Mono.deferContextual(contextView -> {
			loopResources = LoopResources.create("thingshub-" + gb28181ServerConfig.getPort(), gb28181ServerConfig.getBossThreads(),
					gb28181ServerConfig.getWorkerThreads(), true);

			return Mono.just(newUdpServer());
		}).flatMap(view -> view.bind().cast(Connection.class));
	}

	@SuppressWarnings("unchecked")
	private UdpServer newUdpServer() {
		UdpServer udpServer = UdpServer.create();
		if (gb28181ServerConfig.getOptions() != null) {
			gb28181ServerConfig.getOptions().forEach((k, v) -> udpServer.option((ChannelOption<? super Object>) k, v));
		}

		return udpServer.port(gb28181ServerConfig.getPort()) //
				.wiretap(gb28181ServerConfig.isWiretap()) //
//				.metrics(configuration.getMeterConfig() != null) //
				.runOn(loopResources) //
				.doOnChannelInit((observer, channel, remoteAddress) -> {
					// TODO 限制资源使用

					channel.pipeline().addLast(new ChannelTrafficShapingHandler(gb28181ServerConfig.getWriteLimit(), gb28181ServerConfig.getReadLimit()));
					channel.pipeline().addLast(new MessageDebounceHandler());
					channel.pipeline().addLast(SipMessageDatagramDecoder.class.getSimpleName(), new SipMessageDatagramDecoder());
					channel.pipeline().addLast(SipMessageEncoder.class.getSimpleName(), new SipMessageEncoder());
				}).handle((inbound, outbound) -> inbound.receive().then());
	}

}
