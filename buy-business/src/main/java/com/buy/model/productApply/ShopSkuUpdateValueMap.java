package com.buy.model.productApply;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class ShopSkuUpdateValueMap extends Model<ShopSkuUpdateValueMap>{
	private static final long serialVersionUID = 6158141935755316649L;
	


	
	public static final ShopSkuUpdateValueMap dao = new ShopSkuUpdateValueMap();

	/**
	 * 添加隐私
	 * @param skuCode
	 * @param valueId
	 * @author huangzq
	 */
	public void add(String detailId,long valueId){
		ShopSkuUpdateValueMap map = new ShopSkuUpdateValueMap();
		map.set("detail_id", detailId);
		map.set("value_id", valueId);
		map.save();
	}
	
}
