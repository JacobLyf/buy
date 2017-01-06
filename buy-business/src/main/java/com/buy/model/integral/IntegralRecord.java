package com.buy.model.integral;

import com.buy.common.Ret;
import com.buy.model.message.Message;
import com.buy.plugin.event.account.IntegralRecordUpdateEvent;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import net.dreamlu.event.EventKit;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 积分记录(积分对账单)
 * 【说明】记录会员账户积分进出帐记录
 */
public class IntegralRecord extends Model<IntegralRecord>{
	
	/**************积分消费类型***************/
	
	/**
	 * 积分类型-购物
	 */
	public static final int TYPE_SHOPPING = 1;
	/**
	 * 积分类型：取消订单
	 */
	public final static int TYPE_CANCEL_ORDER = 2;
	/**
	 * 积分类型：获取积分
	 */
	public final static int TYPE_GET_INTEGRAL = 3;
	/**
	 * 积分类型：购物返积分
	 */
	public final static int TYPE_SHOPPING_RETURN = 4;
	/**
	 * 积分类型：E趣购
	 */
	public final static int TYPE_EFUN_ORDER = 5;
	/**
	 * 积分类型-退款
	 */
	public static final int TYPE_REFUND = 6;
	
	/**
	 * 积分类型-积分调整（增加）
	 */
	public static final int TYPE_CHANG_ADD = 7;
	/**
	 * 积分类型-积分调整（减少）
	 */
	public static final int TYPE_CHANG_SUB = 8;
	
	/**
	 * 积分类型-活动获取积分
	 */
	public static final int TYPE_ACTIVITY_ADD = 9;
	
	/**
	 * 积分类型-一折购退款
	 */
	public static final int TYPE_EFUN_REFUND = 10;


	private static final long serialVersionUID = 1L;
	
	public static final IntegralRecord dao = new IntegralRecord();

	private static final Object obj = new Object();
	
	/**
	 * 会员积分记录
	 * @param integral
	 * @param remainIntegral
	 * @param orderNo
	 * @param type
	 * @param userId
	 * @param userName
	 * @param remark
	 */
	public boolean add(int integral, int remainIntegral, int type, String userId, String userName, String remark) {
		IntegralRecord integralRecord = new IntegralRecord()
			.set("user_id", 		userId)
			.set("user_no", 		userName)
			.set("user_name", 		userName)
			.set("integral",		integral)
			.set("remain_integral",	remainIntegral)
			.set("type", 			type)
			.set("remark",		 	remark)
			.set("create_time", 	new Date());
		boolean flag = integralRecord.save();
		// 发送消息
		if (flag) {
			integral = new BigDecimal(integral).abs().intValue();
			Ret source =  Message.dao.init4UserIntgralRecord(userId, userName, integral, type, remark);
			EventKit.postEvent(new IntegralRecordUpdateEvent(source));
		}
		return flag;
	}
	
	/**
	 * 用户积分明细
	 * @param page
	 * @param userId
	 * @return Page<Integral> 
	 * @author jekay
	 * @date : 2016年3月7日 下午1:27:16
	 */
	public Page<Integral> getMyIntegralPage(Page page,String userId){
		StringBuffer selectsql = new StringBuffer();
		selectsql.append("SELECT ");
		selectsql.append(" ir.create_time happenTime ");//积分发生时间
		selectsql.append(" ,ir.integral changeIntegal ");//积分变化数，有正有负
		selectsql.append(" ,ir.remark remark ");//积分备注
		selectsql.append(" ,ir.id integralId ");//积分Id
		StringBuffer whereSql = new StringBuffer();
		whereSql.append(" FROM ");
		whereSql.append(" t_integral_record ir ");
		whereSql.append(" WHERE");
		whereSql.append(" ir.user_id = ?");
		whereSql.append(" ORDER BY ir.create_time DESC");
		return Integral.dao.paginate(page.getPageNumber(), page.getPageSize(), selectsql.toString(),whereSql.toString(), userId);
	}


}
