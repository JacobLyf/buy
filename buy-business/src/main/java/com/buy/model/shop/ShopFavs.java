package com.buy.model.shop;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

public class ShopFavs extends Model<ShopFavs> {

	private static final long serialVersionUID = 1L;
	public static final ShopFavs dao = new ShopFavs();

	/**
	 * 判断是否已经收藏店铺
	 * 
	 * @param userId
	 * @param shopId
	 * @return
	 * @author chenhg 2016年3月8日 下午7:31:53
	 */
	public boolean isCollect(String userId, String shopId) {
		String sql = "SELECT id FROM t_shop_favs where user_id = ? and shop_id = ?";
		return Db.findFirst(sql, userId, shopId) != null;
	}

	/**
	 * 返回店铺的收藏数（关注店铺人数）
	 * 
	 * @param shopId
	 * @return
	 * @author chenhg 2016年3月8日 下午7:34:36
	 */
	public long collectNum(String shopId) {
		String sql = "SELECT count(id) num from t_shop_favs where shop_id = ?";
		Record record = Db.findFirst(sql, shopId);
		return record.getLong("num");
	}
	
	/**
	 * 取消收藏的店铺
	 * @author chenhj
	 */
	public void cancelCollectionShop(String userId, String shopId) {
		// 取消店铺收藏.
		StringBuffer sql = new StringBuffer("DELETE FROM t_shop_favs WHERE user_id = ? AND shop_id = ?");
		Db.update(sql.toString(), userId, shopId);
		
		// 更新店铺收藏次数.
		Shop shop = Shop.dao.findById(shopId);
		shop.set("fav_count", shop.getInt("fav_count") - 1).update();
	}
}
