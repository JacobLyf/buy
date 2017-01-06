package com.buy.service.pos.push;

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
import com.buy.model.logistics.LogisticsCompany;
import com.buy.model.pos.PushPosRecord;
import com.buy.string.StringUtil;
import com.google.gson.Gson;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

/**
 * 推送 退货订单
 * @author chenhg
 */
public class PushPosReturnGoods {

	static Logger L = Logger.getLogger(PushPosReturnGoods.class);
	
	private String orderReturnId; //退货单id

	//pos推送接口名称
	public final static String REQ_RETURN_GOODS = "updateReturn";

	
	public PushPosReturnGoods setOrderReturnId(String orderReturnId) {
		this.orderReturnId = orderReturnId;
		return this;
	}

	/**
	 * 推送
	 * @author chenhg
	 * 2016年11月23日 下午4:26:52
	 * @throws Exception 
	 */
	public void push(){
		// 推送 退货订单
		L.info("推送 退货订单,退货单id:"+orderReturnId );
		//获取数据
		List<Record> returnList = null;
		try {
			returnList = getPushReturnMessage(orderReturnId);
		} catch (Exception e) {
			L.error("推送 退货订单,查询快递url失败,退货单id:"+orderReturnId);
		}
		if(returnList != null && returnList.size() > 0){
			//推送
			String subject = orderReturnId;
			push(REQ_RETURN_GOODS, returnList, subject);
		}
	}


	/**
	 * 获取推送信息
	 * @param orderReturnId
	 * @return
	 * @throws Exception
	 * @author chenhg
	 * 2016年11月25日 上午10:01:43
	 */
	List<Record> getPushReturnMessage(String orderReturnId) throws Exception {
		List<Record> result = new ArrayList<Record>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT  ");
		sql.append(" oo.no, ");                  //退货订单编号
		sql.append(" o.id returnId, ");           //退货订单id
		sql.append(" u.user_name member, ");      //会员
		sql.append(" u.mobile phone, ");          //会员手机
		sql.append(" o.cash money, ");            //退款金额
		sql.append(" o.store_no storeNo, ");      //云店/仓库编号
		sql.append(" o.apply_time returnTime, "); //退货时间
		sql.append(" o.logistics_no logisticsBillNo, ");//物流单号
		sql.append(" o.logistics_id logisticsNo ");     //物流公司id
		sql.append(" FROM t_order_return o  ");
		sql.append(" LEFT JOIN t_order oo on o.order_id = oo.id");
		sql.append(" LEFT JOIN t_user u ON o.user_id = u.id ");
		sql.append(" WHERE o.id = ? ");

		Record orderReturn = Db.findFirst(sql.toString(), orderReturnId);
		if(orderReturn == null){
			return null;
		}
		
		String queryUrl = LogisticsCompany.dao.getLogisticsUrl(orderReturn.getInt("logisticsNo"), orderReturn.getStr("logisticsBillNo"));
		orderReturn.set("queryUrl", queryUrl);
		
		StringBuffer proSql = new StringBuffer();
		proSql.append("SELECT  ");
		proSql.append("  od.sku_code skuCode,");
		proSql.append("  od.count quantity,");
		proSql.append("  od.price price");
		proSql.append(" FROM t_order_return ore ");
		proSql.append("  LEFT JOIN t_order_detail od ON ore.order_id = od.order_id");
		proSql.append(" WHERE ore.id = ? ");
		
		orderReturn.set("products", Db.find(proSql.toString(), orderReturnId));
		result.add(orderReturn);
		return result;
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
		
		String json = JsonKit.toJson(list);
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
				.set("type",			PushPosRecord.TYPE_RETURN)
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
