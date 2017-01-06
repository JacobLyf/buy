package com.buy.model.pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.buy.common.JsonMessage;
import com.buy.encryption.MD5Builder;
import com.buy.string.StringUtil;
import com.google.gson.Gson;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class PushPosRecord extends Model<PushPosRecord> {

	static Logger L = Logger.getLogger(PushPosRecord.class);
	
	private static final long serialVersionUID = 1L;
	public static final PushPosRecord dao = new PushPosRecord();
	
	public static final int TYPE_PRODUCT = 1;	//商品信息推送
	public static final int TYPE_LOGISTIC = 2;	//物流信息推送
	public static final int TYPE_INVENTORY = 3;	//锁定库存变化
	public static final int TYPE_RETURN = 4;	//退货订单

	/** 推送状态 - 成功 **/
	public static final int STAUTS_SUCCESS = 1;
	/** 推送状态 - 失败 **/
	public static final int STAUTS_FAIL = 2;
	
	public PushPosRecord getForLock(int id) {
		return PushPosRecord.dao.findFirst("SELECT * FROM t_push_pos_record WHERE id = ? FOR UPDATE", id);
	}
	
	/**
	 * 推送
	 * @param reqName	POS接口名称
	 * @param list		推送数据
	 * @param subject	推送失败时，根据subject查询数据重新推送
	 * @return
	 */
	public JsonMessage push(String reqName, List<Record> list, String subject) {
		JsonMessage jm = new JsonMessage();
		
		if (StringUtil.isBlank(reqName) || StringUtil.isNull(list))
			return jm.setStatusAndMsg("1", "推送POS参数为空");
		
		/*
		 * 转化JSON
		 */
		List<HashMap<String, Object>> descList = new ArrayList<HashMap<String, Object>>();
		for (Record r : list) {
			HashMap<String, Object> temp = new HashMap<String, Object>();
			for (String column : r.getColumnNames())
				temp.put(column, r.get(column));
			descList.add(temp);
		}
		String json = new Gson().toJson(descList);
		L.info("推送参数-JSON:" + json);
		
		/*
		 * 设置推送参数
		 */
		HashMap<String, String> paras = new HashMap<String, String>();
		String paraKey = PropKit.use("global.properties").get("push.pos.para.key");
		paras.put(paraKey, json);
		
		/*
		 * 处理推送数据
		 */
		int i = 0;
		NameValuePair[] data = new NameValuePair[1];
		StringBuffer sb = new StringBuffer();
		if (StringUtil.notNull(paras)) {
			data = new NameValuePair[paras.size() + 1];
			
			Object[] parasKeys = paras.keySet().toArray();
			Arrays.sort(parasKeys);
			
			Set<Entry<String, String>> set = paras.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				String k = (String) entry.getKey();
				String v = (String) entry.getValue();
				
				data[i] = new NameValuePair(k, v);
				i++;
				
				if (!"sign".equals(k) && !"key".equals(k))
					sb.append(k + "=" + v + "&");
			}
			
			String key = PropKit.use("global.properties").get("push.pos.key");
			sb.append("key=" + key);
			String sign = MD5Builder.getMD5Str(sb.toString()).toUpperCase();
			i = i > 0 ? i : 0;
			data[i] = new NameValuePair("sign", sign);
		} else {
			L.info("没有推送参数，不推送");
			L.info("推送POS参数：" + sb.toString());
			return jm.setStatusAndMsg("1", "没有推送参数，不推送");
		}
		
		/*
		 * 推送
		 */
		PushPosRecord error = null;
		jm = mainPush(reqName, data);
		
		/*
		 * 推送失败，再推送4次
		 */
		if (!"0".equals(jm.getStatus())) {
			int c = 0;
			while (c < 4) {
				jm = mainPush(reqName, data);
				if ("0".equals(jm.getStatus()))
					break;
				else
					c++;
			}
			
			if (!"0".equals(jm.getStatus()))
				error = new PushPosRecord().set("remark", jm.getMsg());
		}
		
		// 增加错误记录
		if (StringUtil.notNull(error)) {
			Date now = new Date();
			String reqResult = (String) jm.getData();
			
			error
				.set("request",			reqName)
				.set("type",			PushPosRecord.TYPE_PRODUCT)
				.set("subject",			subject)
				.set("result_json",		reqResult)
				.set("create_time",		now)
				.set("update_time",		now)
				.save();
		}
		
		/*
		 * 推送结果
		 */
		L.info("推送POS接口：" + reqName);
		L.info("推送POS状态：" + jm.getStatus());
		L.info("推送POS数据：" + jm.getData());
		L.info("推送POS消息：" + jm.getMsg());
		return jm;
	}
	
	/**
	 * 推送 - 主方法
	 */
	public static JsonMessage mainPush(String reqName, NameValuePair[] data) {
		JsonMessage result = new JsonMessage();
		String reqResult = null;
		
		try {
			String reqPrefix = PropKit.use("global.properties").get("push.pos.prefix");
			PostMethod post = new PostMethod(reqPrefix + reqName);
			post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; text/html; charset=utf-8");
			post.setRequestBody(data);
			
			HttpClient client = new HttpClient();
			client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			client.executeMethod(post);
			
			reqResult = new String(post.getResponseBodyAsString().getBytes());
			L.info("推送POS结果：" + reqResult);
			
			return new Gson().fromJson(reqResult, JsonMessage.class).setData(reqResult);
		} catch (Exception e) {
			e.printStackTrace();
			return result.setStatusAndMsg("-1", "请求出错");
		}
	}
	
}

