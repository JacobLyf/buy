package com.buy.plugin.event.user;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.common.BaseConstants;
import com.buy.date.DateUtil;
import com.buy.model.order.Order;
import com.buy.model.product.Product;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * 会员中心、购物车、订单--猜你喜欢
 * @author chenhg
 */
@Listener(enableAsync = true)
public class GuessYouLikeListener implements ApplicationListener<GuessYouLikeEvent> {
     //调用EventKit.postEvent(new GuessYouLikeEvent(userId));
	@Override
	public void onApplicationEvent(GuessYouLikeEvent event) {
		String userId = (String)event.getSource();
		
		Set<String> set=new HashSet<String>();//汇总的商品id
		//======================= 初始商品样本==============================//
		//=====会员足迹、收藏商品、收藏店铺、购物车商品、订单商品=====//
		
		//1、我的足迹
		Cache footprintCache = Redis.use(BaseConstants.Redis.CACHE_FOOT_PRINT);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < 31; i++) {
			Date curDate = DateUtil.addDay(date, -i);
			String dayKey = userId + ":" + sdf.format(curDate);
			set.addAll(footprintCache.lrange(dayKey, 0, -1));
		}
		
		StringBuffer sql = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		//2、收藏商品
		sql.append(" SELECT");
		sql.append("  DISTINCT t.productId ");
		sql.append(" FROM ");
		sql.append("  (SELECT ");
		sql.append("    a.product_id productId");
		sql.append("   FROM  ");
		sql.append("    t_pro_favs a");
		sql.append("    WHERE a.user_id = ?");
		paras.add(userId);
		//3、收藏店铺的所有商品
		sql.append("  UNION ");
		sql.append("   SELECT ");
		sql.append("   c.id productId ");
		sql.append("   FROM t_shop_favs b  ");
		sql.append("   LEFT JOIN t_product c ON c.shop_id = c.shop_id ");
		sql.append("   WHERE b.user_id = ? ");
		sql.append("   AND c.`status` = ? ");
		sql.append("   AND c.audit_status = ? ");
		paras.add(userId);
		paras.add(Product.STATUS_SHELVE);
		paras.add(Product.AUDIT_STATUS_SUCCESS);
		//4、购物车商品
		sql.append("  UNION  ");
		sql.append("   SELECT ");
		sql.append("    cart.product_id productId ");
		sql.append("    FROM t_cart cart ");
		sql.append("    WHERE cart.user_id = ?");
		paras.add(userId);
		//5、订单商品
		sql.append("  UNION  ");
		sql.append("    SELECT ");
		sql.append("    od.product_id productId");
		sql.append("   FROM t_order o ");
		sql.append("    LEFT JOIN t_order_detail od ON o.id = od.order_id");
		sql.append("   WHERE o.user_id = ?");
		sql.append("   AND o.trade_status = ? ");
		sql.append(" ) t   ");
		sql.append(" WHERE t.productId IS NOT NULL   ");
		paras.add(userId);
		paras.add(Order.TRADE_NORMAL);
		List<Record> list = Db.find(sql.toString(), paras.toArray());
		for(Record record: list){
			set.add(record.get("productId").toString());
		}
		
		Object[] productIdsArr = set.toArray();//商品样本id

