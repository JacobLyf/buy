package com.buy.plugin.event.sort;

import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 后台三级分类状态更新事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class BackSortStatusUpdateListener implements ApplicationListener<BackSortStatusUpdateEvent> {

	@Override
	public void onApplicationEvent(BackSortStatusUpdateEvent event) {
		// 后台三级分类Id.
		Integer sortId = (Integer) event.getSource();
		
		//====================================
		// 更新商品的Solr数据.
		//====================================*/
		if(null != sortId) {
			ProductIndex.send(sortId, ExchangeType.BACK_SORT_STATUS_UPDATE, 20);
		}
	}
	
}