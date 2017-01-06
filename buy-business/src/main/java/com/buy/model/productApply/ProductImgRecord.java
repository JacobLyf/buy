package com.buy.model.productApply;

import java.util.List;

import com.buy.model.product.ProductImg;
import com.jfinal.plugin.activerecord.Model;

public class ProductImgRecord extends Model<ProductImgRecord>{
	
	/**
	 * 商品图片
	 */
	private static final long serialVersionUID = 1L;
	public static final ProductImgRecord dao = new ProductImgRecord();
	/**
	 * 状态：启用
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：禁用
	 */
	public static int STATUS_DISABLE= 0;
	/**
	 * 根据商品id获取图片
	 * @param productId
	 * @return
	 * @author huangzq
	 */
	public List<ProductImgRecord> findByProductId(int applyId){
		
		return dao.find("select *  from t_pro_img_record where product_id = ? order by is_main desc",applyId);
		
	}
	
	

}
