package io.thingshub.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.SysClient;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 系统客户端管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class SysClientService extends BaseService<SysClient> {

	public SysClient getSysClient(String name) {
		return this.getOne(Lists.newArrayList(new Condition("name", name), new Condition("deleted_status", DeletedStatus.NOT_DELETED.value())));
	}

	public Page<SysClient> querySysClients(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			return new Condition(entry.getKey(), entry.getValue());
		}).collect(Collectors.toList());
		conditions.add(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions, page, size);
	}

	public Long createSysClient(SysClient newSysClient) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("name", newSysClient.getName()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(queryConditions) != null) {
			throw new ServiceException("客户端名称已存在");
		}

		this.save(newSysClient);

		return newSysClient.getId();
	}

	public void updateSysClient(SysClient updatedSysClient) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("name", updatedSysClient.getName()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		SysClient theSysClient = this.getOne(queryConditions);
		if (theSysClient != null && !theSysClient.getId().equals(updatedSysClient.getId())) {
			throw new ServiceException("客户端名称已存在");
		}

		this.updateById(updatedSysClient);
	}

	public void disable(Long id) {
		SysClient sysClient = this.getById(id);
		if (sysClient != null) {
			sysClient.setStatus(AvailableStatus.DISABLED.value());

			this.updateById(sysClient);
		}
	}

	public void remove(Long id) {
		SysClient sysClient = this.getById(id);
		if (sysClient != null) {
			sysClient.setDeletedStatus(DeletedStatus.DELETED.value());

			this.updateById(sysClient);
		}
	}

}
