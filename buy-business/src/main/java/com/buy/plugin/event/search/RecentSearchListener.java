package com.buy.plugin.event.search;

import java.util.Map;

import com.buy.model.search.RecentSearch;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 会员最近搜索记录事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class RecentSearchListener implements ApplicationListener<RecentSearchEvent> {

	@Override
	public void onApplicationEvent(RecentSearchEvent event) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) event.getSource();
		
		RecentSearch.dao.record((String) map.get("userId"), (String) map.get("key"), (Integer) map.get("type"));
	}
	
}