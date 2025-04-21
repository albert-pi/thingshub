package io.thingshub.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.Device;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 设备信息服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class DeviceService extends BaseService<Device> {

	public boolean auth(String sn, String userName, String secret) {
		List<Condition> conditions = Lists.newArrayList(new Condition("sn", sn), new Condition("user_name", userName), new Condition("secret", secret),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.getOne(conditions) == null ? false : true;
	}

	public Device getBySn(String sn) {
		return this.getOne(Lists.newArrayList(new Condition("sn", sn)));
	}

	public Page<Device> queryDevices(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			return new Condition(entry.getKey(), entry.getValue());
		}).collect(Collectors.toList());
		conditions.add(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions, page, size);
	}

	public Long createDevice(Device device) {
		List<Condition> checkConditions = Lists.newArrayList(new Condition("sn", device.getSn()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(checkConditions) != null) {
			throw new ServiceException("设备编号已存在");
		}

		this.save(device);

		return device.getId();
	}

	public void updateDevice(Device device) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("sn", device.getSn()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		Device theDevice = this.getOne(queryConditions);
		if (theDevice != null && !theDevice.getId().equals(device.getId())) {
			throw new ServiceException("设备编号已存在");
		}

		this.updateById(device);
	}

	public void disable(Long id) {
		Device device = this.getById(id);
		if (device != null) {
			device.setStatus(AvailableStatus.DISABLED.value());

			this.updateById(device);
		}
	}

	public void remove(Long id) {
		Device device = this.getById(id);
		if (device != null) {
			device.setDeletedStatus(DeletedStatus.DELETED.value());

			this.updateById(device);
		}
	}

}
