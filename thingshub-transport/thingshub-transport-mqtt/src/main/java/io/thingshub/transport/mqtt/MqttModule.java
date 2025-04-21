package io.thingshub.transport.mqtt;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.thingshub.transport.Processor;
import io.thingshub.transport.TransportServer;
import io.thingshub.transport.event.TransportEventListener;
import io.thingshub.transport.mqtt.codec.MqttTransformDecoder;
import io.thingshub.transport.mqtt.codec.MqttTransformEncoder;
import io.thingshub.transport.mqtt.event.CloseMqttSessionListener;
import io.thingshub.transport.mqtt.event.SendMqttMessageListener;
import io.thingshub.transport.mqtt.processor.DisconnectProcessor;
import io.thingshub.transport.mqtt.processor.PingProcessor;
import io.thingshub.transport.mqtt.processor.PubAckProcessor;
import io.thingshub.transport.mqtt.processor.PubCompProcessor;
import io.thingshub.transport.mqtt.processor.PubRecProcessor;
import io.thingshub.transport.mqtt.processor.PubRelProcessor;
import io.thingshub.transport.mqtt.processor.PublishProcessor;
import io.thingshub.transport.mqtt.processor.SubscribeProcessor;
import io.thingshub.transport.mqtt.processor.UnsubscribeProcessor;

public class MqttModule extends AbstractModule {

	@SuppressWarnings("rawtypes")
	@Override
	protected void configure() {
		Multibinder<Processor> processorBinder = Multibinder.newSetBinder(binder(), Processor.class);
		processorBinder.addBinding().to(PingProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(SubscribeProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(PublishProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(PubRecProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(PubAckProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(PubRelProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(PubCompProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(UnsubscribeProcessor.class).in(Singleton.class);
		processorBinder.addBinding().to(DisconnectProcessor.class).in(Singleton.class);

		bind(MqttTransformDecoder.class).in(Singleton.class);
		bind(MqttTransformEncoder.class).in(Singleton.class);
		bind(MqttTransport.class).in(Singleton.class);
		bind(RetainManager.class).in(Singleton.class);

		Multibinder<TransportEventListener> transportEventListenerBinder = Multibinder.newSetBinder(binder(), TransportEventListener.class);
		transportEventListenerBinder.addBinding().to(SendMqttMessageListener.class).in(Singleton.class);
		transportEventListenerBinder.addBinding().to(CloseMqttSessionListener.class).in(Singleton.class);

		Multibinder<TransportServer> serverBinder = Multibinder.newSetBinder(binder(), TransportServer.class);
		serverBinder.addBinding().to(MqttServer.class).in(Singleton.class);
//		serverBinder.addBinding().to(MqttWebSocketServer.class).in(Singleton.class);
	}

}