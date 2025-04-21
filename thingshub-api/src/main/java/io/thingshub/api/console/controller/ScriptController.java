package io.thingshub.api.console.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import io.thingshub.Broker;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.api.console.params.ScriptRequestParams.ProductScriptFormParams;
import io.thingshub.api.console.params.ScriptRequestParams.QueryProductScriptParams;
import io.thingshub.api.console.params.ScriptRequestParams.QueryServerScriptParams;
import io.thingshub.api.console.params.ScriptRequestParams.ServerScriptFormParams;
import io.thingshub.commons.DataOption;
import io.thingshub.commons.LongId;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.ProductScript;
import io.thingshub.domain.ServerScript;
import io.thingshub.script.ScriptType;
import io.thingshub.service.ProductScriptService;
import io.thingshub.service.ServerScriptService;
import io.thingshub.service.base.BaseService.AvailableStatus;
import io.thingshub.service.model.ProductScriptDetails;
import io.thingshub.service.model.ServerScriptDetails;
import io.thingshub.transport.http.HttpMethod;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import jakarta.inject.Inject;

/**
 * <p>
 * 脚本管理接口
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class ScriptController {

	@Inject
	private ServerScriptService serverScriptService;

	@Inject
	private ProductScriptService productScriptService;

	@RequestMapping(path = "/transport-server/options")
	public List<DataOption> listTransportServerOptions() {
		List<DataOption> options = Lists.newArrayList();

		for (String serverName : Broker.getServerNames()) {
			options.add(new DataOption(serverName, serverName));
		}

		return options;
	}

	@RequestMapping(path = "/script-type/options")
	public List<DataOption> listScriptTypeOptions() {
		List<DataOption> options = Lists.newArrayList();

		for (ScriptType scriptType : ScriptType.values()) {
			options.add(new DataOption(scriptType.lang(), scriptType.title()));
		}

		return options;
	}

	@RequestMapping(path = "/server-script/query")
	public Page<ServerScript> queryServerScripts(QueryServerScriptParams params) {
		Map<String, Object> queryParams = new HashMap<>();
		return serverScriptService.queryServerScripts(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/server-script/details")
	public ServerScriptDetails getServerScriptDetails(@RequestParam Long id) {
		return serverScriptService.getServerScriptDetails(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/server-script/save")
	public LongId saveServerScript(@RequestBody ServerScriptFormParams params) {
		String user = "admin";

		Long serverScriptId = params.getId();
		if (serverScriptId == null) {
			ServerScriptDetails serverScriptDetails = new ServerScriptDetails();
			BeanUtil.copyProperties(params, serverScriptDetails, CopyOptions.create().setIgnoreNullValue(true));

			serverScriptDetails.setStatus(AvailableStatus.NORMAL.value());
			serverScriptDetails.setCreateBy(user);
			serverScriptDetails.setCreateTime(DateUtil.date());
			serverScriptDetails.setUpdateBy(user);
			serverScriptDetails.setUpdateTime(DateUtil.date());
			serverScriptId = serverScriptService.createServerScript(serverScriptDetails);
		} else {
			ServerScriptDetails serverScriptDetails = serverScriptService.getServerScriptDetails(params.getId());
			if (serverScriptDetails == null) {
				throw new ServiceException("无效的Transport Server脚本ID");
			}

			BeanUtil.copyProperties(params, serverScriptDetails, CopyOptions.create().setIgnoreNullValue(true));
			serverScriptDetails.setUpdateBy(user);
			serverScriptDetails.setUpdateTime(DateUtil.date());
			serverScriptService.updateServerScript(serverScriptDetails);
		}

		return new LongId(serverScriptId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/server-script/remove")
	public void removeServerScript(@RequestBody IdParam idParam) {
		serverScriptService.remove(idParam.getId());
	}

	@RequestMapping(path = "/product-script/query")
	public Page<ProductScript> queryProductScripts(QueryProductScriptParams params) {
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("productCode", params.getProductCode());

		return productScriptService.queryProductScripts(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/product-script/details")
	public ProductScriptDetails getProductScriptDetails(@RequestParam Long id) {
		return productScriptService.getProductScriptDetails(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product-script/save")
	public LongId saveProductScript(@RequestBody ProductScriptFormParams params) {
		String user = "admin";

		Long productScriptId = params.getId();
		if (productScriptId == null) {
			ProductScriptDetails productScriptDetails = new ProductScriptDetails();
			BeanUtil.copyProperties(params, productScriptDetails, CopyOptions.create().setIgnoreNullValue(true));

			productScriptDetails.setStatus(AvailableStatus.NORMAL.value());
			productScriptDetails.setCreateBy(user);
			productScriptDetails.setCreateTime(DateUtil.date());
			productScriptDetails.setUpdateBy(user);
			productScriptDetails.setUpdateTime(DateUtil.date());
			productScriptId = productScriptService.createProductScript(productScriptDetails);
		} else {
			ProductScriptDetails productScriptDetails = new ProductScriptDetails();
			BeanUtil.copyProperties(params, productScriptDetails, CopyOptions.create().setIgnoreNullValue(true));

			productScriptDetails.setUpdateBy(user);
			productScriptDetails.setUpdateTime(DateUtil.date());
			productScriptService.updateProductScript(productScriptDetails);
		}

		return new LongId(productScriptId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product-script/remove")
	public void removeProductScript(@RequestBody IdParam idParam) {
		productScriptService.remove(idParam.getId());
	}

}