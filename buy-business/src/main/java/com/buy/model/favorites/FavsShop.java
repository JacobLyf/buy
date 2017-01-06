package com.buy.model.favorites;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class FavsShop extends Model<FavsShop>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static FavsShop dao = new FavsShop();
	
	/**
	 * 判断用户是否已收藏过对应的店铺
	 * @param userId
	 * @param shopId
	 * @return
	 * @return boolean
	 * @throws
	 * @author Eriol
	 * @date 2015年6月26日下午5:28:17
	 */
	public boolean checkShopIsFavsByUser(String userId,int shopId){
		String sql = "SELECT COUNT(t.id) FROM t_shop_favs t WHERE t.user_id like ? AND t.shop_id = ?";
		Long count = Db.queryLong(sql,new Object[]{userId,shopId});
		if(count>0){
			return true;
		}
		return false;
	}

	/**
	 * 判断用户是否已收藏过对应的店铺
	 * @param userId
	 * @param shopId
	 * @return boolean
	 * @author Eriol
	 * @date 2015年6月26日下午5:28:17
	 */
	public boolean isHadCollectShop(String userId,String shopId){
		String sql = "SELECT COUNT(t.id) FROM t_shop_favs t WHERE t.user_id  = ? AND t.shop_id = ?";
		Long count = Db.queryLong(sql,new Object[]{userId,shopId});
		if(count>0){
			return true;
		}
		return false;
	}
}
