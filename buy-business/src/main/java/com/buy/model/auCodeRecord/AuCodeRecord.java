package com.buy.model.auCodeRecord;

import java.util.Date;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 验证码访问记录表
 * @author Eriol
 *
 */
public class AuCodeRecord extends Model<AuCodeRecord>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final AuCodeRecord dao = new AuCodeRecord();
	
	/**
	 * 根据请求的ip获取规定时间内访问的次数
	 * @param hostIp
	 * @param illegalTime
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月22日下午1:21:02
	 */
	public long getReqCountByIp(String hostIp,String legalTime, String mobile){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	count(acr.id) reqCount ");
		sql.append("FROM ");
		sql.append("	t_auth_code_record acr ");
		sql.append("WHERE ");
		sql.append("	acr.host_ip LIKE ? ");
		sql.append("AND acr.create_time >= 	TIMESTAMP(?) ");
		sql.append("AND mobile_num = ? ");
		return Db.queryLong(sql.toString(), hostIp,legalTime,mobile);
	}
	
	/**
	 * 根据手机号码获取规定时间内访问的次数
	 * @param mobileNum
	 * @param illegalTime
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年7月22日下午1:21:02
	 */
	public long getReqCountByMobile(String mobileNum,String legalTime){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	count(acr.id) reqCount ");
		sql.append("FROM ");
		sql.append("	t_auth_code_record acr ");
		sql.append("WHERE ");
		sql.append("	acr.mobile_num LIKE ? ");
		sql.append("AND acr.create_time >= 	TIMESTAMP(?) ");
		return Db.queryLong(sql.toString(),mobileNum,legalTime);
	}
	
	/**
	 * 保存验证码请求记录
	 * @param hostIp
	 * @param telephone
	 * @param authCode
	 * @throws
	 * @author Eriol
	 * @date 2015年7月22日下午3:36:40
	 */
	public void saveAuCodeRecord(String hostIp,String telephone,String authCode){
		AuCodeRecord auCodeRecord = new AuCodeRecord();
		auCodeRecord.set("id", StringUtil.getUUID());
		auCodeRecord.set("host_ip", hostIp);
		auCodeRecord.set("mobile_num", telephone);
		auCodeRecord.set("auth_code", authCode);
		auCodeRecord.set("create_time", new Date());
		auCodeRecord.save();
	}
}
