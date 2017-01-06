package com.buy.model.favorites;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class FavsProduct extends Model<FavsProduct>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static FavsProduct dao = new FavsProduct();
	
	/**
	 * 批量删除收藏商品
	 * @param favsIds
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月1日下午4:28:55
	 */
	public void batchDelete(Integer[] favsIds){
		String favsIdStr = "";
		for(int i=0;i<favsIds.length;i++){
			favsIdStr +=favsIds[i]+",";
		}
		favsIdStr = favsIdStr.substring(0,favsIdStr.length()-1);
		String sql = "DELETE FROM t_pro_favs  WHERE id in(?)";
		Db.update(sql, favsIdStr);
	}
	
	/**
	 * 检查用户是否有收藏过对应的商品
	 * @param userId
	 * @param proId
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月1日下午2:07:42
	 */
	public boolean checkProIsFavsByUser(String userId,int proId){
		String sql = "SELECT COUNT(t.id) FROM t_pro_favs t WHERE t.user_id like ? AND t.product_id = ?";
		Long count = Db.queryLong(sql,new Object[]{userId,proId});
		if(count>0){
			return true;
		}
		return false;
	}
}
