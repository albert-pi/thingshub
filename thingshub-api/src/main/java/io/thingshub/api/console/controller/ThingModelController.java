package io.thingshub.api.console.controller;

import java.util.List;

import com.alibaba.fastjson2.JSON;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.api.console.params.ThingModelRequestParams.ThingEventFormParams;
import io.thingshub.api.console.params.ThingModelRequestParams.ThingPropertyFormParams;
import io.thingshub.api.console.params.ThingModelRequestParams.ThingServiceFormParams;
import io.thingshub.commons.LongId;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.ThingModel;
import io.thingshub.service.ThingModelService;
import io.thingshub.service.base.BaseService.DeletedStatus;
import io.thingshub.service.model.ThingModelType;
import io.thingshub.transport.http.HttpMethod;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import jakarta.inject.Inject;

/**
 * <p>
 * 产品物模型管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class ThingModelController {

	@Inject
	private ThingModelService thingModelService;

	@RequestMapping(path = "/thing-model/list")
	public List<ThingModel> listThingModels(@RequestParam String productCode) {
		return thingModelService.getThingModels(productCode);
	}

	@RequestMapping(path = "/thing-model/info")
	public ThingModel getThingModel(@RequestParam Long id) {
		return thingModelService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/thing-property/save")
	public LongId saveThingProperty(@RequestBody ThingPropertyFormParams params) {
		String user = "admin";

		Long thingModelId = params.getId();
		if (thingModelId == null) {
			ThingModel thingModel = new ThingModel();
			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));

			thingModel.setType(ThingModelType.PROPERTY.name());
			thingModel.setDataType(JSON.toJSONString(params.getDataType()));
			thingModel.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			thingModel.setCreateBy(user);
			thingModel.setCreateTime(DateUtil.date());
			thingModelId = thingModelService.createThingModel(thingModel);
		} else {
			ThingModel thingModel = thingModelService.getById(params.getId());
			if (thingModel == null) {
				throw new ServiceException("无效的物模型ID");
			}

			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));
			thingModel.setDataType(JSON.toJSONString(params.getDataType()));
			thingModelService.updateThingModel(thingModel);
		}

		return new LongId(thingModelId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/thing-service/save")
	public LongId saveThingService(@RequestBody ThingServiceFormParams params) {
		String user = "admin";

		Long thingModelId = params.getId();
		if (thingModelId == null) {
			ThingModel thingModel = new ThingModel();
			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));

			thingModel.setType(ThingModelType.SERVICE.name());
			if (params.getInput() != null) {
				thingModel.setInput(JSON.toJSONString(params.getInput()));
			}
			if (params.getOutput() != null) {
				thingModel.setOutput(JSON.toJSONString(params.getOutput()));
			}

			thingModel.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			thingModel.setCreateBy(user);
			thingModel.setCreateTime(DateUtil.date());
			thingModelId = thingModelService.createThingModel(thingModel);
		} else {
			ThingModel thingModel = thingModelService.getById(params.getId());
			if (thingModel == null) {
				throw new ServiceException("无效的物模型ID");
			}

			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));
			if (params.getInput() != null) {
				thingModel.setInput(JSON.toJSONString(params.getInput()));
			} else {
				thingModel.setInput(null);
			}
			if (params.getOutput() != null) {
				thingModel.setOutput(JSON.toJSONString(params.getOutput()));
			} else {
				thingModel.setOutput(null);
			}
			thingModelService.updateThingModel(thingModel);
		}

		return new LongId(thingModelId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/thing-event/save")
	public LongId saveThingEvent(@RequestBody ThingEventFormParams params) {
		String user = "admin";

		Long thingModelId = params.getId();
		if (thingModelId == null) {
			ThingModel thingModel = new ThingModel();
			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));

			thingModel.setType(ThingModelType.EVENT.name());
			if (params.getOutput() != null) {
				thingModel.setOutput(JSON.toJSONString(params.getOutput()));
			}

			thingModel.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			thingModel.setCreateBy(user);
			thingModel.setCreateTime(DateUtil.date());
			thingModelId = thingModelService.createThingModel(thingModel);
		} else {
			ThingModel thingModel = thingModelService.getById(params.getId());
			if (thingModel == null) {
				throw new ServiceException("无效的物模型ID");
			}

			BeanUtil.copyProperties(params, thingModel, CopyOptions.create().setIgnoreNullValue(true));
			if (params.getOutput() != null) {
				thingModel.setOutput(JSON.toJSONString(params.getOutput()));
			} else {
				thingModel.setOutput(null);
			}
			thingModelService.updateThingModel(thingModel);
		}

		return new LongId(thingModelId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/thing-model/remove")
	public void removeThingModel(@RequestBody IdParam idParam) {
		thingModelService.remove(idParam.getId());
	}

}