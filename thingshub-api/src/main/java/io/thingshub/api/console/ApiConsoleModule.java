package io.thingshub.api.console;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import io.thingshub.api.console.controller.DeviceController;
import io.thingshub.api.console.controller.IndexController;
import io.thingshub.api.console.controller.LogController;
import io.thingshub.api.console.controller.MessageModelController;
import io.thingshub.api.console.controller.MetricController;
import io.thingshub.api.console.controller.ProductController;
import io.thingshub.api.console.controller.ResourceController;
import io.thingshub.api.console.controller.ScriptController;
import io.thingshub.api.console.controller.SysClientController;
import io.thingshub.api.console.controller.ThingModelController;

/**
 * <p>
 * API Server Console Module
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ApiConsoleModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IndexController.class).in(Singleton.class);
		bind(ResourceController.class).in(Singleton.class);
		bind(LogController.class).in(Singleton.class);
		bind(MetricController.class).in(Singleton.class);
		bind(ProductController.class).in(Singleton.class);
		bind(DeviceController.class).in(Singleton.class);
		bind(SysClientController.class).in(Singleton.class);
		bind(ThingModelController.class).in(Singleton.class);
		bind(MessageModelController.class).in(Singleton.class);
		bind(ScriptController.class).in(Singleton.class);
	}

}