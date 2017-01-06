package com.buy.model.productApply;

import java.math.BigDecimal;

import com.buy.model.product.ProductSku;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class ShopSkuUpdateDetail extends Model<ShopSkuUpdateDetail>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final ShopSkuUpdateDetail dao = new ShopSkuUpdateDetail();
	/**
	 * 标记sku是否修改
	 * @param skuDetail
	 * @return
	 * @author huangzq
	 */
	public boolean isSkuChange(Record skuDetail){
		String code  = skuDetail.getStr("sku_code");
		ProductSku sku = ProductSku.dao.findById(code);
		if(sku!=null&&sku.getBigDecimal("market_price").compareTo(new BigDecimal(skuDetail.getStr("market_price")))==0
				&&sku.getBigDecimal("eq_price").compareTo(new BigDecimal(skuDetail.getStr("eq_price")))==0){
				return false;
		}
		return true;
		
	}
	/**
	 * 判断sku个数是否改变
	 * @param productId
	 * @param changeCount
	 * @return
	 * @author huangzq
	 */
	public boolean isSkuCountChange(Integer productId,Integer changeCount){
		String sql = "select count(*) from t_pro_sku where product_id = ?";
		int skuCount = Db.queryLong(sql,productId).intValue();
		if(changeCount!=skuCount){
			return true;
		}
		return false;
		
	}
}
