package io.thingshub.api.console.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import io.thingshub.api.console.params.DeviceRequestParams.DeviceFormParams;
import io.thingshub.api.console.params.DeviceRequestParams.DeviceGroupFormParams;
import io.thingshub.api.console.params.DeviceRequestParams.PublishParams;
import io.thingshub.api.console.params.DeviceRequestParams.QueryDeviceGroupParams;
import io.thingshub.api.console.params.DeviceRequestParams.QueryDeviceParams;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.commons.DataOption;
import io.thingshub.commons.LongId;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.Device;
import io.thingshub.domain.DeviceGroup;
import io.thingshub.domain.Product;
import io.thingshub.service.DeviceGroupService;
import io.thingshub.service.DeviceService;
import io.thingshub.service.ProductService;
import io.thingshub.service.base.BaseService.AvailableStatus;
import io.thingshub.service.base.BaseService.DeletedStatus;
import io.thingshub.transport.http.HttpMethod;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import jakarta.inject.Inject;

/**
 * <p>
 * 设备管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class DeviceController {

	@Inject
	private ProductService productService;

	@Inject
	private DeviceService deviceService;

	@Inject
	private DeviceGroupService deviceGroupService;

	@RequestMapping(path = "/device-group/options")
	public List<DataOption> listDeviceGroupOptions() {
		List<DataOption> deviceGroupOptions = Collections.emptyList();

		List<DeviceGroup> deviceGroups = deviceGroupService.list();
		if (CollUtil.isNotEmpty(deviceGroups)) {
			deviceGroupOptions = deviceGroups.stream().map(grp -> {
				return new DataOption(grp.getId() + "", grp.getName());
			}).collect(Collectors.toList());

			deviceGroupOptions.add(0, new DataOption("0", "默认"));
		}

		return deviceGroupOptions;
	}

	@RequestMapping(path = "/device-group/query")
	public Page<DeviceGroup> queryDeviceGroup(QueryDeviceGroupParams params) {
		Map<String, Object> queryParams = new HashMap<>();

		return deviceGroupService.queryDeviceGroups(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/device-group/info")
	public DeviceGroup getDeviceGroup(@RequestParam Long id) {
		return deviceGroupService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/device-group/save")
	public LongId saveDeviceGroup(@RequestBody DeviceGroupFormParams params) {
		String user = "admin";
		Long groupId = params.getId();
		if (groupId == null) {
			DeviceGroup deviceGroup = new DeviceGroup();
			deviceGroup.setName(params.getName());
			deviceGroup.setRemark(params.getRemark());
			deviceGroup.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			deviceGroup.setCreateBy(user);
			deviceGroup.setCreateTime(DateUtil.date());
			groupId = deviceGroupService.createDeviceGroup(deviceGroup);
		} else {
			DeviceGroup deviceGroup = deviceGroupService.getById(params.getId());
			if (deviceGroup == null) {
				throw new ServiceException("无效的分组ID");
			}

			deviceGroup.setName(params.getName());
			deviceGroup.setRemark(params.getRemark());
			deviceGroupService.updateDeviceGroup(deviceGroup);
		}

		return new LongId(groupId);
	}

	@RequestMapping(path = "/device/query")
	public Page<Device> queryDevcie(@RequestBody QueryDeviceParams params) {
		Map<String, Object> queryParams = new HashMap<>();
		return deviceService.queryDevices(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/device/info")
	public Device getDevice(@RequestParam Long id) {
		return deviceService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/device/save")
	public LongId saveDevice(@RequestBody DeviceFormParams params) {
		String user = "admin";

		Product product = productService.getByCode(params.getProductCode());
		if (product == null) {
			throw new ServiceException("产品不存在");
		}

		DeviceGroup deviceGroup = null;
		if (params.getGroupId() != null) {
			if (params.getGroupId() == 0) {
				deviceGroup = new DeviceGroup();
				deviceGroup.setId(0L);
				deviceGroup.setName("默认");
			} else {
				deviceGroup = deviceGroupService.getById(params.getGroupId());
			}
		}

		Long deviceId = params.getId();
		if (deviceId == null) {
			Device device = new Device();
			BeanUtil.copyProperties(params, device, CopyOptions.create().setIgnoreNullValue(true));

			device.setProductName(product.getName());

			if (deviceGroup != null) {
				device.setGroupId(deviceGroup.getId());
				device.setGroupName(deviceGroup.getName());
			}

			device.setConnectState(0);
			device.setFaultState(0);
			device.setStatus(AvailableStatus.NORMAL.value());
			device.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			device.setCreateBy(user);
			device.setCreateTime(DateUtil.date());
			deviceId = deviceService.createDevice(device);
		} else {
			Device device = deviceService.getById(params.getId());
			if (device == null) {
				throw new ServiceException("无效的设备ID");
			}

			BeanUtil.copyProperties(params, device, CopyOptions.create().setIgnoreNullValue(true));
			device.setProductName(product.getName());
			if (deviceGroup != null) {
				device.setGroupId(deviceGroup.getId());
				device.setGroupName(deviceGroup.getName());
			}

			deviceService.updateDevice(device);
		}

		return new LongId(deviceId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/device/publish")
	public void publish(@RequestBody PublishParams params) {

	}

	@RequestMapping(method = HttpMethod.POST, path = "/device/disable")
	public void disable(@RequestBody IdParam param) {
		deviceService.disable(param.getId());
	}

	@RequestMapping(method = HttpMethod.POST, path = "/device/remove")
	public void remove(@RequestBody IdParam param) {
		deviceService.remove(param.getId());
	}

}