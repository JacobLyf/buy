package com.buy.model.order;

import java.math.BigDecimal;
import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
/**
 * 退款申请表
 * @author huangzq
 *
 */
public class RefundApply extends Model<RefundApply>{

	private static final long serialVersionUID = 1L;
	
	public static final RefundApply dao = new RefundApply();
	
	/**
	 * 类型：买家取消订单
	 */
	public static final int TYPE_USER_CANCLE = 1;
	/**
	 * 类型：买家未按时付款
	 */
	public static final int TYPE_AUTO_CANCLE = 2;
	/**
	 * 类型：买家退款
	 */
	public static final int TYPE_REFUND = 3;
	/**
	 * 类型：买家退货
	 */
	public static final int TYPE_RETURN_GOODS = 4;
	
	//////////////////////状态//////////////////////////////////
	/**
	 * 审核状态：申请中
	 */
	public static final int AUDIT_STATUS_UNCHECK = 0;
	/**
	 * 审核状态：审核通过
	 */
	public static final int AUDIT_STATUS_SUCCESS = 1;
	/**
	 * 审核状态：审核失败
	 */
	public static final int AUDIT_STATUS_FAIL = 2;
	
	/**
	 * 保存退款申请表（后台财务审核）
	 * @param orderId 订单ID
	 * @param userid 会员ID
	 * @param type 类型
	 * @param cash 退款金额
	 * @author Jacob
	 * 2016年1月12日下午6:48:10
	 */
	public void add(String orderId,String userid,Integer type,BigDecimal cash){
		RefundApply refundApply = new RefundApply();
		refundApply.set("order_id", orderId);
		refundApply.set("user_id", userid);
		refundApply.set("type", type);
		refundApply.set("cash", cash);
		refundApply.set("create_time", new Date());
		refundApply.save();
	}
	
	
}
