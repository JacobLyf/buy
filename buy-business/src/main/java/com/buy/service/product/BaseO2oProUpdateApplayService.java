package com.buy.service.product;

import java.util.Date;

import net.dreamlu.event.EventKit;

import com.buy.common.JsonMessage;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.productApply.O2oProUpdateApply;
import com.buy.plugin.event.product.event.ProductUpdateEvent;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * O2O商品改价
 * @author allon
 *
 */
public class BaseO2oProUpdateApplayService{
	/**
	 * 标识完成
	 * @param recordId
	 * @author huangzq
	 */
	@Before(Tx.class)
	public JsonMessage allFinish(Integer recordId){
		JsonMessage result = new JsonMessage();
		
		// 审核通过
		Date now = new Date();
		O2oProUpdateApply apply = O2oProUpdateApply.dao.findById(recordId);
		if(apply.getInt("audit_status")!=O2oProUpdateApply.AUDIT_STATUS_UPDATING){
			return result;
		}
		apply.set("audit_status", O2oProUpdateApply.AUDIT_STATUS_FINISH);
		apply.set("audit_time",now );
		apply.update();
		// 更新SKU价格
		String skuCode = apply.getStr("sku_code");
		ProductSku sku = new ProductSku();
		sku.set("code", skuCode);
		//sku.set("market_price", apply.get("after_market_price"));
		sku.set("eq_price", apply.get("after_eq_price"));
		sku.set("update_time", now);
		sku.update();
		//更新商品索引		
		EventKit.postEvent(new ProductUpdateEvent(apply.getInt("product_id")));		
		// 设置商品价格
		Product.dao.calculationPrice(apply.getInt("product_id"));
		
		/*
		 * 设置返回参数
		 */
		Integer proId = apply.getInt("product_id");
		Record data = new Record()
			.set("proId", proId)
			.set("skuCode", skuCode);
		result.setData(data);
		return result;
	}

}
