package com.buy.model.logistics;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class LogisticsCompany extends Model<LogisticsCompany>{
	
	/**
	 * 快递100地址
	 */
	public static final String URL = "http://api.kuaidi100.com/api";
	/**
	 * 快递100HtmlAPI地址
	 */
	public static final String HtmlAPI_URL = "http://www.kuaidi100.com/applyurl";
	/**
	 * 快递100HtmlAPI地址
	 */
	public static final String HtmlAPI_URL_PHONE = "http://m.kuaidi100.com/index_all.html";
	/**
	 * 快递100id
	 */
	public static final String KEY = "c3de58ffa5b04990";
	/**
	 * 快递100返回信息格式
	 */
	public static final String SHOW = "json";
	/**
	 * 快递100返回信息字符编码
	 */
	public static final String MUTI = "1";
	/**
	 * 快递100返回信息排序
	 */
	public static final String ORDER = "desc";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static LogisticsCompany dao = new LogisticsCompany();
	
	/**
	 * 获取物流信息访问链接
	 * @param logisticsCompanyId 物流公司ID
	 * @param logisticsNo 快递单号
	 * @author Jacob
	 * 2016年1月5日下午3:23:30
	 * @throws Exception 
	 */
	public String getLogisticsUrl(Integer logisticsCompanyId,String logisticsNo) throws Exception{
		String logisticsCompanyCode = 
				logisticsCompanyId!=null?
						dao.findByIdLoadColumns(logisticsCompanyId, "company_code").getStr("company_code"):"";
		/*String url = "http://api.kuaidi100.com/api?id=c3de58ffa5b04990&com="+logisticsCompanyCode
				+ "&nu="+logisticsNo+"&show=0&muti=1&order=desc";*/
		logisticsNo = StringUtil.isNull(logisticsNo)?"":logisticsNo.trim();
		logisticsCompanyCode = StringUtil.isNull(logisticsCompanyCode)?"":logisticsCompanyCode.trim();
		String strURL = HtmlAPI_URL+"?key="+KEY+"&com="+logisticsCompanyCode+"&nu="+logisticsNo;
		String logisticsUrl = StringUtil.postRequest(strURL, "utf8");
		return logisticsUrl;
	}
	
	/**
	 * 获取物流信息访问链接(手机版)
	 * @param logisticsCompanyId 物流公司ID
	 * @param logisticsNo 快递单号
	 * @author Jacob
	 * 2016年1月5日下午3:23:30
	 * @throws Exception 
	 */
	public String getLogisticsUrl4Phone(Integer logisticsCompanyId,String logisticsNo,String callbackUrl) throws Exception{
		String logisticsCompanyCode = 
				logisticsCompanyId!=null?
						dao.findByIdLoadColumns(logisticsCompanyId, "company_code").getStr("company_code"):"";
		/*String url = "http://api.kuaidi100.com/api?id=c3de58ffa5b04990&com="+logisticsCompanyCode
				+ "&nu="+logisticsNo+"&show=0&muti=1&order=desc";*/
		logisticsNo = StringUtil.isNull(logisticsNo)?"":logisticsNo.trim();
		logisticsCompanyCode = StringUtil.isNull(logisticsCompanyCode)?"":logisticsCompanyCode.trim();
		String logisticsUrl = HtmlAPI_URL_PHONE+"?&type="+logisticsCompanyCode+"&postid="+logisticsNo+"&callbackurl="+callbackUrl;
		return logisticsUrl;
	}

	/**
	 * 查找物流公司名稱
	 */
	public String getLogisticsName(int logisticsCompanyId) {
		return Db.queryStr("SELECT `name` FROM t_logistics_company WHERE id = ?", logisticsCompanyId);

	}

}
