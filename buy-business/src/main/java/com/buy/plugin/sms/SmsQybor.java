package com.buy.plugin.sms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.buy.string.StringUtil;
import com.google.gson.Gson;

public class SmsQybor {
	
	// http://www.qybor.com/qyb/
	static final String USERNAME = "gzsssm88";	// 短信平台账号
	static final String PASSWORD = "89090040";	// 短信平台密码
	
	public static final int SMS_ONE_COUNT = 5000;
	
	private String phone;						// 手机号码
	private String msg;							// 短信内容
	private String needstatus;					// 是否需要状态报告
	private String port;						// 扩展码，用户定义扩展码
	private String sendtime;					// 发送时间
	
	/**
	 * 构造方法- 单发
	 */
	public SmsQybor(String phone, String msg, boolean needstatus, String port, Date sendtime) {
		this.phone = phone;
		this.msg = msg;
		this.needstatus = needstatus + "";
		
		this.port = "";
		if (StringUtil.notBlank(port)) {
			port = port.trim();
			if (port.length() > 3)
				this.port = port.substring(0, 3);
		}
		
		this.sendtime = "";
		if (sendtime !=null )
			this.sendtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sendtime);
	}
	
	/**
	 * 构造方法- 群发
	 */
	public SmsQybor(List<String> phones, String msg, boolean needstatus, String port, Date sendtime) {
		this.phone = StringUtil.listToString(",", phones);
		this.msg = msg;
		this.needstatus = needstatus + "";
		
		this.port = "";
		if (StringUtil.notBlank(port)) {
			port = port.trim();
			if (port.length() > 3)
				this.port = port.substring(0, 3);
		}
		
		this.sendtime = "";
		if (sendtime!=null)
			this.sendtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sendtime);
	}

	/**
	 * 发送短信
	 * 短信相同 - 单发/群发（根据构造方法不同）
	 */
	public Report sms() {
		return sendMsg(this.phone, this.msg, this.needstatus, port, this.sendtime);
	}
	
	/**
	 * @param phone			合法的手机号码，号码间用英文逗号分隔
	 * @param msg			短信内容(包含签名)
	 * @param isNeedReport	是否需要状态报告，取值true或false
	 * @param port			扩展码，用户定义扩展码(不能超过三位)
	 * @param sendDate		发送时间
	 * @throws Exception
	 */
	Report sendMsg(String phone, String msg, String needstatus, String port, String sendtime) {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod("http://www.qybor.com:8500/shortMessage");
		post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

		NameValuePair[] data = {
			new NameValuePair("username", 	USERNAME),
			new NameValuePair("passwd", 	PASSWORD),
			new NameValuePair("phone", 		phone),
			new NameValuePair("msg", 		msg),
			new NameValuePair("needstatus",	needstatus),
			new NameValuePair("port", 		port),
			new NameValuePair("sendtime", 	sendtime)
		};

		Logger L = Logger.getLogger(SmsQybor.class);
		try {
			
			L.info("发送短信:");
			post.setRequestBody(data);
			client.executeMethod(post);
			Header[] headers = post.getResponseHeaders();
			
			L.info("发送短信Header:");
			for (Header h : headers)
				L.info(h.getName() + " -- " +h.getValue());
			
			L.info("发送短信状态:" + post.getStatusCode());
			String result = new String(post.getResponseBodyAsString().getBytes());
			
			L.info("发送短信结果:" + result);
			post.releaseConnection();
			Gson gson = new Gson();
			SmsQybor.Report report = gson.fromJson(result, SmsQybor.Report.class);
			return report;
			
		} catch (Exception e) {
			e.printStackTrace();
			SmsQybor.Report report = new Report();
			return report.setBatchno(null).setRespcode("-1").setRespdesc("系统异常");
		}
	}

	public class Report {
		
		private String batchno;
		private String respcode;
		private String respdesc;

		public String getBatchno()	{ return batchno; }
		public Report setBatchno(String batchno) {
			this.batchno = batchno;
			return this;
		}
		
		public String getRespcode()	{ return respcode; }
		public Report setRespcode(String respcode) {
			this.respcode = respcode;
			return this;
		}
		
		public String getRespdesc()	{ return respdesc; }
		public Report setRespdesc(String respdesc) {
			this.respdesc = respdesc;
			return this;
		}
	}
	
}