package io.thingshub.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.domain.ServerScript;
import io.thingshub.service.base.BaseService;
import io.thingshub.service.model.ServerScriptDetails;
import jakarta.inject.Inject;

/**
 * <p>
 * Transport Server协议转换脚本管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ServerScriptService extends BaseService<ServerScript> {

	private ScriptInfoService scriptInfoService;

	@Inject
	public ServerScriptService(ScriptInfoService scriptInfoService) {
		this.scriptInfoService = scriptInfoService;
	}

	public Page<ServerScript> queryServerScripts(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			return new Condition(entry.getKey(), entry.getValue());
		}).collect(Collectors.toList());
		conditions.add(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions, page, size);
	}

	public ServerScriptDetails getServerScriptDetails(Long serverScriptId) {
		ServerScript serverScript = this.getById(serverScriptId);
		if (serverScript == null || serverScript.getDeletedStatus() == DeletedStatus.DELETED.value()) {
			return null;
		}

		ServerScriptDetails serverScriptDetails = new ServerScriptDetails();
		BeanUtil.copyProperties(serverScript, serverScriptDetails, CopyOptions.create().setIgnoreNullValue(true));

		ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(serverScript.getServerName());
		serverScriptDetails.setScriptLang(theScriptInfo.getLang());
		serverScriptDetails.setScriptContent(theScriptInfo.getContent());

		return serverScriptDetails;
	}

	public Long createServerScript(ServerScriptDetails serverScriptDetails) {
		List<Condition> checkConditions = Lists.newArrayList( //
				new Condition("server_name", serverScriptDetails.getServerName()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(checkConditions) != null) {
			throw new ServiceException("Transport Server协议脚本已存在");
		}

		ScriptInfo scriptInfo = new ScriptInfo();
		scriptInfo.setCode(serverScriptDetails.getServerName());
		scriptInfo.setLang(serverScriptDetails.getScriptLang());
		scriptInfo.setContent(serverScriptDetails.getScriptContent());
		scriptInfo.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
		scriptInfoService.save(scriptInfo);

		ServerScript newTransportScript = new ServerScript();
		BeanUtil.copyProperties(serverScriptDetails, newTransportScript, CopyOptions.create().setIgnoreNullValue(true));
		newTransportScript.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
		this.save(newTransportScript);

		return newTransportScript.getId();
	}

	public void updateServerScript(ServerScriptDetails serverScriptDetails) {
		List<Condition> queryConditions = Lists.newArrayList( //
				new Condition("server_name", serverScriptDetails.getServerName()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		ServerScript transportScript = this.getOne(queryConditions);
		if (transportScript != null && !transportScript.getId().equals(serverScriptDetails.getId())) {
			throw new ServiceException("Transport Server协议脚本已存在");
		}

		ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(serverScriptDetails.getServerName());
		theScriptInfo.setLang(serverScriptDetails.getScriptLang());
		theScriptInfo.setContent(serverScriptDetails.getScriptContent());
		scriptInfoService.updateById(theScriptInfo);

		BeanUtil.copyProperties(serverScriptDetails, transportScript, CopyOptions.create().setIgnoreNullValue(true));
		this.updateById(transportScript);
	}

	public void remove(Long id) {
		ServerScript transportScript = this.getById(id);
		if (transportScript != null) {
			transportScript.setDeletedStatus(DeletedStatus.DELETED.value());
			this.updateById(transportScript);

			ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(transportScript.getServerName());
			theScriptInfo.setDeletedStatus(DeletedStatus.DELETED.value());
			scriptInfoService.updateById(theScriptInfo);
		}
	}

}
