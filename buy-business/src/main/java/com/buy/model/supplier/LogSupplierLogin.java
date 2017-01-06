package com.buy.model.supplier;

import java.util.Date;

import com.buy.model.shop.LogShopLogin;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model
 * 供货商 - 登陆记录
 */
public class LogSupplierLogin extends Model<LogSupplierLogin> {

	private static final long serialVersionUID = 1L;
	public static final LogSupplierLogin dao = new LogSupplierLogin();
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
	 * @param supplierId	供货商Id
	 * @return			登录地区
	 * @author jekay
	 */
	public String getLastLoginAddress(String supplierId) {
		String sql = "select address FROM t_log_supplier_login and supplier_id = ? order by create_time desc limit 0, 1";
		return Db.queryStr(sql, supplierId);
	}
	
	/**
	 * 添加登录记录
	 * @param supplier	供货商信息
	 * @param ip	IP地址
	 * @author jekay
	 */
	public void addLogSupplierLogin(Supplier supplier, String ip) {
		// 添加登录记录
		new LogSupplierLogin()
			.set("id", StringUtil.getUUID())
			.set("supplier_id", supplier.get("id"))
			.set("ip", ip)
			.set("create_time", new Date())
			.save();
	}
	
	/**
	 * 是否第一次登陆
	 */
	public boolean isFirstLogin(String supplierId){
		String sql = "SELECT COUNT(*) FROM t_log_supplier_login WHERE supplier_id = ?";
		return Db.queryLong(sql, supplierId) > 0L ? false : true;
	}
	
	/**
	 * 是否第一次获取MAPP提示
	 * @param shopId
	 * @return true则为第一次
	 */
	public boolean getFirstPrompt(String supplieId) {
		// 如果有一条记录 供货商 的show_mapp等于0 都等于已经提示过则返回false
		String sql = "SELECT COUNT(*) FROM t_log_supplier_login WHERE supplier_id = ? and show_mapp = ?";
		Long flag = Db.queryLong(sql, supplieId, NOT_SHOW);
		if (flag > 0L) {			
			return false;
		} else {
			Db.update("update t_log_supplier_login SET show_mapp = ? where supplier_id = ? AND show_mapp = ?"
					+ " ORDER BY create_time DESC LIMIT 1", NOT_SHOW, supplieId, IS_SHOW);
			return true;
		}
	}
	
	/**
	 * 是否显示IM上线
	 * @param shopId
	 * @return
	 */
	public boolean getIsShowIm(String supplieId) {
		// 如果有一条记录 店铺 的show_im等于0 都等于已经提示过则返回false
		String sql = "select count(*) from t_log_supplier_login where supplier_id = ? and show_im = ?";
		Long flag = Db.queryLong(sql, supplieId, NOT_SHOW);
		return flag > 0L ? false : true;
	}
	
	/**
	 * 不再提示IM上线
	 * @param shopId
	 */
	public void notShowIm(String supplieId) {
		Db.update("update t_log_supplier_login set show_im = ? where supplier_id = ? and show_im = ? "
				+ " ORDER BY create_time DESC LIMIT 1", NOT_SHOW, supplieId, IS_SHOW);
	}
}
