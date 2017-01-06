package com.buy.map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapValueComparator {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map sortMap(Map map) {
		ArrayList<Map.Entry<String, BigDecimal>> list = new ArrayList<Map.Entry<String, BigDecimal>>(map.entrySet());  
		
		Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
			@Override
			public int compare(Entry<String, BigDecimal> o1,
					Entry<String, BigDecimal> o2) {
				return o1.getValue().intValue() - o2.getValue().intValue();
			}
		});
		
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}
}
