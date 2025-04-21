package io.thingshub.transport;

import static io.thingshub.transport.throttler.ORCondition.or;

import java.io.File;
import java.util.function.Consumer;

import com.google.common.util.concurrent.RateLimiter;

import cn.hutool.core.util.StrUtil;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.thingshub.config.ServerConfig;
import io.thingshub.config.SslConfig;
import io.thingshub.transport.throttler.Condition;
import io.thingshub.transport.throttler.DirectMemPressureCondition;
import io.thingshub.transport.throttler.HeapMemPressureCondition;
import io.thingshub.transport.throttler.MessageDebounceHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpServer;

/**
 * <p>
 * Transport抽象类，处理Server的绑定、channel初始化等
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Slf4j
public abstract class BaseTransport {

	protected RateLimiter connRateLimiter;

	protected LoopResources loopResources;

	public Mono<DisposableServer> bind() {
		return Mono.deferContextual(contextView -> {
			loopResources = LoopResources.create("thingshub-" + getServiceConfig().getPort(), getServiceConfig().getBossThreads(),
					getServiceConfig().getWorkerThreads(), true);
			connRateLimiter = RateLimiter.create(getServiceConfig().getConnectRateLimit());

			TransportType transportType = contextView.get(TransportType.class);
			if (transportType != null) {
				switch (transportType) {
				case TCP:
					return Mono.just(newTcpServer());
				case HTTP:
					return Mono.just(newHttpServer());
				default:
					return Mono.just(newTcpServer());
				}
			} else {
				return Mono.just(newTcpServer());
			}

		}).doOnError(e -> log.error("Failed to bind Server. Error: ", e)).flatMap(view -> view.bind().cast(DisposableServer.class));
	}

	@SuppressWarnings("unchecked")
	private TcpServer newTcpServer() {
		prepare();

		TcpServer tcpServer = TcpServer.create();
		if (getServiceConfig().getSsl() != null) {
			tcpServer.secure(sslContextSpec -> this.secure(sslContextSpec, getServiceConfig().getSsl()));
		}
		if (getServiceConfig().getOptions() != null) {
			getServiceConfig().getOptions().forEach((k, v) -> tcpServer.option((ChannelOption<? super Object>) k, v));
		}
		if (getServiceConfig().getChildOptions() != null) {
			getServiceConfig().getChildOptions().forEach((k, v) -> tcpServer.childOption((ChannelOption<? super Object>) k, v));
		}

		return tcpServer.host(getServiceConfig().getHost()) //
				.port(getServiceConfig().getPort()) //
//				.childAttr(SESSION_CTX, sessionContext)
				.wiretap(getServiceConfig().isWiretap()) //
//				.metrics(configuration.getMeterConfig() != null) //
				.runOn(loopResources) //
				.doOnChannelInit((observer, channel, remoteAddress) -> {
					// 限制连接数
					if (!connRateLimiter.tryAcquire()) {
						log.debug("Connection dropped due to exceed limit");
						channel.config().setAutoRead(false);
						if (channel.isActive()) {
							channel.close();
						}

						return;
					}

					// 根据内存使用情况决定是否拒绝连接
					Condition rejectCondition = or(DirectMemPressureCondition.INSTANCE, HeapMemPressureCondition.INSTANCE);
					if (rejectCondition.meet()) {
						log.debug("Reject connection due to {}", rejectCondition);

						channel.config().setAutoRead(false);
						if (channel.isActive()) {
							channel.close();
						}

						return;
					}
				}).doOnConnection(connection -> {
					connection.addHandlerLast(new ChannelTrafficShapingHandler(getServiceConfig().getWriteLimit(), getServiceConfig().getReadLimit()));
					connection.addHandlerLast(new MessageDebounceHandler());

					attachHandlers(connection);
				}).handle((inbound, outbound) -> inbound.receive().then());
	}

	@SuppressWarnings("unchecked")
	private HttpServer newHttpServer() {
		HttpServer httpServer = HttpServer.create();

		if (getServiceConfig().getSsl() != null) {
			httpServer.secure(sslContextSpec -> this.secure(sslContextSpec, getServiceConfig().getSsl()));
		}
		if (getServiceConfig().getOptions() != null) {
			getServiceConfig().getOptions().forEach((k, v) -> httpServer.option((ChannelOption<? super Object>) k, v));
		}
		if (getServiceConfig().getChildOptions() != null) {
			getServiceConfig().getChildOptions().forEach((k, v) -> httpServer.childOption((ChannelOption<? super Object>) k, v));
		}

		return httpServer.host(getServiceConfig().getHost()) //
				.port(getServiceConfig().getPort())//
				.wiretap(getServiceConfig().isWiretap()) //
				.route(httpRouter()) //
				.accessLog(accessLog()) //
				.runOn(loopResources) //
				.doOnChannelInit((observer, channel, remoteAddress) -> {
					// 限制连接数
					if (!connRateLimiter.tryAcquire()) {
						log.debug("Connection dropped due to exceed limit");
						channel.config().setAutoRead(false);
						if (channel.isActive()) {
							channel.close();
						}

						return;
					}

					// 根据内存使用情况决定是否拒绝连接
					Condition rejectCondition = or(DirectMemPressureCondition.INSTANCE, HeapMemPressureCondition.INSTANCE);
					if (rejectCondition.meet()) {
						log.debug("Reject connection due to {}", rejectCondition);

						channel.config().setAutoRead(false);
						if (channel.isActive()) {
							channel.close();
						}

						return;
					}
				}).doOnConnection(connection -> attachHandlers(connection));
	}

	protected Consumer<HttpServerRoutes> httpRouter() {
		return null;
	}

	protected boolean accessLog() {
		return false;
	}

	protected void secure(SslProvider.SslContextSpec sslContextSpec, SslConfig sslConfig) {
		try {
			SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(sslConfig.getCrt()), new File(sslConfig.getKey()));
			if (StrUtil.isNotBlank(sslConfig.getCa())) {
				sslContextBuilder = sslContextBuilder.trustManager(new File(sslConfig.getCa()));
				sslContextBuilder.clientAuth(ClientAuth.REQUIRE);
			}

			sslContextSpec.sslContext(sslContextBuilder);
		} catch (Exception e) {
			log.error("SSL read error", e);
		}
	};

	protected abstract ServerConfig getServiceConfig();

	protected void prepare() {
	}

	protected abstract void attachHandlers(Connection connection);

}
