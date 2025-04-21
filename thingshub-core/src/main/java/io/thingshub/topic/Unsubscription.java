package io.thingshub.topic;

import java.util.Objects;

import lombok.Data;

@Data
public class Unsubscription {

	private final String clientId;

	private final String group;

	private final String topicFilter;

	private final String onTransport;

	public Unsubscription(String clientId, String group, String topicFilter, String onTransport) {
		this.clientId = clientId;
		this.group = group;
		this.topicFilter = topicFilter;
		this.onTransport = onTransport;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Unsubscription that = (Unsubscription) o;

		return Objects.equals(this.clientId, that.clientId) && Objects.equals(this.group, that.group) && Objects.equals(this.topicFilter, that.topicFilter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.clientId, this.group, this.topicFilter);
	}

	@Override
	public String toString() {
		return "Unsubscription{clientId='" + this.clientId + "', group='" + this.group + "', topicFilter='" + topicFilter + "'}";
	}
}
