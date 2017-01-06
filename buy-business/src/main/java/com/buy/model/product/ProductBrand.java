package com.buy.model.product;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

public class ProductBrand extends Model<ProductBrand>{
	
	private static final long serialVersionUID = 1L;
	public static final ProductBrand dao = new ProductBrand();
	/**
	 * 状态：无效
	 */
	public static int STATUS_DISABLE = 0;
	/**
	 * 状态：有效
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：已删除
	 */
	public static int STATUS_DELETE = 2;
	/**
	 * 品牌图片最大高度
	 */
	public static int BRAND_MAX_HEIGHT = 280;
	/**
	 * 品牌图片最大宽度
	 */
	public static int BRAND_MAX_WITH = 720;
	
	/**
	 * 获取全部品牌
	 * @return
	 */
	public List<ProductBrand> getAllBrand() {
		String sql = "select id, `name`, logo, is_commend, sort_num, "
				+ "`status`, create_time as createTime from t_pro_brand where `status` = " + STATUS_ENABLE;
		return ProductBrand.dao.find(sql);
	}
}
