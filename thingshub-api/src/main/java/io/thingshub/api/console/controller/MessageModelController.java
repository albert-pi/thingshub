package io.thingshub.api.console.controller;

import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.api.console.params.MessageModelRequestParams.MessageModelFormParams;
import io.thingshub.commons.LongId;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.MessageModel;
import io.thingshub.service.MessageModelService;
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
 * 产品消息模型管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class MessageModelController {

	@Inject
	private MessageModelService messageModelService;

	@RequestMapping(path = "/message-model/list")
	public List<MessageModel> listMessageModels(@RequestParam String productCode) {
		return messageModelService.getMessageModels(productCode);
	}

	@RequestMapping(path = "/message-model/info")
	public MessageModel getMessageModel(@RequestParam Long id) {
		return messageModelService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/message-model/save")
	public LongId saveMessageModel(@RequestBody MessageModelFormParams params) {
		String user = "admin";

		Long messageModelId = params.getId();
		if (messageModelId == null) {
			MessageModel messageModel = new MessageModel();
			BeanUtil.copyProperties(params, messageModel, CopyOptions.create().setIgnoreNullValue(true));

			messageModel.setParamType(ThingModelType.PROPERTY.name());
			messageModel.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			messageModel.setCreateBy(user);
			messageModel.setCreateTime(DateUtil.date());
			messageModelId = messageModelService.createMessageModel(messageModel);
		} else {
			MessageModel messageModel = messageModelService.getById(params.getId());
			if (messageModel == null) {
				throw new ServiceException("无效的消息模型ID");
			}

			BeanUtil.copyProperties(params, messageModel, CopyOptions.create().setIgnoreNullValue(true));
			messageModelService.updateMessageModel(messageModel);
		}

		return new LongId(messageModelId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/message-model/remove")
	public void remove(@RequestBody IdParam idParam) {
		messageModelService.remove(idParam.getId());
	}

}