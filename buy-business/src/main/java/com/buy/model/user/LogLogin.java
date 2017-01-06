package com.buy.model.user;

import java.util.Date;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model
 * 用户 - 登陆记录
 */
public class LogLogin extends Model<LogLogin> {

	private static final long serialVersionUID = 1L;
	public static final LogLogin dao = new LogLogin();
	
	/**
	 * 上一次登录记录 
	 * @param userId	用户Id
	 * @return			登录地区
	 * @author Sylveon
	 */
	public String getLastLoginAddress(String userId) {
		String sql = "select address FROM t_log_user_login and user_id = ? order by create_time desc limit 0, 1";
		return Db.queryStr(sql, userId);
	}
	
	/**
	 * 添加登录记录
	 * @param user	用户信息
	 * @param ip	IP地址
	 * @author Sylveon
	 */
	public void addLogLogin(User user, String ip) {
		/*String address = "";										// IP地区
		if(StringUtil.isNull(ip)) {
			address = "";
		} else if(StringUtil.equals(IpUtil.IP_LOCATION_IPV4, ip) || 0 == ip.indexOf(IpUtil.IP_INTERNAL_IPV4)) {
			// 局域网（测试）
			address = "测试IP";
		} else {
			// 外网
			BaiDuIpReport.AddressDetail addressDetail = BaiDuIpUtil.getAddrFromBaiDu(ip, "UTF-8");
			if(StringUtil.notNull(addressDetail))
				address = addressDetail.getProvince() + addressDetail.getCity();
		}*/
		// 添加登录记录
		new LogLogin()
			.set("id", StringUtil.getUUID())
			.set("user_id", user.get("id"))
			.set("ip", ip)
			//.set("address", address)
			.set("create_time", new Date())
			.save();
	}
	
	/**
	 * 是否第一次登陆
	 */
	public boolean isFirstLogin(String userId){
		String sql = "SELECT COUNT(*) FROM t_log_user_login WHERE user_id = ?";
		return Db.queryLong(sql, userId) > 0L ? false : true;
	}
	
}
