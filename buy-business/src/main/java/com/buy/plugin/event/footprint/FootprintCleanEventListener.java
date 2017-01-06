package com.buy.plugin.event.footprint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.buy.common.BaseConstants;
import com.buy.date.DateUtil;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 我的足迹清理事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class FootprintCleanEventListener implements ApplicationListener<FootprintCleanEvent> {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(FootprintCleanEvent event) {
		// 获取名称为footprint的Redis Cache对象.
		Cache footprintCache = Redis.use(BaseConstants.Redis.CACHE_FOOT_PRINT);
		
		String userId = (String) event.getSource();
		
		// 一个月前的日期.
		Date oneMonthBeforeDate = DateUtil.getMinDate(DateUtil.addMonth(new Date(), -1));
		
		List<String> viewList = footprintCache.lrange(userId, 0, -1);
		
		if(null != viewList && viewList.size() > 0) {
			Collections.reverse(viewList);
			
			String string = viewList.get(0).split(":")[1];
			
			Date date = null;
			try {
				 date = sdf.parse(string);
			} catch (ParseException e) {
			}
			
			if(oneMonthBeforeDate.before(date)) {
				return;
			}
			
			for (int i = 0, size = viewList.size(); i < size; i++) {
				// 【用户Id】+【日期】+【产品Id】
				String value = viewList.get(i);
				
				String dayString = value.split(":")[1];
				
				date = null;
				try {
					 date = sdf.parse(dayString);
				} catch (ParseException e) {
				}
				
				if(null != date) {
					if(oneMonthBeforeDate.after(date)) {
						// 删除商品信息记录.
						footprintCache.del(value);
				        
						// 删除当天的记录.
						if(footprintCache.exists(userId + ":" + value.split(":")[1])) {
							footprintCache.del(userId + ":" + value.split(":")[1]);
						}
				        
				        // 删除当前列表.
				        footprintCache.lrem(userId, 1, viewList.get(i));
					} else {
						return;
					}
				}
			}
		}
		
	}
	
}