package com.buy.model.agent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.Ret;
import com.buy.model.order.Order;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AgentCashRecord extends Model<AgentCashRecord> {
	
	private  Logger L = Logger.getLogger(AgentCashRecord.class);

	private static final long serialVersionUID = 1L;
	public static final AgentCashRecord dao = new AgentCashRecord();
	
	
	/** 交易类型：提现 */
	public static final int TRANSACTION_TYPE_WITHDARWAL = 1;
	/**
	 * 交易类型：会员购物返利
	 */
	public static final int REBATE_USER_SHOPPING = 2;
	/**
	 * 交易类型：店铺租金返利
	 */
	public static final int REBATE_SHOP_RENT = 3;
	/**
	 * 交易类型：幸运一折购返利
	 */
	public static final int REBATE_EFUN = 4;
	/**
	 * 交易类型：引荐厂家销售返利
	 */
	public static final int REBATE_RECOMMEND_SALES = 5;
	
	/**
	 * 交易类型-金额调整（增加）
	 */
	public static final int TYPE_CHANG_ADD = 6;
	/**
	 * 交易类型-金额调整（减少）
	 */
	public static final int TYPE_CHANG_SUB = 7;

	/** 事项--现金申领常量（注：其它类型自己添加常量）*/
	public static final String REMARK_CASHWITHDRAWAL = "现金申领";
	/** 事项--店铺租金返利*/
	public static final String REMARK_REBATE_SHOP_RENT = "店铺租金返利";
	
	/**
	 * 代理商现金对账单
	 * @param cash 变动金额
	 * @param remainCash 账户余额
	 * @param orderNo
	 * @param type
	 * @param targetId
	 * @param agentId
	 * @param remark
	 * @return
	 * @author chenhg
	 * 2016年3月3日 上午10:56:11
	 */
	public boolean add(BigDecimal cash, BigDecimal remainCash, String orderNo,
			int type,String targetId, String agentId, String remark){
		
		Agent agent = Agent.dao.findByIdLoadColumns(agentId, "no,name");
		boolean flag = false;
		if(agent!=null){
			AgentCashRecord cashRecord = new AgentCashRecord();
			cashRecord.set("cash", cash.setScale(2,BigDecimal.ROUND_DOWN));
			cashRecord.set("remain_cash", remainCash.setScale(2,BigDecimal.ROUND_DOWN));
			cashRecord.set("target_id", targetId);
			cashRecord.set("order_no", orderNo);
			cashRecord.set("agent_id", agentId);
			cashRecord.set("agent_no", agent.getStr("no"));
			cashRecord.set("agent_name", agent.getStr("name"));
			cashRecord.set("type", type);
			cashRecord.set("remark", remark);
			cashRecord.set("create_time", new Date());
			flag = cashRecord.save();
		}else{
			L.info("代理商不存在，导致无法添加代理商现金对账单");
		}
		return flag;
	}
	
	
	/**
	 * 代理商会员购物返利--列表（商家版app、pc公用）
	 * @param ret
	 * @param page
	 * @return
	 * @author chenhg
	 * 2016年8月1日 上午10:01:35
	 */
	public Page<Record> getUserRebatesList(Ret ret,Page<Object> page){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		select.append(" select  ");
		select.append("  c.user_name userName, ");
		select.append("  d.`name` shopName, ");
		select.append("  d.`no` shopNo, ");
		select.append("  a.order_no orderNo, ");
		select.append("  b.total, ");
		select.append("  a.cash receivingCash, ");
		select.append("  a.create_time receivingTime ");
		
		where.append(" from   ");
		where.append("  t_agent_cash_record a  ");
		where.append("  LEFT JOIN t_order b ON a.order_no = b.`no` ");
		where.append("  LEFT JOIN t_user c ON b.user_id = c.id ");
		where.append("  LEFT JOIN t_shop d ON b.merchant_id = d.id ");
		where.append(" WHERE a.type = ?    ");
		where.append("   AND a.agent_id = ?  ");
//		where.append("   AND (b.order_type = ? or b.order_type = ?)  ");
		paras.add(AgentCashRecord.REBATE_USER_SHOPPING);
		paras.add(ret.get("agentId"));
//		paras.add(Order.TYPE_SHOP);
//		paras.add(Order.TYPE_SELF_SHOP);
		if("pc".equals(ret.get("type").toString())){
			if(ret.notNull("beginTime")){
				where.append(" AND a.create_time >= ? ");
				paras.add(ret.get("beginTime"));
			}
			if(ret.notNull("endTime")){
				where.append(" AND a.create_time <= ? ");
				paras.add(ret.get("endTime"));
			}
		}
		where.append(" order by a.create_time desc ");
		
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), paras.toArray());
	}
	
	/**
	 * 返利--金额合计（商家版app、pc公用）
	 * @param agentId
	 * @return
	 */
	public String getRebatesSum(String agentId, int type){
		StringBuffer sql = new StringBuffer();
		
		sql.append(" select  ");
		sql.append(" IFNULL(sum(a.cash),0) sumCash  ");
		sql.append(" from  ");
		sql.append("  t_agent_cash_record a  ");
		sql.append("  WHERE a.type = ? ");
		sql.append("  AND a.agent_id = ? ");
		
		Record record =  Db.findFirst(sql.toString(), type, agentId);
		String sumCash = "0.00";
		if(!"0".equals(record.get("sumCash").toString())){
			sumCash = record.get("sumCash").toString();
		}
		return sumCash;
	}
	
	
	/**
	 * 代理商-租金返利记录（app、pc公用）
	 * @param ret
	 * @param page
	 * @return
	 */
	public Page<Record> getRentRebatesList(Ret ret, Page<Object> page){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		select.append(" select  ");
		select.append("  c.`name` shopName, ");
		select.append("  c.`no` shopNo, ");
		select.append("  b.cash rentCash,  ");
		select.append("  a.cash receivingCash, ");
		select.append("  a.create_time receivingTime ");
		 
		where.append(" from     ");
		where.append("   t_agent_cash_record a   ");
		where.append("   LEFT JOIN t_shop_renew b ON a.target_id = b.id   ");
		where.append("   LEFT JOIN t_shop c ON b.shop_id = c.id  ");
		where.append("   where a.type = ? ");
		where.append("   and a.agent_id = ?  ");
		 
		paras.add(AgentCashRecord.REBATE_SHOP_RENT);
		paras.add(ret.get("agentId"));
		
		if("pc".equals(ret.get("type").toString())){
			if(ret.notNull("beginTime")){
				where.append(" AND a.create_time >= ? ");
				paras.add(ret.get("beginTime"));
			}
			if(ret.notNull("endTime")){
				where.append(" AND a.create_time <= ? ");
				paras.add(ret.get("endTime"));
			}
			if(ret.notNull("shopName")){
				where.append(" AND c.`name` like concat('%',?,'%') ");
				paras.add(ret.get("shopName"));
			}
			if(ret.notNull("shopNo")){
				where.append(" AND c.`no` like concat('%',?,'%') ");
				paras.add(ret.get("shopNo"));
			}
		}
		
		where.append(" order by a.create_time desc ");
		
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), paras.toArray());
	}
	
	
	/**
	 * 代理商-幸运一折购返利记录（app、pc公用）
	 * @param ret
	 * @param page
	 * @return
	 * @author chenhg
	 * 2016年8月3日 下午2:33:59
	 */
	public Page<Record> geteFunRebatesList(Ret ret, Page<Object> page){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		select.append(" select    ");
		if("pc".equals(ret.get("type").toString())){
			select.append("  b.id productId,   ");
		}
		select.append("  e.user_name userName,   ");
		select.append("  d.`no` shopNo,   ");
		select.append("  concat(b.product_name,'',IFNULL(b.product_property,'')) productName,   ");
		select.append("  b.efun_id efunId,   ");
		select.append("  a.cash receivingCash,   ");
		select.append("  a.create_time receivingTime   ");
		
		where.append(" from t_agent_cash_record a   ");
		where.append(" LEFT JOIN t_efun_user_order b ON a.target_id = b.id    ");
		where.append(" LEFT JOIN t_user e ON b.user_id = e.id   ");
		where.append(" LEFT JOIN t_product c on b.product_id = c.id    ");
		where.append(" LEFT JOIN t_shop d on c.shop_id = d.id    ");
		where.append(" WHERE 1 = 1 ");
//		where.append(" WHERE a.target_id is not NULL    ");
		where.append(" and a.type = ?    ");
		where.append(" and a.agent_id = ?    ");
		
		paras.add(AgentCashRecord.REBATE_EFUN);
		paras.add(ret.get("agentId"));
		if("pc".equals(ret.get("type").toString())){
			if(ret.notNull("beginTime")){
				where.append(" AND a.create_time >= ? ");
				paras.add(ret.get("beginTime"));
			}
			if(ret.notNull("endTime")){
				where.append(" AND a.create_time <= ? ");
				paras.add(ret.get("endTime"));
			}
			if(ret.notNull("shopName")){
				where.append(" AND d.`name` like concat('%',?,'%') ");
				paras.add(ret.get("shopName"));
			}
			if(ret.notNull("shopNo")){
				where.append(" AND d.`no` like concat('%',?,'%') ");
				paras.add(ret.get("shopNo"));
			}
			if(ret.notNull("userName")){
				where.append(" AND e.user_name like concat('%',?,'%') ");
				paras.add(ret.get("userName"));
			}
		}
		
		where.append(" order by a.create_time desc ");
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), paras.toArray());
	}
	
	/**
	 * 代理商--引荐厂家销售返利记录
	 * @param ret
	 * @param page
	 * @return
	 * @author chenhg
	 * 2016年8月3日 下午3:15:12
	 */
	public Page<Record> getSupplierRebatesList(Ret ret, Page<Object> page){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		select.append(" select   ");
		select.append("  c.`name` supplierName,  ");
		select.append("  c.`no` supplierNo,  ");
		select.append("  a.order_no orderNo,  ");
		select.append("  b.total,  ");
		select.append("  a.cash receivingCash,  ");
		select.append("  a.create_time receivingTime ");
		
		where.append("  from   ");
		where.append("   t_agent_cash_record a  ");
		where.append("   LEFT JOIN t_order b ON a.order_no = b.`no` ");
		where.append("   LEFT JOIN t_supplier c ON b.merchant_id = c.id ");
		where.append("   WHERE a.type = ? ");
		where.append("   AND a.agent_id = ? ");
		where.append("   AND b.order_type IN (?,?,?) ");
		
		paras.add(AgentCashRecord.REBATE_RECOMMEND_SALES);
		paras.add(ret.get("agentId"));
		paras.add(Order.TYPE_SELF_PUBLIC);//订单类型-自营公共订单(供货商)
		paras.add(Order.TYPE_SELL_BY_PROXY);//订单类型-E趣代销订单
		paras.add(Order.TYPE_SUPPLIER_SEND);//厂家自发订单
		
		if("pc".equals(ret.get("type").toString())){
			if(ret.notNull("beginTime")){
				where.append(" AND a.create_time >= ? ");
				paras.add(ret.get("beginTime"));
			}
			if(ret.notNull("endTime")){
				where.append(" AND a.create_time <= ? ");
				paras.add(ret.get("endTime"));
			}
		}
		
		where.append(" order by a.create_time desc ");
		return Db.paginate(page.getPageNumber(), page.getPageSize(), select.toString(), where.toString(), paras.toArray());
	}
	
	
	/**
	 * 代理商--引荐厂家销售返利-金额合计
	 * @param agentId
	 * @return
	 * @author chenhg
	 * 2016年8月3日 下午3:23:49
	 */
	public String getSupplierRebatesSum(String agentId) {
		StringBuffer sql = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		sql.append(" select  ");
		sql.append("   ifnull(sum(a.cash),0) sumCash  ");
		sql.append(" from   ");
		sql.append("   t_agent_cash_record a  ");
		sql.append("   LEFT JOIN t_order b ON a.order_no = b.`no` ");
		sql.append(" WHERE a.type = ?   ");
		sql.append("   AND a.agent_id = ? ");
		sql.append("   AND b.order_type IN (?,?,?) ");
		paras.add(AgentCashRecord.REBATE_RECOMMEND_SALES);
		paras.add(agentId);
		paras.add(Order.TYPE_SELF_PUBLIC);//订单类型-自营公共订单(供货商)
		paras.add(Order.TYPE_SELL_BY_PROXY);//订单类型-E趣代销订单
		paras.add(Order.TYPE_SUPPLIER_SEND);//厂家自发订单
		
		Record record =  Db.findFirst(sql.toString(), paras.toArray());
		return record.get("sumCash").toString();
		
		
	}
}
