package com.buy.plugin.cache;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buy.common.BaseConstants;
import com.buy.model.user.User;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class RedisHandler {
	
	Logger logger = LoggerFactory.getLogger("view");

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	private static SimpleDateFormat secondSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// 获取名称为footprint的Redis Cache对象.
	private static Cache footprintCache = Redis.use(BaseConstants.Redis.CACHE_FOOT_PRINT);
	/**
	 * 已登录用户浏览商品时添加浏览记录到Redis.
	 * 
	 * @param user
	 *            当前已登录的用户.
	 * @param product
	 *            当前浏览的商品.
	 * @author Chengyb
	 */
	public void viewProduct(User user, Record product) {
		Date date = new Date();
		
		//=====================================
		// 记录登录用户的浏览记录到日志.
		// @author Chengyb
		//=====================================*/
		if(null != user) {
			logger.info("【浏览记录】" 
	                  + user.getStr("id") // 用户Id.
	                  + ":"
	                  + product.getInt("productId") // 商品Id.
	                  + ":"
	                  + product.getStr("productName") // 商品名称.
	                  + ":"
	                  + product.getBigDecimal("eqPrice") // 商品价格.
	                  + ":"
	                  + secondSdf.format(date));
		}
		
		//=====================================
		// 记录登录用户的浏览记录到我的足迹.
		// @author Chengyb
		//=====================================*/
		if (user != null) {
			try{
				// =====================================
				// 商品信息.
				// =====================================*/
				String dayProductKey = user.getStr("id") + ":" + sdf.format(date) + ":" + product.getInt("productId");
				
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("id", product.getInt("productId")); // 商品Id.
				map.put("name", product.getStr("productName")); // 商品名称.
				map.put("image", product.getStr("productImg")); // 商品图片.
				map.put("price", product.getBigDecimal("eqPrice")); // 商品价格.
				
				// 添加浏览记录.
				footprintCache.hmset(dayProductKey, map);
					
				footprintCache.expire(dayProductKey, 30 * 24 * 60 * 60); // 30天失效.
				
				// =====================================
				// 商品浏览排序.
				// =====================================*/
				String dayKey = user.getStr("id") + ":" + sdf.format(date);
				
				// 当天是否已经浏览过此商品.
				String productId = product.getInt("productId").toString();
				
				// 移除.
				footprintCache.lrem(dayKey, 0, productId.toString());
				
				footprintCache.lpush(dayKey, productId.toString());
				
				footprintCache.expire(dayKey, 30 * 24 * 60 * 60); // 30天失效.
				
				// 添加【用户Id】-【用户Id:日期:产品Id】列表.
				
				// 移除.
				footprintCache.lrem(user.getStr("id"), 0, dayProductKey);
				
				footprintCache.lpush(user.getStr("id"), dayProductKey);
			}catch(Exception e){
				//TODO 等待异常处理
				e.printStackTrace();
			}
		}
	}
	
}
