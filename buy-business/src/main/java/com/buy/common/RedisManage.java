package com.buy.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.buy.model.product.ProductFrontSort;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * Redis数据管理.
 * 
 * @author Chengyb
 */
public class RedisManage {
	
	public static void initSortCache(Cache frontSortCache, Integer type) {
		frontSortCache.getJedis().flushDB();
		
		// =============================================================
		// 获取分类分组信息.【t_pro_front_sort：前台商品分类表】
		// =============================================================
		// 一级分类.
		List<ProductFrontSort> firstLevelList = ProductFrontSort.dao.findLevelList(type, ProductFrontSort.FIRST_LEVEL, 0);

		for (int i = 0, size = firstLevelList.size(); i < size; i++) {
			String firstSortKey = firstLevelList.get(i).getInt("id").toString();
			
			Map<Object, Object> firstSortMap = productFrontSortToMap(firstLevelList, i);
			
			// 添加商品前台一级分类信息.
			frontSortCache.hmset(firstSortKey, firstSortMap);
			
			// 添加商品前台一级分类信息.【名称:层级】-【分类Id】.
			frontSortCache.mset(firstLevelList.get(i).getStr("name") + ":" + firstLevelList.get(i).getInt("level"), firstLevelList.get(i).getInt("id"));

			List<ProductFrontSort> secondList = ProductFrontSort.dao.findLevelList(type, ProductFrontSort.SECOND_LEVEL, firstLevelList.get(i).getInt("id"));
			
			// 三级分类.
			for (int j = 0, size2 = secondList.size(); j < size2; j++) {
				String secondSortKey = secondList.get(j).getInt("id").toString();
				
				Map<Object, Object> secondSortMap = productFrontSortToMap(secondList, j);
				
				// 添加商品前台二级分类信息.
				frontSortCache.hmset(secondSortKey, secondSortMap);
				
				List<ProductFrontSort> thirdList = ProductFrontSort.dao.findLevelList(type, ProductFrontSort.THIRD_LEVEL, secondList.get(j).getInt("id"));
				
				// =====================================
				// 商品三级分类信息.
				// =====================================*/
				for (int k = 0, size3 = thirdList.size(); k < size3; k++) {
					String thirdSortKey = thirdList.get(k).getInt("id").toString();
					
					Map<Object, Object> thirdSortMap = productFrontSortToMap(thirdList, k);
					
					// 添加商品前台三级分类信息.
					frontSortCache.hmset(thirdSortKey, thirdSortMap);

					// 二级分类下的三级分类列表.
					frontSortCache.lpush("sorts" + ":" + secondList.get(j).getInt("id"), thirdList.get(k).getInt("id"));
				}
				
				// 一级分类下的二级分类列表.
				frontSortCache.lpush("sorts" + ":" + firstLevelList.get(i).getInt("id"), secondList.get(j).getInt("id"));
			}
			
			// 一级分类列表.
			frontSortCache.lpush("sort", firstLevelList.get(i).getInt("id"));
			
			if(type == ProductFrontSort.TYPE_PC) {
				// PC一级分类名称数据.
				frontSortCache.set("pc:" + firstLevelList.get(i).getStr("name") + ":1", firstLevelList.get(i).getInt("id"));
			} else if(type == ProductFrontSort.TYPE_APP) {
				// APP一级分类名称数据.
				frontSortCache.set("app:" + firstLevelList.get(i).getStr("name") + ":1", firstLevelList.get(i).getInt("id"));
			}
		}
		
		frontSortCache.set("version", UUID.randomUUID().toString());
	}
	
	/**
	 * 搜索引擎.
	 * 
	 * 【1】PC前台三级分类对应的Facet Field数据.
	 * 【2】PC前台三级分类对应的商品后台三级分类数据.
	 */
	public static void initSearchCache(Cache searchPropertyCache, Integer type) {
		searchPropertyCache.getJedis().flushDB();
		
		// 前台三级分类对应的Facet Field筛选属性.
		List<Record> facetFields = ProductFrontSort.dao.findBackSortSearchPropertyList(type, ProductFrontSort.THIRD_LEVEL);
		
		for (int i = 0, size = facetFields.size(); i < size; i++) {
			Record record = facetFields.get(i);
			
			Integer frontSortId = record.getInt("front_id"); // 前台三级分类Id.
			Integer propertyId  = record.getInt("property_id"); // 属性Id.
			String propertyName = record.getStr("property_name"); // Facet Field对应的显示名称.
			
			// 搜索属性.
			searchPropertyCache.set(propertyId, propertyName);
			
			// 搜索属性.
			searchPropertyCache.set(frontSortId + "_" + propertyName, SolrConstants.DYNAMIC_PROPERTY_PREFIX + propertyId);
			
			// 三级分类下的搜索属性列表.
			searchPropertyCache.lpush(frontSortId, propertyId);
		}
		
		//=====================================
	    // PC/APP前台三级分类对应的商品后台三级分类数据.
	    //=====================================*/
		Cache frontSortCache = null;
		if(type == ProductFrontSort.TYPE_PC) {
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_PC_FRONT_SORT);
		} else {
			frontSortCache = Redis.use(BaseConstants.Redis.CACHE_APP_FRONT_SORT);
		}
		
		// 前台一级分类列表.
		sortMapping(frontSortCache, type, ProductFrontSort.FIRST_LEVEL);
		
		// 前台二级分类列表.
		sortMapping(frontSortCache, type, ProductFrontSort.SECOND_LEVEL);
				
		// 前台三级分类列表.
		sortMapping(frontSortCache, type, ProductFrontSort.THIRD_LEVEL);
	}
	
	/**
	 * 加载前台三级分类列表到Redis.
	 * 
	 * @param frontSortCache
	 * @param type
	 * @param level
	 * 
	 * @author Chengyb
	 */
	public static void sortMapping(Cache frontSortCache, Integer type, Integer level) {
		List<ProductFrontSort> sortList = ProductFrontSort.dao.findLevelList(type, level, null);
		
		for (int i = 0, size = sortList.size(); i < size; i++) {
			Integer frontSortId = sortList.get(i).getInt("id"); // *级分类Id.
			
			List<Record> list = null;
			if (null != level) {
				if (level == ProductFrontSort.FIRST_LEVEL) { // 一级分类.
					list = ProductFrontSort.dao.findBackSortList(type, frontSortId, null, null);
				}
				if (level == ProductFrontSort.SECOND_LEVEL) { // 二级分类.
					list = ProductFrontSort.dao.findBackSortList(type, null, frontSortId, null);
				}
				if (level == ProductFrontSort.THIRD_LEVEL) { // 三级分类.
					list = ProductFrontSort.dao.findBackSortList(type, null, null, frontSortId);
				}
			}
			
			if(null != list) {
				for (int j = 0, size2 = list.size(); j < size2; j++) {
					Integer backSortId = list.get(j).getInt("id");
					
					frontSortCache.lpush("pc:sort:mapping:" + frontSortId, backSortId);
				}
			}
		}
	}

	private static Map<Object, Object> productFrontSortToMap(List<ProductFrontSort> thirdList, int k) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		
		map.put("name", thirdList.get(k).getStr("name")); // 前台分类名称.
		map.put("logo", thirdList.get(k).getStr("logo")); // 前台分类Logo.
		map.put("level", thirdList.get(k).getInt("level")); // 前台分类名称.
		map.put("status", thirdList.get(k).getInt("status")); // 前台分类状态（0：无效，1：有效）.
		map.put("parent", thirdList.get(k).getInt("parent_id")); // 父级分类Id.
		
		return map;
	}
	
}