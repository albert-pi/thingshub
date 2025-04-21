package io.thingshub.transport.jt808;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.transport.Processor;
import io.thingshub.transport.TransportServer;
import io.thingshub.transport.jt808.processor.AuthProcessor;
import io.thingshub.transport.jt808.processor.HeartbeatProcessor;
import io.thingshub.transport.jt808.processor.LocationProcessor;
import io.thingshub.transport.jt808.processor.RegisterProcessor;
import io.thingshub.transport.jt808.processor.TransmissionDrivingProcessor;
import io.thingshub.transport.jt808.processor.TransmissionFaultProcessor;
import io.thingshub.transport.jt808.processor.TransmissionSleepingProcessor;
import io.thingshub.transport.jt808.processor.TransmissionWakeningProcessor;
import io.thingshub.transport.jt808.processor.UnregisterProcessor;
import io.thingshub.transport.jt808.processor.VersionProcessor;

public class Jt808Module extends AbstractModule {

	@SuppressWarnings("rawtypes")
	@Override
	protected void configure() {
		Multibinder<Processor> processorBinder = Multibinder.newSetBinder(binder(), Processor.class);
		processorBinder.addBinding().to(RegisterProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(AuthProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(HeartbeatProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(LocationProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(VersionProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(TransmissionDrivingProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(TransmissionFaultProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(TransmissionSleepingProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(TransmissionWakeningProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(UnregisterProcessor.class).in(Singleton.class);

		bind(Jt808Transport.class).in(Singleton.class);

		Multibinder<TransportServer> serverBinder = Multibinder.newSetBinder(binder(), TransportServer.class);
		serverBinder.addBinding().to(Jt808Server.class).in(Singleton.class);
	}

}