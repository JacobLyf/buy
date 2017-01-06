package com.buy.model.product;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class ProductSalesCount extends Model<ProductSalesCount> {
	
	private static final Log L = LogFactory.getLog(ProductSalesCount.class);
	private static final long serialVersionUID = 1L;
	public static final ProductSalesCount dao = new ProductSalesCount();

	/**
	 * 商品锁行
	 */
	public ProductSalesCount getByLock(Integer proId) {
		String sql = "SELECT * FROM t_product_sales_count WHERE product_id = ? FOR UPDATE";
		return ProductSalesCount.dao.findFirst(sql, proId);
	}
	
	/**
	 * 更新商品(销量 + 结算)
	 */
	public void  updateBySaleSettle(Integer proId, int count, boolean isSettle) {
		if (StringUtil.isNull(proId))
			return;
		L.info("更新销量的商品ID："+proId);
		ProductSalesCount sales = getByLock(proId);
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
	 * 商品更新结算
	 */
	public void updateBySettle(Integer proId, int count) {
		if (StringUtil.isNull(proId))
			return;
		ProductSalesCount sales = getByLock(proId);
		int balanceCount = sales.getInt("balance_count");
		sales.set("balance_count", balanceCount + count).set("update_time", new Date()).update();
	}
	
	/**
	 * 初始化.
	 * 
	 * @param productId
	 *            商品Id.
	 */
	public void init(Integer productId) {
		Db.update("INSERT INTO t_product_sales_count(product_id) VALUES (?)", productId);
	}
	
}
