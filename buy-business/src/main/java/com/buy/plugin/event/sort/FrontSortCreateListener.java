package com.buy.plugin.event.sort;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.buy.common.BaseConstants;
import com.buy.model.product.ProductFrontSort;
import com.buy.plugin.event.staticHtml.SortEvent;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import net.dreamlu.event.EventKit;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 分类索引创建事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class FrontSortCreateListener implements ApplicationListener<FrontSortCreateEvent> {
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(FrontSortCreateEvent event) {
		Cache frontSortCache = null;
		
		Map<String, Object> map = (Map<String, Object>) event.getSource();
		// 分类Id.
		Integer sortId = (Integer) map.get("sortId");

		// 前台分类所属 PC/App.
		Integer type = (Integer) map.get("frontType");
				
		if(type==ProductFrontSort.TYPE_PC){//更新PC分类Redis缓存数据.
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_PC_FRONT_SORT);
		}
		if(type==ProductFrontSort.TYPE_APP){//更新APP分类Redis缓存数据.
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_APP_FRONT_SORT);
		}
		
		
		//====================================
		// 更新Redis缓存数据.
		//====================================*/
		if(null != map && null != map.get("level") && (Integer) map.get("level") == 3) {
			// 三级分类.
			ProductFrontSort thirdFrontSort = ProductFrontSort.dao.findById(sortId);
			// 二级分类.
			ProductFrontSort secondFrontSort = ProductFrontSort.dao.findById(thirdFrontSort.getInt("parent_id"));
			
			String thirdSortKey = thirdFrontSort.getInt("id").toString();
			String secondSortKey = "sorts" + ":" + secondFrontSort.getInt("id");
			
			// 前台三级分类数据.
			Map<Object, Object> redisMap = new HashMap<Object, Object>();
			redisMap.put("name", map.get("name")); // 前台分类名称.
			redisMap.put("level", map.get("level")); // 前台分类名称.
			redisMap.put("status", map.get("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.
			redisMap.put("logo", map.get("logo")); // 前台分类三级分类logo

			frontSortCache.hmset(thirdSortKey, redisMap);
			
			// 二级分类下的三级分类.
			frontSortCache.lpush(secondSortKey, thirdFrontSort.getInt("id"));
		} else if(null != map && null != map.get("level") && (Integer) map.get("level") == 2) {
			// 二级分类.
			ProductFrontSort secondFrontSort = ProductFrontSort.dao.findById(sortId);
			// 一级分类.
			ProductFrontSort firstFrontSort = ProductFrontSort.dao.findById(secondFrontSort.getInt("parent_id"));
				
			String secondSortKey = secondFrontSort.getInt("id").toString();
			String firstSortKey = "sorts" + ":" + firstFrontSort.getInt("id");
					
			// 前台二级分类数据.
			Map<Object, Object> redisMap = new HashMap<Object, Object>();
			redisMap.put("name", map.get("name")); // 前台分类名称.
			redisMap.put("level", map.get("level")); // 前台分类名称.
			redisMap.put("status", map.get("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.
				
			frontSortCache.hmset(secondSortKey, redisMap);
			
			// 一级分类下的二级分类.
			frontSortCache.lpush(firstSortKey, secondFrontSort.getInt("id"));
		} else if(null != map && null != map.get("level") && (Integer) map.get("level") == 1) {
			// 前台一级分类数据.
			Map<Object, Object> redisMap = new HashMap<Object, Object>();
			redisMap.put("name", map.get("name")); // 前台分类名称.
			redisMap.put("level", map.get("level")); // 前台分类名称.
			redisMap.put("status", map.get("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.
							
			frontSortCache.hmset(sortId, redisMap);
						
			// 一级分类.
			frontSortCache.lpush("sort", sortId);
		}
		
		frontSortCache.set("version", UUID.randomUUID().toString());
		
	}
	
}