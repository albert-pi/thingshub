package io.thingshub.transport;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import io.thingshub.Broker;
import io.thingshub.bus.EventCenter;
import io.thingshub.cluster.compute.RunnableJob;
import io.thingshub.domain.Message;
import io.thingshub.service.MessageService;
import io.thingshub.topic.SubscriptionManager;
import io.thingshub.topic.TopicTreeNodeMember;
import io.thingshub.transport.event.DistributeEvent;

/**
 * <p>
 * 消息订阅匹配与分发
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class DistributeJob implements RunnableJob {

	private static final long serialVersionUID = -1710580244118232250L;

	private final MessageService messageService;

	private final SubscriptionManager subscriptionManager;

	private final EventCenter eventCenter;

	private final Long messageSeq;

	public DistributeJob(Long messageSeq) {
		this.messageSeq = messageSeq;
		this.messageService = Broker.getBean(MessageService.class);
		this.subscriptionManager = Broker.getBean(SubscriptionManager.class);
		this.eventCenter = Broker.getBean(EventCenter.class);
	}

	@Override
	public String getName() {
		return "DISTRIBUTE_MESSAGE_" + messageSeq;
	}

	@Override
	public Boolean isBroadcasted() {
		return true;
	}

	@Override
	public void run() {
		Message message = messageService.getById(messageSeq);
		Set<TopicTreeNodeMember> matchedNodeMembers = subscriptionManager.match(message.getTopic());
		if (CollUtil.isNotEmpty(matchedNodeMembers)) {
			matchedNodeMembers.parallelStream().collect(Collectors.groupingBy(m -> m.getGroup() == null ? "non-group" : m.getGroup()))
					.forEach((grp, members) -> {
						if ("non-grouped".equals(grp)) {
							members.parallelStream().forEach(member -> {
								eventCenter.post(new DistributeEvent(message, member.getSubscriberId(), member.getProps(), member.getOnTransport()));
							});
						} else {
							subscriptionManager.putGroupMessageId(grp + "_" + messageSeq, messageSeq);

							List<TopicTreeNodeMember> membersInGroup = subscriptionManager.getMembersInGroup(grp);
							if (CollUtil.isNotEmpty(membersInGroup)) {
								Long theMessageId = subscriptionManager.pollGroupMessageId(grp + "_" + messageSeq);
								if (theMessageId != null) {// current node hold the message
									String k = message.getTopic() + DateUtil.current();
									int index = Math.abs(k.hashCode()) % membersInGroup.size();
									TopicTreeNodeMember anySub = membersInGroup.get(index);
									eventCenter.post(new DistributeEvent(message, anySub.getSubscriberId(), anySub.getProps(), anySub.getOnTransport()));
								}
							}
						}
					});
		}
	}

}
