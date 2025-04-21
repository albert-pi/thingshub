package io.thingshub.transport.mqtt;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.core.date.DateUtil;
import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.domain.Retain;
import io.thingshub.service.RetainService;
import io.thingshub.topic.PrefixSearchTree;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * <p>
 * Retain消息管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class RetainManager {

	private final RetainService retainService;

	private final PrefixSearchTree<Retain> prefixSearchTree;

	@Inject
	public RetainManager(RetainService retainService) {
		this.retainService = retainService;
		this.prefixSearchTree = new PrefixSearchTree<>();
	}

	@PostConstruct
	public void init() {
		// load all retains into search tree
		Long startId = 0L;
		while (true) {
			Page<Retain> retainPage = retainService.query(Lists.newArrayList(new Condition("active", true)), startId, 100);
			if (retainPage.getRecords() != null && retainPage.getSize() > 0) {
				retainPage.getRecords().forEach(retain -> prefixSearchTree.insert(retain.getTopic(), retain, false));
				startId = retainPage.getRecords().get(retainPage.getSize() - 1).getId();
				if (retainPage.getSize() < 100) {
					break;
				}
			} else {
				break;
			}
		}
	}

	public void cleanRetain(String topic) {
		retainService.remove(Lists.newArrayList(new Condition("topic", topic)));
		prefixSearchTree.delete(topic);
	}

	public Long renewRetain(String topic, Long messageId) {
		Retain updatedRetain = new Retain();
		updatedRetain.setTopic(topic);
		updatedRetain.setActive(false);
		retainService.updateByConditions(updatedRetain, Lists.newArrayList(new Condition("topic", topic), new Condition("active", true)));

		Retain retain = new Retain();
		retain.setTopic(topic);
		retain.setMessageId(messageId);
		retain.setActive(true);
		retain.setCreateTime(DateUtil.date());
		retainService.save(retain);

		prefixSearchTree.insert(topic, retain, true);

		return retain.getId();
	}

	public Retain getRetain(String topic) {
		return retainService.getOne(Lists.newArrayList(new Condition("topic", topic), new Condition("active", true)));
	}

	public List<Retain> getRetainsByPrefix(String topicPrefix) {
		return prefixSearchTree.searchByPrefix(topicPrefix, Integer.MAX_VALUE);
	}

}