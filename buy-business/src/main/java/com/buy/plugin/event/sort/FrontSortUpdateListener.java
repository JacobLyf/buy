package com.buy.plugin.event.sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.buy.common.BaseConstants;
import com.buy.model.product.ProductFrontSort;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 分类索引更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class FrontSortUpdateListener implements ApplicationListener<FrontSortUpdateEvent> {

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(FrontSortUpdateEvent event) {
		Cache frontSortCache = null;

		Map<String, Object> map = (Map<String, Object>) event.getSource();
		// 分类Id.
		Integer sortId = (Integer) map.get("sortId");

		// 前台分类所属 PC/App.
		Integer type = (Integer) map.get("frontType");

		if(type == ProductFrontSort.TYPE_PC){
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_PC_FRONT_SORT);
		}
		if(type == ProductFrontSort.TYPE_APP){
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_APP_FRONT_SORT);
		}

		
		if (null != map && null != sortId && null != type && null != map.get("level")) {

			if ((Integer) map.get("level") == 3) {
				// ====================================
				// 更新Redis缓存数据.
				// ====================================*/
				// 三级分类.
				ProductFrontSort thirdFrontSort = ProductFrontSort.dao.findById(sortId);

				String thirdSortKey = thirdFrontSort.getInt("id").toString();

				// 更新前台三级分类数据.
				Map<Object, Object> redisMap = new HashMap<Object, Object>();
				redisMap.put("name", map.get("name")); // 前台分类名称.
				redisMap.put("level", map.get("level")); // 前台分类名称.
				redisMap.put("status", map.get("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.
				redisMap.put("logo", map.get("logo")); // 前台分类三级分类logo

				frontSortCache.hmset(thirdSortKey, redisMap);

				// 更新前台二级分类列表数据.
				List<ProductFrontSort> thirdLevelList = ProductFrontSort.dao.findLevelList(type,
						ProductFrontSort.THIRD_LEVEL, thirdFrontSort.getInt("parent_id"));

				frontSortCache.del("sorts" + ":" + thirdFrontSort.getInt("parent_id"));

				for (int i = 0, size = thirdLevelList.size(); i < size; i++) {
					// 二级分类下的三级分类列表.
					frontSortCache.lpush("sorts" + ":" + thirdFrontSort.getInt("parent_id"),
							thirdLevelList.get(i).getInt("id"));
				}

				// ====================================
				// 发送三级分类Id、类型数据到MQ.
				// ====================================*/
				ProductIndex.send(sortId, type == ProductFrontSort.TYPE_PC ? ExchangeType.PC_THIRD_SORT_UPDATE
						: ExchangeType.APP_THIRD_SORT_UPDATE, 17);
			} else if ((Integer) map.get("level") == 2) {
				// 二级分类.
				ProductFrontSort secondFrontSort = ProductFrontSort.dao.findById(sortId);

				String secondSortKey = secondFrontSort.getInt("id").toString();

				// 更新前台二级分类.
				Map<Object, Object> redisMap = new HashMap<Object, Object>();
				redisMap.put("name", secondFrontSort.getStr("name")); // 前台分类名称.
				redisMap.put("level", secondFrontSort.getInt("level")); // 前台分类名称.
				redisMap.put("status", secondFrontSort.getInt("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.

				frontSortCache.hmset(secondSortKey, redisMap);

				// 更新前台一级分类列表数据.
				List<ProductFrontSort> secondLevelList = ProductFrontSort.dao.findLevelList(type,
						ProductFrontSort.SECOND_LEVEL, secondFrontSort.getInt("parent_id"));

				frontSortCache.del("sorts" + ":" + secondFrontSort.getInt("parent_id"));

				for (int i = 0, size = secondLevelList.size(); i < size; i++) {
					// 一级分类下的二级分类列表.
					frontSortCache.lpush("sorts" + ":" + secondFrontSort.getInt("parent_id"),
							secondLevelList.get(i).getInt("id"));
				}

				// ====================================
				// 发送二级分类Id、类型数据到MQ.
				// ====================================*/
				ProductIndex.send(sortId, type == ProductFrontSort.TYPE_PC ? ExchangeType.PC_SECOND_SORT_UPDATE
						: ExchangeType.APP_SECOND_SORT_UPDATE, 17);
			} else if ((Integer) map.get("level") == 1) {
				// 一级分类.
				ProductFrontSort firstFrontSort = ProductFrontSort.dao.findById(sortId);

				// 更新前台一级分类数据.
				Map<Object, Object> redisMap = new HashMap<Object, Object>();
				redisMap.put("name", firstFrontSort.getStr("name")); // 前台分类名称.
				redisMap.put("level", firstFrontSort.getInt("level")); // 前台分类名称.
				redisMap.put("status", firstFrontSort.getInt("status")); // 前台分类状态（0：无效，1：有效，2：已删除）.

				frontSortCache.hmset(sortId, redisMap);
				
				// 一级分类列表.
				List<ProductFrontSort> firstLevelList = ProductFrontSort.dao.findLevelList(type,
						ProductFrontSort.FIRST_LEVEL, null);
				
				frontSortCache.del("sort");
				
				for (int j = 0, size = firstLevelList.size(); j < size; j++) {
					frontSortCache.lpush("sort", firstLevelList.get(j).getInt("id"));
				}

				// ====================================
				// 发送一级分类Id、类型数据到MQ.
				// ====================================*/
				ProductIndex.send(sortId, type == ProductFrontSort.TYPE_PC ? ExchangeType.PC_FIRST_SORT_UPDATE
						: ExchangeType.APP_FIRST_SORT_UPDATE, 17);
			}

			frontSortCache.set("version", UUID.randomUUID().toString());
		}
		
	}

}