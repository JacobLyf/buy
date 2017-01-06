package com.buy.model.shop;  

import java.util.List;


import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/** 
 * ClassName:ShopProSort <br/> 
 * Date:     2015年9月30日 下午1:21:00 <br/> 
 * @author   HuangSx
 * @version   
 * @see       
 */
public class ShopProSort extends Model<ShopProSort> {
	public static final ShopProSort dao = new ShopProSort();
	
	/**
	 * 根据店铺id找到店铺一级分类
	 * @author HuangSx
	 * @date : 2015年9月30日 下午2:18:03
	 * @param shopId
	 * @return
	 * 没有用
	 */
	public List<Record> findFristSortNameByShopId(int shopId){
		StringBuffer sql =new StringBuffer("select a.id,a.parent_id as parentId ,a.name from t_shop_pro_sort a where parent_id is null ") ;
		if(shopId<=0){
			  sql.append("and shop_id is null");
			  return Db.find(sql.toString());
		}else {
			sql.append("and shop_id = ?");
			return Db.find(sql.toString(),shopId);
		}
	}
	
	/**
	 * 根据父级id找到下级分类名
	 * @author HuangSx
	 * @date : 2015年9月30日 下午2:17:30
	 * @param parentId
	 * @return
	 * 没有用
	 */
	public List<Record> findSortNameByParentId(int parentId){
		String sql = "select a.id ,a.name from t_shop_pro_sort a where parent_id in ( ? )";
		 return Db.find(sql,parentId);
	}
	/**
	 * 根据店铺编号查到全部的店铺商品分类列表
	 * @author HuangSx
	 * @date : 2015年10月7日 下午4:08:46
	 * @param shopId
	 * @return
	 */
	public List<Record> findAllShopSortNameByShopId(int shopId){
		StringBuffer sql =new StringBuffer("select a.id as id,a.parent_id as pId,a.name as name from t_shop_pro_sort a where shop_id = ? ") ;
		return Db.find(sql.toString(),shopId);
	}
}
      