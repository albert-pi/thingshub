package io.thingshub.bus;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.eventbus.Subscribe;

/**
 * <p>
 * Event listener which listens event from broker event bus.
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public abstract class EventListener<E> {

	protected Class<E> eventClazz;

	@SuppressWarnings("unchecked")
	public EventListener() {
		Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
		this.eventClazz = (Class<E>) types[0];
	};

	@Subscribe
	private void accept(E e) {
		if (this.eventClazz == e.getClass()) {
			if (this.filter(e)) {
				onEvent(e);
			}
		}
	}

	protected boolean filter(E event) {
		return true;
	}

	protected abstract void onEvent(E event);

}
