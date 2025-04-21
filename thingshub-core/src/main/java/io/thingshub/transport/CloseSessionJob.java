package io.thingshub.transport;

import io.thingshub.Broker;
import io.thingshub.bus.EventCenter;
import io.thingshub.cluster.compute.RunnableJob;
import io.thingshub.transport.event.CloseSessionEvent;

/**
 * <p>
 * close session job
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class CloseSessionJob implements RunnableJob {

	private static final long serialVersionUID = -5008562394777417425L;

	private final String clientId;

	private final String onTransport;

	public CloseSessionJob(String clientId, String onTransport) {
		this.clientId = clientId;
		this.onTransport = onTransport;
	}

	@Override
	public String getName() {
		return "JOB_CLOSE_CLIENT_" + clientId;
	}

	@Override
	public Boolean isBroadcasted() {
		return true;
	}

	@Override
	public void run() {
		Broker.getBean(EventCenter.class).post(new CloseSessionEvent(clientId, onTransport));
	}

}
