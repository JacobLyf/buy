package com.buy.model.product.nocargo;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 缺货商品预订统计
 */
public class ProductNoCargo extends Model<ProductNoCargo> {

	private static final long serialVersionUID = 1L;
	public static final ProductNoCargo dao = new ProductNoCargo();
	
	/** 补货状态 - 未补货 **/
	public static final int STATUS_NOCARGO = 0;
	/** 补货状态 - 已补货 **/
	public static final int STATUS_REPLENISH = 1;
	
	public ProductNoCargo getForLock(int id) {
		return ProductNoCargo.dao.findById("SELECT * FROM t_pro_no_cargo WHERE id = ? FOR UPDATE", id);
	}
	
	/**
	 * 获取商品补货信息.
	 * 
	 * @author Chengyb
	 */
	public synchronized ProductNoCargo findCargoByProductId(Integer productId) {
		ProductNoCargo record = ProductNoCargo.dao.findFirst("SELECT * FROM t_pro_no_cargo t WHERE t.pro_id = ? AND t.status = 0", productId);
		if(null == record) {
			Db.update("INSERT INTO t_pro_no_cargo(pro_id, user_count, create_time) VALUES (?, 1, NOW())", productId);
			record = ProductNoCargo.dao.findFirst("SELECT * FROM t_pro_no_cargo t WHERE t.pro_id = ?", productId);
		}
		return record;
	}
	
}