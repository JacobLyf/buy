package com.buy.model.agent;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class AgentGroup extends Model<AgentGroup>
{
	private static final long serialVersionUID = 1L;

	public static final AgentGroup dao = new AgentGroup();
	
	/** 代理商分组 - 默认区 **/
	public static final int AGENT_GROUP_DEFAULT = 1;
	/** 代理商分组 - 华东区 **/
	public static final int AGENT_GROUP_E = 2;
	/** 代理商分组 - 华南区 **/
	public static final int AGENT_GROUP_S = 3;
	/** 代理商分组 - 华北西南区 **/
	public static final int AGENT_GROUP_N_SW = 4;
	/** 代理商分组 - 台湾区 **/
	public static final int AGENT_GROUP_TW = 5;
	
	/**
	 * 添加代理商分组
	 * @param agentId
	 * @param agentNo
	 * @param isDistributable
	 */
	public void addAgentGroup(String agentId, String agentNo, int isDistributable)
	{
		StringBuffer sql = new StringBuffer(); 
		sql.append(" INSERT INTO t_agent_group_map ");
		sql.append(		" (agent_id, agent_no, is_distributable, create_time, update_time) ");
		sql.append(" VALUES ");
		sql.append(" ('" + agentId + "', '" + agentNo + "', " + isDistributable + ", NOW(), NOW()) ");
		Db.update(sql.toString());
	}
	
	/**
	 * 更新代理商分组
	 * @param agentId
	 * @param isDistributable
	 */
	public void updateAgentGrpup(String agentId, int isDistributable)
	{
		String sql = "UPDATE t_agent_group_map SET is_distributable = " + isDistributable + " WHERE agent_id = '" +agentId  + "'";
		Db.update(sql);
	}
	
	public List<Record> groupList() {
		List<Record> result = new ArrayList<Record>();
		result.add(new Record().set("group", AGENT_GROUP_DEFAULT).set("name", "默认区"));
		result.add(new Record().set("group", AGENT_GROUP_E).set("name", "华东区"));
		result.add(new Record().set("group", AGENT_GROUP_S).set("name", "华南区"));
		result.add(new Record().set("group", AGENT_GROUP_N_SW).set("name", "华北西南区"));
		result.add(new Record().set("group", AGENT_GROUP_TW).set("name", "台湾区"));
		return result;
	}
	
}
