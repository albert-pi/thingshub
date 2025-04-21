package io.thingshub.cluster.compute;

import org.apache.ignite.lang.IgniteRunnable;

public interface RunnableJob extends IgniteRunnable {

	String getName();

	Boolean isBroadcasted();

}
