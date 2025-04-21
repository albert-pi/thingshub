package io.thingshub.api.console.controller;

import java.util.HashMap;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.api.console.params.SysClientRequestParams.QuerySysClientParams;
import io.thingshub.api.console.params.SysClientRequestParams.SysClientFormParams;
import io.thingshub.commons.LongId;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.SysClient;
import io.thingshub.service.SysClientService;
import io.thingshub.service.base.BaseService.AvailableStatus;
import io.thingshub.service.base.BaseService.DeletedStatus;
import io.thingshub.transport.http.HttpMethod;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import io.thingshub.transport.http.annotation.Validated;
import jakarta.inject.Inject;

/**
 * <p>
 * 系统客户端管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class SysClientController {

	@Inject
	private SysClientService sysClientService;

	@RequestMapping(path = "/sys-client/query")
	public Page<SysClient> querySysClients(@Validated QuerySysClientParams params) {
		Map<String, Object> queryParams = new HashMap<>();

		return sysClientService.querySysClients(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/sys-client/info")
	public SysClient getSysClient(@RequestParam Long id) {
		return sysClientService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/sys-client/save")
	public LongId saveSysClient(@Validated @RequestBody SysClientFormParams params) {
		String user = "admin";

		Long sysClientId = params.getId();
		if (sysClientId == null) {
			SysClient sysClient = new SysClient();
			BeanUtil.copyProperties(params, sysClient, CopyOptions.create().setIgnoreNullValue(true));

			sysClient.setStatus(AvailableStatus.NORMAL.value());
			sysClient.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			sysClient.setCreateBy(user);
			sysClient.setCreateTime(DateUtil.date());
			sysClientId = sysClientService.createSysClient(sysClient);
		} else {
			SysClient sysClient = sysClientService.getById(params.getId());
			if (sysClient == null) {
				throw new ServiceException("无效的客户端ID");
			}

			BeanUtil.copyProperties(params, sysClient, CopyOptions.create().setIgnoreNullValue(true));
			sysClientService.updateSysClient(sysClient);
		}

		return new LongId(sysClientId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/sys-client/disable")
	public void disable(@Validated @RequestBody IdParam idParam) {
		sysClientService.disable(idParam.getId());
	}

	@RequestMapping(method = HttpMethod.POST, path = "/sys-client/remove")
	public void remove(@Validated @RequestBody IdParam idParam) {
		sysClientService.remove(idParam.getId());
	}

}