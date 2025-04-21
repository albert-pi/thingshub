package io.thingshub.bus;

import java.util.concurrent.Executors;

import com.google.common.eventbus.AsyncEventBus;

public class EventCenter {

	private final AsyncEventBus eventBus;

	public EventCenter() {
		this.eventBus = new AsyncEventBus(Executors.newCachedThreadPool());
	}

	public void register(Object listener) {
		this.eventBus.register(listener);
	}

	public void post(Object event) {
		this.eventBus.post(event);
	}

}
