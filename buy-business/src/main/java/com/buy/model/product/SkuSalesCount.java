package com.buy.model.product;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Model;

public class SkuSalesCount extends Model<SkuSalesCount> {
	
	private static final Log L = LogFactory.getLog(SkuSalesCount.class);
	
	private static final long serialVersionUID = 1L;
	public static final SkuSalesCount dao = new SkuSalesCount();
	
	/**
	 * SKU锁行
	 */
	public SkuSalesCount getByLock(String skuCode) {
		String sql = "SELECT * FROM t_sku_sales_count WHERE sku_code = ? FOR UPDATE";
		return SkuSalesCount.dao.findFirst(sql, skuCode);
	}
	
	/**
	 * 更新SKU(销量 + 结算)
	 */
	public void  updateBySaleSettle(String skuCode, int count, boolean isSettle) {
		if (StringUtil.isNull(skuCode))
			return;
		L.info("更新销量的SKU识别码："+skuCode);
		SkuSalesCount sales = getByLock(skuCode);
		if (StringUtil.notNull(sales)) {
			int salesCount = sales.getInt("sales_count");
			sales.set("sales_count", salesCount + count);
			if (isSettle) {
				int balanceCount = sales.getInt("balance_count");
				sales.set("balance_count", balanceCount + count);
			}
			sales.set("update_time", new Date()).update();
		}
	}

	/**
	 * SKU更新结算
	 */
	public void updateBySettle(String skuCode, int count) {
		if (StringUtil.isNull(skuCode))
			return;
		SkuSalesCount sales = getByLock(skuCode);
		int balanceCount = sales.getInt("balance_count");
		sales.set("balance_count", balanceCount + count).set("update_time", new Date()).update();
	}
	
}
