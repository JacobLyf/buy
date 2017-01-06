package com.buy.ip;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.buy.string.StringUtil;

/**
 * IP工具
 * @author Sylveon
 */
public class IpUtil {
	
	/** IP - 本机IP（IPV6） */
	public static final String IP_LOCATION_IPV6 = "0:0:0:0:0:0:0:1";
	/** IP - 本机IP（IPV4） */
	public static final String IP_LOCATION_IPV4 = "127.0.0.1";
	/** IP - 局域网IP（IPV4） */
	public static final String IP_INTERNAL_IPV4 = "192.168.";
	
	/**
	 * IP地址转化数字（Mysql的INET_ATON函数）
	 * @param ipAddress	IP地址
	 * @return			转化结果
	 */
	public static Long inelAton(String ip) {
		ip = checkIp(ip);
		long result = 0;  
		String[] ipInArray = ip.split("\\.");  
		for (int i = 3; i >= 0; i--) {  
			result |= Long.parseLong(ipInArray[3 - i]) << (i * 8);  
		}
		return result;
	}
	
	/**
	 * 主子转化IP地址（Mysql的INET_NTOA）
	 * @param ipLong	IP Long类型
	 * @return
	 */
	public static String inetNtoa(long ipLong) {
		StringBuilder result = new StringBuilder(15);  
		for (int i = 0; i < 4; i++) {  
		    result.insert(0,Long.toString(ipLong & 0xff));  
		    if (i < 3) {  
		        result.insert(0,'.');  
		    }  
		    ipLong = ipLong >> 8;  
		}  
		return result.toString();  
	}
	
	/**
	 * 检测IP
	 * 服务器和客户端同在一台电脑， IP应为127.0.0.1，但实际获取了IPV6地址
	 * @param ip	IP地址
	 * @return		ip（是本地ip，赋值为127.0.0.1）
	 */
	public static String checkIp(String ip) {
		return StringUtil.equals(IP_LOCATION_IPV6, ip) ? IP_LOCATION_IPV4 : ip;
	}
	
	/**
	 * 局域网IP
	 * @param ip	IP地址
	 * @return
	 */
	public static boolean isLAN(String ip) {
		return StringUtil.equals(IP_LOCATION_IPV4, ip) || ip.indexOf(IP_INTERNAL_IPV4) > 0 ? true : false;
	}
	
	/**
	 * 解析IP地址转化JSON
	 * @param ip		IP地址
	 * @param toUrl		请求接口
	 * @param encoding	编码格式
	 * @return 			JSON
	 * @throws Exception	超时
	 */
	public static String getResultFromUrl(String toUrl, String encoding) {
		URL url = null;
		HttpURLConnection connection = null;
		try {
			url = new URL(toUrl);
			connection = (HttpURLConnection) url.openConnection();// 新建连接实例
			connection.setConnectTimeout(2000);// 设置连接超时时间，单位毫秒
			connection.setReadTimeout(2000);// 设置读取数据超时时间，单位毫秒
			connection.setDoOutput(true);// 是否打开输出流 true|false
			connection.setDoInput(true);// 是否打开输入流true|false
			connection.setRequestMethod("POST");// 提交方法POST|GET
			connection.setUseCaches(false);// 是否缓存true|false
			connection.connect();// 打开连接端口
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());// 打开输出流往对端服务器写数据
//			out.writeBytes(ip);// 写数据,也就是提交你的表单 name=xxx&pwd=xxx
			out.flush();// 刷新
			out.close();// 关闭输出流
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream(), encoding));// 往对端写完数据对端服务器返回数据
			// ,以BufferedReader流来读取
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while (null != (line = reader.readLine())) {
				buffer.append(line);
			}
			reader.close();
			return buffer.toString();
		} catch (IOException e) {
			System.out.println("请求超时");
			e.printStackTrace();
		} finally {
			if (null != connection) {
				connection.disconnect();// 关闭连接
			}
		}
		return null;
	}
	
}