		//======================= 初始商品样本==============================//
	    //判断样本是否为空
		if(StringUtil.notNull(productIdsArr)){
			//======================= 感兴趣的店铺/供货商 下的商品 ====================//
			StringBuffer sqlSS = new StringBuffer();
			List<Object> parasSS = new ArrayList<Object>();
			sqlSS.append(" SELECT ");
			sqlSS.append(" a.id ");
			sqlSS.append(" FROM t_product a ");
			sqlSS.append(" WHERE a.source IN(?,?) ");
			parasSS.add(Product.SOURCE_SELF_EXCLUSIVE);
			parasSS.add(Product.SOURCE_EXCLUSIVE);
			sqlSS.append("  AND a.shop_id IN");
			sqlSS.append(" ( ");
			sqlSS.append(" SELECT z1.id FROM  ");
			sqlSS.append(" ( ");
			sqlSS.append("  SELECT");
			sqlSS.append("  t.id");
			sqlSS.append("  FROM");
			sqlSS.append("  (SELECT");
			sqlSS.append("    s.id,COUNT(s.id) num");
			sqlSS.append("    FROM t_product p1");
			sqlSS.append("    LEFT JOIN t_shop s ON p1.shop_id = s.id");
			sqlSS.append("    WHERE p1.id IN (");
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlSS.append("?");
				}else{
					sqlSS.append("?,");
				}
				parasSS.add(productIdsArr[i]);
			}
			sqlSS.append(")");
			sqlSS.append("    AND p1.source IN(?,?)");
			
			parasSS.add(Product.SOURCE_SELF_EXCLUSIVE);
			parasSS.add(Product.SOURCE_EXCLUSIVE);
			sqlSS.append("    GROUP BY s.id");
			sqlSS.append("  UNION ");
			sqlSS.append("   SELECT");
			sqlSS.append("   sp.id ,COUNT(sp.id) num ");
			sqlSS.append("   FROM t_product p2");
			sqlSS.append("   LEFT JOIN t_supplier sp ON p2.supplier_id= sp.id");
			sqlSS.append("   WHERE p2.id IN (");
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlSS.append("?");
				}else{
					sqlSS.append("?,");
				}
				parasSS.add(productIdsArr[i]);
			}
			sqlSS.append(")");
			sqlSS.append("   AND p2.source IN(?,?,?)");
			parasSS.add(Product.SOURCE_FACTORY);
			parasSS.add(Product.SOURCE_FACTORY_SEND);
			parasSS.add(Product.SOURCE_SELF_PUBLIC);
			sqlSS.append("   GROUP BY sp.id");
			sqlSS.append("  ) t ");
			sqlSS.append("  ORDER BY t.num DESC ");
			sqlSS.append("  LIMIT 5) z1 ");
			sqlSS.append("  ) ");
			sqlSS.append(" UNION   ");
			sqlSS.append("  SELECT  ");
			sqlSS.append("   b.id  ");
			sqlSS.append("  FROM t_product b  ");
			sqlSS.append("  WHERE b.source IN(?,?,?)  ");
			parasSS.add(Product.SOURCE_FACTORY);
			parasSS.add(Product.SOURCE_FACTORY_SEND);
			parasSS.add(Product.SOURCE_SELF_PUBLIC);
			sqlSS.append("  AND b.supplier_id IN  ");
			sqlSS.append("  ( SELECT z2.id FROM");
			sqlSS.append("  (  ");
			sqlSS.append("   SELECT ");
			sqlSS.append("   t.id ");
			sqlSS.append("   FROM  ");
			
			sqlSS.append(" ( SELECT");
			sqlSS.append("     s.id,COUNT(s.id) num ");
			sqlSS.append("     FROM t_product p1");
			sqlSS.append("     LEFT JOIN t_shop s ON p1.shop_id = s.id");
			sqlSS.append("     WHERE p1.id IN (");
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlSS.append("?");
				}else{
					sqlSS.append("?,");
				}
				parasSS.add(productIdsArr[i]);
			}
			sqlSS.append(")");
			sqlSS.append("     AND p1.source IN(?,?)");
			parasSS.add(Product.SOURCE_SELF_EXCLUSIVE);
			parasSS.add(Product.SOURCE_EXCLUSIVE);
			sqlSS.append("     GROUP BY s.id");
			sqlSS.append("   UNION  ");
			sqlSS.append("     SELECT");
			sqlSS.append("     sp.id ,COUNT(sp.id) num ");
			sqlSS.append("     FROM t_product p2");
			sqlSS.append("     LEFT JOIN t_supplier sp ON p2.supplier_id= sp.id");
			sqlSS.append("     WHERE p2.id IN (");
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlSS.append("?");
				}else{
					sqlSS.append("?,");
				}
				parasSS.add(productIdsArr[i]);
			}
			sqlSS.append(")");
			sqlSS.append("     AND p2.source IN(?,?,?)");
			parasSS.add(Product.SOURCE_FACTORY);
			parasSS.add(Product.SOURCE_FACTORY_SEND);
			parasSS.add(Product.SOURCE_SELF_PUBLIC);
			sqlSS.append("     GROUP BY sp.id");
			sqlSS.append("   ) t   ");
			sqlSS.append("  ORDER BY t.num DESC  ");
			sqlSS.append("  LIMIT 5 ) z2 ");
			sqlSS.append(" )    ");
			 
			List<Record> sSList = Db.find(sqlSS.toString(), parasSS.toArray());
			//添加感兴趣的店铺/供货商 下的商品
			for(Record record: sSList){
				set.add(record.get("id").toString());
			}
			
			//======================= 感兴趣的店铺/供货商 下的商品 ====================//
			
			//======================= 感兴趣的品牌 下的商品 ====================//
			
			StringBuffer sqlBrand = new StringBuffer();
			List<Object> parasBrand = new ArrayList<Object>();
			sqlBrand.append(" SELECT ");
			sqlBrand.append(" b.id ");
			sqlBrand.append(" FROM t_product b ");
			sqlBrand.append(" WHERE b.brand_id IN( ");
			sqlBrand.append("  SELECT t.brand_id FROM ");
			sqlBrand.append("  ( ");
			sqlBrand.append("   SELECT ");
			sqlBrand.append("    a.brand_id,COUNT(a.brand_id) num");
			sqlBrand.append("   FROM t_product a");
			sqlBrand.append("  WHERE a.id IN(");
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlBrand.append("?");
				}else{
					sqlBrand.append("?,");
				}
				parasBrand.add(productIdsArr[i]);
			}
			sqlBrand.append("  )");
			sqlBrand.append("  GROUP BY a.brand_id ");
			sqlBrand.append("  ORDER BY num DESC ");
			sqlBrand.append("  LIMIT 5 ");
			sqlBrand.append(" ) t)   ");
			
			List<Record> brandList = Db.find(sqlBrand.toString(), parasBrand.toArray());
			//添加感兴趣的品牌 下的商品
			for(Record record: brandList){
				set.add(record.get("id").toString());
			}
			//======================= 感兴趣的品牌 下的商品 ====================//
			
			//======================= 感兴趣的二级分类商品 ====================//
			StringBuffer sqlSort = new StringBuffer();
			List<Object> parasSort = new ArrayList<Object>();
			sqlSort.append(" SELECT  ");
			sqlSort.append("  f.id ");
			sqlSort.append("  FROM t_product a ");
			sqlSort.append("  LEFT JOIN t_front_back_sort_map b ON a.sort_id = b.back_id ");
			sqlSort.append("  LEFT JOIN t_pro_front_sort c ON b.front_id = c.id ");
			sqlSort.append("  LEFT JOIN t_pro_front_sort d ON c.parent_id = d.parent_id ");
			sqlSort.append("  LEFT JOIN t_front_back_sort_map e ON d.id = e.front_id ");
			sqlSort.append("  LEFT JOIN t_product f ON e.back_id = f.sort_id ");
			sqlSort.append(" WHERE f.id is not null ");
			sqlSort.append("  AND a.id IN(");
			
			for(int i = 0;i < productIdsArr.length;i ++){
				if(i == productIdsArr.length - 1){
					sqlSort.append("?");
				}else{
					sqlSort.append("?,");
				}
				parasSort.add(productIdsArr[i]);
			}
			sqlSort.append("  )");
			
			List<Record> sortList = Db.find(sqlSort.toString(), parasSort.toArray());
			//添加感兴趣的二级分类商品
			for(Record record: sortList){
				set.add(record.get("id").toString());
			}
			
			//======================= 感兴趣的二级分类商品 ====================//
			
			//==================== 所有商品 ========================//
			Object[] allProductIdsArr = set.toArray();
			
			StringBuffer youLikeSql = new StringBuffer();
			List<Object> youLikeParas = new ArrayList<Object>();
			youLikeSql.append(" SELECT");
			youLikeSql.append(" b.id,");
			youLikeSql.append(" b.name,");
			youLikeSql.append(" b.product_img,");
			youLikeSql.append(" b.eq_price,");
			youLikeSql.append(" b.is_free_postage,");
			youLikeSql.append(" a.data_integrity_score + a.profit_score +  a.new_product_score");
			youLikeSql.append(" + a.new_shop_score + a.o2o_product_score + a.efun_product_score");
			youLikeSql.append(" + a.shop_deposit_score + a.peishi_score + a.dentification_score ");
			youLikeSql.append(" + a.depreciate_score + a.popularity_per_month_score + a.turnover_score");
			youLikeSql.append(" + a.positive_ratio AS score");
			youLikeSql.append(" FROM t_product_score a");
			youLikeSql.append(" LEFT JOIN t_product b ON a.pro_id = b.id ");
			youLikeSql.append(" WHERE a.pro_id IN(");
			for(int i = 0;i < allProductIdsArr.length;i ++){
				if(i == allProductIdsArr.length - 1){
					youLikeSql.append("?");
				}else{
					youLikeSql.append("?,");
				}
				youLikeParas.add(allProductIdsArr[i]);
			}
			youLikeSql.append(" )");
			youLikeSql.append(" AND b.id is not null ");
			youLikeSql.append(" AND b.status = 1 ");
			youLikeSql.append(" ORDER BY score DESC");
			youLikeSql.append(" LIMIT 30");
			List<Record> youLikeList = Db.find(youLikeSql.toString(), youLikeParas.toArray());
			
			//判断是否够 30个，不够需要补上（默认得分高的商品）
			if(youLikeList.size() < 30){
				int needNum = 30 - youLikeList.size() ;
				StringBuffer pubSql =new StringBuffer();
				pubSql.append(" SELECT");
				pubSql.append(" b.id,");
				pubSql.append(" b.name,");
				pubSql.append(" b.product_img,");
				pubSql.append(" b.eq_price,");
				pubSql.append(" b.is_free_postage,");
				pubSql.append(" a.data_integrity_score + a.profit_score +  a.new_product_score");
				pubSql.append(" + a.new_shop_score + a.o2o_product_score + a.efun_product_score");
				pubSql.append(" + a.shop_deposit_score + a.peishi_score + a.dentification_score ");
				pubSql.append(" + a.depreciate_score + a.popularity_per_month_score + a.turnover_score");
				pubSql.append(" + a.positive_ratio AS score");
				pubSql.append(" FROM t_product_score a");
				pubSql.append(" LEFT JOIN t_product b ON a.pro_id = b.id ");
				pubSql.append(" WHERE a.pro_id NOT IN(");
				for(int i = 0;i < allProductIdsArr.length;i ++){
					if(i == allProductIdsArr.length - 1){
						pubSql.append("?");
					}else{
						pubSql.append("?,");
					}
				}
				pubSql.append(" )");
				pubSql.append(" AND b.id is not null ");
				pubSql.append(" AND b.status = 1 ");
				pubSql.append(" ORDER BY score DESC");
				pubSql.append(" LIMIT ?");
				youLikeParas.add(needNum);
				List<Record> pubList = Db.find(pubSql.toString(), youLikeParas.toArray());
				for(Record record: pubList){
					youLikeList.add(record);
				}
			}
			
			//==================== 所有商品 ========================//
			//=====================设置到redis中============================//
			Cache youLikeCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
			String key = BaseConstants.Redis.KEY_GYL + userId;
			if(youLikeCache.exists(key)){
				youLikeCache.del(key);
			}
			Date now = new Date();
			Date max = DateUtil.getMaxDate(now);
			int second = (int)(max.getTime() - now.getTime()) / 1000;
			youLikeCache.setex(key, second, youLikeList);
			//=====================设置到redis中============================//
		}else{//新用户 展示商品默认评分最高的前 15个商品
			StringBuffer youLikeSql = new StringBuffer();
			youLikeSql.append(" SELECT");
			youLikeSql.append(" b.id,");
			youLikeSql.append(" b.name,");
			youLikeSql.append(" b.product_img,");
			youLikeSql.append(" b.eq_price,");
			youLikeSql.append(" b.is_free_postage,");
			youLikeSql.append(" a.data_integrity_score + a.profit_score +  a.new_product_score");
			youLikeSql.append(" + a.new_shop_score + a.o2o_product_score + a.efun_product_score");
			youLikeSql.append(" + a.shop_deposit_score + a.peishi_score + a.dentification_score ");
			youLikeSql.append(" + a.depreciate_score + a.popularity_per_month_score + a.turnover_score");
			youLikeSql.append(" + a.positive_ratio AS score");
			youLikeSql.append(" FROM t_product_score a");
			youLikeSql.append(" LEFT JOIN t_product b ON a.pro_id = b.id ");
			
			youLikeSql.append(" WHERE b.id is not null ");
			youLikeSql.append(" and b.status = 1 ");
			youLikeSql.append(" ORDER BY score DESC");
			youLikeSql.append(" LIMIT 30");
			List<Record> youLikeList = Db.find(youLikeSql.toString());
			//=====================设置到redis中============================//
			Cache youLikeCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
			String key = BaseConstants.Redis.KEY_GYL + userId;
			Date now = new Date();
			Date max = DateUtil.getMaxDate(now);
			int second = (int)(max.getTime() - now.getTime()) / 1000;
			youLikeCache.setex(key, second, youLikeList);
			//=====================设置到redis中============================//
		}
			
	}	

}
