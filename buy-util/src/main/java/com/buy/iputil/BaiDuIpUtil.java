package com.buy.iputil;

import com.buy.ip.IpUtil;
import com.buy.string.StringUtil;

/**
 * 百度IP工具
 * @author Sylveon
 */
public class BaiDuIpUtil {
	
	/**
	 * 解析IP地址
	 * @param ip		IP地址
	 * @param encoding	编码格式
	 * @return			返回详细地址
	 */
	public static BaiDuIpReport.AddressDetail getAddrFromBaiDu(String ip, String encoding) {
		// 接口路径
		StringBuffer url = new StringBuffer("http://api.map.baidu.com/location/ip");
		url.append("?ak=6hTbpmm1rwYgSevvOFVKo1QFmRILbUt2");
		url.append("&ip=" + ip);
		// 结果
		BaiDuIpReport.AddressDetail addressDetail = null;
		// 操作
		try {
			String json = IpUtil.getResultFromUrl(url.toString(), encoding);
			addressDetail = new BaiDuIpReport().getIpLocation(json).getAddressDetail();
			return addressDetail;
		} catch (Exception e) {
			System.out.println("解析IP地址失败");
			e.printStackTrace();
			return null;
		}
	}
	
	// 测试
	public static void main(String[] args) {
		String ip = "113.64.67.157";
		BaiDuIpReport.AddressDetail addressDetail = getAddrFromBaiDu(ip, "utf-8");
		if(null == addressDetail) {
			System.out.println("解析IP地址失败");
		} else {
			System.out.println(addressDetail.getProvince());
			System.out.println(addressDetail.getCity());
		}
	}

}
