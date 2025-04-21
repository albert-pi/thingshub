package io.thingshub;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.acl.AclManager;
import io.thingshub.bus.EventCenter;
import io.thingshub.cluster.ClusterManager;
import io.thingshub.logging.LogManager;
import io.thingshub.script.ScriptEngine;
import io.thingshub.script.ScriptEngineFactory;
import io.thingshub.script.graaljs.GraalJsEngine;
import io.thingshub.script.python.PythonEngine;
import io.thingshub.service.SubscribingService;
import io.thingshub.service.ConnInfoService;
import io.thingshub.service.DeviceGroupService;
import io.thingshub.service.DeviceService;
import io.thingshub.service.InboxService;
import io.thingshub.service.MessageModelService;
import io.thingshub.service.MessageService;
import io.thingshub.service.OtaService;
import io.thingshub.service.ProductCatService;
import io.thingshub.service.ProductScriptService;
import io.thingshub.service.ProductService;
import io.thingshub.service.RawPacketService;
import io.thingshub.service.RetainService;
import io.thingshub.service.ScriptInfoService;
import io.thingshub.service.ServerScriptService;
import io.thingshub.service.SessionService;
import io.thingshub.service.SysClientService;
import io.thingshub.service.ThingModelService;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.transport.MessageDispatcher;
import io.thingshub.transport.MessageRetryTimer;
import io.thingshub.transport.auth.AuthManager;
import io.thingshub.transport.throttler.ResourceThrottler;

/**
 * <p>
 * Broker Module
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class BrokerModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<ScriptEngine> scriptEngineBinder = Multibinder.newSetBinder(binder(), ScriptEngine.class);
		scriptEngineBinder.addBinding().to(GraalJsEngine.class).in(Singleton.class);
		scriptEngineBinder.addBinding().to(PythonEngine.class).in(Singleton.class);
		bind(ScriptEngineFactory.class).in(Singleton.class);

		bind(LogManager.class).in(Singleton.class);
		bind(AuthManager.class).in(Singleton.class);
		bind(AclManager.class).in(Singleton.class);
		bind(ResourceThrottler.class).in(Singleton.class);
		bind(ProductCatService.class).in(Singleton.class);
		bind(ProductService.class).in(Singleton.class);
		bind(DeviceGroupService.class).in(Singleton.class);
		bind(DeviceService.class).in(Singleton.class);
		bind(SysClientService.class).in(Singleton.class);
		bind(OtaService.class).in(Singleton.class);
		bind(ScriptInfoService.class).in(Singleton.class);
		bind(ServerScriptService.class).in(Singleton.class);
		bind(ProductScriptService.class).in(Singleton.class);
		bind(MessageModelService.class).in(Singleton.class);
		bind(ThingModelService.class).in(Singleton.class);
		bind(ConnInfoService.class).in(Singleton.class);
		bind(SessionService.class).in(Singleton.class);
		bind(SubscribingService.class).in(Singleton.class);
		bind(MessageService.class).in(Singleton.class);
		bind(RawPacketService.class).in(Singleton.class);
		bind(InboxService.class).in(Singleton.class);
		bind(RetainService.class).in(Singleton.class);
		bind(SubscriptionManager.class).in(Singleton.class);
		bind(ClusterManager.class).in(Singleton.class);
		bind(EventCenter.class).in(Singleton.class);
		bind(MessageRetryTimer.class).in(Singleton.class);
		bind(MessageDispatcher.class).in(Singleton.class);

	}

}