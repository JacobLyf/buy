

package com.buy.common;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;

/**
 * 返回值封装，常用于业务层需要多个返回值
 */
public class Ret {
	
	public  Map<Object, Object> map = new HashMap<Object, Object>();
	
	public Ret() {
		
	}
	
	public Ret(Object key, Object value) {
		map.put(key, value);
	}
	
	public static Ret create() {
		return new Ret();
	}
	
	public static Ret create(Object key, Object value) {
		return new Ret(key, value);
	}
	
	public Ret put(Object key, Object value) {
		map.put(key, value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Object key) {
		return (T)map.get(key);
	}
	
	/**
	 * key 存在，但 value 可能为 null
	 */
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	/**
	 * key 存在，并且 value 不为 null
	 */
	public boolean notNull(Object key) {
		return !isNull(key);
	}
	
	/**
	 * key 不存在，或者 key 存在但 value 为null
	 */
	public boolean isNull(Object key) {
		return (null == map.get(key)|| "".equals(map.get(key).toString().trim()) || "null".equals(map.get(key).toString()));
	}
	
	/**
	 * key 存在，并且 value 为 true，则返回 true
	 */
	public boolean isTrue(Object key) {
		Object value = map.get(key);
		return (value instanceof Boolean && ((Boolean)value));
	}
	
	/**
	 * key 存在，并且 value 为 false，则返回 true
	 */
	public boolean isFalse(Object key) {
		Object value = map.get(key);
		return (value instanceof Boolean && ((Boolean)value));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(Object key) {
		return (T)map.remove(key);
	}
	
	@Override
	public String toString(){
		return map.toString();
	}
	
	public String getStr(String key){
		if(this.isNull(key)){
			return null;
		}
		return map.get(key).toString();
	}
	
	public BigDecimal getBigDecimal(String key){
		if(this.isNull(key)){
			return null;
		}
		return new BigDecimal(map.get(key).toString());
	}
	
	public Integer getInt(String key){
		if(this.isNull(key)){
			return null;
		}
		return Integer.parseInt(map.get(key).toString());
	}
	
	public Long getLong(String key){
		if(this.isNull(key)){
			return null;
		}
		return Long.parseLong(map.get(key).toString());
	}
	
	public Double getDouble(String key){
		if(this.isNull(key)){
			return null;
		}
		return Double.parseDouble(map.get(key).toString());
	}
	
	public Date getDate(String key){
		if(this.isNull(key)){
			return null;
		}
		Date date = null;
		try {
			date =  DateUtil.parseDate((map.get(key).toString()));
		} catch (DateParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	
}


