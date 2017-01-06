package com.buy.plugin.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.buy.date.DateUtil;
import com.buy.string.StringUtil;

/**
 * 用于发送短信的工具
 * @author Eriol
 *
 */
public class SMSUtil {
	
	private static final String SPCODE = "223414";//企业
	private static final String LOGINNAME = "admin3";//账号名
	private static final String PASSWORD = "efun4009999630";//密码
	
	private static final String BASE_URL = "http://sms.api.ums86.com:8899/sms/Api/";
	
	private static String getUrl(String mobile, String content, String scheduleTime) throws UnsupportedEncodingException {
		// 处理
		content=content.replaceAll("<br/>", " ");
		content = URLEncoder.encode(content.toString(), "GBK");
		String serialNumber = StringUtil.getRandomNum(6)+DateUtil.DateToString(new Date(), "yyyyMMddHHmmss");
		scheduleTime = StringUtil.isNull(scheduleTime) ? "" : scheduleTime;
		// 设置url
		StringBuffer url = new StringBuffer(BASE_URL + "Send.do");
		url.append("?SpCode="			+ SPCODE);
		url.append("&LoginName=" 		+ LOGINNAME);
		url.append("&Password=" 		+ PASSWORD);
		url.append("&UserNumber="		+ mobile);
		url.append("&MessageContent=" 	+ content);
		url.append("&SerialNumber=" 	+ serialNumber);
		url.append("&ScheduleTime=" 	+ scheduleTime);
		url.append("&f=1");
		return url.toString();
	}
	
	/**
	 * 发送短信
	 * @param mobile		手机号码
	 * @param content		发送内容
	 * @param scheduleTime	定时发送格式！格式：yyyyMMddHHmmss如20140721152435，为空表示立即发送
	 * @return status 		状态码，等于0则发送成功，否则则失败
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @author Eriol
	 * @author Sylveon modify
	 */
	public static int sendSMS(String mobile, String content, String scheduleTime) throws MalformedURLException, UnsupportedEncodingException {
		URL url = new URL(getUrl(mobile, content, scheduleTime));
		
		// 打开和URL之间的连接
		HttpURLConnection conn;
        long status = 0;
        PrintWriter out = null;
        BufferedReader in = null; 
		try {
			conn = (HttpURLConnection) url.openConnection();
	        // 设置通用的请求属性
			conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-Type", "application/octet-stream"); 
	        conn.setRequestProperty("connection", "Keep-Alive");
	        // 发送POST请求必须设置如下两行
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
            
	        // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            
            // flush输出流的缓冲
            out.flush();
            String result = "";					
            System.out.println("开始发送短信手机号码为 ：" + mobile);
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),"GBK"));
            
            result = in.readLine();
            Map<String,String> resultMap = changeResultToMap(result);
			status = Long.parseLong(resultMap.get("result"));
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("网络异常,发送短信失败！");
			status = -20;
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
			}
		}
		System.out.println("结束发送短信返回值：  " + status);
		return (int) status;
	}
	
	/**
	 * 批量发送短信,向多个手机号码发送相同的信息
	 * @param Mobile
	 * @param Content
	 * @param scheduleTime 定时发送格式！格式：yyyyMMddHHmmss如20140721152435，为空表示立即发送
	 * @return map   result为状态，faillist为失败号码列表
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @author Eriol
	 * @date 2015年7月10日下午1:27:38
	 */
	public static Map<String,String> batchSendSMS(String[] mobiles,String content,String scheduleTime) throws MalformedURLException, UnsupportedEncodingException {
		String mobileStr = "";
		for(String m:mobiles){
			mobileStr += m+",";
		}
		mobileStr = mobileStr.substring(0,mobileStr.length()-1);
		URL url = null;
		String send_content=content.replaceAll("<br/>", " ");//发送内容
		send_content = new String(send_content.getBytes("GBK"),"GBK");//设置发送的内容编码为GBK
		String SerialNumber = StringUtil.getRandomNum(6)+DateUtil.DateToString(new Date(), "yyyyMMddHHmmss");
		
		url = new URL(BASE_URL+"Send.do?SpCode="+SPCODE+"&LoginName="+LOGINNAME+"&Password="+PASSWORD+"&UserNumber="+mobileStr+"&MessageContent="+send_content+"&SerialNumber="+SerialNumber+"&ScheduleTime="+scheduleTime+"&f=1");
		BufferedReader in = null;
		
		String result = "";
		Map<String,String> resultMap = null;
		try {
			System.out.println("开始发送短信手机号码为 ："+mobileStr);
			in = new BufferedReader(new InputStreamReader(url.openStream(),"GBK"));
			result = in.readLine();
			resultMap = changeResultToMap(result);
		} catch (Exception e) {
			System.out.println("网络异常,发送短信失败！");
			resultMap = new HashMap<String,String>();
			resultMap.put("status", "-20");
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		return resultMap;
	}
	
	/**
	 * 查询剩余短信数
	 * @return
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws status 状态码，大于0则为剩余短信数，否则则失败
	 * @author Eriol
	 * @date 2015年7月10日下午2:02:59
	 */
	public static long checkBalance()  throws MalformedURLException, UnsupportedEncodingException {
		URL url = null;
		url = new URL(BASE_URL+"SearchNumber.do?SpCode="+SPCODE+"&LoginName="+LOGINNAME+"&Password="+PASSWORD);
		BufferedReader in = null;
		long resultNum = 0;
		try {
			in = new BufferedReader(new InputStreamReader(url.openStream(),"GBK"));
			String result = in.readLine();
			Map<String,String> resultMap = SMSUtil.changeResultToMap(result);
			if(null != resultMap.get("number")){
				resultNum = Long.parseLong(resultMap.get("number"));
			}else{
				resultNum = Long.parseLong(resultMap.get("status"));
			}
		} catch (Exception e) {
			System.out.println("网络异常,发送短信失败！");
			resultNum=20;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		System.out.println("查询剩余短信数返回值：  "+resultNum);
		return resultNum;
	}
	
	/**
	 * 将短信接口返回结果为map
	 * @param result
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月21日下午3:28:05
	 */
	private static Map<String, String> changeResultToMap(String result){
		String[] results = result.split("&");
		Map<String,String> resultMap = new HashMap<String,String>();
		for (String res : results) {
			int pos = res.indexOf('=');
			String key = res.substring(0,pos);
			String value = res.substring(pos+1, res.length());
			resultMap.put(key,value);
		}
		return resultMap;
	}
	
	public static void main(String[] args) {
		try {
//			SMSUtil.sendSMS("13794414881", "尊敬的Nya,您于2015年07月15日16:35:16参与的幸运一折购商品:奖区为:zgs498133,抽奖号为:19;感谢您的参与,祝您好运!回复TD拒收", "");
			SMSUtil.checkBalance();
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
