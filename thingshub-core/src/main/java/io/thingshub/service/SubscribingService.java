package io.thingshub.service;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.sql.Condition;
import io.thingshub.domain.Subscribing;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 客户端订阅服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class SubscribingService extends BaseService<Subscribing> {

	public void saveSubscribing(Subscribing subscribing) {
		List<Condition> conditions = Lists.newArrayList( //
				new Condition("client_id", subscribing.getClientId()), //
				new Condition("sub_group", subscribing.getSubGroup()), //
				new Condition("topic", subscribing.getTopic()));
		Subscribing theSubscribing = this.getOne(conditions);
		if (theSubscribing == null) {
			subscribing.setSubTime(DateUtil.date());
			this.save(subscribing);
		} else {
			theSubscribing.setProps(subscribing.getProps());
			theSubscribing.setOnTransport(subscribing.getOnTransport());
			theSubscribing.setSubTime(DateUtil.date());
			this.updateById(theSubscribing);
		}
	}

	public void removeSubscribing(String clientId, String topic) {
		this.remove(Lists.newArrayList(new Condition("client_id", clientId), new Condition("topic", topic)));
	}

	public void removeSubscribings(String clientId) {
		this.remove(Lists.newArrayList(new Condition("client_id", clientId)));
	}

	public Subscribing getSubscribing(String clientId, String group, String topic) {
		return this.getOne(Lists.newArrayList(new Condition("client_id", clientId), new Condition("sub_group", group), new Condition("topic", topic)));
	}

	public List<Subscribing> getSubscribings(String clientId) {
		return this.query(Lists.newArrayList(new Condition("client_id", clientId)));
	}

}