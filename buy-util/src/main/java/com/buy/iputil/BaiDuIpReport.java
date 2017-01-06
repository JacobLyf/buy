package com.buy.iputil;

import com.google.gson.Gson;

/**
 * 百度IP返回数据
 * @author Sylvon
 */
public class BaiDuIpReport {

	/*
	 * 以后看根据需要扩展，添加属性
	 * 暂返回详细地址类
	 */
	
	private BaiDuIpReport.AddressDetail addressDetail;	// 详细地址
	
	/**
	 * 解析IP地址所在地区
	 * @param jsonResult	向百度发送请求返回的数据
	 * @return				IP地址所在地区
	 * @throws Exception	报错解析失败
	 */
	public BaiDuIpReport getIpLocation (String jsonResult) throws Exception {
			Gson g = new Gson();
			// 输出返回结果
			System.out.println(jsonResult);
			// 解析返回数据
			BaiDuIpReport.Result result = g.fromJson(jsonResult, BaiDuIpReport.Result.class);
			// 解析详细内容
			BaiDuIpReport.Content content = g.fromJson(g.toJson(result.getContent()), BaiDuIpReport.Content.class);
			// 解析详细地址
			BaiDuIpReport.AddressDetail addressDetail = g.fromJson(g.toJson(content.getAddress_detail()), BaiDuIpReport.AddressDetail.class);
			System.out.println(addressDetail);
			// 返回数据
			BaiDuIpReport report = new BaiDuIpReport();
			report.setAddressDetail(addressDetail);
		return report;
	}

	/**
	 * 返回数据
	 */
	public class Result {

		private String address;				// 地址
		private Object content; 			// 详细内容
		private int status;					// 返回状态码

		public String getAddress() {
			return address;
		}
		
		public void setAddress(String address) {
			this.address = address;
		}

		public Object getContent() {
			return content;
		}

		public void setContent(Object content) {
			this.content = content;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}
		
	}

	/**
	 * 返回数据 - 详细内容
	 */
	public class Content {

		private String address;			 	// 简要地址
		private Object address_detail;		// 详细地址信息
		private Object point;				// 百度经纬度坐标值

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Object getAddress_detail() {
			return address_detail;
		}

		public void setAddress_detail(Object address_detail) {
			this.address_detail = address_detail;
		}

		public Object getPoint() {
			return point;
		}

		public void setPoint(Object point) {
			this.point = point;
		}

	}

	/**
	 * 返回数据 - 详细内容 - 详细地址信息
	 */
	public class AddressDetail {
		private String city; 				// 城市
		private int city_code; 				// 百度城市代码
		private String district; 			// 区县
		private String province; 			// 省份
		private String street; 				// 街道
		private String street_number; 		// 门址

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public int getCity_code() {
			return city_code;
		}

		public void setCity_code(int city_code) {
			this.city_code = city_code;
		}

		public String getDistrict() {
			return district;
		}

		public void setDistrict(String district) {
			this.district = district;
		}

		public String getProvince() {
			return province;
		}

		public void setProvince(String province) {
			this.province = province;
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getStreet_number() {
			return street_number;
		}

		public void setStreet_number(String street_number) {
			this.street_number = street_number;
		}

	}

	/**
	 * 返回数据 - 详细内容 - 百度经纬度坐标值
	 */
	public class Point {
		
		private String x; 					// x坐标
		private String y; 					// y坐标

		public String getX() {
			return x;
		}

		public void setX(String x) {
			this.x = x;
		}

		public String getY() {
			return y;
		}

		public void setY(String y) {
			this.y = y;
		}

	}

	public BaiDuIpReport.AddressDetail getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(BaiDuIpReport.AddressDetail addressDetail) {
		this.addressDetail = addressDetail;
	}

}
