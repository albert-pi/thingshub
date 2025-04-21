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
import io.thingshub.domain.ProductScript;
import io.thingshub.domain.ScriptInfo;
import io.thingshub.service.base.BaseService;
import io.thingshub.service.model.ProductScriptDetails;
import jakarta.inject.Inject;

/**
 * <p>
 * 设备通信协议转换脚本管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ProductScriptService extends BaseService<ProductScript> {

	private ScriptInfoService scriptInfoService;

	@Inject
	public ProductScriptService(ScriptInfoService scriptInfoService) {
		this.scriptInfoService = scriptInfoService;
	}

	public Page<ProductScript> queryProductScripts(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			if (entry.getKey().equals("productCode")) {
				return new Condition("product_code", entry.getValue());
			}
			return null;
		}).collect(Collectors.toList());
		conditions.add(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions, page, size);
	}

	public ProductScriptDetails getProductScriptDetails(Long id) {
		ProductScript prodScript = this.getById(id);
		if (prodScript == null || prodScript.getDeletedStatus() == DeletedStatus.DELETED.value()) {
			return null;
		}

		ProductScriptDetails productScriptDetails = new ProductScriptDetails();
		BeanUtil.copyProperties(prodScript, productScriptDetails, CopyOptions.create().setIgnoreNullValue(true));

		ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(prodScript.getProductCode());
		productScriptDetails.setScriptLang(theScriptInfo.getLang());
		productScriptDetails.setScriptContent(theScriptInfo.getContent());

		return productScriptDetails;
	}

	public Long createProductScript(ProductScriptDetails productScriptDetails) {
		List<Condition> queryConditions = Lists.newArrayList( //
				new Condition("product_code", productScriptDetails.getProductCode()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(queryConditions) != null) {
			throw new ServiceException("该型号产品协议脚本已存在");
		}

		ScriptInfo scriptInfo = new ScriptInfo();
		scriptInfo.setCode(productScriptDetails.getProductCode());
		scriptInfo.setLang(productScriptDetails.getScriptLang());
		scriptInfo.setContent(productScriptDetails.getScriptContent());
		scriptInfo.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
		scriptInfoService.save(scriptInfo);

		ProductScript newProductScript = new ProductScript();
		BeanUtil.copyProperties(productScriptDetails, newProductScript, CopyOptions.create().setIgnoreNullValue(true));
		newProductScript.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
		this.save(newProductScript);

		return newProductScript.getId();
	}

	public void updateProductScript(ProductScriptDetails productScriptDetails) {
		List<Condition> queryConditions = Lists.newArrayList( //
				new Condition("product_code", productScriptDetails.getProductCode()), //
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		ProductScript productScript = this.getOne(queryConditions);
		if (productScript != null && !productScript.getId().equals(productScriptDetails.getId())) {
			throw new ServiceException("该型号产品协议脚本已存在");
		}

		ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(productScriptDetails.getProductCode());
		theScriptInfo.setLang(productScriptDetails.getScriptLang());
		theScriptInfo.setContent(productScriptDetails.getScriptContent());
		scriptInfoService.updateById(theScriptInfo);

		BeanUtil.copyProperties(productScriptDetails, productScript, CopyOptions.create().setIgnoreNullValue(true));
		this.updateById(productScript);
	}

	public void remove(Long id) {
		ProductScript productScript = this.getById(id);
		if (productScript != null) {
			productScript.setDeletedStatus(DeletedStatus.DELETED.value());
			this.updateById(productScript);

			ScriptInfo theScriptInfo = scriptInfoService.getScriptInfo(productScript.getProductCode());
			theScriptInfo.setDeletedStatus(DeletedStatus.DELETED.value());
			scriptInfoService.updateById(theScriptInfo);
		}
	}

}
