package com.buy.model.shop;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.SysParam;
import com.buy.model.agent.Agent;
import com.buy.model.agent.AgentGroup;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;

/**
 * 店铺分配
 */
public class ShopAssignment {
	
	public static final ShopAssignment dao = new ShopAssignment();
	
	/**
	 * 分配店铺
	 */
	public Shop assign(Date applyDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(applyDate);
		Shop shop = null;
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case 1:
				return randomByAgnet(AgentGroup.AGENT_GROUP_TW);	// 台湾馆（日）
			case 2: case 4: case 6:
				return randomShops(null);							// 所有（一、三、五）
			case 3:
				return randomByAgnet(AgentGroup.AGENT_GROUP_S);		// 华南区（二）
			case 5:
				return randomByAgnet(AgentGroup.AGENT_GROUP_E);		// 华东区（四）
			case 7:
				return randomByAgnet(AgentGroup.AGENT_GROUP_N_SW);	// 华北西南区（六）
		}
		if (StringUtil.isNull(shop))
			return randomShops(null);
		return shop;
	}
	
	/**
	 * 分配店铺
	 */
	public Shop assign(String agentNo) {
		String agentId = Agent.dao.getIdByNo(agentNo);
		return randomShops(agentId);
	}
	
	/**
	 * 所有店铺随机分配
	 */
	Shop randomShops(String agentId) {
		StringBuffer sql = new StringBuffer(" SELECT * FROM t_shop");
		
		List<Object> paraList = new ArrayList<Object>();
		sql.append(" WHERE status = ? ");			paraList.add(Shop.STATUS_UNTURNOUT);
		sql.append(" AND is_belong_efun = ? ");		paraList.add(Shop.BELONG_SELL);
		
		if (StringUtil.notNull(agentId)) {
			sql.append(" AND agent_id = ? ");
			paraList.add(agentId);
		}
		
		sql.append(" ORDER BY RAND() LIMIT 1 FOR UPDATE ");
		return Shop.dao.findFirst(sql.toString(), paraList.toArray());
	}
	
	/**
	 * 根据代理商分配
	 */
	Shop randomByAgnet(Integer group) {
		StringBuffer sql = new StringBuffer(" SELECT agent_id FROM t_agent_group_map ");
		sql.append(" WHERE is_distributable = ? AND `group` = ? ");
		
		String agentNos = "";
		List<String> agentNoList = SysParam.dao.findValueByCode("agent_self");
		if (StringUtil.notNull(agentNoList))
			agentNos = StringUtil.listToStringForSql(",", agentNoList);
		if (StringUtil.notBlank(agentNos))
			sql.append(" AND agent_no NOT IN (" + agentNos + ") ");
		
		sql.append(" ORDER BY RAND() LIMIT 1 ");
		String agentId = Db.queryStr(sql.toString(), BaseConstants.YES, group);
		
		if (StringUtil.isNull(agentId))
			return null;
		
		return randomShops(agentId);
	}

}
