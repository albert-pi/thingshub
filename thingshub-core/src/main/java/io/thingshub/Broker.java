package io.thingshub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.ConnectorConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import io.thingshub.bus.EventCenter;
import io.thingshub.config.ClusterConfig;
import io.thingshub.config.NodeConfig;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.ioc.Config;
import io.thingshub.logging.LogIgniteService;
import io.thingshub.script.ScriptEngine;
import io.thingshub.service.ScriptInfoService;
import io.thingshub.service.base.BaseService;
import io.thingshub.service.base.DataRegion;
import io.thingshub.transport.TransportServer;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.utils.TaskTracker;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * <p>
 * Broker
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Builder
@Slf4j
public class Broker {

	public static String currentNode;

	private static Injector injector;

	static {
		currentNode = NetUtil.getLocalhost().getHostAddress();
	}

	public static <T> T getBean(Class<T> clazz) {
		return injector.getInstance(clazz);
	}

	public static List<String> getServerNames() {
		return injector.findBindingsByType(TypeLiteral.get(TransportServer.class)).stream().map(binding -> {
			return injector.getInstance(binding.getKey()).getName();
		}).collect(Collectors.toList());
	}

	private Map<String, Object> configs;

	@SuppressWarnings("rawtypes")
	public Mono<Broker> startup() {
		System.getProperties().put("java.net.preferIPv4Stack", "true"); // using IPv4
		System.getProperties().put("IGNITE_QUIET", "false");
		System.getProperties().put("IGNITE_TO_STRING_INCLUDE_SENSITIVE", "false");
		System.getProperties().put("IGNITE_UPDATE_NOTIFIER", "false");
		System.getProperties().put("polyglot.engine.WarnInterpreterOnly", "false");

		Reflections reflections = new Reflections(Broker.class.getPackage().getName());

		/**
		 * Inject properties into configuration bean
		 */
		Map<Class, Object> configurations = new HashMap<>();
		Set<Class<?>> clazzsWithConfig = reflections.getTypesAnnotatedWith(Config.class);
		if (CollUtil.isNotEmpty(clazzsWithConfig)) {
			try {
				for (Class<?> configClazz : clazzsWithConfig) {
					Object configBean = configClazz.newInstance();
					Configurer.inject(configs, configBean);
					configurations.put(configClazz, configBean);
				}
			} catch (Exception e) {
				log.error("Failed to inject config value to bean. Error: ", e);
				throw new RuntimeException("", e);
			}
		}

		/**
		 * Configure data storage
		 */
		DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
		DataRegionConfiguration defaultRegion = new DataRegionConfiguration();
		defaultRegion.setName(BaseService.DEFAULT_DATA_REGION.name());
		defaultRegion.setInitialSize(BaseService.DEFAULT_DATA_REGION.initSize());
		defaultRegion.setMaxSize(BaseService.DEFAULT_DATA_REGION.maxSize());// 20% of total mem
		defaultRegion.setPersistenceEnabled(BaseService.DEFAULT_DATA_REGION.persistent());
		dataStorageConfiguration.setDefaultDataRegionConfiguration(defaultRegion);

		Set<Class<?>> clazzsWithCustomDataRegion = reflections.getTypesAnnotatedWith(DataRegion.class);
		if (CollUtil.isNotEmpty(clazzsWithCustomDataRegion)) {
			DataRegionConfiguration[] customDataRegionConfigurations = new DataRegionConfiguration[clazzsWithCustomDataRegion.size()];
			int i = 0;
			for (Class<?> clazz : clazzsWithCustomDataRegion) {
				DataRegion dataRegion = clazz.getAnnotation(DataRegion.class);
				customDataRegionConfigurations[i] = new DataRegionConfiguration().setName(dataRegion.name()).setInitialSize(dataRegion.initSize())
						.setMaxSize(dataRegion.maxSize()).setPersistenceEnabled(dataRegion.persistent());

				i++;
			}

			dataStorageConfiguration.setDataRegionConfigurations(customDataRegionConfigurations);
			dataStorageConfiguration.setWalSegmentSize(128 * 1024 * 1024);
//			dataStorageConfiguration.setStoragePath("/region");// relative to work directory
		}

		ClusterConfig clusterConfig = (ClusterConfig) configurations.get(ClusterConfig.class);
		String localAddress = Optional.ofNullable(clusterConfig.getLocalAddress()).orElse(currentNode);
		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
//		igniteConfiguration.setIgniteInstanceName("thingshub");
		igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
		igniteConfiguration.setLocalHost(localAddress);
		igniteConfiguration.setConnectorConfiguration(new ConnectorConfiguration().setHost(localAddress));
		igniteConfiguration.setGridLogger(new Slf4jLogger());
		igniteConfiguration.setClientMode(false);

		/**
		 * Set Ignite node attributes
		 */
		NodeConfig nodeConfig = (NodeConfig) configurations.get(NodeConfig.class);
		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("thingshub", "thingshub");
		if (!CollectionUtil.isEmpty(nodeConfig.getAttributes())) {
			userAttributes.putAll(nodeConfig.getAttributes().stream().collect(Collectors.toMap(String::toString, String::toString)));
		}
		igniteConfiguration.setUserAttributes(userAttributes);
		if (StrUtil.isNotBlank(nodeConfig.getNodeId())) {
			igniteConfiguration.setConsistentId(nodeConfig.getNodeId());
		}

		/**
		 * Configure TCP/IP discovery
		 */
		TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
		if (clusterConfig.getAddresses() != null) {// static IP finder
			ipFinder.setAddresses(clusterConfig.getAddresses());
		} else {// multicast IP finder
			String multicastGroup = clusterConfig.getMulticastGroup();
			if (multicastGroup != null) {
				ipFinder.setMulticastGroup(multicastGroup);
			}
			Integer multicastPort = clusterConfig.getMulticastPort();
			if (multicastPort != null) {
				ipFinder.setMulticastPort(multicastPort);
			}
		}
		TcpDiscoverySpi spi = new TcpDiscoverySpi();
		spi.setIpFinder(ipFinder);
		igniteConfiguration.setDiscoverySpi(spi);

		/**
		 * Set ignite work directory
		 */
		if (StrUtil.isNotBlank(nodeConfig.getDataDir())) {
			igniteConfiguration.setWorkDirectory(nodeConfig.getDataDir().concat("/ignite"));
		}

		Ignite ignite = Ignition.start(igniteConfiguration);
		if (!ignite.cluster().state().active()) {
			ignite.cluster().state(ClusterState.ACTIVE);
		}

		/**
		 * Deploy services
		 */
		ClusterGroup clusterGroup = ignite.cluster().forAttribute("thingshub", "thingshub");
		ignite.services(clusterGroup).deployNodeSingleton("bizLogService", new LogIgniteService());

		/**
		 * Assemble IOC dependency
		 */
		final ImmutableList.Builder<AbstractModule> modules = ImmutableList.builder();
		modules.add(new AbstractModule() {

			@SuppressWarnings("unchecked")
			@Override
			protected void configure() {
				binder().requireExplicitBindings();
				bind(Ignite.class).toInstance(ignite);

				if (!configurations.isEmpty()) {
					configurations.entrySet().forEach(entry -> bind(entry.getKey()).toInstance(entry.getValue()));
				}
			}

		});

		Set<Class<? extends AbstractModule>> moduleClazzs = reflections.getSubTypesOf(AbstractModule.class);
		if (CollUtil.isNotEmpty(moduleClazzs)) {
			moduleClazzs.forEach(moduleClazz -> {
				try {
					if (!moduleClazz.toString().contains("$")) {
						modules.add(moduleClazz.newInstance());
					}
				} catch (InstantiationException | IllegalAccessException e) {
					log.warn("Failed to build guice module. Error: ", e);
				}
			});
		}

		injector = Guice.createInjector(Stage.PRODUCTION, modules.build());

		EventCenter eventCenter = injector.getInstance(EventCenter.class);
		injector.findBindingsByType(TypeLiteral.get(TransportEventListener.class)).stream().forEach(binding -> {
			eventCenter.register(injector.getInstance(binding.getKey()));
		});

		/**
		 * Complete initialization and start transport server
		 */
		List<Mono<?>> monos = injector.findBindingsByType(TypeLiteral.get(TransportServer.class)).stream().map(binding -> {
			return injector.getInstance(binding.getKey()).start();
		}).collect(Collectors.toList());

		return Mono.when(monos).doOnNext(v -> {
			ScriptInfoService scriptInfoService = injector.getInstance(ScriptInfoService.class);
			injector.findBindingsByType(TypeLiteral.get(ScriptEngine.class)).stream().forEach(binding -> {
				ScriptEngine scriptEngine = injector.getInstance(binding.getKey());
				String lang = scriptEngine.scriptType().lang();

				List<ScriptInfo> scriptInfos = scriptInfoService.getScriptInfosByLang(lang);
				if (CollUtil.isNotEmpty(scriptInfos)) {
					scriptInfos.forEach(scriptInfo -> {
						scriptEngine.load(scriptInfo.getCode(), scriptInfo.getContent());
					});
				}
			});

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				TaskTracker.getInstance().stop();

				ignite.close();
			}));
		}).then().doOnSuccess(v -> log.info("Thingshub started successfully")).thenReturn(this);
	}

}
