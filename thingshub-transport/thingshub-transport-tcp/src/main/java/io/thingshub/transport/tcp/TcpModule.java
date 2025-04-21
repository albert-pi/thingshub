package io.thingshub.transport.tcp;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.transport.Processor;
import io.thingshub.transport.TransportServer;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.tcp.codec.TcpTransformDecoder;
import io.thingshub.transport.tcp.codec.TcpTransformEncoder;
import io.thingshub.transport.tcp.event.CloseTcpSessionListener;
import io.thingshub.transport.tcp.event.SendTcpMessageListener;
import io.thingshub.transport.tcp.processor.GenericProcessor;
import io.thingshub.transport.tcp.processor.HeartbeatProcessor;

public class TcpModule extends AbstractModule {

	@SuppressWarnings("rawtypes")
	@Override
	protected void configure() {
		Multibinder<Processor> processorBinder = Multibinder.newSetBinder(binder(), Processor.class);
		processorBinder.addBinding().to(HeartbeatProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(GenericProcessor.class).in(Singleton.class);

		bind(TcpTransformDecoder.class).in(Singleton.class);
		bind(TcpTransformEncoder.class).in(Singleton.class);
		bind(TcpTransport.class).in(Singleton.class);

		Multibinder<TransportEventListener> transportEventListenerBinder = Multibinder.newSetBinder(binder(), TransportEventListener.class);
		transportEventListenerBinder.addBinding().to(SendTcpMessageListener.class).in(Singleton.class);
		transportEventListenerBinder.addBinding().to(CloseTcpSessionListener.class).in(Singleton.class);

		Multibinder<TransportServer> serverBinder = Multibinder.newSetBinder(binder(), TransportServer.class);
		serverBinder.addBinding().to(TcpServer.class).in(Singleton.class);
	}

}