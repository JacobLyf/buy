package com.buy.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.buy.encryption.MD5Builder;
import com.jfinal.kit.HttpKit;
/**
 * 与Pos对接
 * @author huangzq
 *
 */
public class SignUtil {

	/** 密钥 */
	private String key;
	
	/** 请求的参数 */
	private SortedMap<String,String> parameters = new TreeMap<String,String>();
	
	/**
	*获取密钥
	*/
	public String getKey() {
		return key;
	}


	/**
	*设置密钥
	*/
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * 获取参数值
	 * @param parameter 参数名称
	 * @return String 
	 */
	public String getParameter(String parameter) {
		String s = (String)this.parameters.get(parameter); 
		return (null == s) ? "" : s;
	}
	
	/**
	 * 设置参数值
	 * @param parameter 参数名称
	 * @param parameterValue 参数值
	 */
	public void setParameter(String parameter, String parameterValue) {
		String v = "";
		if(null != parameterValue) {
			v = parameterValue.trim();
		}
		this.parameters.put(parameter, v);
	}
	/**
	 * 请求接口
	 * @param url
	 * @author huangzq
	 */
	public String requestRemote(String url){
		this.createSign();
		System.out.println(this.getData());
		return HttpKit.post(url, this.getData());
		
		
	}
	
	/**
	 * 返回所有的参数
	 * @return SortedMap
	 */
	public SortedMap<String,String> getAllParameters() {		
		return this.parameters;
	}
	/**
	 * 返回所有的参数
	 * @return SortedMap
	 */
	public String getData() {		
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		if(sb.length()>0){
			sb = sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

	
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	protected void createSign() {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(null != v && !"".equals(v) 
					&& !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + this.getKey());
	
		String sign = MD5Builder.getMD5Str(sb.toString()).toUpperCase();
		
		this.setParameter("sign", sign);
		
		
	}
	
	/**
	 * 验证签名,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 * @return boolean
	 */
	public boolean checkSign() {
		StringBuffer sb = new StringBuffer();
		SortedMap<String,String> map = getAllParameters();
		Set es = map.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if(!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}
		
		sb.append("key=" + this.getKey());
		
		//算出摘要
		//String enc = TenpayUtil.getCharacterEncoding(this.request, this.response);
		String sign = MD5Builder.getMD5Str(sb.toString()).toLowerCase();
		
		String tempSign = map.get("sign").toLowerCase();
		
		//debug信息
		Logger.getLogger(SignUtil.class).info(sb.toString() + " => sign:" + sign +
				" tenpaySign:" + tempSign);
		
		return tempSign.equals(sign);
	}
	

}
