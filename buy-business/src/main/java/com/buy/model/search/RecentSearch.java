package com.buy.model.search;

import java.util.List;
import com.buy.common.BaseConstants;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class RecentSearch {
	
	public static final RecentSearch dao = new RecentSearch();

	/* ================================ 
	 * 会员最近搜索记录. 
	 * ================================*/
	Cache recentSearchCache = Redis.use(BaseConstants.Redis.CACHE_RECENT_SEARCH_DATA);
	
	/**
	 * 商品.
	 */
	public static final Integer TYPE_PRODUCT = 1;
	
	/**
	 * 店铺.
	 */
	public static final Integer TYPE_SHOP = 2;
	
	/**
	 * 记录会员最近搜索记录.
	 * 
	 * @author Chengyb
	 */
	@SuppressWarnings("unchecked")
	public void record(String userId, String key, Integer type) {
		// 删除已存在的相同记录.
		recentSearchCache.lrem(userId, 0, key + ":" + type);
		
		// 记录搜索词到Redis.
		List<String> list = recentSearchCache.lrange(userId, 0, -1);
			
		if(null != list) {
			// 保留10条记录.
			if(list.size() >= 10) {
				for (int i = 10; i <= list.size(); i++) {
					// 右边出队.
					recentSearchCache.rpop(userId);
				}
			}
			
			recentSearchCache.lpush(userId, key + ":" + type);
		}
	}
	
}