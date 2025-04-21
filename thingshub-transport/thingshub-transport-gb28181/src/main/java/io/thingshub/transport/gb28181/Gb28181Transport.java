package io.thingshub.transport.gb28181;

import io.sipstack.netty.codec.sip.SipMessageDatagramDecoder;
import io.sipstack.netty.codec.sip.SipMessageEncoder;
import io.thingshub.config.ServerConfig;
import io.thingshub.transport.BaseTransport;
import jakarta.inject.Inject;
import reactor.netty.Connection;

/**
 * <p>
 * 绑定TCP Server，添加SIP协议编解码handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class Gb28181Transport extends BaseTransport {

	private Gb28181ServerConfig gb28181ServerConfig;

	@Inject
	public Gb28181Transport(Gb28181ServerConfig gb28181ServerConfig) {
		this.gb28181ServerConfig = gb28181ServerConfig;
	}

	@Override
	protected void attachHandlers(Connection connection) {
		connection.addHandlerLast(SipMessageDatagramDecoder.class.getSimpleName(), new SipMessageDatagramDecoder());
		connection.addHandlerLast(SipMessageEncoder.class.getSimpleName(), new SipMessageEncoder());
	}

	@Override
	protected ServerConfig getServiceConfig() {
		return gb28181ServerConfig;
	}

}
