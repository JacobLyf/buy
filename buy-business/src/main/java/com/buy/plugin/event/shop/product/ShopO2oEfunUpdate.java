package com.buy.plugin.event.shop.product;

import java.util.List;

import com.buy.model.product.Product;
import com.buy.model.shop.Shop;
import com.buy.string.StringUtil;

public class ShopO2oEfunUpdate {

	private String shopId;
	private String[] shopIds;
	
	private Integer proId;
	private Integer[] proIds;
	private List<Integer> proIdList;
	
	public ShopO2oEfunUpdate(String shopId) {
		this.shopId = shopId;
	}
	
	public ShopO2oEfunUpdate(String[] shopIds) {
		this.shopIds = shopIds;
	}
	
	public ShopO2oEfunUpdate(Integer proId) {
		this.proId = proId;
	}
	
	public ShopO2oEfunUpdate(Integer[] proIds) {
		this.proIds = proIds;
	}
	
	public ShopO2oEfunUpdate(List<Integer> proIdList) {
		this.proIdList = proIdList;
	}
	
	public void updateByO2o() {
		if (StringUtil.notNull(this.shopId)) {
			Shop.dao.updateByO2o(this.shopId);
			return;
		}
		
		if (StringUtil.notNull(this.shopIds)) {
			for(String id : this.shopIds)
				Shop.dao.updateByO2o(id);
			return;
		}
		
		if (StringUtil.notNull(this.proId)) {
			String shopId = Product.dao.getShopIdByProId(proId);
			Shop.dao.updateByO2o(shopId);
			return;
		}
		
		
		if (StringUtil.notNull(this.proIds)) {
			List<String> shopIds = Product.dao.findShopIdByProIds(this.proIds);
			if (StringUtil.notNull(shopIds)) {
				for (String id : shopIds)
					Shop.dao.updateByO2o(id);
				return;
			}
		}
		
		if (StringUtil.notNull(this.proIdList)) {
			List<String> shopIds = Product.dao.findShopIdByProIds(this.proIdList);
			if (StringUtil.notNull(shopIds)) {
				for (String id : shopIds)
					Shop.dao.updateByO2o(id);
				return;
			}
		}
	}
	
	public void updateByEfun() {
		if (StringUtil.notNull(this.shopId)) {
			Shop.dao.updateByEfun(this.shopId);
			return;
		}
		
		if (StringUtil.notNull(this.shopIds)) {
			for(String id : this.shopIds)
				Shop.dao.updateByEfun(id);
			return;
		}
		
		if (StringUtil.notNull(this.proId)) {
			String shopId = Product.dao.getShopIdByProId(proId);
			Shop.dao.updateByEfun(shopId);
			return;
		}
		
		
		if (StringUtil.notNull(this.proIds)) {
			List<String> shopIds = Product.dao.findShopIdByProIds(this.proIds);
			if (StringUtil.notNull(shopIds)) {
				for (String id : shopIds)
					Shop.dao.updateByEfun(id);
				return;
			}
		}
		
		if (StringUtil.notNull(this.proIdList)) {
			List<String> shopIds = Product.dao.findShopIdByProIds(this.proIdList);
			if (StringUtil.notNull(shopIds)) {
				for (String id : shopIds)
					Shop.dao.updateByEfun(id);
				return;
			}
		}
	}
	
}
