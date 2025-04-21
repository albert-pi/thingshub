package io.thingshub.transport.throttler;

public enum ResourceType {
	TotalConnections, //
	TotalSessionMemoryBytes, //
	TotalPersistentSessions, //
	TotalPersistentSessionSpaceBytes, //
	TotalSharedSubscriptions, //
	TotalTransientSubscriptions, //
	TotalPersistentSubscriptions, //
	TotalRetainMessageSpaceBytes, //
	TotalRetainTopics, //
	TotalConnectPerSecond, //
	TotalInboundBytesPerSecond, //
	TotalTransientSubscribePerSecond, //
	TotalPersistentSubscribePerSecond, //
	TotalTransientUnsubscribePerSecond, //
	TotalPersistentUnsubscribePerSecond, //
	TotalTransientFanOutBytesPerSeconds, //
	TotalPersistentFanOutBytesPerSeconds, //
	TotalRetainedMessagesPerSeconds, //
	TotalRetainedBytesPerSecond, //
	TotalRetainMatchPerSeconds, //
	TotalRetainMatchBytesPerSecond,
}