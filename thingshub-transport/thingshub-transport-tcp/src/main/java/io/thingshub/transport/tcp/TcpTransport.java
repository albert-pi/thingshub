package io.thingshub.transport.tcp;

import io.thingshub.config.ServerConfig;
import io.thingshub.transport.BaseTransport;
import io.thingshub.transport.tcp.handler.TcpPreludeHandler;
import jakarta.inject.Inject;
import reactor.netty.Connection;

/**
 * <p>
 * 绑定TCP Server，添加TCP私有协议处理handler
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class TcpTransport extends BaseTransport {

	private TcpServerConfig tcpServerConfig;

	@Inject
	public TcpTransport(TcpServerConfig tcpServerConfig) {
		this.tcpServerConfig = tcpServerConfig;
	}

	@Override
	protected void attachHandlers(Connection connection) {
		connection.addHandlerLast(TcpPreludeHandler.class.getSimpleName(), new TcpPreludeHandler(tcpServerConfig));
	}

	@Override
	protected ServerConfig getServiceConfig() {
		return tcpServerConfig;
	}

}
