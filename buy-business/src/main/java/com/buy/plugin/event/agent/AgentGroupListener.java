package com.buy.plugin.event.agent;

import com.buy.common.BaseConstants;
import com.buy.model.agent.AgentGroup;
import com.buy.model.shop.Shop;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener(enableAsync = true)
public class AgentGroupListener implements ApplicationListener<AgentGroupEvent>
{
	@Override
	public void onApplicationEvent(AgentGroupEvent event)
	{
		// 查询 - 代理商ID
		String shopId = (String) event.getSource();
		String agentId = Db.queryStr("SELECT agent_id FROM t_shop WHERE id = '" + shopId + "'");
		// 设置代理商是否可分配店铺
		if (StringUtil.notNull(agentId))
		{
			// 查询 - 代理商未转出店铺
			StringBuffer sql = new StringBuffer();
			sql.append(" SELECT COUNT(1) FROM t_shop WHERE 1 = 1 ");
			sql.append(" AND status = ? ");
			sql.append(" AND agent_id = ? ");
			long count = Db.queryLong(sql.toString(), Shop.STATUS_UNTURNOUT, agentId);
			// 更新
			int isDistributable = count > 0 ? BaseConstants.YES : BaseConstants.NO;
			AgentGroup.dao.updateAgentGrpup(agentId, isDistributable);
		}
	}

}
