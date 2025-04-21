package io.thingshub.transport.event;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.thingshub.bus.EventListener;
import io.thingshub.transport.BaseTransport;

/**
 * <p>
 * Listens event on transport.
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public abstract class TransportEventListener<E extends TransportEvent, T extends BaseTransport> extends EventListener<E> {

	private Class<T> transportClazz;

	@SuppressWarnings("unchecked")
	public TransportEventListener() {
		Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		this.eventClazz = (Class<E>) types[0];
		this.transportClazz = (Class<T>) types[1];
	};

	@Override
	protected boolean filter(E event) {
		return transportClazz.getSimpleName().equals(event.getOnTransport());
	}

}
