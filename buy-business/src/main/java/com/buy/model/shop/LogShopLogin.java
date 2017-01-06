package com.buy.model.shop;

import java.util.Date;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model
 * 店主 - 登陆记录
 */
public class LogShopLogin extends Model<LogShopLogin> {

	private static final long serialVersionUID = 1L;
	public static final LogShopLogin dao = new LogShopLogin();
	/**
	 * 是否显示提示（是）
	 */
	public final static int IS_SHOW = 1;
	/**
	 * 是否显示提示（否）
	 */
	public final static int NOT_SHOW = 0;
	/**
	 * 上一次登录记录 
	 * @param shopId	店主Id
	 * @return			登录地区
	 * @author jekay
	 */
	public String getLastLoginAddress(String shopId) {
		String sql = "select address FROM t_log_shop_login and shop_id = ? order by create_time desc limit 0, 1";
		return Db.queryStr(sql, shopId);
	}
	
	/**
	 * 添加登录记录
	 * @param shop	店主信息
	 * @param ip	IP地址
	 * @author jekay
	 */
	public void addLogShopLogin(Shop shop, String ip) {
		
		// 添加登录记录
		new LogShopLogin()
			.set("id", StringUtil.getUUID())
			.set("shop_id", shop.get("id"))
			.set("ip", ip)
			.set("create_time", new Date())
			.save();
	}
	
	/**
	 * 是否第一次登陆
	 */
	public boolean isFirstLogin(String shopId){
		String sql = "SELECT COUNT(*) FROM t_log_shop_login WHERE shop_id = ?";
		return Db.queryLong(sql, shopId) > 0L ? false : true;
	}
	
	/**
	 * 是否第一次获取MAPP提示
	 * @param shopId
	 * @return true则为第一次
	 */
	public boolean getFirstPrompt(String shopId) {
		// 如果有一条记录 店铺 的show_mapp等于0 都等于已经提示过则返回false
		String sql = "SELECT COUNT(*) FROM t_log_shop_login WHERE shop_id = ? and show_mapp = ?";
		Long flag = Db.queryLong(sql, shopId, LogShopLogin.NOT_SHOW);
		if (flag > 0L) {			
			return false;
		} else {
			Db.update("update t_log_shop_login SET show_mapp = ? where shop_id = ? AND show_mapp = ?"
					+ " ORDER BY create_time DESC LIMIT 1 ", NOT_SHOW, shopId, IS_SHOW);
			return true;
		}
	}
	
	/**
	 * 是否显示IM上线
	 * @param shopId
	 * @return
	 */
	public boolean getIsShowIm(String shopId) {
		// 如果有一条记录 店铺 的show_im等于0 都等于已经提示过则返回false
		String sql = "select count(*) from t_log_shop_login where shop_id = ? and show_im = ?";
		Long flag = Db.queryLong(sql, shopId, LogShopLogin.NOT_SHOW);
		return flag > 0L ? false : true;
	}
	
	/**
	 * 不再提示IM上线
	 * @param shopId
	 */
	public void notShowIm(String shopId) {
		Db.update("update t_log_shop_login set show_im = ? where shop_id = ? and show_im = ? "
				+ " ORDER BY create_time DESC LIMIT 1", NOT_SHOW, shopId, IS_SHOW);
	}
}
