package io.thingshub.config;

import java.util.Map;

import io.netty.channel.ChannelOption;
import io.thingshub.ioc.Value;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 服务器配置参数
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public abstract class ServerConfig {

	public abstract String getHost();

	public abstract Integer getPort();

	@Getter
	@Value("${thingshub.server.wiretap: false}")
	protected boolean wiretap;

	@Getter
	@Setter
	protected Integer bossThreads = 1;

	@Getter
	@Setter
	protected Integer workerThreads = Math.max(Runtime.getRuntime().availableProcessors() / 2, 2);

	// TODO default ChannelOptions
	@Getter
	@Setter
	protected Map<ChannelOption<?>, Object> options;

	@Getter
	@Setter
	protected Map<ChannelOption<?>, Object> childOptions;

	// TODO tenant settings
	@Getter
	@Setter
	protected int connectRateLimit = 1024000;

	@Getter
	protected long writeLimit = 512 * 1024; // byte/s

	@Getter
	protected long readLimit = 512 * 1024; // byte/s

	@Getter
	protected int maxBytesInMessage = 256 * 1024;

	@Getter
	@Setter
	private SslConfig ssl;

}
