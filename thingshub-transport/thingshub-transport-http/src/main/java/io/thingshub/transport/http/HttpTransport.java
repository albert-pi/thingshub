package io.thingshub.transport.http;

import java.util.function.Consumer;

import io.netty.handler.codec.json.JsonObjectDecoder;
import io.thingshub.config.ServerConfig;
import io.thingshub.transport.BaseTransport;
import jakarta.inject.Inject;
import reactor.netty.Connection;
import reactor.netty.http.server.HttpServerRoutes;

/**
 * <p>
 * 绑定HTTP Server，处理HTTP请求路由
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class HttpTransport extends BaseTransport {

	private HttpServerConfig httpServerConfig;

	private HttpRouter httpRouter;

	@Inject
	public HttpTransport(HttpServerConfig httpServerConfig, HttpRouter httpRouter) {
		this.httpRouter = httpRouter;
		this.httpServerConfig = httpServerConfig;
	}

	@Override
	protected boolean accessLog() {
		return httpServerConfig.isAccessLog();
	}

	@Override
	protected Consumer<HttpServerRoutes> httpRouter() {
		return this.httpRouter;
	}

	@Override
	protected void attachHandlers(Connection connection) {
		connection.addHandlerLast(new JsonObjectDecoder());
	}

	@Override
	protected ServerConfig getServiceConfig() {
		return httpServerConfig;
	}

}
