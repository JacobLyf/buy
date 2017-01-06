package com.buy.iputil;

import com.buy.ip.IpUtil;
import com.buy.string.StringUtil;

/**
 * 淘宝IP工具
 * @author Sylveon
 */
public class TaoBaoIpUtil {
	
	/**
	 * 解析IP地址
	 * @param ip		IP地址
	 * @param encoding	编码格式
	 * @return			返回主要内容
	 */
	public static TaoBaoIpReport.Data getAddrFromTaobao(String ip, String encoding) {
		// 接口路径
		StringBuffer url = new StringBuffer("http://ip.taobao.com/service/getIpInfo.php");
		url.append("?ip=" + ip);
		// 结果
		TaoBaoIpReport.Data data = null;
		// 操作
		try {
			String json = IpUtil.getResultFromUrl(url.toString(), encoding);
			data = new TaoBaoIpReport().getIpLocation(json).getData();
			return data;
		} catch (Exception e) {
			System.out.println("解析IP地址失败");
			e.printStackTrace();
			return null;
		}
	}

	// 测试
	public static void main(String[] args) {
		String ip= "14.23.60.98";
		TaoBaoIpReport.Data data = getAddrFromTaobao(ip, "utf-8");
		if(null == data) {
			System.out.println("解析IP地址失败");
		} else {
			System.out.println(data.getRegion());
			System.out.println(data.getCity());
		}
		
	}
	
}
