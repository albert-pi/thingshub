package io.thingshub.topic;

import java.util.Map;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicTreeNodeMember {

	private final String subscriberId;

	private final Map<String, Object> props;

	private final String group;

	private final String onTransport;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		TopicTreeNodeMember that = (TopicTreeNodeMember) o;

		return Objects.equals(this.subscriberId, that.subscriberId) && Objects.equals(this.group, that.group) && Objects.equals(this.props, that.props);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.subscriberId, this.props, this.group);
	}
}
