package io.thingshub.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.ProductCat;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 产品类别服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ProductCatService extends BaseService<ProductCat> {

	public Page<ProductCat> queryProductCats(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			return new Condition(entry.getKey(), entry.getValue());
		}).collect(Collectors.toList());
		return this.query(conditions, page, size);
	}

	public Long createProductCat(ProductCat productCat) {
		List<Condition> queryCondtions = Lists.newArrayList(new Condition("name", productCat.getName()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		ProductCat theCat = this.getOne(queryCondtions);
		if (theCat != null) {
			throw new ServiceException("品类名称已存在");
		}

		this.save(productCat);

		return productCat.getId();
	}

	public void updateProductCat(ProductCat productCat) {
		List<Condition> queryCondtions = Lists.newArrayList(new Condition("name", productCat.getName()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		ProductCat theCat = this.getOne(queryCondtions);
		if (theCat != null && !theCat.getId().equals(productCat.getId())) {
			throw new ServiceException("品类名称已存在");
		}

		this.updateById(theCat);
	}

}
