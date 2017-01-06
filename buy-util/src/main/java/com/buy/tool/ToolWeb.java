package com.buy.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.buy.date.DateUtil;
import com.buy.ip.IpUtil;
import com.buy.string.StringUtil;

/**
 * WEB工具类
 * @author 董华健 2012-9-3 下午7:39:43
 */
public abstract class ToolWeb {

	private static Logger log = Logger.getLogger(ToolWeb.class);

	/**
	 * 获取客户端IP地址
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		
		if (StringUtil.notNull(ip)) {
			int index = ip.indexOf(",");
			if (index > 0) {
				for (String s : ip.split(",")) {
					if (!"unknown".equalsIgnoreCase(s)) {
						ip = s.trim();
						break;
					} 
				}
			}
		}
		
		if (null == ip || 0 == ip.length() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (null == ip || 0 == ip.length() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (null == ip || 0 == ip.length() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
			
		return IpUtil.checkIp(ip);
	}

	/**
	 * 获取上下文URL全路径
	 * 
	 * @param request
	 * @return
	 */
	public static String getContextPath(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		StringBuilder serverPort = new  StringBuilder("");
		if(80!=request.getServerPort()){
			serverPort.append(':').append(request.getServerPort());
		}
		sb.append(request.getScheme()).append("://").append(request.getServerName()).append(serverPort).append(request.getContextPath());
		String path = sb.toString();
		sb = null;
		return path;
	}
	/**
	 * 获取域名
	 * 
	 * @param request
	 * @return
	 */
	public static String getDomain(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		StringBuilder serverPort = new  StringBuilder("");
		if(80!=request.getServerPort()){
			serverPort.append(':').append(request.getServerPort());
		}
		sb.append(request.getScheme()).append("://").append(request.getServerName()).append(serverPort);
		String path = sb.toString();
		sb = null;
		return path;
	}

	/**
	 * 获取完整请求路径(含内容路径及请求参数)
	 * 
	 * @param request
	 * @return
	 */
	public static String getRequestURIWithParam(HttpServletRequest request) {
		return request.getRequestURI() + (null == request.getQueryString() ? "" : "?" + request.getQueryString());
	}

	/**
	 * 获取请求参数
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getParam(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (null != value && !value.isEmpty()) {
			try {
				return URLDecoder.decode(value, StringUtil.encoding).trim();
			} catch (UnsupportedEncodingException e) {
				log.error("decode异常：" + value);
				return value;
			}
		}
		return value;
	}

	/**
	 * 获取ParameterMap
	 * @param request
	 * @return
	 */
	public static Map<String, String> getParamMap(HttpServletRequest request){
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> enume = request.getParameterNames();
		while (enume.hasMoreElements()) {
			String name = (String) enume.nextElement();
			map.put(name, request.getParameter(name));
		}
		return map;
	}

