package io.thingshub.transport.gb28181;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.transport.Processor;
import io.thingshub.transport.gb28181.processor.AckRequestProcessor;
import io.thingshub.transport.gb28181.processor.ByeRequestProcessor;
import io.thingshub.transport.gb28181.processor.CancelRequestProcessor;
import io.thingshub.transport.gb28181.processor.InfoRequestProcessor;
import io.thingshub.transport.gb28181.processor.InviteRequestProcessor;
import io.thingshub.transport.gb28181.processor.NotifyRequestProcessor;
import io.thingshub.transport.gb28181.processor.RegisterRequestProcessor;
import io.thingshub.transport.gb28181.processor.SubscribeRequestProcessor;

public class Gb28181Module extends AbstractModule {

	@SuppressWarnings("rawtypes")
	@Override
	protected void configure() {
		Multibinder<Processor> processorBinder = Multibinder.newSetBinder(binder(), Processor.class);
		processorBinder.addBinding().to(AckRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(ByeRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(CancelRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(InfoRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(InviteRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(NotifyRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(RegisterRequestProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(SubscribeRequestProcessor.class).in(Singleton.class);

		bind(Gb28181UdpTransport.class).in(Singleton.class);
		bind(Gb28181Transport.class).in(Singleton.class);

//		Multibinder<Server> serverBinder = Multibinder.newSetBinder(binder(), Server.class);
//		serverBinder.addBinding().to(Gb28181Server.class).in(Singleton.class);
//		serverBinder.addBinding().to(Gb28181UdpServer.class).in(Singleton.class);
	}

}