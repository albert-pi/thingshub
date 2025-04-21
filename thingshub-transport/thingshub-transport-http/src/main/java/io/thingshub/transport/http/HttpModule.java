package io.thingshub.transport.http;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.transport.TransportServer;

public class HttpModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(HttpRouter.class).in(Singleton.class);
		bind(HttpTransport.class).in(Singleton.class);

		Multibinder<TransportServer> serverBinder = Multibinder.newSetBinder(binder(), TransportServer.class);
		serverBinder.addBinding().to(HttpServer.class).in(Singleton.class);
	}

}