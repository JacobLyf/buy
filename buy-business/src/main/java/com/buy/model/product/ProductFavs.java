package com.buy.model.product;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;

public class ProductFavs extends Model<ProductFavs>{
	
	private static final long serialVersionUID = 1L;
	public static final ProductFavs dao = new ProductFavs();
	
	/**
	 * 取消收藏的商品
	 * @author chenhj
	 */
	public void cancelCollectionProduct(String userId, int productId) {
		// 取消商品收藏.
		StringBuffer sql = new StringBuffer("DELETE FROM t_pro_favs WHERE user_id = ? AND product_id = ?");
		Db.update(sql.toString(), userId, productId);
		
		// 更新商品收藏次数.
		Product product = Product.dao.findById(productId);
		product.set("fav_count", product.getInt("fav_count") - 1).update();
	}
	
	/**
	 * 判断用户是否已收藏该商品
	 * @param userId
	 * @param productId
	 * @return true：已收藏；false：未收藏
	 * @author chenhg
	 * 2016年3月2日 下午1:46:04
	 */
	public boolean isCollectProduct(String userId, Integer productId){
		String sql = "SELECT s.id FROM t_pro_favs s WHERE s.user_id = ? AND s.product_id = ?";
		return Db.findFirst(sql, userId, productId) != null;
	}
	
	/**
	 * 判断用户是否可以收藏某个商品.
	 * 
	 * @param userId
	 *            用户Id
	 * @param productId
	 *            商品Id
	 * 
	 * @author Chengyb
	 * @return
	 */
	public Boolean canCollection(String userId, Integer productId) {
		String sql = "SELECT COUNT(1) FROM t_pro_favs s WHERE s.user_id = ? AND s.product_id = ?";
		return Db.queryLong(sql, userId, productId) > 0 ? false : true;
	}
	
	/**
	 * 对未收藏过的商品进行收藏.
	 * 
	 * @param userId
	 *            用户Id.
	 * @param list
	 *            需要收藏的商品列表.
	 * @author Chengyb
	 * @return
	 */
	@Before(Tx.class)
	public void collection(String userId, List<Integer> list) {
		Date date = new Date();
		
		for (int i = 0, size = list.size(); i < size; i++) {
			// 商品Id.
			Integer productId = list.get(i);
			// =====================================
			// 【1】判断当前商品是否存在.
			// =====================================*/
			Product product = Product.dao.findById(productId);
			if (null != product) {
				ProductFavs productFavs = new ProductFavs();
				// =====================================
				// 【2】判断用户是否已经收藏过此商品.
				// =====================================*/
				if(canCollection(userId, productId)) {
					// =====================================
					// 【3】收藏此商品.
					// =====================================*/
					productFavs.set("user_id", userId)
					                   .set("product_id", product.get("id"))
					                   .set("create_time", date)
							           .set("eq_price", product.getBigDecimal("eq_price"))
							           .save();
					// =====================================
					// 【4】更新此商品的收藏次数.
					// =====================================*/
					product.set("fav_count", product.getInt("fav_count") + 1);
					product.update();
				}
			}
		}
	}

}
