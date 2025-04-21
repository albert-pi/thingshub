package io.thingshub.service;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.sql.Condition;
import io.thingshub.domain.Inbox;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 收件箱
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class InboxService extends BaseService<Inbox> {

	public boolean deliverMessage(String publisher, String recipient, Long messageSeq) {
		Inbox inbox = this.getOne(Lists.newArrayList(new Condition("recipient", recipient), new Condition("message_seq", messageSeq)));
		if (inbox != null) {
			return false;
		}

		inbox = new Inbox();
		inbox.setPublisher(publisher);
		inbox.setRecipient(recipient);
		inbox.setMessageSeq(messageSeq);
		inbox.setDeliverTime(DateUtil.date());
		inbox.setAcked(false);
		this.save(inbox);

		return true;
	}

	public void ackDelivery(String recipient, Long messageSeq) {
		Inbox inbox = this.getOne(Lists.newArrayList(new Condition("recipient", recipient), new Condition("message_seq", messageSeq)));
		if (inbox != null) {
			inbox.setAcked(true);
			inbox.setAckTime(DateUtil.date());
			this.updateById(inbox);
		}
	}

	public void cleanUnackedDeliveries(String clientId) {
		this.remove(Lists.newArrayList(new Condition("recipient", clientId), new Condition("acked", 0)));
	}

	public List<Inbox> queryUnackedDeliveries(String clientId) {
		return this.query(Lists.newArrayList(new Condition("recipient", clientId), new Condition("acked", 0)));
	}

}