	/**
	 * 输出servlet文本内容
	 * 
	 * @author 董华健 2012-9-14 下午8:04:01
	 * @param response
	 * @param content
	 * @param contentType
	 */
	public static void outPage(HttpServletResponse response, String content, String contentType) {
		try {
			outPage(response, content.getBytes(StringUtil.encoding), contentType); // char to byte 性能提升
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出servlet文本内容
	 * 
	 * @author 董华健 2012-9-14 下午8:04:01
	 * @param response
	 * @param content
	 * @param contentType
	 */
	public static void outPage(HttpServletResponse response, byte[] content, String contentType) {
		if(null == contentType || contentType.isEmpty()){
			contentType = "text/html; charset=UTF-8";
		}
		response.setContentType(contentType);
		response.setCharacterEncoding(StringUtil.encoding);
		// PrintWriter out = response.getWriter();
		// out.print(content);
		try {
			response.getOutputStream().write(content);// char to byte 性能提升
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出CSV文件下载
	 * 
	 * @author 董华健 2012-9-14 下午8:02:33
	 * @param response
	 * @param content CSV内容
	 */
	public static void outDownCsv(HttpServletResponse response, String content) {
		response.setContentType("application/download; charset=gb18030");
		try {
			response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(DateUtil.DateToString(new Date(), DateUtil.pattern_ymd_hms_s) + ".csv", StringUtil.encoding));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// PrintWriter out = response.getWriter();
		// out.write(content);
		try {
			response.getOutputStream().write(content.getBytes(StringUtil.encoding));
		} catch (IOException e) {
			e.printStackTrace();
		}// char to byte 性能提升
			// out.flush();
			// out.close();
	}

	/**
	 * 请求流转字符串
	 * 
	 * @param request
	 * @return
	 */
	public static String requestStream(HttpServletRequest request) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			request.setCharacterEncoding(StringUtil.encoding);
			inputStream = (ServletInputStream) request.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, StringUtil.encoding);
			bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while (null != (line = bufferedReader.readLine())) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			log.error("request.getInputStream() to String 异常", e);
			return null;
		} finally { // 释放资源
			if(null != bufferedReader){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error("bufferedReader.close()异常", e);
				}
				bufferedReader = null;
			}
			
			if(null != inputStreamReader){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					log.error("inputStreamReader.close()异常", e);
				}
				inputStreamReader = null;
			}
			
			if(null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("inputStream.close()异常", e);
				}
				inputStream = null;
			}
		}
	}
	
	/**
	 * 
	 * @param response
	 * @param name		cookie的名称
	 * @param value		cookie的值
	 * @param maxAge	cookie存放的时间(以秒为单位,假如存放三天,即3*24*60*60; 如果值为0,cookie将随浏览器关闭而清除)
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		if(maxAge > 0)
			cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}
	 
	/**
	 * 
	 * @param response
	 * @param domain		设置cookie所在域
	 * @param path			设置cookie所在路径
	 * @param isHttpOnly	是否只读
	 * @param name			cookie的名称
	 * @param value			cookie的值
	 * @param maxAge		cookie存放的时间(以秒为单位,假如存放三天,即3*24*60*60; 如果值为0,cookie将随浏览器关闭而清除)
	 */
	public static void addCookie(HttpServletResponse response, 
			String domain, String path, boolean isHttpOnly, 
			String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);

		// 所在域：比如a1.4bu4.com 和 a2.4bu4.com 共享cookie
		if(null != domain && !domain.isEmpty()){
			cookie.setDomain(domain);			
		}
		
		// 设置cookie所在路径
		cookie.setPath("/");
		if(null != path && !path.isEmpty()){
			cookie.setPath(path);				
		}
		
		// 是否只读
		//cookie.setHttpOnly(isHttpOnly);
		
		// 设置cookie的过期时间
		if (maxAge > 0){
			cookie.setMaxAge(maxAge);
		}
		
		// 添加cookie
		response.addCookie(cookie);
	}

	/**
	 * 获取cookie的值
	 * 
	 * @param request
	 * @param name
	 *            cookie的名称
	 * @return
	 */
	public static String getCookieValueByName(HttpServletRequest request, String name) {
		Map<String, Cookie> cookieMap = ToolWeb.readCookieMap(request);
		// 判断cookie集合中是否有我们像要的cookie对象 如果有返回它的值
		if (cookieMap.containsKey(name)) {
			Cookie cookie = (Cookie) cookieMap.get(name);
			return cookie.getValue();
		} else {
			return null;
		}
	}

	/**
	 * 获得cookie
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static Cookie getCookieByName(HttpServletRequest request, String name) {
		Map<String, Cookie> cookieMap = ToolWeb.readCookieMap(request);
		// 判断cookie集合中是否有我们像要的cookie对象 如果有返回它的值
		if (cookieMap.containsKey(name)) {
			Cookie cookie = (Cookie) cookieMap.get(name);
			return cookie;
		} else {
			return null;
		}
	}

	/**
	 * 获得所有cookie
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Cookie> readCookieMap(HttpServletRequest request) {
		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
		// 从request范围中得到cookie数组 然后遍历放入map集合中
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (int i = 0; i < cookies.length; i++) {
				cookieMap.put(cookies[i].getName(), cookies[i]);
			}
		}
		return cookieMap;
	}

	/**
	 * 去除HTML代码
	 * 
	 * @param inputString
	 * @return
	 */
	public static String HtmltoText(String inputString) {
		String htmlStr = inputString; // 含HTML标签的字符串
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		java.util.regex.Pattern p_ba;
		java.util.regex.Matcher m_ba;

		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
																										// }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
																									// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
			String patternStr = "\\s+";

			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签

			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签

			p_ba = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
			m_ba = p_ba.matcher(htmlStr);
			htmlStr = m_ba.replaceAll(""); // 过滤空格

			textStr = htmlStr;

		} catch (Exception e) {
			System.err.println("Html2Text: " + e.getMessage());
		}
		return textStr;// 返回文本字符串
	}

	/**
	 * 把页面的信息替换成我们想要的信息存放数据库里面
	 * 
	 * @param sourcestr
	 *            页面得到的信息
	 * @return
	 */
	public static String getHTMLToString(String sourcestr) {
		if (null == sourcestr) {
			return "";
		}
		sourcestr = sourcestr.replaceAll("\\x26", "&amp;");// &
		sourcestr = sourcestr.replaceAll("\\x3c", "&lt;");// <
		sourcestr = sourcestr.replaceAll("\\x3e", "&gt;");// >
		sourcestr = sourcestr.replaceAll("\\x09", "&nbsp;&nbsp;&nbsp;&nbsp;");// tab键
		sourcestr = sourcestr.replaceAll("\\x20", "&nbsp;");// 空格
		sourcestr = sourcestr.replaceAll("\\x22", "&quot;");// "

		sourcestr = sourcestr.replaceAll("\r\n", "<br>");// 回车换行
		sourcestr = sourcestr.replaceAll("\r", "<br>");// 回车
		sourcestr = sourcestr.replaceAll("\n", "<br>");// 换行
		return sourcestr;
	}

	/**
	 * 把数据库里面的信息回显到页面上
	 * 
	 * @param sourcestr
	 *            数据库取得的信息
	 * @return
	 */
	public static String getStringToHTML(String sourcestr) {
		if (null == sourcestr) {
			return "";
		}
		sourcestr = sourcestr.replaceAll("&amp;", "\\x26");// &
		sourcestr = sourcestr.replaceAll("&lt;", "\\x3c");// <
		sourcestr = sourcestr.replaceAll("&gt;", "\\x3e");// >
		sourcestr = sourcestr.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;", "\\x09");// tab键
		sourcestr = sourcestr.replaceAll("&nbsp;", "\\x20");// 空格
		sourcestr = sourcestr.replaceAll("&quot;", "\\x22");// "

		sourcestr = sourcestr.replaceAll("<br>", "\r\n");// 回车换行
		sourcestr = sourcestr.replaceAll("<br>", "\r");// 回车
		sourcestr = sourcestr.replaceAll("<br>", "\n");// 换行
		return sourcestr;
	}

}
