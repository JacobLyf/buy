package com.buy.model.agent;

import java.util.Date;

import com.buy.model.shop.LogShopLogin;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * Model
 * 代理商 - 登陆记录
 */
public class LogAgentLogin extends Model<LogAgentLogin> {

	private static final long serialVersionUID = 1L;
	public static final LogAgentLogin dao = new LogAgentLogin();
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
	 * @param agentId	代理商Id
	 * @return			登录地区
	 * @author jekay
	 */
	public String getLastLoginAddress(String agentId) {
		String sql = "select address FROM t_log_agent_login and agent_id = ? order by create_time desc limit 0, 1";
		return Db.queryStr(sql, agentId);
	}
	
	/**
	 * 添加登录记录
	 * @param agent	代理商信息
	 * @param ip	IP地址
	 * @author jekay
	 */
	public void addLogAgentLogin(Agent agent, String ip) {
		// 添加登录记录
		new LogAgentLogin()
			.set("id", StringUtil.getUUID())
			.set("agent_id", agent.get("id"))
			.set("ip", ip)
			.set("create_time", new Date())
			.save();
	}
	
	/**
	 * 是否第一次登陆
	 */
	public boolean isFirstLogin(String agentId){
		String sql = "SELECT COUNT(*) FROM t_log_agent_login WHERE agent_id = ?";
		return Db.queryLong(sql, agentId) > 0L ? false : true;
	}
	
	/**
	 * 是否第一次获取MAPP提示
	 * @param shopId
	 * @return true则为第一次
	 */
	public boolean getFirstPrompt(String shopId) {
		// 如果有一条记录 代理商 的show_mapp等于0 都等于已经提示过则返回false
		String sql = "SELECT COUNT(*) FROM t_log_agent_login WHERE agent_id = ? and show_mapp = ?";
		Long flag = Db.queryLong(sql, shopId, LogShopLogin.NOT_SHOW);
		if (flag > 0L) {
			return false;
		} else {
			Db.update("update t_log_agent_login SET show_mapp = ? where agent_id = ? AND show_mapp = ? "
					+ " ORDER BY create_time DESC LIMIT 1 ", NOT_SHOW, shopId, IS_SHOW);
			return true;
		}
	}
}
