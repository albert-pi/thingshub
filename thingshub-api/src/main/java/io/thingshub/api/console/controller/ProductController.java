package io.thingshub.api.console.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import io.thingshub.api.console.params.IdParam;
import io.thingshub.api.console.params.ProductRequestParams.ProductCatFormParams;
import io.thingshub.api.console.params.ProductRequestParams.ProductFormParams;
import io.thingshub.api.console.params.ProductRequestParams.QueryProductCatParams;
import io.thingshub.api.console.params.ProductRequestParams.QueryProductParams;
import io.thingshub.commons.DataOption;
import io.thingshub.commons.LongId;
import io.thingshub.commons.Page;
import io.thingshub.commons.ServiceException;
import io.thingshub.domain.Product;
import io.thingshub.domain.ProductCat;
import io.thingshub.service.ProductCatService;
import io.thingshub.service.ProductService;
import io.thingshub.service.ThingModelService;
import io.thingshub.service.base.BaseService.AvailableStatus;
import io.thingshub.service.base.BaseService.DeletedStatus;
import io.thingshub.service.model.ProductNetMode;
import io.thingshub.service.model.ProductNodeType;
import io.thingshub.transport.TransportType;
import io.thingshub.transport.http.HttpMethod;
import io.thingshub.transport.http.annotation.Controller;
import io.thingshub.transport.http.annotation.RequestBody;
import io.thingshub.transport.http.annotation.RequestMapping;
import io.thingshub.transport.http.annotation.RequestParam;
import jakarta.inject.Inject;

/**
 * <p>
 * 产品管理
 * </p>
 *
 * @author albert pi
 * @since 1.0.0
 */

@Controller
public class ProductController {

	@Inject
	private ProductCatService productCatService;

	@Inject
	private ProductService productService;

	@Inject
	private ThingModelService thingModelService;

	@RequestMapping(path = "/product-cat/options")
	public List<DataOption> listProductCatOptions() {
		List<DataOption> options = Collections.emptyList();

		List<ProductCat> productCats = productCatService.list();
		if (CollUtil.isNotEmpty(productCats)) {
			options = productCats.stream().map(cat -> {
				return new DataOption(cat.getId() + "", cat.getName());
			}).collect(Collectors.toList());
		}

		return options;
	}

	@RequestMapping(path = "/product-cat/query")
	public Page<ProductCat> queryProductCat(QueryProductCatParams params) {
		Map<String, Object> queryParams = new HashMap<>();

		return productCatService.queryProductCats(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(path = "/product-cat/info")
	public ProductCat getProductCat(@RequestParam Long id) {
		return productCatService.getById(id);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product-cat/save")
	public LongId saveProductCat(@RequestBody ProductCatFormParams params) {
		String user = "admin";
		Long catId = params.getId();
		if (catId == null) {
			ProductCat productCat = new ProductCat();
			productCat.setName(params.getName());
			productCat.setRemark(params.getRemark());
			productCat.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			productCat.setCreateBy(user);
			productCat.setCreateTime(DateUtil.date());
			catId = productCatService.createProductCat(productCat);
		} else {
			ProductCat productCat = productCatService.getById(params.getId());
			if (productCat == null) {
				throw new ServiceException("无效的产品品类ID");
			}

			productCat.setName(params.getName());
			productCat.setRemark(params.getRemark());
			productCatService.updateProductCat(productCat);
		}

		return new LongId(catId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/node-type/options")
	public List<DataOption> listProductNodeTypeOptions() {
		List<DataOption> options = Lists.newArrayList();

		for (ProductNodeType productNodeType : ProductNodeType.values()) {
			options.add(new DataOption(productNodeType.type() + "", productNodeType.title()));
		}

		return options;
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/net-mode/options")
	public List<DataOption> listProductNetModeOptions() {
		List<DataOption> options = Lists.newArrayList();

		for (ProductNetMode netMode : ProductNetMode.values()) {
			options.add(new DataOption(netMode.type() + "", netMode.title()));
		}

		return options;
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/transport/options")
	public List<DataOption> listProductTransportOptions() {
		List<DataOption> options = Lists.newArrayList();

		for (TransportType transportType : TransportType.values()) {
			options.add(new DataOption(transportType.name() + "", transportType.desc()));
		}

		return options;
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/options")
	public List<DataOption> listProductOptions() {
		List<DataOption> options = Collections.emptyList();

		List<Product> products = productService.list();
		if (CollUtil.isNotEmpty(products)) {
			options = products.stream().map(p -> {
				return new DataOption(p.getCode(), p.getName());
			}).collect(Collectors.toList());
		}

		return options;
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/query")
	public Page<Product> queryProduct(QueryProductParams params) {
		Map<String, Object> queryParams = new HashMap<>();
		return productService.queryProducts(queryParams, params.getPage(), params.getSize());
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/info")
	public Product getProduct(@RequestBody IdParam param) {
		return productService.getById(param.getId());
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/save")
	public LongId saveProduct(@RequestBody ProductFormParams params) {
		String user = "admin";
		ProductCat productCat = productCatService.getById(params.getCatId());
		if (productCat == null) {
			throw new ServiceException("无效的产品品类");
		}

		Long productId = params.getId();
		if (productId == null) {
			Product product = new Product();
			BeanUtil.copyProperties(params, product, CopyOptions.create().setIgnoreNullValue(true));

			product.setCatName(productCat.getName());
			product.setStatus(AvailableStatus.NORMAL.value());
			product.setDeletedStatus(DeletedStatus.NOT_DELETED.value());
			product.setCreateBy(user);
			product.setCreateTime(DateUtil.date());
			productId = productService.createProduct(product);
		} else {
			Product product = productService.getById(params.getId());
			if (product == null) {
				throw new ServiceException("无效的产品ID");
			}

			BeanUtil.copyProperties(params, product, CopyOptions.create().setIgnoreNullValue(true));
			product.setCatName(productCat.getName());
			productService.updateProduct(product);
		}

		return new LongId(productId);
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/disable")
	public void disableProduct(@RequestBody IdParam idParam) {
		productService.disable(idParam.getId());
		// TODO 禁用所有的设备？？？禁用设备的所有消息模型？？？
	}

	@RequestMapping(method = HttpMethod.POST, path = "/product/remove")
	public void removeProduct(@RequestBody IdParam idParam) {
		productService.remove(idParam.getId());
		// TODO 删除所有的设备？？？
	}

}