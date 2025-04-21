package io.thingshub.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.db.sql.Condition;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.Product;
import io.thingshub.service.base.BaseService;

/**
 * <p>
 * 产品服务
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

public class ProductService extends BaseService<Product> {

	public Product getByCode(String code) {
		return this.getOne(Lists.newArrayList(new Condition("code", code), new Condition("deleted_status", DeletedStatus.NOT_DELETED.value())));
	}

	public List<Product> getProducts() {
		List<Condition> conditions = Lists.newArrayList(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions);
	}

	public Page<Product> queryProducts(Map<String, Object> params, int page, int size) {
		List<Condition> conditions = params.entrySet().stream().map(entry -> {
			// TODO key到col 名称的映射
			return new Condition(entry.getKey(), entry.getValue());
		}).collect(Collectors.toList());
		conditions.add(new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));

		return this.query(conditions, page, size);
	}

	public Long createProduct(Product newProduct) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("code", newProduct.getCode()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		if (this.getOne(queryConditions) != null) {
			throw new ServiceException("产品编号已存在");
		}

		this.save(newProduct);

		return newProduct.getId();
	}

	public void updateProduct(Product updatedProduct) {
		List<Condition> queryConditions = Lists.newArrayList(new Condition("code", updatedProduct.getCode()),
				new Condition("deleted_status", DeletedStatus.NOT_DELETED.value()));
		Product prod = this.getOne(queryConditions);
		if (prod != null && !prod.getId().equals(updatedProduct.getId())) {
			throw new ServiceException("产品编号已存在");
		}

		this.updateById(updatedProduct);
	}

	public void disable(Long id) {
		Product product = this.getById(id);
		if (product != null) {
			product.setStatus(AvailableStatus.DISABLED.value());

			this.updateById(product);
		}
	}

	public void remove(Long id) {
		Product product = this.getById(id);
		if (product != null) {
			product.setDeletedStatus(DeletedStatus.DELETED.value());

			this.updateById(product);
		}
	}

}
