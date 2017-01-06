package com.buy.model.productApply;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;

public class PublicSkuUpdateValueMap extends Model<PublicSkuUpdateValueMap>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2728215230131403179L;

	public static final PublicSkuUpdateValueMap dao = new PublicSkuUpdateValueMap();
	
	/**
	 * 添加映射
	 * @param skuCode
	 * @param valueId
	 */
	public void add(String code,long valueId){
		new PublicSkuUpdateValueMap().set("sku_code", code)
									 .set("value_id", valueId)
									 .save();
	}
	
	/**
	 * 删除映射
	 * 
	 * @param skuCode
	 */
	@Before(Tx.class)
	public void delete(String skuCode) {
		Db.update("delete from t_public_sku_update_value_map where sku_code = ?", skuCode);
	}

}
