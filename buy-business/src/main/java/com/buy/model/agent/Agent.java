package com.buy.model.agent;

import com.buy.model.shop.Shop;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class Agent extends Model<Agent>{
	
	private static final long serialVersionUID = 1L;
	
	

	
	public final static Agent dao = new Agent();
	
	/**
	 * 代理商是否存在
	 * @param agentId	代理商ID
	 * @param agentNo	代理商编号
	 * @return			true 存在； false 不存在
	 * @author			Sylveon
	 */
	public boolean existAgent(String agentId, String agentNo) {
		String sql = "SELECT id FROM t_agent WHERE 1 = 1 AND id != ? AND no = ?";
		Agent agent = Agent.dao.findFirst(sql, agentId, agentNo);
		boolean result = agent!=null ? true : false;
		return result;
	}
	
	/**
	 * 代理商是否存在
	 * @param agentNo	代理商编号
	 * @return			true 存在； false 不存在
	 * @author			Sylveon
	 */
	public boolean existAgent(String agentNo) {
		String sql = "SELECT id FROM t_agent WHERE 1 = 1 AND no = ?";
		Agent agent = Agent.dao.findFirst(sql, agentNo);
		boolean result = agent!=null ? true : false;
		return result;
	}
	
	/**
	 * 根绝代理商编号查找ID
	 * @param agentNo	代理商编号
	 * @return			代理商ID
	 * @author			Sylveon
	 */
	public String getIdByNo(String agentNo) {
		String sql = "SELECT id FROM t_agent WHERE no = ?";
		String result = Db.queryStr(sql, agentNo);
		return result;
	}
	
	/**
	 * 手机是否存在
	 * @param mobile	手机
	 * @return			true 存在, false 不存在
	 * @author Sylveon
	 */
	public boolean existMobile(String mobile) {
		return Db.queryLong("SELECT count(*) FROM t_agent WHERE mobile = ?", mobile) > 0 ? true : false;
	}
	
	/**
	 * 根据用户ID查找手机号码
	 * @param agentId	代理商ID
	 * @return			手机号码
	 * @author Sylveon
	 */
	public String getMobileByAgentId(String agentId) {
		return Db.queryStr("SELECT mobile FROM t_agent WHERE id = ?", agentId);
	}
	
	/**
	 * 根据代理商ID获取编号
	 * @author Sylveon
	 */
	public String getNoByAgentId(String agentId) {
		return Db.queryStr("SELECT no FROM t_agent WHERE id = ?", agentId);
	}

	/**
	 * 现金提取--验证支付密码是否正确
	 * @param payPassword
	 * @param agentId
	 * @return
	 * @author chenhg
	 * 2016年7月25日 上午10:56:07
	 */
	public boolean checkUserPayPassword(String payPassword, String agentId) {
		String sql = "SELECT id FROM t_agent WHERE id = ? AND pay_password = ?";
		String result = Db.queryStr(sql, agentId, payPassword);
		return StringUtil.notNull(result) ? true : false;
	}
	
	public boolean hasShop(String agentId) {
		long count = Db.queryLong(new StringBuilder(" SELECT COUNT(1) FROM t_shop s ")
				.append(" WHERE s.`status` = ? ")
				.append(" AND s.agent_id = ? ")
				.toString(),
				Shop.STATUS_UNTURNOUT, agentId
		);
		return count > 0 ? true : false;
	}
	
	public Agent getNameAndMobile(String agentId) {
		return Agent.dao.findByIdLoadColumns(agentId, "name, mobile");
	}
}
