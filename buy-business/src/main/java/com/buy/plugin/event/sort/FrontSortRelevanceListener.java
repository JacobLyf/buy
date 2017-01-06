package com.buy.plugin.event.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buy.model.product.ProductFrontSort;
import com.buy.plugin.rabbitmq.aop.product.ProductIndex;
import com.buy.plugin.rabbitmq.wrapper.ExchangeType;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 分类索引关联事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class FrontSortRelevanceListener implements ApplicationListener<FrontSortRelevanceEvent> {

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(FrontSortRelevanceEvent event) {
		Map<String, Object> map = (Map<String, Object>) event.getSource();
		// 类型【1：PC；2：APP】
		Integer type = (Integer) map.get("type");
		ExchangeType exchangeType = null;
		
		if(null != type) {
			if(type == ProductFrontSort.TYPE_PC) {
				exchangeType = ExchangeType.PC_SORT_MAPPING_UPDATE;
			} else if(type == ProductFrontSort.TYPE_APP) {
			    exchangeType = ExchangeType.APP_SORT_MAPPING_UPDATE;
			}
		}
		
		// 旧的关联数据.
		List<Record> list = (List<Record>) map.get("oldMapping");
		
		// 新关联的后台分类数据.
		List<Integer> newList = (List<Integer>) map.get("newMapping");
		
		if(null != list) {
			List<Integer> list1 = new ArrayList<Integer>();
			
			for (int i = 0, size = list.size(); i < size; i++) {
				list1.add(list.get(i).getInt("back_id")); // 后台分类Id.
			}
			
			List<Integer> updateList = getDiffrent(list1, newList);
			
			ProductIndex.send(updateList,  exchangeType);
		} else {
			ProductIndex.send(newList,  exchangeType);
		}
	}
	
	/**
	 * 获取两个List的不同元素
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	private static List<Integer> getDiffrent(List<Integer> list1, List<Integer> list2) {
		List<Integer> diff = new ArrayList<Integer>();
		List<Integer> maxList = list1;
		List<Integer> minList = list2;
		if (list2.size() > list1.size()) {
			maxList = list2;
			minList = list1;
		}

		// 将List中的数据存到Map中
		Map<Integer, Integer> maxMap = new HashMap<Integer, Integer>(maxList.size());
		for (Integer string : maxList) {
			maxMap.put(string, 1);
		}

		// 循环minList中的值，标记 maxMap中 相同的 数据2
		for (Integer string : minList) {
			// 相同的
			if (maxMap.get(string) != null) {
				maxMap.put(string, 2);
				continue;
			}
			// 不相等的
			diff.add(string);
		}

		// 循环maxMap
		for (Map.Entry<Integer, Integer> entry : maxMap.entrySet()) {
			if (entry.getValue() == 1) {
				diff.add(entry.getKey());
			}
		}
		return diff;
	}
	
}