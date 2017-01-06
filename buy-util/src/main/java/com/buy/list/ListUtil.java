package com.buy.list;

import java.util.List;

import com.jfinal.plugin.activerecord.Record;

public class ListUtil {

	/**
	 * 列表转换成数组
	 * @author chenhg
	 * @param list
	 * @param columnName
	 * @return
	 * @date 2016年12月23日 下午4:10:29
	 */
	public static Object[] listToArr(List<Record> list, String columnName){
		if(list == null || list.size() < 1){
			return null;
		}
		Object[] arr = new Object[list.size()];
		int i = 0;
		for(Record rec : list){
			arr[i] = rec.get(columnName);
			i = i + 1;
		}
		
		return arr;
	}
}
