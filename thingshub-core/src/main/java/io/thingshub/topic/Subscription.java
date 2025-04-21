package io.thingshub.topic;

import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class Subscription {

	private final String subscriberId;

	private final String group;

	private final String topicFilter;

	private final Map<String, Object> props;

	private final String onTransport;

	public Subscription(String subscriberId, String group, String topicFilter, Map<String, Object> props, String onTransport) {
		this.subscriberId = subscriberId;
		this.group = group;
		this.topicFilter = topicFilter;
		this.props = props;
		this.onTransport = onTransport;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Subscription that = (Subscription) o;

		return Objects.equals(this.subscriberId, that.subscriberId) && Objects.equals(this.group, that.group)
				&& Objects.equals(this.topicFilter, that.topicFilter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.subscriberId, this.group, this.topicFilter);
	}

	@Override
	public String toString() {
		return "Subscription{clientId='" + this.subscriberId + "', group='" + this.group + "', topicFilter='" + topicFilter + "', props='" + props.toString()
				+ "'}";
	}
}
