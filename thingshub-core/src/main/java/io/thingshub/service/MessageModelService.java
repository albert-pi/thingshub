package io.thingshub.service;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.MessageModel;
import io.thingshub.service.base.BaseService;
import io.thingshub.service.model.StreamDirection;

/**
 * <p>
 * 产品消息模型管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class MessageModelService extends BaseService<MessageModel> {

	public List<MessageModel> getDownMessageModels(String productCode) {
		return this.query(Lists.newArrayList(new Condition("product_code", productCode), new Condition("stream_direction", StreamDirection.DOWN.name()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value())));
	}

	public List<MessageModel> getMessageModels(String productCode) {
		return this.query(Lists.newArrayList(new Condition("product_code", productCode), new Condition("deleted_status", DeletedStatus.NOT_DELETED.value())));
	}

	public MessageModel getMessageModel(String productCode, String name) {
		List<Condition> conditions = Lists.newArrayList(new Condition("product_code", productCode), new Condition("name", name));
		return this.getOne(conditions);
	}

	public Long createMessageModel(MessageModel newMessageModel) {
		List<Condition> checkConditions = Lists.newArrayList( //
				new Condition("product_code", newMessageModel.getProductCode()), //
				new Condition("name", newMessageModel.getName()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(checkConditions) != null) {
			throw new ServiceException("消息名称已存在");
		}

		this.save(newMessageModel);

		return newMessageModel.getId();
	}

	public void updateMessageModel(MessageModel updatedMessageModel) {
		List<Condition> checkConditions = Lists.newArrayList( //
				new Condition("product_code", updatedMessageModel.getProductCode()), //
				new Condition("name", updatedMessageModel.getName()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		MessageModel theMessageModel = this.getOne(checkConditions);
		if (theMessageModel != null && !theMessageModel.getId().equals(updatedMessageModel.getId())) {
			throw new ServiceException("消息名称已存在");
		}

		this.updateById(theMessageModel);
	}

	public void remove(Long id) {
		MessageModel messageModel = this.getById(id);
		if (messageModel != null) {
			messageModel.setDeletedStatus(DeletedStatus.DELETED.value());

			this.updateById(messageModel);
		}
	}

}
