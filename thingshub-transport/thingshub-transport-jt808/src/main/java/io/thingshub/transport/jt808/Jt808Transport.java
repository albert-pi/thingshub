package io.thingshub.transport.jt808;

import io.thingshub.config.ServerConfig;
import io.thingshub.transport.BaseTransport;
import io.thingshub.transport.jt808.codec.Jt808Decoder;
import io.thingshub.transport.jt808.codec.Jt808Encoder;
import jakarta.inject.Inject;
import reactor.netty.Connection;

/**
 * <p>
 * 绑定TCP Server，添加JT808协议编解码handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class Jt808Transport extends BaseTransport {

	private Jt808ServerConfig jt808ServerConfig;

	@Inject
	public Jt808Transport(Jt808ServerConfig jt808ServerConfig) {
		this.jt808ServerConfig = jt808ServerConfig;
	}

	@Override
	protected void attachHandlers(Connection connection) {
		connection.addHandlerLast(Jt808Decoder.INSTANCE);
		connection.addHandlerLast(Jt808Encoder.INSTANCE);
	}

	@Override
	protected ServerConfig getServiceConfig() {
		return jt808ServerConfig;
	}

}
