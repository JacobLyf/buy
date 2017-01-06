package com.buy.model.order;

import com.buy.common.BaseConstants.init;
import com.buy.plugin.event.pos.order.PushPosOrderReturnEvent;
import com.buy.service.pos.push.PushPosReturnGoods;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;
/**
 * 订单退款表
 * @author eriol
 *
 */
public class OrderReturn extends Model<OrderReturn>{

	private static final long serialVersionUID = 1L;
	
	public static final OrderReturn dao = new OrderReturn();
	
	/**
	 * 退款类型-退款
	 */
	public static final int TYPE_REFUND = 1;
	/**
	 * 退款类型-退货
	 */
	public static final int TYPE_RETURN_GOOD = 2;
	
	//////////////////////状态//////////////////////////////////
	/**
	 * 退款状态-申请中
	 */
	public static final int RETURN_STATUS_APPLY = 0;
	/**
	 * 退款状态-等待买家寄回商品(退货类型才进入此状态)
	 */
	public static final int RETURN_STATUS_WAIT_BACK = 1;
	/**
	 * 退款状态-等待卖家确认收货(退货类型才进入此状态)
	 */
	public static final int RETURN_STATUS_RETURNING = 2;
	/**
	 * 退款状态-退款成功
	 */
	public static final int RETURN_STATUS_SUCCESS = 3;
	/**
	 * 退款状态-退款失败
	 */
	public static final int RETURN_STATUS_FAIL = 4;
	/**
	 * 退款状态-等待买家寄回商品到云店
	 */
	public static final int RETURN_STATUS_WAIT_STORE_BACK = 5;
	
	/**
	 * 编号前缀
	 */
	public static final String NO_PREFIX = "OR";
	
	/**
	 * 根据退单ID获取退单信息
	 * @param returnId
	 * @return
	 * @author Sylveon
	 */
	public Record getReturnOrder(int returnId) {
		StringBuffer sql = new StringBuffer();
		// 退单信息
		sql.append(" SELECT");
		sql.append(" 	r.id returnId,");
		sql.append(" 	r.return_type returnType,");
		sql.append(" 	r.return_reason returnReason,");
		sql.append(" 	r.return_status returnStatus,");
		sql.append(" 	r.logistics_company logisticsCompany,");
		sql.append(" 	r.logistics_no logisticsNo,");
		sql.append(" 	CONCAT(r.province, r.city, r.address) detailAddress,");
		sql.append(" 	r.zip returnZip,");
		// 退单人信息
		sql.append(" 	r.concat returnUserName,");
		sql.append(" 	r.mobile returnMobile,");
		//物流信息
		sql.append(" 	r.logistics_id logisticsId,");
		sql.append(" 	r.logistics_no logisticsNo");
		sql.append(" FROM t_order_return r");
		sql.append(" WHERE r.id = ?");
		return Db.findFirst(sql.toString(), returnId);
	}
	
	/**
	 * 根据订单订单ID查找订单关闭原因
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public String getReturnReason(String orderId) {
		String sql = "SELECT return_reason FROM t_order_return WHERE order_id = ? LIMIT 1";
		return Db.queryStr(sql, orderId);
	}
	
	/**
	 * 获取退款状态
	 * @param returnId
	 * @return
	 * @author huangzq
	 */
	public Integer getReturnStatus(Integer returnId){
		
		return OrderReturn.dao.findByIdLoadColumns(returnId, "return_status").getInt("return_status");
	}
	
	/**
	 * 根据订单Id获取退货/退款订单
	 * @param orderId
	 * @return
	 * @author Jacob
	 * 2015年12月28日上午10:22:19
	 */
	public OrderReturn getOrderReturn(String orderId){
		String sql = " SELECT * FROM t_order_return WHERE order_id = ?";
		return OrderReturn.dao.findFirst(sql);
	}

	/**
	 * 根据订单ID查找退单ID
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public Integer getIdByOrderId(String orderId) {
		return Db.queryInt("SELECT id FROM t_order_return WHERE order_id = ?", orderId);
	}
	/**
	 * 获取订单退款退货（锁定）
	 *
	 * @author huangzq 
	 * @date 2016年6月22日 下午1:46:49
	 * @return
	 */
	public OrderReturn getOrderReturnForUpdate(Integer returnId){
		String sql = "select * from t_order_return o where o.id = ? for update";
		return dao.findFirst(sql,returnId);
		
	}
	
	/**
	 * 退货给商城的订单推送给pos
	 * @param returnId
	 */
	public void posPushReturn(String returnId){
		OrderReturn orderReturn = OrderReturn.dao.findById(returnId);
		//退货给商城，需要推送到pos
		if(StringUtil.notNull(orderReturn.getStr("store_no"))){
			/*******************推送退货订单到POS @author chenhg ********************/
			PushPosReturnGoods source = new PushPosReturnGoods().setOrderReturnId(returnId);
			EventKit.postEvent(new PushPosOrderReturnEvent(source));
			/*******************推送退货订单到POS @author chenhg ********************/
		}
	}

	/**
	 * 根据退单ID获取订单ID
	 * @param returnId
	 * @return
	 */
	public String getOrderIdByReturnId(int returnId) {
		return Db.queryStr("SELECT order_id FROM t_order_return WHERE id = ?", returnId);
	}

}
