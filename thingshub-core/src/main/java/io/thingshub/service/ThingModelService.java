package io.thingshub.service;

import java.util.List;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.ThingModel;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 产品物模型管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ThingModelService extends BaseService<ThingModel> {

	public List<ThingModel> getThingModels(String productCode) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("product_code", productCode));

		return this.query(queryConditions);
	}

	public ThingModel getThingModel(String productCode, String identifier) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("product_code", productCode), new Condition("identifier", identifier));

		return this.getOne(queryConditions);
	}

	public Long createThingModel(ThingModel newThingModel) {
		List<Condition> checkConditions = Lists.newArrayList( //
				new Condition("product_code", newThingModel.getProductCode()), //
				new Condition("identifier", newThingModel.getIdentifier()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(checkConditions) != null) {
			throw new ServiceException("标识符已存在");
		}

		this.save(newThingModel);

		return newThingModel.getId();
	}

	public void updateThingModel(ThingModel updatedThingModel) {
		List<Condition> checkConditions = Lists.newArrayList( //
				new Condition("product_code", updatedThingModel.getProductCode()), //
				new Condition("identifier", updatedThingModel.getIdentifier()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		ThingModel theThingModel = this.getOne(checkConditions);
		if (theThingModel != null && !theThingModel.getId().equals(updatedThingModel.getId())) {
			throw new ServiceException("标识符已存在");
		}

		this.updateById(theThingModel);
	}

	public void remove(Long id) {
		ThingModel thingModel = this.getById(id);
		if (thingModel != null) {
			thingModel.setDeletedStatus(DeletedStatus.DELETED.value());

			this.updateById(thingModel);
		}
	}

}
