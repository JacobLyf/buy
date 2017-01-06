package com.buy.iputil;

import com.google.gson.Gson;

/**
 * 淘宝IP返回数据
 * 
 * @author Sylvon
 */
public class TaoBaoIpReport {

	/*
	 * 以后看根据需要扩展，添加属性 暂返回详细地址类
	 */
	
	private TaoBaoIpReport.Data data;	// 返回数据内容
	
	/**
	 * 解析IP地址所在地区
	 * @param jsonResult	向淘宝发送请求返回的数据
	 * @return				IP地址所在地区
	 * @throws Exception	报错解析失败
	 */
	public TaoBaoIpReport getIpLocation (String jsonResult) throws Exception {
		Gson g = new Gson();
		// 输出返回结果
		System.out.println(jsonResult);
		// 解析返回数据
		TaoBaoIpReport.Result result = g.fromJson(jsonResult, TaoBaoIpReport.Result.class);
		// 解析详细内容
		TaoBaoIpReport.Data data = g.fromJson(g.toJson(result.getData()), TaoBaoIpReport.Data.class);
		// 返回数据
		TaoBaoIpReport report = new TaoBaoIpReport();
		report.setData(data);
		return report;
	}

	/**
	 * 返回数据
	 */
	public class Result {

		private int code;			// 状态
		private Object data;		// 内容

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

	}

	/**
	 * 返回数据 - 内容
	 */
	public class Data {
		
		private String country; 	// 国家
		private String country_id;	// 国家ID（英文简写）
		private String area;
		private String area_id;
		private String region; 		// 省
		private String region_id;
		private String city; 		// 城市
		private String city_id;
		private String county_id;
		private String isp;
		private String isp_id;
		private String ip;

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getCountry_id() {
			return country_id;
		}

		public void setCountry_id(String country_id) {
			this.country_id = country_id;
		}

		public String getArea() {
			return area;
		}

		public void setArea(String area) {
			this.area = area;
		}

		public String getArea_id() {
			return area_id;
		}

		public void setArea_id(String area_id) {
			this.area_id = area_id;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getRegion_id() {
			return region_id;
		}

		public void setRegion_id(String region_id) {
			this.region_id = region_id;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getCity_id() {
			return city_id;
		}

		public void setCity_id(String city_id) {
			this.city_id = city_id;
		}

		public String getCounty_id() {
			return county_id;
		}

		public void setCounty_id(String county_id) {
			this.county_id = county_id;
		}

		public String getIsp() {
			return isp;
		}

		public void setIsp(String isp) {
			this.isp = isp;
		}

		public String getIsp_id() {
			return isp_id;
		}

		public void setIsp_id(String isp_id) {
			this.isp_id = isp_id;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

	}

	public TaoBaoIpReport.Data getData() {
		return data;
	}

	public void setData(TaoBaoIpReport.Data data) {
		this.data = data;
	}

}
