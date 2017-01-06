package com.buy.model.order;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.buy.model.sms.SMS;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.buy.aop.EggPairInterceptor;
import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.common.Ret;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.address.Address;
import com.buy.model.agent.AgentCashRecord;
import com.buy.model.efun.EfunOrderDetail;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.freight.FreightRule;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.integral.IntegralUserRecord;
import com.buy.model.logistics.LogisticsCompany;
import com.buy.model.message.Message;
import com.buy.model.order.util.OrderUtil;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.store.Store;
import com.buy.model.store.StoreSkuMap;
import com.buy.model.supplier.Supplier;
import com.buy.model.supplier.SupplierCashRecord;
import com.buy.model.user.RecAddress;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;
import com.buy.plugin.event.push.OrderSendPushEvent;
import com.buy.plugin.event.sms.merchant.DeliverSmsEvent;
import com.buy.plugin.event.sms.user.DeliverFinishSmsEvent;
import com.buy.plugin.event.sms.user.ShopOrderApplyReturnEvent;
import com.buy.plugin.event.user.OrderUpdateEvent;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.dreamlu.event.EventKit;
/**
 * 订单
 * @author huangzq
 *
 */
public class Order extends Model<Order>{
	
	private  Logger L = Logger.getLogger(Order.class);
	
	////////////订单状态//////////////////
	
	/**
	 * 订单状态-未付款
	 */
	public static final int STATUS_WAIT_FOR_PAYMENT = 0;
	/**
	 * 订单状态-待发货
	 */
	public static final int STATUS_WAIT_FOR_SEND = 1;
	/**
	 * 订单状态-已发货
	 */
	public static final int STATUS_HAD_SEND = 2;
	/**
	 * 订单状态-待评价
	 */
	public static final int STATUS_WAIT_FOR_EVALUATION = 3;
	/**
	 * 订单状态-已评价
	 */
	public static final int STATUS_HAD_EVALUATION = 4;
	/**
	 * 订单状态-全部
	 */
	public static final int STATUS_ALL = -1;
	
	////////////订单交易状态//////////////////
	/**
	 * 交易状态-正常
	 */
	public static final int TRADE_NORMAL = 0;
	
	/**
	 * 交易状态-退款中
	 */
	public static final int TRADE_RETURNING_MONEY = 1;
	/**
	 * 交易状态-退换货中
	 */
	public static final int TRADE_RETURNING_GOODS = 2;
	/**
	 * 交易状态-取消交易
	 */
	public static final int TRADE_CANCLE = 3;
	/**
	 * 交易状态-退款成功交易关闭
	 */
	public static final int TRADE_RETURN_MONEY_SUCCESS = 4;
	/**
	 * 交易状态-退换货成功交易关闭（退款退货一起，可查看退货流程）
	 */
	public static final int TRADE_RETURN_GOODS_SUCCESS = 5;
	/**
	 * 交易状态-未及时付款订单关闭
	 */
	public static final int TRADE_UNPAYMENT_IN_TIME = 6;
	
	////////////////////订单类型///////////////////////
	
	/**
	 * 订单类型-店铺专卖订单
	 */
	public static final int TYPE_SHOP = 1;
	/**
	 * 订单类型-自营专卖订单
	 */
	public static final int TYPE_SELF_SHOP = 2;
	
	/**
	 * 订单类型-自营公共订单
	 */
	public static final int TYPE_SELF_PUBLIC = 3;
	/**
	 * 订单类型-E趣代销订单（供货商货物在我们这边）
	 */
	public static final int TYPE_SELL_BY_PROXY = 4;
	/**
	 * 订单类型-厂家自发订单
	 */
	public static final int TYPE_SUPPLIER_SEND = 5;
	
	////////////////////返利状态///////////////////////
	/**
	 * 返利状态-待返利（等待订单完成）
	 */
	public static final int REBATE_WAIT = 0;
	/**
	 * 返利状态-返利成功
	 */
	public static final int REBATE_SUCCESS = 1;
	/**
	 * 返利状态-返利失败
	 */
	public static final int REBATE_FAIL = 2;
	/**
	 * 删除状态-删除
	 */
	public static final int DELETE_STATUS_DELETE = 0;
	/**
	 * 删除状态-正常
	 */
	public static final int DELETE_STATUS_NORMAL = 1;
	/**
	 * 编号前缀
	 */
	public static final String NO_PREFIX = "ORD";
	
	////////////////////数据来源(1:PC,2:APP)//////////////
	/**
	* 数据来源-PC
	*/
	public static final int DATA_FROM_PC = 1;
	/**
	* 数据来源-APP
	*/
	public static final int DATA_FROM_APP = 2;
	/**
	* 数据来源-线下云店
	*/
	public static final int DATA_FROM_OFFLINE = 3;
	/**
	* 数据来源-WAP(扫一扫项目)
	*/
	public static final int DATA_FROM_WAP = 4;
	
	////////////////////是否已结算(0:否，1：是)//////////////
	/**
	* 是否已结算-否
	*/
	public static final int IS_BALANCE_NO = 0;
	/**
	* 是否已结算-是
	*/
	public static final int IS_BALANCE_YES = 1;
	
	//////是否延长收货时间：0：否（使用系统配置的截至收货时间）；1：是（使用系统配置的延长截至收货时间）/////
	/**
	* 是否延长收货时间-否
	*/
	public static final int IS_DELAY_RECEIPT_NO = 0;
	/**
	* 是否延长收货时间-是
	*/
	public static final int IS_DELAY_RECEIPT_YES = 1;
	
	///////////////////////订单配送方式///////////////////////
	/**
	 * 配送方式 - 快递
	 */
	public static final int DELIVERY_TYPE_EXPRESS  = 1;
	/**
	 * 配送方式 - 自提
	 */
	public static final int DELIVERY_TYPE_SELF  = 2;
	/**
	 * 配送方式 - 云店销售单
	 */
	public static final int DELIVERY_TYPE_STORE_SALE  = 3;
	/**
	 * 配送方式 - 到店消费单
	 */
	public static final int DELIVERY_TYPE_SHOP_CONSUME  = 4;
	/**
	 * 配送方式 - 快递自提
	 */
	public static final int DELIVERY_TYPE_EXPRESS_SELF = 5;
	
	///////////////////////多订单支付成功页面配送类型///////////////////////
	/**
	* 多订单支付成功页面配送类型 - 全部快递
	*/
	public static final int PAYMENT_SUCCESS_EXPRESS  = 1;
	/**
	* 多订单支付成功页面配送类型 - 全部自提
	*/
	public static final int PAYMENT_SUCCESS__SELF  = 2;
	/**
	* 多订单支付成功页面配送类型 - 混合快递和自提
	*/
	public static final int PAYMENT_SUCCESS__MIXTURE  = 3;
	
	////////////////////支付方式（1：在线支付，2：货到付款，3：门店现金支付）///////////////////////////
	/**
	* 支付方式 - 在线支付
	*/
	public static final int PAY_TYPE_ONLINE  = 1;
	/**
	* 支付方式 - 货到付款
	*/
	public static final int PAY_TYPE_CASH_ON_DELIVERY  = 2;
	/**
	* 支付方式 - 门店现金支付
	*/
	public static final int PAY_TYPE_STORE_CASH  = 3;
	/**
	* 支付方式 - 其他(包含线下刷卡等)
	*/
	public static final int PAY_TYPE_OTHER  = 4;
	
	////////////////////订单截至收货时间暂停状态////////////////////
	/**
	 * 订单截至收货时间暂停状态-正常
	 */
	public static final int RECIEVED_STOP_TYPE_NORMAL = 0;
	/**
	 * 订单截至收货时间暂停状态-申请退货暂停
	 */
	public static final int RECIEVED_STOP_TYPE_RETURN = 1;
	
	////////////////////订单状态提示///////////////////////
	
	////////////////////幸运一折+标识///////////////////////
	/** 幸运一折+标识 - 普通订单 **/
	public final static int EFUN_PLUS_ORDINARY = 1;
	/** 幸运一折+标识 - 幸运一折吃订单 **/
	public final static int EFUN_PLUS_EAT = 2;
		
	/**
	 * 订单是否释放了锁定库存
	 */
	public static final int IS_RELEASE_LOCK_COUNT_Y = 1;  
	public static final int IS_RELEASE_LOCK_COUNT_N = 0;
	
	/**
	 * 是否超卖订单
	 */
	public static final int IS_OVER_SELL = 1;
	public static final int NOT_OVER_SELL = 0;
	
	
	/**
	 * 店铺订单状态提示
	 */
	public static final String[] SHOP_ORDER_STATUS = {
		// 正常交易
		"等待买家付款", "等待卖家发货", "等待买家收货", "交易成功", "交易成功",
		// 非正常交易
		"", "", "交易关闭"
	};
	
	/**
	 * 供货商订单状态提示
	 */
	public static final String[] SUPPLIER_ORDER_STATUS = {
		// 正常交易
		"等待买家付款", "等待卖家发货", "等待买家收货", "交易成功", "交易成功",
		// 非正常交易
		"", "", "交易关闭"
	};

	private static final long serialVersionUID = 1L;
	
	public static final Order dao = new Order();
	
	/**
	 * 初始化订单
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年4月20日上午10:17:31
	 */
	public Order init(String userId,String dataFrom,String remark){ 
		Order order = new Order();
		String orderId = StringUtil.getUUID();
		order.set("id", orderId);//主键id
		order.set("no", StringUtil.getUnitCode(Order.NO_PREFIX));//编号
		order.set("user_id", userId);//会员id
		order.set("user_name", User.dao.getUserName(userId));//会员账号
		order.set("status", STATUS_WAIT_FOR_PAYMENT);//订单状态
		order.set("trade_status", TRADE_NORMAL);//交易状态
		order.set("delete_status", DELETE_STATUS_NORMAL);//会员是否删除订单（0：删除，1：正常）
		order.set("order_time", new Date());//下单时间
		//获取系统参数配置的付款截止时间长度
		Integer hours = SysParam.dao.getIntByCode("pay_till_day_num");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, hours);
		order.set("pay_till_time", cal.getTime());//截止付款时间
		order.set("data_from", dataFrom);//数据来源(1:PC,2:APP,3:线下,4:WAP)
		order.set("is_balance", IS_BALANCE_NO);//是否已结算(0:否，1：是)
		order.set("is_delay_receipt", IS_DELAY_RECEIPT_NO);//是否延长收货时间：0：否（使用系统配置的截至收货时间）；1：是（使用系统配置的延长截至收货时间）
		order.set("rebate_status", REBATE_WAIT);//返利状态(0：待返利（等待订单完成），1：返利成功，2：返利失败)
		if(StringUtil.notNull(remark)) order.set("remark", remark);//下订单时填写备注
		order.save();
		//生成订单日志-下单
		OrderLog.dao.add(orderId, "order_log_order", dataFrom);
		return order;
	}

	/**
	 * 根据订单id获取订单
	 */
	public Record getOrderByRet(Ret r) {
		StringBuilder sql = new StringBuilder(" SELECT ")
		// 订单基础信息
		.append(" o.id, ")
		.append(" o.no orderNo, ")
		.append(" o.order_time orderTime, ")
		.append(" o.send_time sendTime, ")
		.append(" o.pay_time payTime, ")
		.append(" o.comfirm_time comfirmTime, ")
		.append(" o.recieved_till_time recievedTillTime, ")
		// 订单收货人信息
		.append(" o.concat, ")
		.append(" o.mobile, ")
		.append(" o.tel, ")
		.append(" o.remark, ")
		.append(" CONCAT(o.province,o.city,o.area,o.address) toAddress, ")
		// 订单金额信息
		.append(" o.total, ")
		.append(" o.freight, ")
		.append(" o.cash, ")
		.append(" o.integral_discount integralDiscount, ")
		.append(" (o.total + o.freight - o.cash - o.integral_discount) waitePay, ")
		.append(" o.cost realPay, ")
		// 订单状态信息
		.append(" o.status, ")
		.append(" o.is_over_sell isOverSell, ")
		.append(" o.efun_plus_type, ")
		.append(" o.trade_status tradeStatus, ")
		.append(" (NOW() >= o.pay_till_time) timeoutPay, ")
		.append(" (NOW() >= o.recieved_till_time) timeoutRec, ")
		// 订单自提信息
		.append(" o.o2o_shop_name o2oName, ")
		.append(" o.o2o_shop_no o2oNo,")
		.append(" o.o2o_shop_address o2oAddress, ")
		// 订单其他信息
		.append(" o.delivery_type deliveryType, ")
		.append(" o.order_type orderType, ")
		.append(" o.logistics_id logisticsId, ")
		.append(" o.logistics_no logisticsNo, ")
		.append(" r.store_no returnStoreNo, ")
		.append(" r.return_status returnStatus, ")
		.append(" r.id returnId ")
		
		.append(" FROM t_order o")
		.append(" LEFT JOIN t_order_return r ON r.order_id = o.id ")
		.append(" WHERE 1 = 1 ");
		
		List<Object> list = new ArrayList<>();
		if(r.notNull("orderId")) {
			sql.append(" AND o.id = ? ");
			list.add(r.get("orderId"));
		}
		if(r.notNull("orderShopId")) {
			sql.append(" AND o.shop_id = ? ");
			list.add(r.get("orderShopId"));
		}
		if(r.notNull("supplierId")) {
			sql.append(" AND o.merchant_id = ? ");
			list.add(r.get("supplierId"));
		}
		if(r.notNull("shopId")) {
			sql.append(" AND o.merchant_id = ? ");
			list.add(r.get("shopId"));
		}
		if(r.notNull("suppliserId")) {
			sql.append(" AND o.merchant_id = ? ");
			list.add(r.get("suppliserId"));
		}
		
		Record order = Db.findFirst(sql.toString(), list.toArray());
		OrderUtil.handleStatusMsg(order);			// 处理订单状态
		return order;
	}
	/**
	 * 通过订单编号获取订单
	 * @param no
	 * @return
	 * @author huangzq
	 */
	public Order getOrderByNo(String no){
		return dao.findFirst("select * from t_order where no = ?", no);
	}
	
	/**
	 * 根据订单id获取订单详情列表
	 * @param orderId
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年12月7日下午6:52:50
	 */
	public List<Record> findOrderDetailsByOrderId(String orderId){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("	od.id orderDtId, ");
		sql.append("	od.product_img productImg, ");
		sql.append("	od.product_id productId, ");
		sql.append("	od.product_no productNo, ");
		sql.append("	od.product_name productName, ");
		sql.append("	od.product_property productProperty, ");
		sql.append("	od.price, ");
		sql.append("	od.supplier_price supplierPrice, ");
		sql.append("	od.market_price marketPrice, ");
		sql.append("	od.count, ");
		sql.append("	od.order_shop_id orderShopId, ");
		sql.append("	od.sku_code skuCode, ");
		sql.append("	od.sales_discount salesDiscount, ");
		sql.append("	(od.sales_discount * 10) salesDiscount10, ");
		sql.append("	od.original_price originalPrice, ");
		sql.append("	od.ev ev, ");
		sql.append("	od.discount_type discountType ");
		sql.append("FROM ");
		sql.append("	t_order_detail od ");
		sql.append("WHERE ");
		sql.append("	od.order_id = ? ");
		sql.append("ORDER BY od.id DESC");

		List<Record> result = Db.find(sql.toString(),orderId);
		if (StringUtil.notNull(result)) {
			for (Record r : result) {
				// 处理折扣
				BigDecimal discount10_bd = r.getBigDecimal("salesDiscount10");
				String salesDiscount10 = StringUtil.removeTailZero(discount10_bd);
				r.set("salesDiscount10", salesDiscount10);
			}
		}

		return result;
	}
	
	/**
	 * 根据订单id获取订单详情列表
	 * @author Sylveon
	 */
	public List<Record> findOrderDetailsByOrderIdSimple(String orderId){
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT ");
		
		sql.append(" od.product_name productName, ");
		sql.append(" od.product_property productProperty, ");
		sql.append(" od.count ");
		
		sql.append(" FROM t_order_detail od ");
		sql.append(" WHERE od.order_id = ? ");
		sql.append(" ORDER BY od.id DESC ");
		return Db.find(sql.toString(),orderId);
	}
	
	/**
	 * 退货回归库存操作
	 * @param orderId
	 * @param storeNo
	 * @author chenhg
	 * 2016年11月15日 下午5:52:10
	 */
	public void returnStore(String orderId, String storeNo){
		List<Record> orderDeteils = OrderDetail.dao.findSkuCodeList(orderId);
		for(Record ord : orderDeteils){
			String skuCode = ord.getStr("skuCode");
			Integer count = ord.getInt("count");
			ProductSku sku = ProductSku.dao.findById(skuCode);
			if(sku!=null){
				//如果存在发货仓库
				if(StringUtil.notNull(storeNo)){
					sku.plusRealCount(skuCode, count,storeNo);
				}else{
					sku.plusVirtualCount(skuCode, count);
				}
			}
		}
	}
	
	/**
	 * 退款申请信息
	 * 根据订单ID获取退款申请信息（最新）
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public Record getOrderRefundApply(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	apply_time applyTime, return_reason returnReason, return_status returnStatus");
		sql.append(" FROM t_order_return");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND return_type = ?");
		sql.append(" AND order_id = ?");
		sql.append(" ORDER BY apply_time DESC");
		return Db.findFirst(sql.toString(), OrderReturn.TYPE_REFUND, orderId);
	}
	
	/**
	 * 退货申请信息
	 * 根据订单ID获取退货申请信息（最新信息）
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public Record getOrderReturnApply(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	apply_time applyTime, return_reason returnReason, return_status returnStatus");
		sql.append(" FROM t_order_return");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND return_type = ?");
		sql.append(" AND order_id = ?");
		sql.append(" ORDER BY apply_time DESC");
		return Db.findFirst(sql.toString(), OrderReturn.TYPE_RETURN_GOOD, orderId);
	}
	
	/**
	 * 退货信息
	 * 根据订单ID获取退货信息（最新信息）
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public Record getReturnInfo(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	CONCAT(r.province, r.city, r.area, r.address) returnAddress, return_status returnStatus");
		sql.append(" FROM t_order_return r");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND return_type = ?");
		sql.append(" AND return_status > ?");
		sql.append(" AND order_id = ?");
		sql.append(" ORDER BY apply_time DESC");
		return Db.findFirst(sql.toString(), OrderReturn.TYPE_RETURN_GOOD, OrderReturn.RETURN_STATUS_APPLY, orderId);
	}
	
	/**
	 * 获取订单列表
	 * @param orderIds
	 * @return
	 * @author Jacob
	 * 2015年12月24日下午3:47:28
	 */
	public List<Order> findList(String orderIds){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	* ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND o.id IN ( ");
		String[] strList = orderIds.split(",");
		for(String id : strList){
			sql.append("?,");
			params.add(id);
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" ) ");
		return dao.find(sql.toString(),params.toArray());
	}
	
	/**
	 * 根据订单ID（可多个，以英文逗号连接）获取订单编号（多个编号时，以英文逗号连接）
	 * @param orderIds
	 * @return
	 * @author Jacob
	 * 2015年12月24日下午1:26:21
	 */
	public String getOrderNos(String orderIds){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	GROUP_CONCAT(o.`no`) orderNos ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND o.id IN ( ");
		String[] strList = orderIds.split(",");
		for(String id : strList){
			sql.append("?,");
			params.add(id);
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" ) ");
		sql.append(" GROUP BY user_id ");
		return Db.queryStr(sql.toString(),params.toArray());
	}
	
	/**
	 * 改变关闭订单交易状态
	 * @param orderId
	 * @param tradeType
	 * @param isAutoClose
	 * @author Jacob
	 * 2015年12月30日下午5:09:49
	 */
	public void changeClosedOrderTradeStatus(String orderId,Integer tradeType,boolean isAutoClose){
		//////////////////【获取订单】//////////////////
		Order order = Order.dao.getOrderForUpdate(orderId);
		order.set("trade_status", tradeType);
		//判断是否自动关闭
		if(isAutoClose){
			order.set("close_time", order.getTimestamp("pay_till_time"));
		}else{
			order.set("close_time", new Date());
		}
		order.update();
	}
	
	
	/**
	 * 自提订单申请退款，返回积分、账户余额，第三方支付的款项
	 * @param order
	 * @author chenhg
	 */
	public void returnRefundForDeliverySelf(Order order){
		Date now = new Date();
		String orderId = order.getStr("id");
		//部分支付，或已支付的订单
		if(order.getInt("use_integral")>0
				||order.getBigDecimal("cash").compareTo(new BigDecimal(0))==1
				||order.getInt("status")!=Order.STATUS_WAIT_FOR_PAYMENT){
			/******************【1更新账户】*******************/
			//获取会员账户
			Account account = Account.dao.getAccountForUpdate(order.getStr("user_id"), Account.TYPE_USER);
			account.set("update_time", now);
			
			//备注
			String remark="订单退款";
			int userCashRecordType = UserCashRecord.TYPE_REFUND;
			int integraRecordType = IntegralRecord.TYPE_REFUND;
			
			//添加退款申请记录
			RefundApply refundApply = new RefundApply();
			refundApply.set("order_id", orderId);
			refundApply.set("user_id", order.getStr("user_id"));
			refundApply.set("type", RefundApply.TYPE_REFUND);
			refundApply.set("audit_status", RefundApply.AUDIT_STATUS_SUCCESS);
			/******************【2添加积分记录】*******************/
			//获取用户账号
			User user = User.dao.findByIdLoadColumns(order.getStr("user_id"), "user_name");
			if(order.getInt("use_integral")>0){
				//更新积分
				account.set("integral", account.getInt("integral")+order.getInt("use_integral"));
				account.update();
				/******************【2.1添加积分记录】*******************/
				IntegralRecord.dao.add(order.getInt("use_integral"), Math.abs(account.getInt("integral")),  integraRecordType, order.getStr("user_id"), user.getStr("user_name"), remark);
				//记录退回积分
				refundApply.set("integral", order.getInt("use_integral"));
				/******************【2.2退回积分并保存订单退款积分退回记录】*******************/
				IntegralUserRecord.dao.saveIntegralUserRecord4Return(orderId,IntegralUserRecord.TYPE_SHOPPING_ORDER);
			}
			/******************【3添加现金记录】*******************/
			
			//退回现金
			BigDecimal returnCash = order.getBigDecimal("cash");
			//退回金额（第三方部分）
			BigDecimal returnCost = order.getBigDecimal("cost");
			
			//退回现金>0
			if(returnCash.compareTo(new BigDecimal(0))==1){
				account.set("cash", account.getBigDecimal("cash").add(returnCash));
				account.update();
				//添加现金对账单
				BigDecimal freezeCashUser = account.getBigDecimal("freeze_cash");
				UserCashRecord.dao.add(returnCash, account.getBigDecimal("cash").add(freezeCashUser),  userCashRecordType, order.getStr("user_id"), remark+"退回现金"+order.getStr("no"));
			
				refundApply.set("cash", returnCash);
				
			}
			//退回金额（第三方部分）>0而且已付款
			if(returnCost.compareTo(new BigDecimal(0))==1
					&&order.getInt("status")>Order.STATUS_WAIT_FOR_PAYMENT){
				refundApply.set("cost", returnCost);
				refundApply.set("audit_status", RefundApply.AUDIT_STATUS_UNCHECK);
				
			}else{
				refundApply.set("audit_time", now);
			}
			refundApply.set("create_time", now);
			refundApply.save();
		}
	}
	
	/**
	 * 订单退款
	 * @param orderId 订单Id
	 * @param type 退货订单状态
	 * @author Jacob
	 * 2015年12月30日下午5:12:48
	 */
	public void returnRefund(String orderId,int type){
		//////////////////【获取订单】//////////////////
		Order order = Order.dao.getOrderForUpdate(orderId);
		Date now = new Date();
		//部分支付，或已支付的订单
		if(order.getInt("use_integral")>0
				||order.getBigDecimal("cash").compareTo(new BigDecimal(0))==1
				||order.getInt("status")!=Order.STATUS_WAIT_FOR_PAYMENT){
			/******************【1更新账户】*******************/
			//获取会员账户
			Account account = Account.dao.getAccountForUpdate(order.getStr("user_id"), Account.TYPE_USER);
			account.set("update_time", now);
			//备注
			String remark = "";
			int userCashRecordType = 0;
			int integraRecordType = 0;
			if(type==RefundApply.TYPE_USER_CANCLE){
				remark="取消订单";
				userCashRecordType = UserCashRecord.TYPE_CANCEL_ORDER;
				integraRecordType = IntegralRecord.TYPE_CANCEL_ORDER;
			}else if(type==RefundApply.TYPE_AUTO_CANCLE){
				remark="未按时付款自动取消订单";
				userCashRecordType = UserCashRecord.TYPE_CANCEL_ORDER;
				integraRecordType = IntegralRecord.TYPE_CANCEL_ORDER;
			}else if(type==RefundApply.TYPE_REFUND){
				remark="订单退款";
				userCashRecordType = UserCashRecord.TYPE_REFUND;
				integraRecordType = IntegralRecord.TYPE_REFUND;
			}else if(type==RefundApply.TYPE_RETURN_GOODS){
				remark="订单退货";
				userCashRecordType = UserCashRecord.TYPE_REFUND;
				integraRecordType = IntegralRecord.TYPE_REFUND;
			}
			
			//添加退款申请记录
			RefundApply refundApply = new RefundApply();
			refundApply.set("order_id", orderId);
			refundApply.set("user_id", order.getStr("user_id"));
			refundApply.set("type", type);
			refundApply.set("audit_status", RefundApply.AUDIT_STATUS_SUCCESS);
			/******************【2添加积分记录】*******************/
			//获取用户账号
			User user = User.dao.findByIdLoadColumns(order.getStr("user_id"), "user_name");
			if(order.getInt("use_integral")>0){
				//更新积分
				account.set("integral", account.getInt("integral")+order.getInt("use_integral"));
				account.update();
				/******************【2.1添加积分记录】*******************/
				IntegralRecord.dao.add(order.getInt("use_integral"), Math.abs(account.getInt("integral")),  integraRecordType, order.getStr("user_id"), user.getStr("user_name"), remark);
				//记录退回积分
				refundApply.set("integral", order.getInt("use_integral"));
				/******************【2.2退回积分并保存订单退款积分退回记录】*******************/
				IntegralUserRecord.dao.saveIntegralUserRecord4Return(orderId,IntegralUserRecord.TYPE_SHOPPING_ORDER);
			}
			/******************【3添加现金记录】*******************/
			
			//退回现金
			BigDecimal returnCash = new BigDecimal(0);
			//退回金额（第三方部分）
			BigDecimal returnCost = new BigDecimal(0);
			//运费
			BigDecimal freight =  new BigDecimal(0);
			//退货订单
			if(type==RefundApply.TYPE_RETURN_GOODS){
				freight = order.getBigDecimal("freight");
			}
			//使用现金
			BigDecimal cash = order.getBigDecimal("cash");
			//使用在线支付
			BigDecimal cost = order.getBigDecimal("cost");
			//需退的总金额 = 使用现金+使用在线支付-运费
			BigDecimal returnTotal = cash.add(cost).subtract(freight);
			//需退回的总金额 >0
			if(returnTotal.compareTo(new BigDecimal(0))==1){
				//现金>需退的总金额
				if(cash.compareTo(returnTotal)==1){
					//退回现金=使用现金-运费
					returnCash = returnTotal;
				//现金<=需退的总金额
				}else{
					//退回现金=使用现金
					returnCash = cash;
					//退回金额=需退的总金额-使用现金
					returnCost = returnTotal.subtract(cash);
				}
			}
			//退回现金>0
			if(returnCash.compareTo(new BigDecimal(0))==1){
				account.set("cash", account.getBigDecimal("cash").add(returnCash));
				account.update();
				//添加现金对账单
				BigDecimal freezeCashUser = account.getBigDecimal("freeze_cash");
				UserCashRecord.dao.add(returnCash, account.getBigDecimal("cash").add(freezeCashUser),  userCashRecordType, order.getStr("user_id"), remark+"退回现金"+order.getStr("no"));
			
				refundApply.set("cash", returnCash);
				//运费>0的情况
				if(freight.compareTo(BigDecimal.ZERO) == 1 || order.getBigDecimal("store_freight").compareTo(BigDecimal.ZERO) ==1 ){
					String merchantId = order.getStr("merchant_id");
					//结算给卖家
					int orderType = order.getInt("order_type");
					//专卖订单
					if(orderType == Order.TYPE_SHOP || orderType == Order.TYPE_SELF_SHOP){
						//店铺账户
						Account shopAccount = Account.dao.getAccountForUpdate(merchantId, Account.TYPE_SHOP);
						//退回运费
						BigDecimal returnFreight = freight;//商家自发运费
						//如果云店代发，需要结算差价给商家
						if(StringUtil.notNull(order.getStr("o2o_shop_no")) && orderType == Order.TYPE_SHOP){
							returnFreight = freight.subtract(order.getBigDecimal("store_freight"));
						}
						shopAccount.set("cash", shopAccount.getBigDecimal("cash").add(returnFreight));
						shopAccount.set("update_time",now);
						shopAccount.update();
						
						BigDecimal freezeCashShop = shopAccount.getBigDecimal("freeze_cash");
						ShopCashRecord.dao.add(returnFreight, shopAccount.getBigDecimal("cash").add(freezeCashShop), order.getStr("no"), ShopCashRecord.TYPE_BALANCE, merchantId, "订单"+order.getStr("no")+"退回运费结算");
					}else{
						//供货商账户
						Account supplierAccount = Account.dao.getAccountForUpdate(merchantId, Account.TYPE_SUPPLIER);
						
						//退回运费
						BigDecimal returnFreight = freight;//商家自发运费
						if(orderType == Order.TYPE_SUPPLIER_SEND){
							//如果云店代发，需要结算差价给商家
							if(StringUtil.notNull(order.getStr("o2o_shop_no"))){
								returnFreight = freight.subtract(order.getBigDecimal("store_freight"));
							}
						}
						
						
						supplierAccount.set("cash", supplierAccount.getBigDecimal("cash").add(returnFreight));
						supplierAccount.set("update_time", now);
						supplierAccount.update();
						BigDecimal freezeCashSupplier = supplierAccount.getBigDecimal("freeze_cash");
						SupplierCashRecord.dao.add(returnFreight, supplierAccount.getBigDecimal("cash").add(freezeCashSupplier), order.getStr("no"), SupplierCashRecord.TYPE_BALANCE, merchantId, "订单"+order.getStr("no")+"退回运费结算");
					}
				}
				
			}
			//退回金额（第三方部分）>0而且已付款
			if(returnCost.compareTo(new BigDecimal(0))==1
					&&order.getInt("status")>Order.STATUS_WAIT_FOR_PAYMENT){
				refundApply.set("cost", returnCost);
				refundApply.set("audit_status", RefundApply.AUDIT_STATUS_UNCHECK);
				
			}else{
				refundApply.set("audit_time", now);
			}
			refundApply.set("create_time", now);
			refundApply.save();
		}
	}
	
	/**
	 * 发货
	 * @param orderId
	 * @param logisticsNo
	 * @param logisticsId
	 * @param sendUser
	 * @author huangzq
	 */
	public void deliver(Order order,String sendUser, String dataFrom){
		Order r = Order.dao.getOrderForUpdate(order.getStr("id"));
		//判断订单状态
		if(r.getInt("status")!=Order.STATUS_WAIT_FOR_SEND){
			return;
		}
		
		
		Date now = new Date();
		//收货截止时间天数
		int days = SysParam.dao.getIntByCode("recevied_till_day_num");
		Date receviedDate = DateUtil.addDay(now, days);
		//更新订单
		//物流公司
		LogisticsCompany logisticsCompany = LogisticsCompany.dao.findByIdLoadColumns(order.getInt("logistics_id"),"name");
		order.set("send_user", sendUser);//发货人姓名
		order.set("status", Order.STATUS_HAD_SEND);//订单状态
		order.set("send_time", now);//发货时间
		order.set("recieved_till_time", receviedDate);//订单截止收货时间
		order.set("logistics_company", logisticsCompany.getStr("name"));//物流公司名称
		order.set("is_over_sell", Order.NOT_OVER_SELL);
		order.update();
		//添加订单日志
		OrderLog.dao.add(order.getStr("id"), OrderLog.CODE_ORDER_SEND, dataFrom);
		// 发短信
		List<String> orderIdList = new ArrayList<String>();
		orderIdList.add(order.getStr("id"));
		EventKit.postEvent(new DeliverFinishSmsEvent(orderIdList));
		// 推送
		EventKit.postEvent(new OrderSendPushEvent(orderIdList));
	}
	
	/**
	 * 会员退货寄回商品
	 * @param orderId
	 * @param logisticsCompanyId
	 * @param logisticsCompany
	 * @param logisticsNo
	 * @author Jacob
	 * 2015年12月28日上午10:20:59
	 */
	public JsonMessage saveReturnGoodsLogistics(String returnId,Integer logisticsCompanyId,String logisticsCompany,String logisticsNo){
		JsonMessage jm = new JsonMessage();
		//获取退货/退款订单
		OrderReturn orderReturn  = OrderReturn.dao.getOrderReturnForUpdate(Integer.valueOf(returnId));
		if(orderReturn.getInt("return_status") != OrderReturn.RETURN_STATUS_WAIT_BACK){
			jm.setStatusAndMsg("1", "您已填写过寄回商品物流信息");
			return jm;
		}
		orderReturn.set("return_status", OrderReturn.RETURN_STATUS_RETURNING);
		orderReturn.set("logistics_id", logisticsCompanyId);
		orderReturn.set("logistics_company", logisticsCompany);
		orderReturn.set("logistics_no", logisticsNo);
		orderReturn.set("return_goods_time", new Date());
		//更新退货订单信息
		orderReturn.update();
		return jm;
	}
	
	/**
	 * 同意退货
	 * @param orderReturn
	 * @param merchantId 店铺或供货商id
	 * @author huangzq
	 */
	public void accessReturnGoods(OrderReturn orderReturn ,String merchantId, String dataFrom){
		OrderReturn dbReturn = OrderReturn.dao.getOrderReturnForUpdate(orderReturn.getInt("id"));
		//判断状态
		if(dbReturn.getInt("return_status")!=OrderReturn.RETURN_STATUS_APPLY){
			return;
		}
		Date now = new Date();
		orderReturn.set("return_status",OrderReturn.RETURN_STATUS_WAIT_BACK );
		orderReturn.set("audit_time",now);
		orderReturn.update();
		
		//添加订单日志
		OrderLog.dao.add(orderReturn.getStr("order_id"), OrderLog.CODE_AGREE_RETURN_GOOD, dataFrom);
		//减掉销售数量
		//Product.dao.reduceSalesCount(orderId);
		// 发送消息
		int returnId = orderReturn.getInt("id");
		Ret source = Message.dao.init4UserOrder(returnId, Message.TITLE_ORDER_RETURN, SmsAndMsgTemplate.ORDER_RETURN_PASS);
		EventKit.postEvent(new OrderUpdateEvent(source));
	}
	
	/**
	 * 拒绝退货申请
	 * @param orderId
	 * @param returnId
	 * @author huangzq
	 */
	public void refuseReturnGoods(String orderId ,Integer returnId, String dataFrom){
		Date now = new Date();
		//修改订单状态
		Order order = dao.getOrderForUpdate(orderId);
		order.set("trade_status", Order.TRADE_NORMAL);
		//不同意退货则恢复申请退货时剩余的截至收货时间
		Long millSec = now.getTime()+order.getLong("remian_recieved_milliseconds");
		Date date= new Date(millSec);
		order.set("recieved_till_time", date);//更新截至收货时间
		order.set("recieved_stop_type", Order.RECIEVED_STOP_TYPE_NORMAL);//设置订单截至收货时间暂停状态为正常
		order.update();
		//修改退货状态
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.set("id", returnId);
		orderReturn.set("return_status",OrderReturn.RETURN_STATUS_FAIL );
		orderReturn.set("audit_time",now);
		orderReturn.update();
		//添加订单日志
		OrderLog.dao.add(orderId, OrderLog.CODE_RETURN_GOOD_FAIL, dataFrom);
		// 发送消息
		Ret source = Message.dao.init4UserOrder(returnId, Message.TITLE_ORDER_RETURN, SmsAndMsgTemplate.ORDER_RETURN_UNPASS);
		EventKit.postEvent(new OrderUpdateEvent(source));
	}
	
	/**
	 * 拒绝退款申请
	 * @param orderId
	 * @param returnId
	 * @author huangzq
	 */
	public void refuseRefund(String orderId ,Integer returnId, String dataFrom){
		Date now = new Date();
		//修改订单状态
		Order order = new Order();
		order.set("id", orderId);
		order.set("trade_status", Order.TRADE_NORMAL);
		order.update();
		//修改退货状态
		OrderReturn orderReturn = new OrderReturn();
		orderReturn.set("id", returnId);
		orderReturn.set("return_status",OrderReturn.RETURN_STATUS_FAIL );
		orderReturn.set("audit_time",now);
		orderReturn.update();
		//添加订单日志
		OrderLog.dao.add(orderId, OrderLog.CODE_REFUND_FAIL, dataFrom);
		// 发送消息
		Ret source = Message.dao.init4UserOrder(returnId, Message.TITLE_ORDER_REFUND, SmsAndMsgTemplate.ORDER_REFUND_UNPASS);
		EventKit.postEvent(new OrderUpdateEvent(source));
	}
	
	/**
	 * 同意退款
	 * @param orderId
	 * @param returnId
	 * @param type  类型（店铺，供货商）
	 * @param merchantId 店铺或供货商id
	 * @author huangzq
	 */
	public void accessRefund(String orderId,Integer returnId, String dataFrom){
		
		Date now = new Date();
		OrderReturn orderReturn = OrderReturn.dao.getOrderReturnForUpdate(returnId);
		//判断状态
		if(orderReturn.getInt("return_status")==OrderReturn.RETURN_STATUS_APPLY){
			//修改退货状态
			orderReturn.set("id", returnId);
			orderReturn.set("return_status",OrderReturn.RETURN_STATUS_SUCCESS );
			orderReturn.set("audit_time",now);
			orderReturn.update();
			//修改订单状态
			Order order = Order.dao.findById(orderId);
			order.set("id", orderId);
			order.set("trade_status", Order.TRADE_RETURN_MONEY_SUCCESS);
			order.update();
			//对于超单的，因为没有加锁定库存，所以不用释放
			if(order.getInt("is_over_sell") == BaseConstants.NO){
				List<Record> skuCodeList = OrderDetail.dao.findSkuCodeList(orderId);
				//判断是否是商城代发
				if(StringUtil.isNull(order.getStr("o2o_shop_no"))){
					//释放商品锁定库存
					for(Record od : skuCodeList){
						ProductSku.dao.subtractLockCount(od.getStr("skuCode"), od.getInt("count"));
					}
				}else{
					for(Record od : skuCodeList){
						ProductSku.dao.subtractStoreLockCount(order.getStr("o2o_shop_no"), od.getStr("skuCode"), od.getInt("count"));
					}
				}
				
			}
	
			//订单退款
			dao.returnRefund(orderId,RefundApply.TYPE_REFUND);
			
			//添加订单日志
			OrderLog.dao.add(orderId, OrderLog.CODE_AGREE_REFUND, dataFrom);
			//减掉销售数量
			//Product.dao.reduceSalesCount(orderId);
			// 发送消息
			Ret source = Message.dao.init4UserOrder(returnId, Message.TITLE_ORDER_REFUND, SmsAndMsgTemplate.ORDER_REFUND_PASS);
			EventKit.postEvent(new OrderUpdateEvent(source));
		}
		
	}
	
	/**
	 * 卖家确认收货
	 * @param orderId
	 * @param returnId
	 * @param type
	 * @param merchantId 卖家id
	 * @author huangzq
	 * @return 
	 */
	public boolean merchantSureGoods(String orderId ,Integer returnId, String dataFrom){
		//修改订单状态
		Order order = dao.getOrderForUpdate(orderId);
		if(order.getInt("trade_status")!=Order.TRADE_RETURNING_GOODS){
			return false;
		}
		order.set("trade_status", Order.TRADE_RETURN_GOODS_SUCCESS);
		order.update();
		//修改退货状态
		OrderReturn orderReturn = OrderReturn.dao.findById(returnId);
		orderReturn.set("return_status",OrderReturn.RETURN_STATUS_SUCCESS );
		orderReturn.update();
		//回归库存
		Order.dao.returnStore(orderId, orderReturn.getStr("store_no"));
		//添加订单日志
		OrderLog.dao.add(orderId, OrderLog.CODE_RETURN_GOOD_SUCCESS, dataFrom);
		//订单退款
		dao.returnRefund(orderId,RefundApply.TYPE_RETURN_GOODS);
		return true;
		
		
	}

	/**
	 * 是否存在该订单的退款退货
	 * @param orderId
	 * @param returnId
	 * @return
	 * @author huangzq
	 */
	public boolean isReturnExist(String orderId, Integer returnId){
		long count = Db.queryLong("select count(*) from t_order_return t where t.order_id = ? and t.id = ?");
        return count == 1;
    }
	
	/**
	 * 订单结算（公共商品和专卖商品结算）
	 * @param orderId
	 * @author Jacob
	 * 2016年1月3日上午10:57:19
	 * Modify By Sylveon 增加返回参数（0 无结算；1 有结算）
	 */
	public int settlement(String orderId) {
		int hasSettle = BaseConstants.NO;
		//获取订单
		Order order = dao.getOrderForUpdate(orderId);
		if(order.getInt("is_balance")==BaseConstants.YES){
			hasSettle = BaseConstants.YES;
			return hasSettle;
		}
		///////////////////////【订单明细列表】////////////////////////////
		List<Record> orderDtails = this.findOrderDetailsByOrderId(orderId);
		///////////////////////【判断订单类型】////////////////////////////
		//1.E趣代售订单和自营公共订单：订单中的商品结算价*数量
		if(order.getInt("order_type")==Order.TYPE_SELL_BY_PROXY||order.getInt("order_type")==Order.TYPE_SELF_PUBLIC){
			this.publicProductSettlement(order, orderDtails, false);
			hasSettle = BaseConstants.YES;
		}
		//2.厂家自配订单：订单中的商品结算价*数量+运费
		if(order.getInt("order_type")==Order.TYPE_SUPPLIER_SEND){
			this.publicProductSettlement(order, orderDtails, true);
			hasSettle = BaseConstants.YES;
		}
		//3.店铺专卖订单和自营专卖订单：订单中的全部商品金额*（1-扣佣点）+运费（店铺有缴纳保证金的确认收货立即返回，未缴纳保证金的店铺需要确认收货后第七天自动结算）
		if((order.getInt("order_type")==Order.TYPE_SHOP||order.getInt("order_type")==Order.TYPE_SELF_SHOP)){
			if(StringUtil.notNull(Shop.dao.findById(order.getStr("merchant_id")))&&Shop.dao.isPayDeposit(order.getStr("merchant_id"))){
				this.monopolyPorductSettlement(order, orderDtails);
				hasSettle = BaseConstants.YES;
			}
		}
		return hasSettle;
	}
	
	/**
	 * 专卖商品结算
	 * @param order 订单
	 * @param orderDtails 订单明细列表
	 * @author Jacob
	 * 2016年5月4日下午1:56:25
	 */
	public void monopolyPorductSettlement(Order order, List<Record> orderDtails){
		//获取店铺账户
		Account account = Account.dao.getAccountForUpdate(order.getStr("merchant_id"), Account.TYPE_SHOP);
		//获取店铺
		Shop shop = Shop.dao.findById(order.getStr("merchant_id"));
		//结算总额(使用运费初始化时)
		BigDecimal freight = order.getBigDecimal("freight");
		int orderType = order.getInt("order_type");
		if(orderType == Order.TYPE_SHOP){
			//云店代发
			if(StringUtil.notNull(order.getStr("o2o_shop_no"))){
				freight = freight.subtract(order.getBigDecimal("store_freight"));
			}
		}
		BigDecimal total = freight;//将运费初始化到总金额中
		BigDecimal productsTotal = new BigDecimal(0);
		for(Record orderDetail : orderDtails){
			//累加商品结算价（商品SKU的e趣价*(1-扣佣点)）*数量
			total = total.add(orderDetail.getBigDecimal("supplierPrice").multiply(new BigDecimal(orderDetail.getInt("count"))));
			productsTotal = productsTotal.add(orderDetail.getBigDecimal("price").multiply(new BigDecimal(orderDetail.getInt("count"))));
		}
		//结算总额大于0时才会结算
		if(total.compareTo(BigDecimal.ZERO)==1){
			account.set("cash", account.getBigDecimal("cash").add(total).setScale(2,BigDecimal.ROUND_DOWN));
			account.set("update_time", new Date());
			account.update();
			//添加供货商现金对账记录
			String remark = "";
			String discountStr = "";
			List<Record> detailList = OrderDetail.dao.findOrderDetailList(order.getStr("id"));
			for(Record r : detailList){
				if(r.getBigDecimal("sales_discount").compareTo(BigDecimal.ONE)!=0){
					discountStr += r.getStr("product_name")+(StringUtil.notNull(r.getStr("product_property"))?"("+r.getStr("product_property")+")":"")+"的折扣："+r.getBigDecimal("sales_discount")+",";
				}
			}
			remark = "订单"+order.getStr("no")+"结算，商品金额："+productsTotal;
			//运费大于0才记录
			if(1==freight.compareTo(BigDecimal.ZERO)){
				remark += "，运费："+order.getBigDecimal("freight");
			}
			if(StringUtil.notBlank(discountStr)){
				remark += "，"+discountStr.substring(0, discountStr.length()-1);
			}
			BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
			ShopCashRecord.dao.add(total.setScale(2,BigDecimal.ROUND_DOWN), account.getBigDecimal("cash").setScale(2,BigDecimal.ROUND_DOWN).add(freezeCash), order.getStr("no"), ShopCashRecord.TYPE_BALANCE, shop.getStr("id"), remark);
			//更新订单结算状态
			order.set("is_balance", IS_BALANCE_YES);
			order.update();
			//结算后更新会员账户的剩余股权数
			//Account.dao.updateUserStock(order.getStr("user_id"));
		}else{
			L.info("订单编号："+order.getStr("no")+"结算总为0！");
		}
	}
	
	/**
	 * 公共商品结算
	 * @param order
	 * @param orderDtails
	 * @param isSupplierSend 是否厂家自发
	 * @author Jacob
	 * 2016年5月4日下午3:08:56
	 */
	public void publicProductSettlement(Order order, List<Record> orderDtails, boolean isSupplierSend){
		//获取供货商
		Supplier supplier = Supplier.dao.findById(order.getStr("merchant_id"));
		//获取供货商账户
		Account account = Account.dao.getAccountForUpdate(order.getStr("merchant_id"), Account.TYPE_SUPPLIER);
		//结算总额
		BigDecimal total = new BigDecimal(0);
		//商品总价
		BigDecimal productsTotal = new BigDecimal(0);
		//厂家自发
		if(isSupplierSend){
			BigDecimal freight= order.getBigDecimal("freight");
			//云店代发
			if(StringUtil.notNull(order.getStr("o2o_shop_no"))){
				freight = freight.subtract(order.getBigDecimal("store_freight"));
			}
			//厂家自发结算时需要加上订单运费
			total = freight;
		}
		for(Record orderDetail : orderDtails){
			//累加商品供货价（结算价）*数量
			total = total.add(orderDetail.getBigDecimal("supplierPrice").multiply(new BigDecimal(orderDetail.getInt("count"))));
			productsTotal = productsTotal.add(orderDetail.getBigDecimal("price").multiply(new BigDecimal(orderDetail.getInt("count"))));
		}
		//结算总额大于0时才会结算
		if(total.compareTo(BigDecimal.ZERO)==1){
			account.set("cash", account.getBigDecimal("cash").add(total).setScale(2,BigDecimal.ROUND_DOWN));
			account.set("update_time", new Date());
			account.update();
			//添加供货商现金对账记录
			String remark = "";
			String orderNo = order.getStr("no");
			String discountStr = "";
			List<Record> detailList = OrderDetail.dao.findOrderDetailList(order.getStr("id"));
			for(Record r : detailList){
				if(r.getBigDecimal("sales_discount").compareTo(BigDecimal.ONE)!=0){
					discountStr += r.getStr("product_name")+(StringUtil.notNull(r.getStr("product_property"))?"("+r.getStr("product_property")+")":"")+"的折扣："+r.getBigDecimal("sales_discount")+",";
				}
			}
			if(order.getInt("order_type")==Order.TYPE_SUPPLIER_SEND&&1==order.getBigDecimal("freight").compareTo(BigDecimal.ZERO)){
				remark = "订单"+orderNo+"结算，商品金额："+productsTotal+"，运费："+order.getBigDecimal("freight");
			}else{
				remark = "订单"+orderNo+"结算，商品金额："+productsTotal;
			}
			if(StringUtil.notBlank(discountStr)){
				remark += "，"+discountStr.substring(0, discountStr.length()-1);
			}
			
			BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
			SupplierCashRecord.dao.add(total.setScale(2,BigDecimal.ROUND_DOWN), account.getBigDecimal("cash").setScale(2,BigDecimal.ROUND_DOWN).add(freezeCash), order.getStr("no"), SupplierCashRecord.TYPE_BALANCE, supplier.getStr("id"), remark);
			//更新订单结算状态
			order.set("is_balance", IS_BALANCE_YES);
			order.update();
			//结算后更新会员账户的剩余股权数
			//Account.dao.updateUserStock(order.getStr("user_id"));
		}else{
			L.info("订单编号："+order.getStr("no")+"结算总为0！");
		}
	}
	
	/**
	 * 获取会员所有订单商品总额（已收货并且交易正常的订单）
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年2月26日下午2:52:20
	 */
	public BigDecimal getAllOrderTotal(String userId){
		String sql = " SELECT SUM(o.total) sumTotal FROM t_order o WHERE o.`status` > ? AND o.trade_status = ? AND o.user_id = ? GROUP BY o.user_id ";
		return Db.queryBigDecimal(sql, STATUS_HAD_SEND, TRADE_NORMAL, userId);
	}
	
	/**
	 * 根据交易状态及流程状态确定订单状态
	 * @param status
	 * @param tradeStatus
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年3月2日下午5:34:13
	 */
	public int getOrderStatus(int status, int tradeStatus) {
		switch(tradeStatus){
			case Order.TRADE_NORMAL:
				switch(status){
					case Order.STATUS_WAIT_FOR_PAYMENT:
						return 0;//待付款
					case Order.STATUS_WAIT_FOR_SEND:
						return 1;//待发货
					case Order.STATUS_HAD_SEND:
						return 2;//待收货
					case Order.STATUS_WAIT_FOR_EVALUATION:
						return 3;//待评价
					case Order.STATUS_HAD_EVALUATION:
						return 4;//已评价
					default:
						return 0;//待付款
				}
			case Order.TRADE_RETURNING_MONEY:
				return 5;//退款中
			case Order.TRADE_RETURNING_GOODS:
				return 6;//退货中
			case Order.TRADE_CANCLE:
				return 7;//取消订单交易关闭
			case Order.TRADE_UNPAYMENT_IN_TIME:
				return 8;//未及时付款交易关闭
			case Order.TRADE_RETURN_MONEY_SUCCESS:
				return 9;//退款成功交易关闭
			case Order.TRADE_RETURN_GOODS_SUCCESS:
				return 10;//退货成功交易关闭
			default:
				return 0;//待付款
		}
	}
	
	/**
	 * 处理交易关闭订单（1.更新订单交易状态；2.退款；3.恢复库存）
	 * @param orderId 订单类型
	 * @param tradeType 订单交易类型
	 * @param refundType 退货订单类型
	 * @param isAutoClose 是否自动关闭（用于自动取消订单）
	 * @author Jacob
	 * 2015年12月29日下午4:43:23
	 * @throws SQLException 
	 */
	public void handleTransactionClosedOrder(String orderId,Integer tradeType,Integer refundType ,boolean isAutoClose, String userId, String dataFrom) throws SQLException{
		Order order = new Order();
		//判断订单状态是否满足取消订单
		if(isAutoClose){
			order = dao.getOrderForUpdate(orderId);
		}else{
			order = getOrder4User(orderId, userId, Order.STATUS_WAIT_FOR_PAYMENT);
		}
		//&&order.getInt("trade_status")==Order.TRADE_NORMAL
		if(order!=null&&order.getInt("trade_status")!=Order.TRADE_CANCLE){
			/******************【1.改变订单交易状态】*******************/
			Order.dao.changeClosedOrderTradeStatus(orderId, tradeType, isAutoClose);
			
			/******************【2.退款】*******************/
			Order.dao.returnRefund(orderId,refundType);
			
			/******************【3.减掉锁定库存】*******************/
			Order.dao.subLockCountForOrderIds(orderId);
			
			/******************【4.保存订单日志】*******************/
			OrderLog.dao.add(orderId, "order_log_cancel", dataFrom);

			/******************【5.所有商品恢复待领取/购买状态】*******************/
			EfunOrderDetail.dao.restoreByCancelOrder(orderId);

		}
		
	}
	
	/**
	 * 申请退款
	 * @param orderId 订单ID
	 * @param userId 会员ID
	 * @param reason 退款原因
	 * @author Jacob
	 * 2015年12月24日下午4:53:52
	 * @throws SQLException 
	 */
	@Before(Tx.class)
	public JsonMessage applyRefund(String orderId,String userId,String reason, String dataFrom) throws SQLException {
		JsonMessage jsonMessage = new JsonMessage();
		
		//获取订单(避免并发)
		Order order = getOrder4UserForUpdate(orderId, userId);
		
		if(order!=null){
			/***************hgchen****************** 1) 验  证  *******************hgchen***********************/
			//判断是否有在申请中的记录了
			if(order.getInt("trade_status")==Order.TRADE_RETURNING_MONEY){
				jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
				return jsonMessage;
			}else{
				int status = order.getInt("status");
				int deliveryType = order.getInt("delivery_type");
				//判断当前订单如果是快递，是否为待发货状态，如果是自提，是否为已发货状态
				if(!((status == Order.STATUS_WAIT_FOR_SEND && deliveryType == Order.DELIVERY_TYPE_EXPRESS)
						|| (deliveryType == Order.DELIVERY_TYPE_SELF && status == Order.STATUS_HAD_SEND)
						|| (deliveryType == Order.DELIVERY_TYPE_SELF && 
							status == Order.STATUS_WAIT_FOR_SEND && 
							order.getInt("is_over_sell") == Order.IS_OVER_SELL))){
					jsonMessage.setStatusAndMsg("2", "订单状态已过期，请刷新订单后再次操作");
					return jsonMessage;
				}
				//判断订单是否是当前会员的订单
				if(!order.getStr("user_id").equals(userId)){
					jsonMessage.setStatusAndMsg("3", "订单状态已过期，请刷新订单后再次操作");
					return jsonMessage;
				}
				
				/******************【2.生成申请退款记录】*******************/
				OrderReturn orderReturn = new OrderReturn();
				orderReturn.set("no", StringUtil.getUnitCode(OrderReturn.NO_PREFIX));//生成编号
				orderReturn.set("user_id", userId);//会员ID
				orderReturn.set("order_id", orderId);//订单id
				orderReturn.set("merchant_id", order.getStr("merchant_id"));//商家id
				orderReturn.set("apply_time", new Date());//申请时间
				orderReturn.set("return_type", OrderReturn.TYPE_REFUND);//退款类型：1.退款，2.退货
				orderReturn.set("return_reason", reason);//退款原因
				orderReturn.set("cash", order.getBigDecimal("total").add(order.getBigDecimal("freight")));//退款金额(商品总额+运费)
				/**
				 * 自提订单：申请时就释放仓库锁定库存、直接退款成功、订单关闭
				 */
				if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
					//订单交易关闭；释放仓库锁定库存；退款成功
					order.set("trade_status", Order.TRADE_RETURN_MONEY_SUCCESS);//订单关闭
					orderReturn.set("return_status", OrderReturn.RETURN_STATUS_SUCCESS);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））
					
					//对于超单的，因为没有加锁定库存，所以不用扣回去
					if(order.getInt("is_over_sell") == BaseConstants.NO){
						String storeNo = order.getStr("o2o_shop_no");
						List<Record> skuCodeList = OrderDetail.dao.findSkuCodeList(orderId);
						for(Record od : skuCodeList){
							boolean flag = ProductSku.dao.subtractStoreLockCount(storeNo, od.getStr("skuCode"), od.getInt("count"));
							if(!flag){
								//事务回滚
								DbKit.getConfig().getConnection().rollback();
								return jsonMessage.setStatusAndMsg("4", "库存异常，请刷新订单后再次操作");
							}
						}
					}
					//退款操作（积分、账户金额、第三方支付）
					returnRefundForDeliverySelf(order);
					//TODO 推送到pos
					
				}else{//快递
					order.set("trade_status", Order.TRADE_RETURNING_MONEY);//退款中
					orderReturn.set("return_status", OrderReturn.RETURN_STATUS_APPLY);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））
				}
				
				// 判断操作
				Integer returnId = OrderReturn.dao.getIdByOrderId(orderId);
				if (returnId == null) {
					orderReturn.save();			// 添加
				} else {
					orderReturn.set("id", returnId);
					orderReturn.update();		// 修改
				}
				
				/******************【2.改变订单交易状态】*******************/
				
				order.update();
				/******************【3.生成订单日志】*******************/
				OrderLog.dao.add(orderId, "order_log_apply_refund", dataFrom);
				
				
				/******************发短信通知买家退款*******************/
				Integer orderType = order.getInt("order_type");// 订单类型
				// 店铺订单，自营专卖订单，厂家自发订单 才需要短信提醒
				if (orderType == Order.TYPE_SELF_SHOP || orderType == Order.TYPE_SHOP || orderType == Order.TYPE_SUPPLIER_SEND) {
					String merchantId = order.getStr("merchant_id");// 商家ID
					Ret ret = new Ret();
					// 触发短信来源
					ret.put("dataFrom", dataFrom);
					// 卖家ID
					ret.put("merchantId", merchantId);
					// 订单编号
					ret.put("orderNo", order.getStr("no"));
					ret.put("orderType", orderType);
					ret.put("SMSremark", "退款");
					EventKit.postEvent(new ShopOrderApplyReturnEvent(ret));
				}
			}
		}else{
			jsonMessage.setStatusAndMsg("2", "订单不存在");
		}
		
		return jsonMessage;
	}
	
	/**
	 * 申请退货
	 * @param orderId 订单ID
	 * @param userId 会员ID
	 * @param reason 退货原因
	 * @author Jacob
	 * 2015年12月24日下午4:56:19
	 */
	@Before(Tx.class)
	public JsonMessage applyReturn(String orderId,String userId,String reason, String dataFrom){
		
		JsonMessage jsonMessage = new JsonMessage();
		
		//获取订单//获取订单(避免并发)
		Order order = getOrder4UserForUpdate(orderId, userId);
		
		if(order!=null){
			Integer delivery_type = order.getInt("delivery_type");
			if (Order.DELIVERY_TYPE_SELF == delivery_type) {
				jsonMessage.setStatusAndMsg("5", "自提订单不能退货");
				return jsonMessage;
			}else{
				//判断是否有在申请中的记录了
				if(order.getInt("trade_status")==Order.TRADE_RETURNING_GOODS){
					jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
					return jsonMessage;
				}else{
					
					//判断当前订单是否待发货状态
					if(order.getInt("status") != Order.STATUS_HAD_SEND){
						jsonMessage.setStatusAndMsg("2", "订单状态已过期，请刷新订单后再次操作");
						return jsonMessage;
					}
					//判断订单是否是当前会员的订单
					if(!order.getStr("user_id").equals(userId)){
						jsonMessage.setStatusAndMsg("3", "订单状态已过期，请刷新订单后再次操作");
						return jsonMessage;
					}
					
					/******************【1.改变订单交易状态】*******************/
					order.set("trade_status", Order.TRADE_RETURNING_GOODS);//退货中
					Date now = new Date();
					Long remianRecievedmilliseconds = order.getDate("recieved_till_time").getTime()-now.getTime();
					order.set("remian_recieved_milliseconds", remianRecievedmilliseconds);//设置剩余截至收货时间
					order.set("recieved_stop_type", Order.RECIEVED_STOP_TYPE_RETURN);//设置订单截至收货时间暂停状态
					order.update();
					/******************【2.生成申请退款记录】*******************/
					OrderReturn orderReturn = new OrderReturn();
					orderReturn.set("no", StringUtil.getUnitCode(OrderReturn.NO_PREFIX));//生成编号
					orderReturn.set("user_id", userId);//会员ID
					orderReturn.set("order_id", orderId);//订单id
					orderReturn.set("merchant_id", order.getStr("merchant_id"));//商家id
					orderReturn.set("apply_time", new Date());//申请时间
					orderReturn.set("return_type", OrderReturn.TYPE_RETURN_GOOD);//退款类型：1.退款，2.退货
					orderReturn.set("return_reason", reason);//退款原因
					orderReturn.set("return_status", OrderReturn.RETURN_STATUS_APPLY);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））
					orderReturn.set("cash", order.getBigDecimal("total"));//退款金额(商品总额，退货将不退还运费)
					// 判断操作
					Integer returnId = OrderReturn.dao.getIdByOrderId(orderId);
					if (returnId == null) {
						orderReturn.save();			// 添加
					} else {
						orderReturn.set("id", returnId);
						orderReturn.update();		// 修改
					}
					/******************【3.生成订单日志】*******************/
					OrderLog.dao.add(orderId, "order_log_return_good", dataFrom);
					
					
					/******************发短信通知买家退货*******************/
					Integer orderType = order.getInt("order_type");// 订单类型
					// 店铺订单，自营专卖订单，厂家自发订单 才需要短信提醒
					if (orderType == Order.TYPE_SELF_SHOP || orderType == Order.TYPE_SHOP || orderType == Order.TYPE_SUPPLIER_SEND) {
						String merchantId = order.getStr("merchant_id");// 商家ID
						Ret ret = new Ret();
						// 触发短信来源
						ret.put("dataFrom", dataFrom);
						// 卖家ID
						ret.put("merchantId", merchantId);
						// 订单编号
						ret.put("orderNo", order.getStr("no"));
						ret.put("orderType", orderType);
						ret.put("SMSremark", "退货");
						EventKit.postEvent(new ShopOrderApplyReturnEvent(ret));
					}
				}
			}
		}else{
			jsonMessage.setStatusAndMsg("4", "订单不存在");
		}
		
		return jsonMessage;
	}
	
	 /**
     * 计算订单应付金额
     * @param userId
     * @param orderIds
     * @return
     * @author huangzq
     * 2017年1月1日 下午6:12:15
     *
     */
    public BigDecimal caculateOrderCost(String userId , String... orderIds){
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT");
		sql.append("	IFNULL(SUM(r.cost), 0)");
		sql.append(" FROM");
		sql.append("	t_order r");
		sql.append(" WHERE");
		sql.append("	r.`status` = ?");
		sql.append(" AND r.trade_status = ?");
		sql.append(" AND r.user_id = ?");
		sql.append(" AND r.id IN ("+StringUtil.arrayToStringForSql(",", orderIds)+")");
		return Db.queryBigDecimal(sql.toString(), Order.STATUS_WAIT_FOR_PAYMENT,Order.TRADE_NORMAL,userId);
    }
	
	/**
	 * 判断购物车列表是否全为自提订单
	 * @param cartList
	 * @return
	 * @author Jacob
	 * 2015年12月28日下午5:52:21
	 */
	public boolean isAllPickUpOrder(List<Record> cartList){
		for(Record cart : cartList){
			if(cart.getInt("delivery_type")==Order.DELIVERY_TYPE_EXPRESS){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 保存自提订单
	 * @param userId 当前会员ID
	 * @param deliveryTypes 购买的购物车配送方式数组
	 * @param orderType 订单类型
	 * @return
	 * @author Jacob
	 * 2015年12月15日下午7:58:39
	 */
	public Order addPickUpOrder(String userId,Record cart,Integer orderType,
			String merchantId,String merchantName,String merchantNo,String dataFrom, String remark){
		//初始化订单
		Order order = new Order().init(userId, dataFrom, remark);
		order.set("total", cart.getBigDecimal("sku_price").multiply(new BigDecimal(cart.getInt("count"))));//商品总金额（不含运费）商品sku价格*购买数量
		order.set("delivery_type", Order.DELIVERY_TYPE_SELF);//配送方式
		order.set("o2o_shop_no", cart.getStr("o2o_shop_no"));//o2o门店编号
		order.set("o2o_shop_name",  cart.getStr("o2o_shop_name"));//o2o门店名称
		order.set("o2o_shop_address", cart.getStr("o2o_shop_address"));//o2o门店地址
		order.set("concat", cart.getStr("pick_up_name"));//收货人姓名
		order.set("mobile", cart.getStr("pick_up_mobile"));//收货人手机号
		order.set("order_type", orderType);//订单类型
		order.set("merchant_id", merchantId);//商家ID
		order.set("merchant_no", merchantNo);//商家编号
		order.set("merchant_name", merchantName);//商家名称
		if(orderType==Order.TYPE_SHOP||orderType==Order.TYPE_SELF_SHOP){
			//根据店铺类型，记录订单幸运一折+类型
			order.set("efun_plus_type", Shop.dao.getType(merchantId));//幸运一折+类型
		}
		if(orderType==Order.TYPE_SHOP||orderType==Order.TYPE_SELF_SHOP){
			//根据店铺类型，记录订单幸运一折+类型
			order.set("efun_plus_type", Shop.dao.getType(merchantId));//幸运一折+类型
		}
		//更新订单
		order.update();
		return order;
	}
	
	/**
	 * 保存快递（或幸运一折+）订单
	 * @param order  初始化订单数据
	 * @param userId  会员ID
	 * @param addressId  收货地址ID
	 * @param total  商品总额
	 * @param orderType  订单类型
	 * @param merchantId  商家ID
	 * @param merchantName  商家名称
	 * @author Jacob
	 * 2015年12月16日上午9:59:26
	 */
	public Order addExpressOrder(Order order,String userId,Integer addressId,BigDecimal total,Integer orderType,
			String merchantId,String merchantName,String merchantNo, String remark){
		order.set("total", total);//商品总金额（不含运费）商品sku价格*购买数量累计
		order.set("delivery_type", Order.DELIVERY_TYPE_EXPRESS);//配送方式
		//获取收货地址信息
		RecAddress address = RecAddress.dao.findById(addressId);
		order.set("concat", address.get("contact"));//收货人姓名
		order.set("mobile", address.get("mobile"));//收货人手机号
		order.set("tel", address.get("tel"));//收货人固话
		order.set("province", Address.dao.getNameByCode(address.getInt("province_code")));//收货人所在省
		order.set("city", Address.dao.getNameByCode(address.getInt("city_code")));//收货人所在市
		order.set("area", Address.dao.getNameByCode(address.getInt("area_code")));//收货人所在区
		order.set("address", address.get("address"));//收货人具体地址
		order.set("zip", address.get("zip"));//收货人邮编
		order.set("order_type", orderType);//订单类型
		order.set("merchant_id", merchantId);//商家ID
		order.set("merchant_name", merchantName);//商家名称
		order.set("merchant_no", merchantNo);//商家编号
		order.set("remark", remark);//下订单时填写备注
		if(orderType==Order.TYPE_SHOP||orderType==Order.TYPE_SELF_SHOP){
			//根据店铺类型，记录订单幸运一折+类型
			order.set("efun_plus_type", Shop.dao.getType(merchantId));//幸运一折+类型
			//判断是否为幸运一折吃订单
			if(Shop.dao.getType(merchantId)==Order.EFUN_PLUS_EAT){
				//设置幸运一折吃配送方式:到店消费
				order.set("delivery_type", Order.DELIVERY_TYPE_SHOP_CONSUME);//幸运一折+类型
			}
		}
		if(orderType==Order.TYPE_SHOP||orderType==Order.TYPE_SELF_SHOP){
			//根据店铺类型，记录订单幸运一折+类型
			order.set("efun_plus_type", Shop.dao.getType(merchantId));//幸运一折+类型
			//判断是否为幸运一折吃订单
			if(Shop.dao.getType(merchantId)==Order.EFUN_PLUS_EAT){
				//设置幸运一折吃配送方式:到店消费
				order.set("delivery_type", Order.DELIVERY_TYPE_SHOP_CONSUME);//幸运一折+类型
			}
		}
		//更新订单
		order.update();
		
		return order;
	}
	
	
	
	/**
	 * 获取订单金额从小到大排序订单列表(供订单支付使用)
	 * @param orderIds 订单ID以","连接字符串
	 * @return
	 * @author Jacob
	 * 2015年12月17日下午6:19:30
	 */
	public List<Record> findOrderList4Payment(String orderIds){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	o.id, ");
		sql.append(" 	o.`no`, ");
		sql.append(" 	o.total, ");
		sql.append(" 	o.freight, ");
		sql.append(" 	o.total+o.freight total_amount, ");
		sql.append(" 	o.delivery_type, ");
		sql.append(" 	o.o2o_shop_name, ");
		sql.append(" 	o.o2o_shop_address, ");
		sql.append(" 	o.province, ");
		sql.append(" 	o.city, ");
		sql.append(" 	o.area, ");
		sql.append(" 	o.address, ");
		sql.append(" 	o.concat, ");
		sql.append(" 	o.mobile ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append(" 	id IN ( ");
		String[] strList = orderIds.split(",");
		for(String id : strList){
			sql.append("?,");
			params.add(id);
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" ) ");
		sql.append(" Order BY ");
		sql.append(" 	total_amount ASC ");
		return Db.find(sql.toString(),params.toArray());
	}
	
	/**
	 * 支付完成后更新会员账户和生成对账单
	 * @param userId 会员ID
	 * @param useCash 使用的账户现金金额
	 * @param useIntegral 使用的账户积分数
	 * @param orderNo 订单ID（可多个，以“,”连接）
	 * @author Jacob
	 * 2015年12月30日下午4:48:37
	 */
	public void updateAccountAfterPay(String userId,BigDecimal useCash,Integer useIntegral,String orderNo){
		//获取用户账号
		User user = User.dao.findByIdLoadColumns(userId, "user_name");
		
		if(useCash.compareTo(new BigDecimal("0"))==1||useIntegral>0){
			/************************* 1更新账户信息***********************/
			Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
			//更新账户现金余额减去订单使用了的现金金额
			account.set("cash", account.getBigDecimal("cash").subtract(useCash));//账户现金余额
			//更新账户积分值减去订单使用了的积分值
			account.set("integral", account.getInt("integral")-useIntegral);//账户积分值
			account.set("update_time", new Date());
			account.update();
			
			/************************* 2添加账户流水记录(现金记录跟积分记录)***********************/
			if(useCash.compareTo(new BigDecimal("0"))==1){
				
				BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
				UserCashRecord.dao.add(useCash.multiply(new BigDecimal(-1)), account.getBigDecimal("cash").add(freezeCash), UserCashRecord.TYPE_SHOPPING, userId, "购物订单-"+orderNo);
			}
			if(useIntegral>0){
				IntegralRecord.dao.add(-useIntegral, account.getInt("integral"),  IntegralRecord.TYPE_SHOPPING, userId, user.getStr("user_name"), "购物订单-"+orderNo);
			}
		}
	}
	
	/**
	 * 根据商品SKU识别码获取订单SKU（包含商家信息、商品信息、商品sku信息、商品属性及属性值信息）
	 * @param skuCode 商品SKU识别码
	 * @param addressId 收货地址ID
	 * @param proNum 购买数量
	 * @return
	 * @author Jacob
	 * 2015年12月21日下午2:23:16
	 */
	public Record getOrderSku(String skuCode,Integer addressId,Integer proNum){
		
		Record orderSku = ProductSku.dao.getOrderSku(skuCode);
		//运费计算所需map
		Map<String,Integer> sukCodeCountMap = new HashMap<String,Integer>();
		sukCodeCountMap.put(skuCode, proNum);
		//初始化订单运费
		BigDecimal freight = new BigDecimal("0");
		if(addressId!=null){
			RecAddress recAddress = RecAddress.dao.findByIdLoadColumns(addressId, "province_code,city_code,area_code");
			//计算该组运费
			if(recAddress!=null){
				freight = FreightTemplate.dao.calculate(sukCodeCountMap, recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
				freight = freight.setScale(2,BigDecimal.ROUND_CEILING);
			}
		}
		//保存运费
		orderSku.set("freight", freight);
		return orderSku;
	}
	
	/**
	 * 获取订单总额
	 * @param orderIds  订单ID以","连接字符串
	 * @return
	 * @author Jacob
	 * 2015年12月17日上午11:44:21
	 */
	public BigDecimal getTotal(String orderIds){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT SUM(total+freight) FROM t_order WHERE id IN ( ");
		String[] strList = orderIds.split(",");
		for(String id : strList){
			sql.append("?,");
			params.add(id);
		}
		sql.deleteCharAt(sql.length()-1);
		sql.append(" ) ");
		return Db.queryBigDecimal(sql.toString(),params.toArray());
	}
	
	/**
	 * 获取会员订单
	 * @param orderId
	 * @param userId 会员ID
	 * @param orderStatus 订单状态
	 * @return
	 * @author Jacob
	 * 2016年4月20日下午3:04:12
	 */
	public Order getOrder4User(String orderId,String userId,int orderStatus){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_order WHERE `status` = ? AND user_id = ? AND id = ? ");
		Order order =  dao.findFirst(sql.toString(), orderStatus, userId, orderId);
		if(order!=null){
			order = dao.getOrderForUpdate(orderId);
		}
		return order;
	}
	
	/**
	 * 获取会员订单
	 * @param orderId
	 * @param userId 会员ID
	 * @param orderStatus 订单状态
	 * @return
	 * @author Jacob
	 * 2016年4月20日下午3:04:12
	 */
	public Order getOrder4User(String orderId,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_order WHERE user_id = ? AND id = ? ");
		return dao.findFirst(sql.toString(), userId, orderId);
	}
	
	/**
	 * 获取会员订单
	 * @param orderId
	 * @param userId 会员ID
	 * @param orderStatus 订单状态
	 * @return
	 * @author Jacob
	 * 2016年4月20日下午3:04:12
	 */
	public Order getOrder4UserForUpdate(String orderId,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_order WHERE user_id = ? AND id = ? for update ");
		return dao.findFirst(sql.toString(), userId, orderId);
	}
	
	/**
	 * 获取商家订单
	 * @param orderId
	 * @param merchantId 商家ID（店铺ID或供货商ID）
	 * @param orderStatus 订单状态
	 * @return
	 * @author Jacob
	 * 2016年4月20日下午3:04:12
	 */
	public Order getOrder4Merchant(String orderId,String merchantId,int orderStatus){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_order WHERE `status` = ? AND merchant_id = ? AND id = ? ");
		return dao.findFirst(sql.toString(), orderStatus, merchantId, orderId);
	}
	
	/**
	 * 在进行超卖订单发货时
	 * 如果该商家存在非超卖、待发货订单，那么判断：当前库存-待发货订单的库存 >= 当前超卖订单发货
	 * @param orderId
	 * @param merchantId
	 * @return
	 
	public boolean isEnoughForOverSellOrderSend(String orderId, String merchantId){
		List<Record> skuList = OrderDetail.dao.findSkuCodeList(orderId);
		for(Record sku : skuList){
			StringBuffer sql = new StringBuffer();
			List<Object> paras = new ArrayList<Object>();
			sql.append(" SELECT  ");
			sql.append("  sum(od.count) as num,");
			sql.append(" FROM t_order o ");
			sql.append("  LEFT JOIN t_order_detail od ON od.order_id = o.id");
			sql.append(" WHERE o.id <> ? ");
			sql.append("  AND o.merchant_id = ?");
			sql.append("  AND o.`status` = ?");
			sql.append("  AND o.trade_status = ?");
			sql.append("  AND o.is_over_sell = ?");
			sql.append("  AND od.sku_code = ?");
			sql.append(" GROUP BY od.sku_code ");
			paras.add(orderId);
			paras.add(merchantId);
			paras.add(Order.STATUS_WAIT_FOR_SEND);
			paras.add(Order.TRADE_NORMAL);
			paras.add(BaseConstants.NO);
			paras.add(sku.getStr("skuCode"));
			
			Record rec = Db.findFirst(sql.toString(), paras);
			
			if(rec != null){
				int total = rec.getNumber("num").intValue() + sku.getInt("count");
				if(!ProductSku.dao.enoughVirtualCountForSend(sku.getStr("skuCode"), total)){
					return false;
				}
			}
		}
		
		return true;
	}
	*/
	
	/**
	 * 获取订单状态
	 * @param orderId
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年4月20日下午4:30:39
	 */
	public int getStatus(String orderId, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT `status` FROM t_order WHERE user_id = ? AND id = ? ");
		return Db.queryInt(sql.toString(), userId, orderId);
	}
	
	/**
	 * 获取订单收货信息
	 * @param orderId
	 * @param userId
	 * @return
	 * @author Sylveon
	 */
	public Order getRecievedInfo(String orderId, String userId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(		" id, delivery_type, send_time, recieved_till_time ");
		sql.append(" FROM t_order ");
		sql.append(" WHERE 1 = 1 ");
		sql.append(" AND id = ? ");
		sql.append(" AND user_id = ? ");
		sql.append(" AND status = ? ");
		return Order.dao.findFirst(sql.toString(), orderId, userId, Order.STATUS_HAD_SEND);	
	}
	/**
	 * 
	 * 获取订单（锁定）
	 * @author huangzq 
	 * @date 2016年6月22日 上午11:11:01
	 * @return
	 */
	public Order getOrderForUpdate(String orderId){
		String sql = "select * from t_order r where r.id = ? for update";
		return Order.dao.findFirst(sql,orderId);
		
	}
	/**
	 * 根据订单ID获取会员ID
	 * @author Sylveon
	 */
	public String getUserId(String orderId) {
		return Db.queryStr("SELECT user_id FROM t_order WHERE id = ?", orderId);
	}
	/**
	 * 添加订单详情运费描述
	 * @param orderId
	 */
	public void addOrderRuleDesc(String orderId){
		//查询运费规则
		Order order = Order.dao.findByIdLoadColumns(orderId, "delivery_type,province,city,area");
		//过滤非快递订单
		if(order.getInt("delivery_type")!=Order.DELIVERY_TYPE_EXPRESS){
			return;
		}
		List<Record> orderDetailList = OrderDetail.dao.findOrderDetailList(orderId);
		for(Record orderDetail : orderDetailList){
			//添加运费规则描述
			Product product = Product.dao.findByIdLoadColumns(orderDetail.getInt("product_id"), "is_free_postage,freight_id");
			String freightRuleDesc = "";
			if(product.getInt("is_free_postage")==BaseConstants.YES){
				freightRuleDesc="包邮";
			}else{
			
				Record provinceRecord = Address.dao.getProvCityAreaStreetByName(order.getStr("province")); // 省.
				Record cityRecord = Address.dao.getProvCityAreaStreetByName(order.getStr("city")); // 市.
				Record areaRecord = Address.dao.getProvCityAreaStreetByName(order.getStr("area")); // 市.				
				Record freightRule = FreightTemplate.dao.matchingFreightRule(product.getInt("freight_id"),provinceRecord.getInt("code"), cityRecord.getInt("code"), areaRecord.getInt("code"));					
				//免邮规则
				String rule = "";
				int ruleCode = freightRule.getInt("rule_code");
				if(ruleCode==FreightRule.TYPE_NOT){
					rule="不免邮";
				}else if(ruleCode==FreightRule.TYPE_AMOUNT){
					rule="满X元免邮";
				}else if(ruleCode==FreightRule.TYPE_NUMBER){
					rule="满X件免邮";
				}
				freightRuleDesc = "省代码："+freightRule.getInt("province_code")
						+"; 市代码："+freightRule.getInt("city_code")
						+"; 区代码："+freightRule.getInt("area_code")
						+"; 首重金额："+freightRule.getBigDecimal("first_weigth_cash")
						+"; 首重量数："+freightRule.getBigDecimal("first_weigth_num")
						+"; 续重金额："+freightRule.getBigDecimal("add_weight_cash")
						+"; 免邮规则："+rule
						+"; 条件值："+freightRule.getBigDecimal("condition");
				
			}
			orderDetail.set("freight_rule", freightRuleDesc);//运费规则
			Db.update("t_order_detail", orderDetail);
		}
		
	}
	
	/**
	 * 根据订单ID删除订单
	 * @author chenhj
	 * @param orderId
	 * @return
	 */
	public boolean deleteOrder(String orderId, String userId){
		Order order = getOrder4UserForUpdate(orderId, userId);
		if (order!=null) {
			order.set("delete_status", DELETE_STATUS_DELETE);
			return order.update();
		}
		return false;
	}
	
	/**
	 * 获取过了付款截至时间的订单ID列表
	 * @return
	 * @author Jacob
	 * 2015年12月23日下午4:14:46
	 */
	public List<String> findInvalidOrderList4Pay(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT o.id FROM t_order o WHERE o.`status` = ? AND o.trade_status = ? AND o.pay_till_time < NOW() ");
		return Db.query(sql.toString(),Order.STATUS_WAIT_FOR_PAYMENT,Order.TRADE_NORMAL);
	}
	
	/**
	 * 处理过了收货截至时间的订单
	 * @author Jacob
	 * 2015年12月24日下午4:30:18
	 * Modify BySylveon 更新销售销量
	 */
	public void handleInvalidOrderList4Receipt(){
		
		L.info("处理过了收货截至时间的订单");
		
		/**********************【1.查询过了收货截至时间的订单息】************************/
		StringBuffer searchSql = new StringBuffer(" SELECT o.id FROM t_order o ");
		searchSql.append(" WHERE o.`status` = ? ");
		searchSql.append(" AND o.trade_status = ? ");
		searchSql.append(" AND o.delivery_type = ? ");
		searchSql.append(" AND o.recieved_stop_type = ? ");
		searchSql.append(" AND o.recieved_till_time >= STR_TO_DATE('2016-04-21 00:00:00','%Y-%m-%d %H:%i:%s') ");
		searchSql.append(" AND o.recieved_till_time <= NOW() ");
		List<String> orderIdList = Db.query(searchSql.toString(), Order.STATUS_HAD_SEND, Order.TRADE_NORMAL, Order.DELIVERY_TYPE_EXPRESS, Order.RECIEVED_STOP_TYPE_NORMAL);
		
		/**********************【2.改变订单状态】************************/
		//////【确认收货后订单状态改变为待评价，确认收货时间保存为确认收货截至时间】
		StringBuffer sql = new StringBuffer();
		sql.append(" UPDATE t_order o ");
		sql.append(" SET o.`status` = ?, ");
		sql.append("  o.comfirm_time = o.recieved_till_time ");
		sql.append(" WHERE ");
		sql.append(" 	o.`status` = ? ");
		sql.append(" AND o.trade_status = ? ");
		sql.append(" AND o.delivery_type = ? ");
		sql.append(" AND o.recieved_stop_type = ? ");
		sql.append(" AND o.recieved_till_time >= STR_TO_DATE('2016-04-21 00:00:00','%Y-%m-%d %H:%i:%s') ");
		sql.append(" AND o.recieved_till_time <= NOW() ");
		Db.update(sql.toString(), Order.STATUS_WAIT_FOR_EVALUATION, Order.STATUS_HAD_SEND, Order.TRADE_NORMAL, Order.DELIVERY_TYPE_EXPRESS, Order.RECIEVED_STOP_TYPE_NORMAL);
		
		/**********************【3.增加销售量】************************/
		for (String oid : orderIdList)
			Product.dao.plusSalesSettle(oid, false);
		
		/**********************【4.双旦活动】************************/
		for (String oid : orderIdList)
			EggPairInterceptor.confirmGoods(oid);
	}
	
	/**
	 * 获取待自动结算的订单ID列表
	 * @return
	 * @author Jacob
	 * 2015年12月24日下午4:28:51
	 */
	public List<String> findInvalidOrderIdList4Receipt(){
		L.info("获取待自动结算的订单ID列表");
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	id ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	o.`status` >= ? ");
		sql.append(" AND o.trade_status = ? ");
		sql.append(" AND o.is_balance = ? ");
		sql.append(" AND o.delivery_type != ? ");
		sql.append(" AND o.comfirm_time >= STR_TO_DATE('2016-04-14 00:00:00','%Y-%m-%d %H:%i:%s') ");
		sql.append(" AND o.comfirm_time <= NOW() ");
		return Db.query(sql.toString(), Order.STATUS_WAIT_FOR_EVALUATION, Order.TRADE_NORMAL, BaseConstants.NO, Order.DELIVERY_TYPE_STORE_SALE);
	}
	
	/**
	 * 获取未缴纳保证金店铺待结算订单列表
	 * @return
	 * @author Jacob
	 * 2016年1月3日下午1:27:39
	 */
	public List<Order> findNotPayDepositShopBalanceOrderList(){
		L.info("获取未缴纳保证金店铺待结算订单列表");
		//获取系统配置参数：未缴纳保证金店铺订单待结算时间长度（天数）【注明：减1的意义是由于系统每天定时凌晨处理前一天待结算的订单】
		Integer balanceDays = SysParam.dao.getIntByCode("balance_day")-1;
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	* ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	o.trade_status = ? ");
		params.add(Order.TRADE_NORMAL);
		//订单是未结算的
		sql.append(" AND ");
		sql.append(" 	o.is_balance = ? ");
		params.add(BaseConstants.NO);
		//店铺订单(第三方店铺跟自营店铺订单)
		sql.append(" AND ");
		sql.append(" 	o.order_type IN (?,?) ");
		params.add(Order.TYPE_SHOP);
		params.add(Order.TYPE_SELF_SHOP);
		//订单是已收货的
		sql.append(" AND ");
		sql.append(" 	o.status > ? ");
		params.add(Order.STATUS_HAD_SEND);
		//订单总额大于0
		sql.append(" AND ");
		sql.append(" 	o.total > 0 ");
		sql.append(" AND o.comfirm_time >= STR_TO_DATE( ");
		sql.append(" 	'2016-04-14 00:00:00', ");
		sql.append(" 	'%Y-%m-%d %H:%i:%s' ");
		sql.append(" ) ");
		sql.append(" AND DATE_ADD( ");
		sql.append(" 	o.comfirm_time, ");
		sql.append(" 	INTERVAL ? DAY ");
		params.add(balanceDays);
		sql.append(" ) < DATE_FORMAT( ");
		sql.append(" 	DATE_ADD(NOW(), INTERVAL - 1 DAY), ");
		sql.append("	'%Y-%m-%d 23:59:59' ");
		sql.append(" ) ");
		return Order.dao.find(sql.toString(),params.toArray());
	}
	
	/**
	 * 判断虚拟库存是否足够该订单发货
	 * @param orderId
	 * @param merchantId
	 * @return
	 */
	public JsonMessage isEnoughCount(String orderId, String merchantId){
		JsonMessage jsonMessage = new JsonMessage();
		//判断订单是否属于商家及状态
		Order order = Order.dao.getOrder4Merchant(orderId, merchantId, Order.STATUS_WAIT_FOR_SEND);
		if(order==null){
			jsonMessage.setStatusAndMsg("1", "订单不是待发货状态，请刷新后再操作");
		}
		//获取订单明细的商品SKU识别码和购买数量列表
		List<Record> skuCodeCountList = OrderDetail.dao.findSkuCodeList(orderId);
		for(Record r : skuCodeCountList){
			if(!ProductSku.dao.enoughVirtualCountForSend(r.getStr("skuCode"), r.getInt("count"))){
				jsonMessage.setStatusAndMsg("1", "库存不足，请增加商品库存后，再操作！");
				break;
			}
		}
		
		return jsonMessage;
	}
	
	/**
	 * 获取待返利订单列表
	 * @return
	 * @author Jacob
	 * 2016年1月3日下午1:27:39
	 */
	public List<Order> findRebatesList(){
		//获取系统配置参数：订单返利时间长度（天数）【注明：减1的意义是由于系统每天定时凌晨处理前一天待返利的订单】
		Integer rebatesDays = SysParam.dao.getIntByCode("rebates_day")-1;
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	* ");
		sql.append(" FROM ");
		sql.append(" 	t_order o ");
		sql.append(" WHERE ");
		sql.append(" 	o.trade_status = ? ");
		params.add(Order.TRADE_NORMAL);
		sql.append(" AND ");
		sql.append(" 	o.rebate_status = ? ");
		params.add(Order.REBATE_WAIT);
		sql.append(" AND o.comfirm_time >= STR_TO_DATE( ");
		sql.append(" 	'2016-04-14 00:00:00', ");
		sql.append(" 	'%Y-%m-%d %H:%i:%s' ");
		sql.append(" ) ");
		sql.append(" AND DATE_ADD( ");
		sql.append(" 	o.comfirm_time, ");
		sql.append(" 	INTERVAL ? DAY ");
		params.add(rebatesDays);
		sql.append(" ) <= DATE_FORMAT( ");
		sql.append(" 	DATE_ADD(NOW(), INTERVAL - 1 DAY), ");
		sql.append("	'%Y-%m-%d 23:59:59' ");
		sql.append(" ) ");
		return Order.dao.find(sql.toString(),params.toArray());
	}
	
	/**
	 * 订单返利
	 * @param order 订单信息
	 * @author Jacob
	 * 2016年3月26日下午2:34:52
	 */
	public void orderRebates(Order order){
		//下单会员ID
		String userId = order.getStr("user_id");
		
		//获取会员所在店铺ID和代理商ID
		User user = User.dao.findByIdLoadColumns(userId, "shop_id,agent_id");
		String userShopId = user.getStr("shop_id");
		String userAgentId = user.getStr("agent_id");
		//获取会员所在店铺账户
		Account userShopAccount = Account.dao.getAccountForUpdate(userShopId, Account.TYPE_SHOP);
		//获取会员所属代理商账户
		Account userAgentAccount = Account.dao.getAccountForUpdate(userAgentId, Account.TYPE_AGENT);
		//初始化会员所在店铺EV值返利总额
		BigDecimal totalShopEvRebates = new BigDecimal("0");
		//初始化会员所属代理商EV值返利总额
		BigDecimal totalAgentEvRebates = new BigDecimal("0");
		//初始化经代理商推荐的厂家商品有产生交易时该代理商EV值返利总额
		BigDecimal totalAgentRecommendEvRebates = new BigDecimal("0");
		//获取当前订单的订单详情列表
		List<Record> orderDetailList = Order.dao.findOrderDetailsByOrderId(order.getStr("id"));
		//当前订单现金总额（余额支付+在线支付-运费）
		BigDecimal totalCash = order.getBigDecimal("cash").add(order.getBigDecimal("cost").subtract(order.getBigDecimal("freight")));
		//初始化店铺返利类型
		int shopRebateType = 0;
		/////【1.计算EV值】////
		for(Record orderDetail : orderDetailList){
			//初始化EV值
			BigDecimal ev = StringUtil.notNull(orderDetail.getBigDecimal("ev"))?orderDetail.getBigDecimal("ev"):BigDecimal.ZERO;
			/**公共商品（自营公共订单、E趣代销订单、厂家自发订单）:EV值=公共商品的市场价*7%*购买数量**/
			if(order.getInt("order_type")==Order.TYPE_SELF_PUBLIC||order.getInt("order_type")==Order.TYPE_SELL_BY_PROXY||
					order.getInt("order_type")==Order.TYPE_SUPPLIER_SEND){
				/////【2.3.2.经代理商推荐的厂家商品有产生交易（返利：返EV值*10%的现金）】////
				//获取推荐供货商的代理商ID
				String agentId = Supplier.dao.findByIdLoadColumns(order.getStr("merchant_id"), "agent_id").getStr("agent_id");
				if(StringUtil.notNull(agentId)){
					totalAgentRecommendEvRebates = totalAgentRecommendEvRebates.add(ev.multiply(new BigDecimal("0.1")));
				}
			}
			/////【2.2.店铺-现金】////
			//获取商品下单所在店铺ID
			String orderShopId = orderDetail.getStr("orderShopId");
			//获取商品所属店铺ID
			String shopId = Product.dao.findByIdLoadColumns(orderDetail.getInt("productId"), "shop_id").getStr("shop_id");
			//获取商品来源
			int productSource = Product.dao.findByIdLoadColumns(orderDetail.getInt("productId"), "source").getInt("source");
			/////【2.2.1.会员直接在所属店铺进行购物（返利：返EV值*30%的现金）】////
			if(userShopId.equals(shopId)){
				shopRebateType = ShopCashRecord.TYPE_REBATE_SELF_ORDER;
				totalShopEvRebates = totalShopEvRebates.add(ev.multiply(new BigDecimal("0.3")));
			}else if(userShopId.equals(orderShopId)&&(productSource==Product.SOURCE_FACTORY||productSource==Product.SOURCE_FACTORY_SEND||productSource==Product.SOURCE_SELF_PUBLIC)){/////【2.2.2.会员直接在所属店铺购买公共商品（返利：返EV值*30%的现金）】////
				shopRebateType = ShopCashRecord.TYPE_REBATE_PUBLIC_ORDER;
				totalShopEvRebates = totalShopEvRebates.add(ev.multiply(new BigDecimal("0.3")));
			}else{/////【2.2.3.会员在其他店铺购物（返利：返EV值*5%的现金）////
				shopRebateType = ShopCashRecord.TYPE_REBATE_OTHER_ORDER;
				totalShopEvRebates = totalShopEvRebates.add(ev.multiply(new BigDecimal("0.05")));
			}
			/////【2.3.代理商-现金】////
			/////【2.3.1.代理商名下各店铺的绑定会员发生购物行为（返利：返EV值*5%的现金）】////
			totalAgentEvRebates = totalAgentEvRebates.add(ev.multiply(new BigDecimal("0.05")));
		}
		
		/////【2.返利】////
		/////【2.1.会员返利-积分】////
		if(order.getInt("return_integral")>0){
			//保存积分获取记录（同时更新会员账户积分数并生成账户积分变动记录）
			//Integral.dao.save(userId, order.getInt("return_integral"), "购物返积分-"+order.getStr("no"),order.getStr("no"));
		}
		
		if(totalShopEvRebates.compareTo(new BigDecimal("0"))==1){
			
			if(totalShopEvRebates.compareTo(new BigDecimal("0.01"))==-1){
				totalShopEvRebates = new BigDecimal("0.01");
			}
			//更新会员所属店铺账户
			userShopAccount.set("cash", userShopAccount.getBigDecimal("cash").add(totalShopEvRebates).setScale(2,BigDecimal.ROUND_DOWN));
			userShopAccount.set("update_time", new Date());
			userShopAccount.update();
			
			//生成会员所在店铺账户现金变化记录
			BigDecimal freezeCash = userShopAccount.getBigDecimal("freeze_cash");
			ShopCashRecord.dao.add(totalShopEvRebates, userShopAccount.getBigDecimal("cash").add(freezeCash), order.getStr("no"), 
					shopRebateType, userShopId, "订单"+order.getStr("no")+"返利："+totalShopEvRebates.setScale(2,BigDecimal.ROUND_DOWN)+"元");
		}
		
		if(totalAgentEvRebates.compareTo(new BigDecimal("0"))==1){
			
			if(totalAgentEvRebates.compareTo(new BigDecimal("0.01"))==-1){
				totalAgentEvRebates = new BigDecimal("0.01");
			}
			
			//更新会员所属代理商账户
			userAgentAccount.set("cash", userAgentAccount.getBigDecimal("cash").add(totalAgentEvRebates).setScale(2,BigDecimal.ROUND_DOWN));
			userAgentAccount.set("update_time", new Date());
			userAgentAccount.update();
			
			//生成会员所在代理商账户现金变化记录
			BigDecimal freezeCash = userAgentAccount.getBigDecimal("freeze_cash");
			AgentCashRecord.dao.add(totalAgentEvRebates, userAgentAccount.getBigDecimal("cash").add(freezeCash), order.getStr("no"), 
					AgentCashRecord.REBATE_USER_SHOPPING,null, userAgentId, "订单"+order.getStr("no")+"返利："+totalAgentEvRebates.setScale(2,BigDecimal.ROUND_DOWN)+"元");
		}
		
		//获取经代理商推荐的厂家商品有产生交易时该代理商商账户
		if((order.getInt("order_type")==Order.TYPE_SELF_PUBLIC||order.getInt("order_type")==Order.TYPE_SELL_BY_PROXY||
				order.getInt("order_type")==Order.TYPE_SUPPLIER_SEND)&&totalAgentRecommendEvRebates.compareTo(new BigDecimal("0"))==1){
			
			if(totalAgentEvRebates.compareTo(new BigDecimal("0.01"))==-1){
				totalAgentEvRebates = new BigDecimal("0.01");
			}
			
			//获取推荐供货商的代理商ID
			String agentId = Supplier.dao.findByIdLoadColumns(order.getStr("merchant_id"), "agent_id").getStr("agent_id");
			if(StringUtil.notNull(agentId)){
				Account userAgentRecommendAccount = Account.dao.getAccountForUpdate(agentId, Account.TYPE_AGENT);
				
				//更新会员所属代理商账户
				userAgentRecommendAccount.set("cash", userAgentRecommendAccount.getBigDecimal("cash").add(totalAgentRecommendEvRebates).setScale(2,BigDecimal.ROUND_DOWN));
				userAgentRecommendAccount.set("update_time", new Date());
				userAgentRecommendAccount.update();
				
				//生成推荐供货商的代理商账户现金变化记录
				AgentCashRecord.dao.add(totalAgentRecommendEvRebates, userAgentRecommendAccount.getBigDecimal("cash"), order.getStr("no"), 
						AgentCashRecord.REBATE_RECOMMEND_SALES,null, agentId, "订单"+order.getStr("no")+"返利："+totalAgentRecommendEvRebates.setScale(2,BigDecimal.ROUND_DOWN)+"元");
			}
		}
		//更新订单返利状态
		order.set("rebate_status", Order.REBATE_SUCCESS);
		//更新订单信息
		order.update();
	}
	
	/**
	 * 取消订单，扣减锁定库存
	 * @param orderIds 多个订单id用英文逗号隔开
	 * @return
	 * @throws SQLException
	 * @author chenhg
	 * 2016年11月18日 下午1:50:46
	 */
	public boolean subLockCountForOrderIds(String orderIds) throws SQLException{
		String[] orderIdArr = orderIds.split(",");
		for(String orderId : orderIdArr){
			Order order = dao.getOrderForUpdate(orderId);
			if(order.getInt("status") != Order.STATUS_WAIT_FOR_PAYMENT){
				L.error("非待支付的订单--取消订单，扣减锁定库存失败，订单id："+orderId);
				continue;
			}
			
			//九折购订单，释放参与记录的锁定库存
			if(order.getInt("is_efun_nine") == BaseConstants.YES 
					&& order.getInt("is_release_lock_count") == Order.IS_RELEASE_LOCK_COUNT_Y){
				EfunUserOrder euo = EfunUserOrder.dao.getEfunUserOrderForUpdateWithOrderId(orderId);
				if(euo.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_N){
					if(ProductSku.dao.subtractLockCount(euo.getStr("sku_code"), 1)){
						//更新一折购参与记录是否释放锁定库存的标志
						euo.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y);
						euo.update();
						order.set("is_release_lock_count", Order.IS_RELEASE_LOCK_COUNT_Y);
						order.update();
					}
					
				}
			}else{
				//锁定库存没有释放，释放锁定库存
				if(order.getInt("is_release_lock_count") == Order.IS_RELEASE_LOCK_COUNT_N){
					JsonMessage jm = ProductSku.dao.subtractLockCountForOrderId(order.getInt("delivery_type"), order.getStr("o2o_shop_no"), orderId);
					if("10".equals(jm.getStatus())){
						L.error("取消订单，扣减锁定库存失败，订单id："+orderId);
						continue;
					}
					order.set("is_release_lock_count", Order.IS_RELEASE_LOCK_COUNT_Y);
					order.update();
				}
			}
		}
		return true;
	}
	
	/**
	 * 根据订单ID返回收货地址编码
	 * @return
	 */
	public Record getToAddressCodeById(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append("select t1.`code` as provinceCode, t2.`code` as cityCode, t3.`code` as areaCode ")
		    .append(" from t_order o")
		    .append(" left join t_address t1 on o.province = t1.`name`")
		    .append(" left join t_address t2 on o.city = t2.`name` and t2.parent_code = t1.`code`")
		    .append(" left join t_address t3 on o.area = t3.`name` and t3.parent_code = t2.`code`")
		    .append(" where t2.`code` IS NOT NULL and t3.`code` IS NOT NULL ")
		    .append(" and o.id = ?");
		return Db.findFirst(sql.toString(), orderId);
	}
	
	/**
	 * 统计商家超卖订单
	 * @param merchantId
	 * @return
	 */
	public Integer countMerchantOverSell(String merchantId) {
		String sql = "select count(*) from t_order where merchant_id = ? and is_over_sell = ? and status in (?, ?) and trade_status < ?";
		return Db.queryLong(sql, merchantId, IS_OVER_SELL, STATUS_WAIT_FOR_SEND, STATUS_HAD_SEND, TRADE_CANCLE).intValue();
	}
	
	/**
	 * 统计会员超卖订单
	 * @param userId
	 * @return
	 */
	public Integer countUserOverSellOrder(String userId) {
		String sql = "select count(*) from t_order where user_id = ? and is_over_sell = ? and status in (?, ?) and trade_status < ?";
		return Db.queryLong(sql, userId, IS_OVER_SELL, STATUS_WAIT_FOR_SEND, STATUS_HAD_SEND, TRADE_CANCLE).intValue();
	}
	
	/**
	 * 统计会员一折购超卖订单
	 * @return
	 */
	public Integer countUserEfunOverSell(String userId) {
		String sql = "select count(*) from t_efun_user_order where status in (?, ?) and user_id = ? and is_over_sell = ?";
		return Db.queryLong(sql, STATUS_WAIT_FOR_SEND, STATUS_HAD_SEND, userId, IS_OVER_SELL).intValue();
	}
	/**
	 * 是否存在超卖订单
	 * @param orderIds
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2016年12月4日 上午10:52:57
	 *
	 */
	public boolean isOverSell(String orderIds,String userId){
		String[] orderArray = orderIds.split(",");
		String sql  = "select count(1) from t_order r where r.is_over_sell = ? and r.id in ("+StringUtil.arrayToStringForSql(",", orderArray)+") and r.user_id = ?";
		long count = Db.queryLong(sql,IS_OVER_SELL,userId);
        return count > 0;
    }
	
	/**
	 * 查询快递自提订单到货短信通知所需信息
	 * @param orderId
	 * @return
	 */
	public Record expressSelfSMSInfo(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ")
		   .append("  o.no as orderNo,")
		   .append("  o.o2o_shop_name as o2oName,")
		   .append("  o.taking_code as takingCode,")
		   .append("  u.user_name as userName,")
		   .append("  u.mobile")
		   .append(" from t_order o ")
		   .append(" left join t_user u on u.id = o.user_id")
		   .append(" where o.id = ?");
		return Db.findFirst(sql.toString(), orderId);
	}
	
	/**
	 * 立即领取提交订单.
	 * 
	 * @param prizeEfunOrderIdList
	 *            中奖领取的一折购订单Id列表.
	 * @author Chengyb
	 */
	public JsonMessage submitOrderFromPrize(
			String userId,
			Integer useIntegral,
			BigDecimal useCash,
			int deliveryType,
			Integer addressId,
			String o2oShopNo,
			String contact,
			String mobile,
			String payPassword,
			List<String> prizeEfunOrderIdList,
			String dataFrom,
			String remark) throws SQLException {
		
		DbKit.getConfig().setThreadLocalConnection(DbKit.getConfig().getConnection());
        DbKit.getConfig().getThreadLocalConnection().setAutoCommit(false);
		
		JsonMessage jsonMessage = new JsonMessage();
															
		List<String> orderIdList = new ArrayList<String>();	// 订单ID集合
		// 是否已经完全支付(用于判断是否增加锁定库存数).
	    boolean hasPayFlag = false;
		// 发送短信标识【0:不发送,1:待发货,2:已发货】.
		int smsTypeFlag = 0;

		//==================================================================
		// 库存不足商品数据.
		//==================================================================
		List<Map<String, Object>> understockList = new ArrayList<Map<String, Object>>();
		
		//==================================================================
		// 店铺冻结商品数据.
		//==================================================================
		List<Map<String, Object>> storeFrozenList = new ArrayList<Map<String, Object>>();
		
		//==================================================================
		// 商品下架删除数据.
		//==================================================================
		List<Map<String, Object>> unableBuyList = new ArrayList<Map<String, Object>>();
		
		//==================================================================
		// Sku数据. key-sku编码;value-下单数量.
		//==================================================================
		Map<String, Integer> skuMap = new HashMap<String, Integer>();
		
		//==================================================================
		// Sku商家数据. key-sku编码;value-店铺Id/供货商Id.
		//==================================================================
		Map<String, String> sellerMap = new HashMap<String, String>();
		
		//==================================================================
		// 云店自提订单.
		//==================================================================
		Map<String, Integer> cloudStoreOrderMap = new HashMap<String, Integer>();
		
		//==================================================================
		// 快递配送订单.
		//==================================================================
		Map<String, Map<String, Integer>> expressOrderMap = new HashMap<String, Map<String, Integer>>();
				
		//==================================================================
		// 快递自提订单.
		//==================================================================
		Map<String, Map<String, Integer>> cloudStoreExpressOrderMap = new HashMap<String, Map<String, Integer>>();
		
		// 可领取的一折购订单列表.
		List<EfunUserOrder> prizeEfunOrderList = new ArrayList<EfunUserOrder>();
		
		//==================================================================
		// 验证一折购订单所属用户以及是否已领取.
		//==================================================================
		Map<String, Object> map = validationPrize(userId, prizeEfunOrderIdList, jsonMessage, prizeEfunOrderList, skuMap, sellerMap);
		if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			return jsonMessage;
		}
		
		//==================================================================
		// 验证收货地址.
		//==================================================================
		if(null != addressId) {
			validationAddress(userId, addressId, jsonMessage);
			if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
				return jsonMessage;
			}
		}
		
		//==================================================================
		// 验证使用积分和余额.
		//==================================================================
		validationIntegralAndCash(userId, useIntegral, useCash, jsonMessage);
		if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			return jsonMessage;
		}
		
		//==================================================================
		// 验证库存.
		// 自提订单云店库存充足时直接生成自提订单,云店库存不足时转为云店自提+快递自提订单.
		//==================================================================
		validationInventory(prizeEfunOrderList, skuMap, deliveryType, o2oShopNo, jsonMessage, understockList);
		if(understockList.size() > 0) {
			return jsonMessage;
		}
		
		//==================================================================
		// 按照 1.店铺/供货商 2.库存 进行拆单.
		//==================================================================
//		divisionOrder(skuMap, sellerMap, deliveryType, o2oShopNo, cloudStoreOrderMap, expressOrderMap, cloudStoreExpressOrderMap);
		
		//==================================================================
		// 生成订单.
		// 按照店铺/供货商进行拆单.
		//==================================================================
//		Map<String, Object> orderMap = createOrder(userId, skuMap, prizeEfunOrderList, (List<String>) map.get("efunOrderDetailIdList"), BigDecimal.ZERO, true, orderSku, proNum, deliveryType, addressId, o2oShopNo, contact, mobile, dataFrom, remark);
//		Order order = (Order) orderMap.get("order");
		
		//==================================================================
		// 积分余额支付订单.
		//==================================================================
//		pay4CreateOrder(userId, order, dataFrom, BigDecimal.ZERO, deliveryType, useCash, useIntegral, orderSku, smsTypeFlag, hasPayFlag, jsonMessage);
		
		//==================================================================
		// 中奖领取订单不进行返利.
		//==================================================================
		
		//==================================================================
		// 锁定库存.
		//==================================================================
//		lockInventory(hasPayFlag, skuCode, proNum, deliveryType, o2oShopNo, jsonMessage, (EfunUserOrder) map.get("efunOrder"), order);
		if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			return jsonMessage;
		}
		
		/**完全支付，e趣代销、自营公共、自营专卖 由商城发货，自动分配云店发货；成功则 增加仓库锁定存数，商品锁定库存数**/
		//设置根据发货规则进行分配发货的事件  所需的参数
//		if(hasPayFlag){
//			dataMap.put("addressId", addressId);
//			dataMap.put("orderIds", orderId);
//			dataMap.put("is_efun_order", "false");//不是幸运购一折购订单
//		}
		// 更新订单支付数据
//		order.update();
//		orderIdList.add(order.getStr("id"));
//		//添加订单详情运费规则描述
//		this.addOrderRuleDesc(order.getStr("id"));
		
		//==================================================================
		// 更新会员账户和生成对账单.
		//==================================================================
//		updateAccountAfterPay(userId, useCash, useIntegral, order.getStr("no"));
		
		//==================================================================
		// 云店自提库存不足时创建快递自提订单.
		//==================================================================
		// 成功下单的Sku数量.
//		int orderQuantity = (int) orderMap.get("orderQuantity");
//		if(deliveryType == Order.DELIVERY_TYPE_SELF && orderQuantity < proNum) {
//			createOrder(userId, (EfunUserOrder) map.get("efunOrder"), (List<String>) map.get("efunOrderDetailIdList"), BigDecimal.ZERO, true, orderSku, proNum - orderQuantity, Order.DELIVERY_TYPE_EXPRESS_SELF, addressId, o2oShopNo, contact, mobile, dataFrom, remark);
//		}
		
		//==================================================================
		// 提交事务.
		//==================================================================
		DbKit.getConfig().getConnection().commit();
		
		L.info("\r\t=========================== 立即领取 ==============================\r\t" + "会员Id: " + userId
//				+ ", 一折购订单Id: " + efunOrderId + ", 订单Id: " + order.getStr("id") + "\r\t"
				+ "==================================================================");
		
//		jsonMessage.setData(dataMap);
		
		// 发短信.
		if (STATUS_HAD_SEND == smsTypeFlag)
			EventKit.postEvent(new DeliverFinishSmsEvent(orderIdList));	// 通知商家发货
		else if (STATUS_WAIT_FOR_SEND == smsTypeFlag)
			EventKit.postEvent(new DeliverSmsEvent(orderIdList));		// 通知会员已发货
		
		return jsonMessage;
	}
	
	/**
	 * 提交订单.
	 * 
	 * @param prizeEfunOrderIdList
	 *            中奖领取的一折购订单Id列表.
	 * @param discountEfunOrderDetailIdList
	 *            购买的一折购订单详情Id列表.
	 * @author Chengyb
	 */
	public JsonMessage submitOrder(
			String userId,
			Integer useIntegral,
			BigDecimal useCash,
			int deliveryType,
			Integer addressId,
			String cloudStoreNo,
			String contact,
			String mobile,
			String payPassword,
			List<String> prizeEfunOrderIdList,
			List<String> discountEfunOrderDetailIdList,
			String dataFrom,
			String remark) throws SQLException {
		
		DbKit.getConfig().setThreadLocalConnection(DbKit.getConfig().getConnection());
        DbKit.getConfig().getThreadLocalConnection().setAutoCommit(false);
		
		JsonMessage jsonMessage = new JsonMessage();
															
		List<String> orderIdList = new ArrayList<String>();	// 订单ID集合
		// 是否已经完全支付(用于判断是否增加锁定库存数).
	    boolean hasPayFlag = false;
		// 发送短信标识【0:不发送,1:待发货,2:已发货】.
		int smsTypeFlag = 0;
		
		// ==================================================================
		// 验证收货地址.
		// ==================================================================
		if (null != addressId) {
			validationAddress(userId, addressId, jsonMessage);
			if (!jsonMessage.getStatus().equalsIgnoreCase("0")) {
				return jsonMessage;
			}
		}
		
		//==================================================================
		// 订单拆分.
		//==================================================================
		JsonMessage message = divisionOrder(userId, addressId, prizeEfunOrderIdList, discountEfunOrderDetailIdList, cloudStoreNo, jsonMessage);
		if(!message.getStatus().equalsIgnoreCase("0")) {
			return message;
		}
		
		// ==================================================================
		// 验证使用积分和余额.
		// ==================================================================
		Account account = validationIntegralAndCash(userId, useIntegral, useCash, jsonMessage);
		if (!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			DbKit.getConfig().getConnection().commit();
			return jsonMessage;
		}
		
		//==================================================================
		// 生成订单.
		//==================================================================
		// 商家商品来源. key: 商家Id, value: 商品来源.
		Map<String, Integer> sourceMap = new HashMap<String, Integer>();
		
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) message.getData();
		// 云店自提订单(无需付款).
		Map<String, Object> cloudStoreMap = (Map<String, Object>) data.get("cloudStore");
//		createOrder(userId, cloudStoreMap, dataFrom, remark);
		
		// 快递订单.
		Map<String, Object> expressMap = (Map<String, Object>) data.get("express");
//		createOrder(userId, cloudStoreMap, dataFrom, remark);
		
		// 快递自提订单.
		Map<String, Object> cloudStoreExpressMap = (Map<String, Object>) data.get("cloudStoreExpress");
//		createOrder(userId, cloudStoreMap, sourceMap, dataFrom, remark);
		
//		Map<String, Object> orderMap = createOrder(userId, skuMap, prizeEfunOrderList, (List<String>) map.get("efunOrderDetailIdList"), BigDecimal.ZERO, true, orderSku, proNum, deliveryType, addressId, o2oShopNo, contact, mobile, dataFrom, remark);
//		Order order = (Order) orderMap.get("order");
		
		//==================================================================
		// 积分余额支付订单.
		//==================================================================
//		pay4CreateOrder(userId, order, dataFrom, BigDecimal.ZERO, deliveryType, useCash, useIntegral, orderSku, smsTypeFlag, hasPayFlag, jsonMessage);
		
		//==================================================================
		// 中奖领取订单不进行返利.
		//==================================================================
		
		//==================================================================
		// 锁定库存.
		//==================================================================
//		lockInventory(hasPayFlag, skuCode, proNum, deliveryType, o2oShopNo, jsonMessage, (EfunUserOrder) map.get("efunOrder"), order);
		if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			return jsonMessage;
		}
		
		/**完全支付，e趣代销、自营公共、自营专卖 由商城发货，自动分配云店发货；成功则 增加仓库锁定存数，商品锁定库存数**/
		//设置根据发货规则进行分配发货的事件  所需的参数
//		if(hasPayFlag){
//			dataMap.put("addressId", addressId);
//			dataMap.put("orderIds", orderId);
//			dataMap.put("is_efun_order", "false");//不是幸运购一折购订单
//		}
		// 更新订单支付数据
//		order.update();
//		orderIdList.add(order.getStr("id"));
//		//添加订单详情运费规则描述
//		this.addOrderRuleDesc(order.getStr("id"));
		
		//==================================================================
		// 更新会员账户和生成对账单.
		//==================================================================
//		updateAccountAfterPay(userId, useCash, useIntegral, order.getStr("no"));
		
		//==================================================================
		// 云店自提库存不足时创建快递自提订单.
		//==================================================================
		// 成功下单的Sku数量.
//		int orderQuantity = (int) orderMap.get("orderQuantity");
//		if(deliveryType == Order.DELIVERY_TYPE_SELF && orderQuantity < proNum) {
//			createOrder(userId, (EfunUserOrder) map.get("efunOrder"), (List<String>) map.get("efunOrderDetailIdList"), BigDecimal.ZERO, true, orderSku, proNum - orderQuantity, Order.DELIVERY_TYPE_EXPRESS_SELF, addressId, o2oShopNo, contact, mobile, dataFrom, remark);
//		}
		
		//==================================================================
		// 提交事务.
		//==================================================================
		DbKit.getConfig().getConnection().commit();
		
		L.info("\r\t=========================== 立即领取 ==============================\r\t" + "会员Id: " + userId
//				+ ", 一折购订单Id: " + efunOrderId + ", 订单Id: " + order.getStr("id") + "\r\t"
				+ "==================================================================");
		
//		jsonMessage.setData(dataMap);
		
		// 发短信.
		if (STATUS_HAD_SEND == smsTypeFlag)
			EventKit.postEvent(new DeliverFinishSmsEvent(orderIdList));	// 通知商家发货
		else if (STATUS_WAIT_FOR_SEND == smsTypeFlag)
			EventKit.postEvent(new DeliverSmsEvent(orderIdList));		// 通知会员已发货
		
		return jsonMessage;
	}

	/**
	 * 锁定库存.
	 * 
	 * @param hasPayFlag
	 * @param skuCode
	 * @param proNum
	 * @param deliveryType
	 * @param pickUpInfo
	 * @param jsonMessage
	 * @param efunOrder
	 * @param order
	 * @throws SQLException
	 */
	private void lockInventory(
			boolean hasPayFlag,
			String skuCode,
			Integer proNum,
			int deliveryType,
			String o2oShopNo,
			JsonMessage jsonMessage,
			EfunUserOrder efunOrder,
			Order order) throws SQLException {
		//======================自提：增加仓库锁定存数||快递：增加商品锁定库存数=============================//
		/**
		 * 1、立即购买订单
		 * 		a、如果是自提，则增加仓库锁定库存
		 * 		b、如果是选择快递，则判断增加商品锁定库存
		 * 2、九折购订单
		 * 		1）已经释放商品锁定库存
		 * 			a、如果是自提，则增加仓库锁定库存
		 * 			b、如果是选择快递，则判断增加商品锁定库存
		 * 		2）没有释放商品锁定库存
		 * 			如果是自提，则把商品锁定库存 转换为 仓库锁定库存
		 */
		//支付完成不用释放锁定库存；没有支付完不会增加锁定库存，所以也不用释放锁定库存。
		order.set("is_release_lock_count", Order.IS_RELEASE_LOCK_COUNT_Y);
		if(hasPayFlag){
			boolean addLockOk = true;
			//九折购订单
			if(efunOrder != null){
				if(efunOrder.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){//已经释放商品锁定库存
					if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
						addLockOk = ProductSku.dao.addStoreLockCount(o2oShopNo, skuCode, proNum);
					}else{//快递
						addLockOk = ProductSku.dao.addLockCount(skuCode, proNum);
					}
				}else{//没有释放商品锁定库存
					if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
						addLockOk = ProductSku.dao.transferLockCount(o2oShopNo, skuCode, proNum);
						if(addLockOk){
							efunOrder.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y);
						}
					}else{
						efunOrder.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y);
					}
					
					efunOrder.update();
				}
				
			//立即购买订单
			}else{
				//自提：增加仓库锁定存数
				if(deliveryType==Order.DELIVERY_TYPE_SELF){
					addLockOk = ProductSku.dao.addStoreLockCount(o2oShopNo, skuCode, proNum);
					
				//快递：增加商品锁定库存数
				}else{
					addLockOk = ProductSku.dao.addLockCount(skuCode, proNum);
				}
			}
			
			//是否增加锁定库存成功
			if(!addLockOk){
				//事务回滚
				DbKit.getConfig().getConnection().rollback();
				if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
					jsonMessage.setData(ProductSku.dao.getSkuInventoryMessageForPickUp(skuCode, o2oShopNo));
				}else{
					jsonMessage.setData(ProductSku.dao.getSkuInventoryMessage(skuCode));
				}
				jsonMessage.setStatusAndMsg("99", "库存不足");
			}
		}
		
		//======================自提：增加仓库锁定存数||快递：增加商品锁定库存数=============================//
	}

	/**
	 * 订单返利.
	 * 
	 * @param order
	 *            订单.
	 * @author Chengyb
	 */
	private void rebate(Order order) {
		//==================================================================
		// 中奖订单将t_order_detail表ev值设置为0,不进行返利.
		//==================================================================
		if(order.getBigDecimal("total").signum() == 1) {
			// 返利积分倍数.
			Integer integralMultiple = SysParam.dao.getIntByCode("integral_multiple");
			BigDecimal totalCash = order.getBigDecimal("total").subtract(order.getBigDecimal("integral_discount"));
			// 返利积分数(商品金额的现金支付部分/5).
			Integer evIntegral = ((totalCash.divide(new BigDecimal("5"))).setScale(0, BigDecimal.ROUND_DOWN)).intValue();
			evIntegral = evIntegral * integralMultiple;
			// 保存返利积分数额.
			order.set("return_integral", evIntegral);
		}
	}

	/**
	 * 验证一折购订单是否已经领取.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param prizeEfunOrderIdList
	 *            中奖的一折购订单Id列表.
	 * @param jsonMessage
	 * @author Chengyb
	 */
	private Map<String, Object> validationPrize(
			String userId,
			List<String> prizeEfunOrderIdList,
			JsonMessage jsonMessage,
			List<EfunUserOrder> prizeEfunOrderList,
			Map<String, Integer> skuMap,
			Map<String, String> sellerMap) throws SQLException {
		Map<String, Object> map = new HashMap<String, Object>();
		
		// 无效的一折购订单Id列表.
		List<String> invalidEfunOrderIdList = new ArrayList<String>();
		// 可领取的一折购订单详情Id.
		List<String> efunOrderDetailIdList = new ArrayList<String>();
		
		for (int i = 0, size = prizeEfunOrderIdList.size(); i < size; i++) {
			String efunOrderId = prizeEfunOrderIdList.get(i);
			List<Record> orderList = EfunOrderDetail.dao.findEfunOrderDetail(userId, efunOrderId, true);
			if(null == orderList || orderList.size() == 0) {
				invalidEfunOrderIdList.add(efunOrderId);
			} else {
				EfunUserOrder efunOrder = EfunUserOrder.dao.getEfunUserOrderForUpdate(efunOrderId);
				if (BaseConstants.YES == efunOrder.getInt("is_win_get")) {
					invalidEfunOrderIdList.add(efunOrderId);
				} else {
					prizeEfunOrderList.add(efunOrder);
					
					efunOrder.set("is_win_get", BaseConstants.YES);
					efunOrder.update();
				}
				
				//==================================================================
				// 一折购中奖的Sku对应的商家.
				//==================================================================
				if(!sellerMap.containsKey(efunOrder.getStr("sku_code"))) {
					sellerMap.put(efunOrder.getStr("sku_code"), efunOrder.getStr("merchant_id"));
				}
				
				//==================================================================
				// 一折购中奖的Sku对应的数量.
				//==================================================================
				if(skuMap.containsKey(efunOrder.getStr("sku_code"))) {
					skuMap.put(efunOrder.getStr("sku_code"), skuMap.get(efunOrder.getStr("sku_code")) + orderList.size());
				} else {
					skuMap.put(efunOrder.getStr("sku_code"), orderList.size());
				}
				
				//==================================================================
				// 一折购中奖的详情Id列表.
				//==================================================================
				for (int j = 0, sizej = orderList.size(); j < sizej; j++) {
					efunOrderDetailIdList.add(orderList.get(j).getStr("detail_id"));
				}
			}
		}
		
		//==================================================================
		// 存在非法的一折购订单Id.
		//==================================================================
		if(invalidEfunOrderIdList.size() > 0) {
			DbKit.getConfig().getConnection().rollback();
			jsonMessage.setStatusAndMsg("6", "无效的一折购中奖订单Id: " + StringUtils.join(invalidEfunOrderIdList, ","));
		}
		
		map.put("invalidEfunOrderIdList", invalidEfunOrderIdList);
		map.put("efunOrderDetailIdList", efunOrderDetailIdList);
		
		return map;
	}
	
	/**
	 * 验证收货地址.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param addressId
	 *            收货地址Id.
	 * @param jsonMessage
	 * @author Chengyb
	 */
	private void validationAddress(String userId, Integer addressId, JsonMessage jsonMessage) {
		if(!RecAddress.dao.isHadAddress(addressId, userId)) {
			jsonMessage.setStatusAndMsg("6", "无效的收货地址Id!");
		}
	}
	
	/**
	 * 库存验证.
	 * 
	 * @param prizeEfunOrderList
	 *            一折购中奖订单列表.
	 * @param skuMap
	 * @param deliveryType
	 * @param cloudStoreNo
	 *            云店编号.
	 * @param jsonMessage
	 * @param understockList
	 *            库存不足商品数据.
	 * @author Chengyb
	 */
	private void validationInventory(
			List<EfunUserOrder> prizeEfunOrderList,
			Map<String, Integer> skuMap,
			Integer deliveryType,
			String cloudStoreNo,
			JsonMessage jsonMessage,
			List<Map<String, Object>> understockList) {        
		for (int i = 0, size = prizeEfunOrderList.size(); i < size; i++) {
			EfunUserOrder efunOrder = prizeEfunOrderList.get(i);
			String skuCode = efunOrder.getStr("sku_code");
			Integer orderQuantity = skuMap.get(skuCode);
			
			if (efunOrder.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y) { // 已经释放商品锁定库存.
				// a、如果是自提，则判断云店实际显示库存 （云店库存 - 云店锁定库存）
				// b、如果是选择快递，则判断 商品显示库存（sku实际库存 + 虚拟库存 - 锁定库存 - 仓库锁定库存）
				if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 自提.
					// 云店可用库存.
					Integer inventory = StoreSkuMap.dao.getStoreRealCount(skuCode, cloudStoreNo);
					if(inventory.intValue() < orderQuantity.intValue()) {
						// 云店库存不足时,可以使用快递自提方式创建订单.
						if (!ProductSku.dao.enoughCount(skuCode, orderQuantity - inventory)) {
//							addUnderStock(efunOrder, inventory + ProductSku.dao.getCount(skuCode), understockList);
						}
					}
				} else if (deliveryType == Order.DELIVERY_TYPE_EXPRESS) { // 快递.
					if (!ProductSku.dao.enoughCount(skuCode, orderQuantity)) {
//						addUnderStock(efunOrder, ProductSku.dao.getCount(skuCode), understockList);
					}
				} else if(deliveryType == Order.DELIVERY_TYPE_EXPRESS_SELF) { // 快递自提.
					if (!ProductSku.dao.enoughCount(skuCode, orderQuantity)) {
//						jsonMessage.setStatusAndMsg("-1", msg);
					}
				}
			} else { // 没有释放商品锁定库存.
				// 参与时,锁定的是商品锁定库存,当订单类型为云店自提时,需要判断云店库存是否充足.
				if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 自提.
					if (!StoreSkuMap.dao.enoughStoreCount(cloudStoreNo, skuCode, orderQuantity)) {
						
					}
				}
			}
		}
	}
	
	/**
	 * 添加商品库存不足信息.
	 * 
	 * @param productName
	 *            商品名称.
	 * @param productProperty
	 *            销售属性.
	 * @param skuStock
	 *            实际库存.
	 * @param understockList
	 * 
	 * @author Chengyb
	 */
	private void addUnderStock(
			String productName,
			String productProperty,
			int skuStock,
			List<Map<String, Object>> understockList) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("productName", productName); // 商品名.
		map.put("salesProperty", productProperty); // 销售属性.
		map.put("skuStock", skuStock); // 库存数量.
		understockList.add(map);
	}
	
	/**
	 * 添加商品店铺信息.
	 * 
	 * @param efunOrder
	 * @param skuStock
	 * @param understockList
	 * 
	 * @author Chengyb
	 */
	private void addStoreFrozen(EfunUserOrder efunOrder, int skuStock,List<Map<String, Object>> understockList) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("productName", efunOrder.getStr("product_name")); // 商品名.
		map.put("salesProperty", efunOrder.getStr("product_property")); // 销售属性.
		map.put("skuStock", skuStock); // 库存数量.
		understockList.add(map);
	}

	/**
	 * 验证会员使用的积分和余额.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param useIntegral
	 *            使用积分.
	 * @param useCash
	 *            使用余额.
	 * @param jsonMessage
	 * @author Chengyb
	 * @return
	 */
	private Account validationIntegralAndCash(
			String userId,
			Integer useIntegral,
			BigDecimal useCash,
			JsonMessage jsonMessage) {
		// 当使用了账户积分或现金时需要判断支付密码是否正确(WebOrderValidator.java已验证).
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		
		Integer integral = account.getInt("integral");
		BigDecimal cash = account.getBigDecimal("cash");
		// 判断使用积分是否小于0.
		if (useIntegral < 0) {
			jsonMessage.setStatusAndMsg("300", "使用积分不能小于0！");
		}
		// 判断使用现金是否小于0.
		if (useCash.signum() == -1) {
			jsonMessage.setStatusAndMsg("400", "使用现金不能小于0！");
		}
		// 判断使用积分是否大于会员账户积分余额.
		if (useIntegral > integral) {
			jsonMessage.setStatusAndMsg("100", "使用积分不能大于账户剩余积分！");
		}
		// 判断使用现金是否大于会员账户现金余额.
		if (useCash.compareTo(cash) == 1) {
			jsonMessage.setStatusAndMsg("200", "使用现金不能大于账户剩余现金！");
		}
		return account;
	}
	
	/**
	 * 拆分订单.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param addressId
	 *            快递收货地址Id.
	 * @param prizeEfunOrderIdList
	 *            中奖领取订单Id.
	 * @param discountEfunOrderDetailIdList
	 *            折扣订单详情Id.
	 * @param cloudStoreNo
	 *            自提云店编号.
	 * @author Chengyb
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JsonMessage divisionOrder(
			String userId,
			Integer addressId,
			List<String> prizeEfunOrderIdList,
			List<String> discountEfunOrderDetailIdList,
			String cloudStoreNo,
			JsonMessage jsonMessage) {
		// ==================================================================
		// Sku数据. key-sku编码;value-下单数量.
		// ==================================================================
		Map<String, Integer> skuMap = new HashMap<String, Integer>();
		
		// ==================================================================
		// Sku中奖数据. key-sku编码;value-中奖数量.
		// ==================================================================
		Map<String, Integer> skuFreeMap = new HashMap<String, Integer>();
		
		// ==================================================================
		// Sku折扣数据. key-sku编码;value-折扣数据.
		// ==================================================================
		Map<String, List<BigDecimal>> skuDiscountMap = new HashMap<String, List<BigDecimal>>();

		// ==================================================================
		// Sku商家数据. key-sku编码;value-店铺/供货商信息.
		// ==================================================================
		Map<String, Map<String, Object>> sellerDataMap = new HashMap<String, Map<String, Object>>();
		
		// ==================================================================
		// Sku相关属性数据. key-sku编码;value-sku相关属性数据.
		// ==================================================================
		Map<String, Map<String, Object>> skuDataMap = new HashMap<String, Map<String, Object>>();
		
		// ==================================================================
		// 订单验证.
		// ==================================================================
		validation4Order(userId, prizeEfunOrderIdList, discountEfunOrderDetailIdList, cloudStoreNo, jsonMessage, skuMap, skuFreeMap, skuDiscountMap, sellerDataMap, skuDataMap);
		
		if(!jsonMessage.getStatus().equalsIgnoreCase("0")) {
			return jsonMessage;
		}
		
		// ==================================================================
		// 云店自提订单.
		// ==================================================================
		Map<String, Object> cloudStoreOrderMap = new HashMap<String, Object>();

		// ==================================================================
		// 快递配送订单.
		// ==================================================================
		Map<String, Object> expressOrderMap = new HashMap<String, Object>();

		// ==================================================================
		// 快递自提订单.
		// ==================================================================
		Map<String, Object> cloudStoreExpressOrderMap = new HashMap<String, Object>();
		
		//==================================================================
		// 按照店铺/供货商拆分订单.
		//==================================================================
		if(null != addressId || (null == addressId && StringUtil.isBlank(cloudStoreNo))) { // 默认使用会员收货地址,快递运输.直接拆单.
			Iterator<Entry<String, Integer>> iterator = skuMap.entrySet().iterator();
			Map<String, Object> sellerProductMap = null;
			List<Map<String, Object>> skuList = null;
			while (iterator.hasNext()) {
				addProductSku2OrderMap(skuDataMap, sellerDataMap, iterator.next(), expressOrderMap, sellerProductMap, skuList);
			}
		} else if(!StringUtil.isBlank(cloudStoreNo)) { // 使用云店作为收货地址,云店自提.云店库存不足时,使用快递自提.
			Iterator<Entry<String, Integer>> iterator = skuMap.entrySet().iterator();
			Map<String, Object> sellerProductMap = null;
			List<Map<String, Object>> skuList = null;
			while (iterator.hasNext()) {
				Entry<String, Integer> entry = iterator.next();
				
				String skuCode = entry.getKey(); // Sku编码.
				Integer orderQuantity = entry.getValue(); // 下单数量.
//				String sellerId = (String) sellerDataMap.get(skuCode).get("merchantId"); // 店铺/供货商Id.
				
				// 判断Sku是否为云店商品.
				if(ProductSku.dao.isO2o(skuCode)) {
					// 云店可用库存.
					Integer inventory = StoreSkuMap.dao.getStoreRealCount(skuCode, cloudStoreNo);
					if(inventory.intValue() < orderQuantity.intValue()) {
						if(inventory.intValue() > 0) {
							// 先创建云店自提订单.
							entry.setValue(inventory);
							addProductSku2OrderMap(skuDataMap, sellerDataMap, entry, cloudStoreOrderMap, sellerProductMap, skuList);
//							// 云店库存不足时,可以使用快递自提方式创建订单.
//							if (!ProductSku.dao.enoughCount(skuCode, orderQuantity - inventory)) {
//								addUnderStock((String) skuDataMap.get(skuCode).get("productName"),
//										(String) skuDataMap.get(skuCode).get("skuProperty"),
//										inventory + ProductSku.dao.getCount(skuCode), understockList);
//							} else {
								// 创建快递自提订单.
								entry.setValue(orderQuantity - inventory);
								addProductSku2OrderMap(skuDataMap, sellerDataMap, entry, cloudStoreExpressOrderMap, sellerProductMap, skuList);
//							}
						} else {
							addProductSku2OrderMap(skuDataMap, sellerDataMap, entry, cloudStoreExpressOrderMap, sellerProductMap, skuList);
						}
					} else {
						addProductSku2OrderMap(skuDataMap, sellerDataMap, entry, cloudStoreOrderMap, sellerProductMap, skuList);
					}
				} else {
					addProductSku2OrderMap(skuDataMap, sellerDataMap, entry, cloudStoreExpressOrderMap, sellerProductMap, skuList);
				}
			}
		}
		
		//==================================================================
	    // 计算多个订单的商品总金额及运费.
		//==================================================================
		calculationOrdersCost(addressId, cloudStoreNo, jsonMessage, skuFreeMap, skuDiscountMap, skuDataMap, cloudStoreOrderMap,
				expressOrderMap, cloudStoreExpressOrderMap);
		
		// ===================================================================
		// 订单Map转为List供App使用.
		// ===================================================================
		transformOrderMap2List("cloudStore", jsonMessage);
		transformOrderMap2List("express", jsonMessage);
		transformOrderMap2List("cloudStoreExpress", jsonMessage);
		
		// ===================================================================
		// 用户积分/余额信息.
		// ===================================================================
		Integer integral = Account.dao.getUserIntegralBalance(userId);
		BigDecimal cash = Account.dao.getUserCash(userId);
		// 订单商品的总金额.
		BigDecimal totalAmount = (BigDecimal) ((Map<String, Object>) jsonMessage.getData()).get("totalAmount");
		// 订单商品的总运费.
		BigDecimal totalFreight = (BigDecimal) ((Map<String, Object>) jsonMessage.getData()).get("totalFreight");
		// 积分：1.不可以抵扣运费；2.只能抵扣商品金额的一半. 1元=100积分.
		BigDecimal canUseIntegral = totalAmount.multiply(new BigDecimal("50"));
		((Map<String, Object>) jsonMessage.getData()).put("integral", integral); // 会员积分余额.
		((Map<String, Object>) jsonMessage.getData()).put("canUseIntegral", integral.intValue() >= canUseIntegral.intValue() ? canUseIntegral.intValue() : integral.intValue()); // 订单可用积分.
		((Map<String, Object>) jsonMessage.getData()).put("cash", cash); // 会员现金余额.
		((Map<String, Object>) jsonMessage.getData()).put("canUseCash", cash.compareTo(totalAmount.add(totalFreight)) == -1 ? cash : totalAmount.add(totalFreight)); // 订单可用.
		
		return jsonMessage;
	}

	/**
	 * 订单Map转为List供App使用.
	 * 
	 * @param jsonMessage
	 * 
	 * @author Chengyb
	 */
	@SuppressWarnings("unchecked")
	private void transformOrderMap2List(String orderTypeKey, JsonMessage jsonMessage) {
		// 云店自提订单.
		List<Map<String, Object>> orderList = new ArrayList<Map<String, Object>>();
		Map<String, Object> orderMap = (Map<String, Object>) ((Map<String, Object>) jsonMessage.getData()).get(orderTypeKey);
		if(null != orderMap) {
			Iterator<Entry<String, Object>> iterator = orderMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				
				String key = entry.getKey();
				
				if(!key.equalsIgnoreCase("amount") && !key.equalsIgnoreCase("freight")) {
					Map<String, Object> map = (Map<String, Object>) entry.getValue();
					map.put("merchantId", key);
					orderList.add(map);
				}
			}
			
			((Map<String, Object>) jsonMessage.getData()).remove("cloudStore");
			((Map<String, Object>) jsonMessage.getData()).put(orderTypeKey, orderList);
		}
	}
	

	/**
	 * 计算批量订单的费用(商品金额+运费).
	 * 
	 * @param addressId
	 * @param cloudStoreNo
	 * @param jsonMessage
	 * @param skuFreeMap
	 * @param skuDiscountMap
	 * @param skuDataMap
	 * @param cloudStoreOrderMap
	 * @param expressOrderMap
	 * @param cloudStoreExpressOrderMap
	 * 
	 * @author Chengyb
	 */
	private void calculationOrdersCost(
			Integer addressId,
			String cloudStoreNo,
			JsonMessage jsonMessage,
			Map<String, Integer> skuFreeMap,
			Map<String, List<BigDecimal>> skuDiscountMap,
			Map<String, Map<String, Object>> skuDataMap,
			Map<String, Object> cloudStoreOrderMap,
			Map<String, Object> expressOrderMap,
			Map<String, Object> cloudStoreExpressOrderMap) {
		RecAddress address = RecAddress.dao.findByIdLoadColumns(addressId, "province_code, city_code, area_code");
		
		if(!StringUtil.isBlank(cloudStoreNo) && cloudStoreOrderMap.size() > 0) {
			Record store = Store.dao.findAddressNameByNo(cloudStoreNo);
			calculationAmount2OrderMap(cloudStoreOrderMap, skuFreeMap, skuDiscountMap, skuDataMap, store.getInt("province_code"), store.getInt("city_code"), store.getInt("area_code"));
		} else if(null != addressId && expressOrderMap.size() > 0) {
			calculationAmount2OrderMap(expressOrderMap, skuFreeMap, skuDiscountMap, skuDataMap, address.getInt("province_code"), address.getInt("city_code"), address.getInt("area_code"));
		} else if(cloudStoreExpressOrderMap.size() > 0) {
			Record store = Store.dao.findAddressNameByNo(cloudStoreNo);
			calculationAmount2OrderMap(cloudStoreExpressOrderMap, skuFreeMap, skuDiscountMap, skuDataMap, store.getInt("province_code"), store.getInt("city_code"), store.getInt("area_code"));
		} else if(null == addressId && StringUtil.isBlank(cloudStoreNo)) {
			calculationAmount2OrderMap(expressOrderMap, skuFreeMap, skuDiscountMap, skuDataMap, null, null, null);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("cloudStore", cloudStoreOrderMap);
		map.put("express", expressOrderMap);
		map.put("cloudStoreExpress", cloudStoreExpressOrderMap);
		
		// 分别计算三种订单的商品总金额和总运费金额.
		calculation2OrderType(expressOrderMap);
		calculation2OrderType(cloudStoreOrderMap);
		calculation2OrderType(cloudStoreExpressOrderMap);
		
		// 所有订单商品总金额和总运费金额.
		BigDecimal amount = BigDecimal.ZERO, freight = BigDecimal.ZERO;
		Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			Map<String, Object> typeMap = (Map<String, Object>) entry.getValue();
			if(typeMap.size() > 0) {
				amount = amount.add((BigDecimal) ((Map<String, Object>) entry.getValue()).get("amount"));
				freight = freight.add((BigDecimal) ((Map<String, Object>) entry.getValue()).get("freight"));
			}
		}
		map.put("totalAmount", amount);
		map.put("totalFreight", freight);
		
		jsonMessage.setData(map);
	}

	/**
	 * 订单验证.
	 * 
	 * @param userId
	 * @param prizeEfunOrderIdList
	 * @param discountEfunOrderDetailIdList
	 * @param cloudStoreNo
	 * @param jsonMessage
	 * @param skuMap
	 * @param skuFreeMap
	 * @param skuDiscountMap
	 * @param sellerDataMap
	 * @param skuDataMap
	 * 
	 * @author Chengyb
	 */
	private JsonMessage validation4Order(
			String userId,
			List<String> prizeEfunOrderIdList,
			List<String> discountEfunOrderDetailIdList,
			String cloudStoreNo,
			JsonMessage jsonMessage,
			Map<String, Integer> skuMap,
			Map<String, Integer> skuFreeMap,
			Map<String, List<BigDecimal>> skuDiscountMap,
			Map<String, Map<String, Object>> sellerDataMap,
			Map<String, Map<String, Object>> skuDataMap) {
		// ==================================================================
		// 库存不足商品数据.
		// ==================================================================
		List<Map<String, Object>> understockList = new ArrayList<Map<String, Object>>();

		// ==================================================================
		// 店铺冻结商品数据.
		// ==================================================================
		List<Map<String, Object>> shopFrozenList = new ArrayList<Map<String, Object>>();

		// ==================================================================
		// 商品下架删除数据.
		// ==================================================================
		List<Map<String, Object>> unableBuyList = new ArrayList<Map<String, Object>>();
				
		// 无效的一折购订单Id列表.
		List<String> invalidEfunOrderIdList = new ArrayList<String>();
		// 无效的一折购折扣购买详情Id列表.
		List<String> timeoutEfunOrderDetailIdList = new ArrayList<String>();
		// 超时的一折购订单详情Id列表.
		List<String> invalidEfunOrderDteailIdList = new ArrayList<String>();
		// 可领取的一折购订单详情Id.
		List<String> efunOrderDetailIdList = new ArrayList<String>();
		
		//==================================================================
		// 验证一折购中奖领取Id列表.
		//==================================================================
		validatePrizeEfunOrderIdList(userId, prizeEfunOrderIdList, skuMap, skuFreeMap, sellerDataMap,
				skuDataMap, invalidEfunOrderIdList, efunOrderDetailIdList);

		// ==================================================================
		// 验证一折购折扣购买Id列表.
		// ==================================================================
		validateDiscountEfunOrderDetailIdList(userId, discountEfunOrderDetailIdList, skuMap, skuDiscountMap,
				sellerDataMap, skuDataMap, invalidEfunOrderDteailIdList, timeoutEfunOrderDetailIdList,
				efunOrderDetailIdList);
		
		//==================================================================
		// 存在非法的一折购订单Id.
		//==================================================================
		if(invalidEfunOrderIdList.size() > 0) {
			jsonMessage.setStatusAndMsg("1", "无效的一折购中奖订单Id: " + StringUtils.join(invalidEfunOrderIdList, ","));
			return jsonMessage;
		}
		
		//==================================================================
		// 存在非法的一折购订单Id.
		//==================================================================
		if(invalidEfunOrderDteailIdList.size() > 0) {
			jsonMessage.setStatusAndMsg("2", "无效的一折购购买订单Id: " + StringUtils.join(invalidEfunOrderDteailIdList, ","));
			return jsonMessage;
		}
		
		//==================================================================
		// 存在超时的一折购订单详情Id.
		//==================================================================
		if(timeoutEfunOrderDetailIdList.size() > 0) {
			jsonMessage.setStatusAndMsg("3", "您部分折扣商品已超过24小时的购买时限，请重新领取/购买！！！");
			return jsonMessage;
		}
		
		// ==================================================================
		// 验证Sku对应的店铺状态和商品状态.
		// ==================================================================
		validationSkuStatus(skuMap, skuDataMap, shopFrozenList, unableBuyList);
		if(shopFrozenList.size() > 0) {
			jsonMessage.setStatusAndMsg("4", "您的部分商品所属的店铺已冻结，请重新领取/购买！！！");
			return jsonMessage;
		}
		if(unableBuyList.size() > 0) {
			jsonMessage.setStatusAndMsg("5", "您的部分商品已下架，请重新领取/购买！！！");
			return jsonMessage;
		}
		
		//==================================================================
		// 验证库存.
		//==================================================================
		// 可领取的一折购订单列表.
		List<EfunUserOrder> prizeEfunOrderList = new ArrayList<EfunUserOrder>();
		if(StringUtil.isBlank(cloudStoreNo)) { // 默认使用会员收货地址,快递运输.
			validationInventory(prizeEfunOrderList, skuMap, Order.DELIVERY_TYPE_EXPRESS, cloudStoreNo, jsonMessage, understockList);
		} else {
			validationInventory(prizeEfunOrderList, skuMap, Order.DELIVERY_TYPE_SELF, cloudStoreNo, jsonMessage, understockList);
		}
		
//		//==================================================================
//		// 存在库存不足的商品.
//		//==================================================================
//		if(understockList.size() > 0) {
			return jsonMessage;
//		}
	}
	
	/**
	 * 验证Sku的购买状态.
	 * 
	 * @author Chengyb
	 */
	private void validationSkuStatus(
			Map<String, Integer> skuMap,
			Map<String, Map<String, Object>> skuDataMap,
			List<Map<String, Object>> shopFrozenList,
			List<Map<String, Object>> unableBuyList) {
		Iterator<Entry<String, Integer>> iterator = skuMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			
			String skuCode = entry.getKey(); // Sku编码.
			Record record = ProductSku.dao.getProductStatusAndShopStatus(skuCode);
			// 商品状态.状态（0：下架，1：上架，2：已删除）
			int productStatus = record.getInt("status");
			if(productStatus != 1) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("productName", skuDataMap.get(skuCode).get("productName")); // 商品名.
				map.put("salesProperty", skuDataMap.get(skuCode).get("skuProperty")); // 销售属性.
				unableBuyList.add(map);
			}
			
			String shopId = record.getStr("shop_id");
			if(!StringUtil.isBlank(shopId)) {
				// 店铺状态及禁用状态.
				// 状态（0：未转出，1：转出待审核，2：待激活，3：激活待审核，4：已激活,5:转出失败，6：转出已退款）
				// 禁用状态（0:正常，1：未续费禁用，2：违规禁用）
				int shopStatus = record.getInt("shop_status");
				int shopForbiddenStatus = record.getInt("shop_forbidden_status");
				if(shopStatus != 4 || shopForbiddenStatus != 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("productName", skuDataMap.get(skuCode).get("productName")); // 商品名.
					map.put("salesProperty", skuDataMap.get(skuCode).get("skuProperty")); // 销售属性.
					shopFrozenList.add(map);
				}
			}
		}
	}
	
	/**
	 * 计算订单Map的金额和运费.
	 * 
	 * @param orderMap
	 *            订单Map.
	 * @param skuFreeMap
	 *            Sku中奖数据.
	 * @param skuDiscountMap
	 *            Sku折扣数据.
	 * @param skuDataMap
	 *            Sku相关属性数据.
	 * @param provinceCode
	 *            收货省编码.
	 * @param cityCode
	 *            收货市编码.
	 * @param areaCode
	 *            收货区编码.
	 * @param totalAmount
	 *            商品总金额.
	 * @param totalFreight
	 *            商品总运费.
	 * 
	 * @author Chengyb
	 */
	private void calculationAmount2OrderMap(
			Map<String, Object> orderMap,
			Map<String, Integer> skuFreeMap,
			Map<String, List<BigDecimal>> skuDiscountMap,
			Map<String, Map<String, Object>> skuDataMap,
			Integer provinceCode,
			Integer cityCode,
			Integer areaCode) {
		Iterator<Entry<String, Object>> iterator = orderMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			
			Map<String, Integer> productMap = new HashMap<String, Integer>();
			
			String sellerId = entry.getKey(); // 店铺/供货商Id.
			Map<String, Object> buyData = (Map<String, Object>) entry.getValue(); // 购物数据.
			
			// 当前商家订单总金额.
			BigDecimal amount = BigDecimal.ZERO;
			
			List<Map<String, Object>> list = (List<Map<String, Object>>) buyData.get("productList"); // 商品列表.List<Map<String, Object>>
			List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
			for (int i = 0, size = list.size(); i < size; i++) {
				Map<String, Object> sku = list.get(i);
				
				String skuCode = (String) sku.get("skuCode");
				Integer quantity = (Integer) sku.get("quantity"); // 数量.
				
				sku.remove("quantity");
				
				// 折扣排序.
				if(null != skuDiscountMap.get(skuCode)) {
					Collections.sort(skuDiscountMap.get(skuCode));
				}
				
				for (int j = 0; j < quantity; j++) {
					Map<String, Object> skuClone = (Map<String, Object>) ((HashMap) sku).clone();
					
					if(skuFreeMap.containsKey(skuCode) && j + 1 <= skuFreeMap.get(skuCode)) {
						skuClone.put("status", 1); // 幸运中奖.
					} else {
						skuClone.put("status", 0); // 折扣中奖
						
						BigDecimal eqPrice = (BigDecimal) skuDataMap.get(skuCode).get("eqPrice");
						// 始终取链表的第一条记录.
						BigDecimal skuDiscount = skuDiscountMap.get(skuCode).get(0);
						skuClone.put("discount", skuDiscount);
						amount = amount.add(eqPrice.multiply(skuDiscount));
						skuDiscountMap.get(skuCode).remove(0);
					}
					
					newList.add(skuClone);
				}
				
				((Map<String, Object>) orderMap.get(sellerId)).put("productList", newList); // 商家订单的商品列表.
				((Map<String, Object>) orderMap.get(sellerId)).put("amount", amount); // 商家订单的总金额.
				productMap.put(skuCode, quantity);
			}
			// 运费合计.
			if(null == provinceCode && null == cityCode && null == areaCode) {
				((Map<String, Object>) orderMap.get(sellerId)).put("freight", BigDecimal.ZERO);
			} else {
				BigDecimal freight = FreightTemplate.dao.calculate(productMap, provinceCode, cityCode, areaCode);
				((Map<String, Object>) orderMap.get(sellerId)).put("freight", freight); // 运费合计.
			}
		}
	}
	
	/**
	 * 计算订单类型的商品总额和运费总额.
	 * 
	 * @author Chengyb
	 */
	@SuppressWarnings("unchecked")
	private void calculation2OrderType(Map<String, Object> orderMap) {
		BigDecimal amount = BigDecimal.ZERO, freight = BigDecimal.ZERO;
		Iterator<Entry<String, Object>> iterator = orderMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			amount = amount.add((BigDecimal) ((Map<String, Object>) entry.getValue()).get("amount"));
			freight = freight.add((BigDecimal) ((Map<String, Object>) entry.getValue()).get("freight"));
		}
		if(orderMap.size() > 0) {
			orderMap.put("amount", amount);
			orderMap.put("freight", freight);
		}
	}

	/**
	 * 把商品Sku加入对应的订单Map.
	 * 
	 * @param sellerDataMap
	 * @param entry
	 * @param orderMap
	 * 
	 * @author Chengyb
	 */
	private void addProductSku2OrderMap(
			Map<String, Map<String, Object>> skuDataMap,
			Map<String, Map<String, Object>> sellerDataMap,
			Entry<String, Integer> entry,
			Map<String, Object> orderMap,
			Map<String, Object> sellerProductMap,
			List<Map<String, Object>> skuList) {
		String skuCode = entry.getKey(); // Sku编码.
		Integer orderQuantity = entry.getValue(); // 下单数量.
		String sellerId = (String) sellerDataMap.get(skuCode).get("merchantId"); // 店铺/供货商Id.
		
		if(orderMap.containsKey(sellerId)) {
			sellerProductMap = (Map<String, Object>) orderMap.get(sellerId);
			skuList = (List<Map<String, Object>>) sellerProductMap.get("productList"); // 商品列表.List<Map<String, Object>>
		} else {
			sellerProductMap = new HashMap<String, Object>();
			skuList = new ArrayList<Map<String, Object>>();
		}
		
		Map<String, Object> sku = new HashMap<String, Object>();
		sku.put("productId",  skuDataMap.get(skuCode).get("productId")); // 商品Id.
		sku.put("skuCode", skuDataMap.get(skuCode).get("skuCode")); // 商品Sku.
		sku.put("productName",  skuDataMap.get(skuCode).get("productName")); // 商品名称.
		sku.put("productImage",  skuDataMap.get(skuCode).get("productImage")); // 商品名称.
		sku.put("skuProperty", skuDataMap.get(skuCode).get("skuProperty")); // 销售属性.
		sku.put("eqPrice", skuDataMap.get(skuCode).get("eqPrice")); // 一折购价格.
		sku.put("quantity", orderQuantity); // 数量.
		
		skuList.add(sku);
		
		sellerProductMap.put("merchantName", sellerDataMap.get(skuCode).get("merchantName"));
		sellerProductMap.put("productList", skuList);
		
		orderMap.put(sellerId, sellerProductMap);
	}

	/**
	 * 验证一折购中奖领取订单Id列表.
	 * 
	 * @param userId
	 * @param prizeEfunOrderIdList
	 * @param skuMap
	 * @param skuFreeMap
	 * @param sellerDataMap
	 * @param skuDataMap
	 * @param invalidEfunOrderIdList
	 * @param efunOrderDetailIdList
	 * 
	 * @author Chengyb
	 */
	private void validatePrizeEfunOrderIdList(
			String userId,
			List<String> prizeEfunOrderIdList,
			Map<String, Integer> skuMap,
			Map<String, Integer> skuFreeMap,
			Map<String, Map<String, Object>> sellerDataMap,
			Map<String, Map<String, Object>> skuDataMap,
			List<String> invalidEfunOrderIdList,
			List<String> efunOrderDetailIdList) {
		if(null != prizeEfunOrderIdList) {
			for (int i = 0, size = prizeEfunOrderIdList.size(); i < size; i++) {
				String efunOrderId = prizeEfunOrderIdList.get(i);
				List<Record> orderList = EfunOrderDetail.dao.findEfunOrderDetail(userId, efunOrderId, true);
				if(null == orderList || orderList.size() == 0) {
					invalidEfunOrderIdList.add(efunOrderId);
				} else {
					//==================================================================
					// 一折购中奖的Sku对应的商家.
					//==================================================================
					EfunUserOrder efunOrder = EfunUserOrder.dao.findById(efunOrderId);
					if (BaseConstants.YES == efunOrder.getInt("is_win_get")) {
						invalidEfunOrderIdList.add(efunOrderId);
					}
					
					//==================================================================
					// Sku对应的销售属性.
					//==================================================================
					if(!skuDataMap.containsKey(efunOrder.getStr("sku_code"))) {
						Map<String, Object> sku = new HashMap<String, Object>();
						sku.put("productId", efunOrder.getInt("product_id")); // 商品Id.
						sku.put("skuCode", efunOrder.getStr("sku_code")); // 商品Sku.
						sku.put("productName", efunOrder.getStr("product_name")); // 商品名称.
						sku.put("productImage", efunOrder.getStr("product_img")); // 商品图片.
						sku.put("skuProperty", efunOrder.getStr("product_property")); // 销售属性.
						sku.put("eqPrice", efunOrder.getBigDecimal("eq_price")); // 一折购价格.
						
						skuDataMap.put(efunOrder.getStr("sku_code"), sku);
					}
					
					//==================================================================
					// Sku对应的商家信息.
					//==================================================================
					if(!sellerDataMap.containsKey(efunOrder.getStr("sku_code"))) {
						Map<String, Object> seller = new HashMap<String, Object>();
						seller.put("merchantId", efunOrder.getStr("merchant_id")); // 店铺/供货商Id.
						seller.put("merchantName", efunOrder.getStr("merchant_name")); // 店铺/供货商名称.
						
						sellerDataMap.put(efunOrder.getStr("sku_code"), seller);
					}
					
					//==================================================================
					// 一折购订单的Sku对应的数量.
					//==================================================================
					if(skuMap.containsKey(efunOrder.getStr("sku_code"))) {
						skuMap.put(efunOrder.getStr("sku_code"), skuMap.get(efunOrder.getStr("sku_code")) + orderList.size());
					} else {
						skuMap.put(efunOrder.getStr("sku_code"), orderList.size());
					}
					
					//==================================================================
					// 一折购中奖的详情Id列表.
					//==================================================================
					for (int j = 0, sizej = orderList.size(); j < sizej; j++) {
						//==================================================================
						// 一折购订单中奖的Sku对应的数量.
						//==================================================================
						if(orderList.get(j).getInt("number") == orderList.get(j).getInt("win_number")) { // 中奖.
							if(skuFreeMap.containsKey(efunOrder.getStr("sku_code"))) {
								skuFreeMap.put(efunOrder.getStr("sku_code"), skuFreeMap.get(efunOrder.getStr("sku_code")) + 1);
							} else {
								skuFreeMap.put(efunOrder.getStr("sku_code"), 1);
							}
						}
						
//						//==================================================================
//						// 验证一折购详情翻牌超时.
//						// 购买折扣商品时,在订单确认页（本页面时限额外加五分钟）超时未确定.
//						//==================================================================
//						Date beginValidTime = orderList.get(j).getDate("begin_valid_time");
//						if(null != beginValidTime) {
//							Date timeOut = DateUtil.addMinute(beginValidTime, 1445); // 24小时 + 5分钟
//							if(new Date().after(timeOut)) {
//								timeoutEfunOrderDetailIdList.add(orderList.get(j).getStr("detail_id"));
//							} else {
//								efunOrderDetailIdList.add(orderList.get(j).getStr("detail_id"));
//							}
//						} else {
//							efunOrderDetailIdList.add(orderList.get(j).getStr("detail_id"));
//						}
					}
				}
			}
		}
	}
	
	/**
	 * 验证折扣购物Id列表.
	 * 
	 * @param userId
	 * @param prizeEfunOrderIdList
	 * @param skuMap
	 * @param skuDiscountMap
	 * @param sellerDataMap
	 * @param skuDataMap
	 * @param invalidEfunOrderDteailIdList
	 * @param timeoutEfunOrderDetailIdList
	 * @param efunOrderDetailIdList
	 */
	private void validateDiscountEfunOrderDetailIdList(
			String userId,
			List<String> discountEfunOrderDetailId,
			Map<String, Integer> skuMap,
			Map<String, List<BigDecimal>> skuDiscountMap,
			Map<String, Map<String, Object>> sellerDataMap,
			Map<String, Map<String, Object>> skuDataMap,
			List<String> invalidEfunOrderDteailIdList,
			List<String> timeoutEfunOrderDetailIdList,
			List<String> efunOrderDetailIdList) {
		if(null != discountEfunOrderDetailId) {
			for (int i = 0, size = discountEfunOrderDetailId.size(); i < size; i++) {
				String efunOrderDetailId = discountEfunOrderDetailId.get(i);
				Record efunOrderDetail = EfunOrderDetail.dao.findDiscountEfunOrderDetail(userId, efunOrderDetailId);
				if(null == efunOrderDetail) {
					invalidEfunOrderDteailIdList.add(efunOrderDetailId);
				} else {
					//==================================================================
					// 一折购折扣是否已经生成过订单.
					//==================================================================
					if(!StringUtil.isBlank(efunOrderDetail.getStr("order_id"))) {
						invalidEfunOrderDteailIdList.add(efunOrderDetailId);
					}
					
					//==================================================================
					// 一折购折扣是否已经翻牌.
					//==================================================================
					List<BigDecimal> list = null;
					BigDecimal discount = efunOrderDetail.getBigDecimal("discount_val"); // 折扣值.
					if(null == discount) { // 未翻牌.
						invalidEfunOrderDteailIdList.add(efunOrderDetailId);
					} else {
						if(skuDiscountMap.containsKey(efunOrderDetail.getStr("sku_code"))) {
							list = skuDiscountMap.get(efunOrderDetail.getStr("sku_code"));
						} else {
							list = new ArrayList<BigDecimal>();
						}
						list.add(discount);
						skuDiscountMap.put(efunOrderDetail.getStr("sku_code"), list);
					}
					
					//==================================================================
					// 验证一折购详情翻牌超时.
					// 购买折扣商品时,在订单确认页（本页面时限额外加五分钟）超时未确定.
					//==================================================================
					Date beginValidTime = efunOrderDetail.getDate("begin_valid_time");
					if(null != beginValidTime) {
						Date timeOut = DateUtil.addMinute(beginValidTime, 1445); // 24小时 + 5分钟
						if(new Date().after(timeOut)) {
							timeoutEfunOrderDetailIdList.add(efunOrderDetail.getStr("detail_id"));
						} else {
							efunOrderDetailIdList.add(efunOrderDetail.getStr("detail_id"));
						}
					}
					
					//==================================================================
					// Sku对应的销售属性.
					//==================================================================
					if(!skuDataMap.containsKey(efunOrderDetail.getStr("sku_code"))) {
						Map<String, Object> sku = new HashMap<String, Object>();
						sku.put("productId", efunOrderDetail.getInt("product_id")); // 商品Id.
						sku.put("skuCode", efunOrderDetail.getStr("sku_code")); // 商品Sku.
						sku.put("productName", efunOrderDetail.getStr("product_name")); // 商品名称.
						sku.put("productImage", efunOrderDetail.getStr("product_img")); // 商品图片.
						sku.put("skuProperty", efunOrderDetail.getStr("product_property")); // 销售属性.
						sku.put("eqPrice", efunOrderDetail.getBigDecimal("eq_price")); // 一折购价格.
						
						skuDataMap.put(efunOrderDetail.getStr("sku_code"), sku);
					}
					
					//==================================================================
					// Sku对应的商家信息.
					//==================================================================
					if(!sellerDataMap.containsKey(efunOrderDetail.getStr("sku_code"))) {
						Map<String, Object> seller = new HashMap<String, Object>();
						seller.put("merchantId", efunOrderDetail.getStr("merchant_id")); // 店铺/供货商Id.
						seller.put("merchantName", efunOrderDetail.getStr("merchant_name")); // 店铺/供货商名称.
						
						sellerDataMap.put(efunOrderDetail.getStr("sku_code"), seller);
					}
					
					//==================================================================
					// 一折购折扣订单的Sku对应的数量.
					//==================================================================
					if(skuMap.containsKey(efunOrderDetail.getStr("sku_code"))) {
						skuMap.put(efunOrderDetail.getStr("sku_code"), skuMap.get(efunOrderDetail.getStr("sku_code")) + 1);
					} else {
						skuMap.put(efunOrderDetail.getStr("sku_code"), 1);
					}
				}
			}
		}
	}
	
	/**
	 * 拆单.
	 * 
	 * @author Chengyb
	 */
	private void division(Map<String, Integer> skuMap, Map<String, String> sellerMap) {
		Iterator<Entry<String, Integer>> iterator = skuMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			
			String skuCode = entry.getKey();
			Integer order = entry.getValue();
		}
	}
	
	/**
	 * 生成云店自提订单.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param cloudStoreOrderMap
	 *            云店自提商品数据. SkuCode - 数量.
	 * @param efunOrder
	 *            一折购订单.
	 * @param total
	 *            商品总金额(不含运费).
	 * @param orderQuantity
	 *            下单数量.
	 * @param isWin
	 *            是否中奖.
	 * @author Chengyb
	 * @return Map<String, Object>
	 *          <br>Key: order    Value: 订单对象
	 *          <br>Key: orderQuantity Value: 下单成功数量
	 */
	private Map<String, Object> createCloudStoreOrder(
			String userId,
			Map<String, String> sellerMap,
			Map<String, Integer> cloudStoreOrderMap,
			List<EfunUserOrder> efunOrderList,
			List<String> efunOrderDetailIdList,
			BigDecimal total,
			boolean isWin,
			Record orderSku,
			Integer addressId,
			String o2oShopNo,
			String contact,
			String mobile,
			String dataFrom,
			String remark) {
		// ==================================================================
		// 云店自提订单按照店铺/供货商拆单.
		// ==================================================================
		Map<String, Map<String, Integer>> divisionMap = new HashMap<String, Map<String, Integer>>();
		
		Iterator<Entry<String, Integer>> iterator = cloudStoreOrderMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String skuCode = entry.getKey(); // Sku编码.
			int orderQuantity = entry.getValue(); // 下单数量.
			String sellerId = sellerMap.get(skuCode); // 店铺/供货商Id.
			if(divisionMap.containsKey(sellerId)) {
				// 当前Sku已有记录数.
				Map<String, Integer> currentSellerMap = divisionMap.get(sellerId);
				int quantity = currentSellerMap.get(skuCode);
				currentSellerMap.put(skuCode, quantity + orderQuantity);
				divisionMap.put(sellerId, currentSellerMap);
			} else {
				Map<String, Integer> currentSellerMap = new HashMap<String, Integer>();
				currentSellerMap.put(skuCode, orderQuantity);
				divisionMap.put(sellerId, currentSellerMap);
			}
		}
		
		// ==================================================================
		// 创建订单.
		// ==================================================================
//		createOrder(userId, efunOrder, efunOrderDetailIdList, total, isWin, orderSku, orderQuantity, deliveryType, addressId, o2oShopNo, contact, mobile, dataFrom, remark);
		
		Map<String, Object> map = new HashMap<String, Object>();
		// Sku编码.
		String skuCode = orderSku.get("sku_code");

		// 初始化订单.
		Order order = new Order().init(userId, dataFrom, remark);

		// 订单类型.
		if (orderSku.getInt("source") == Product.SOURCE_EXCLUSIVE) {
			order.set("order_type", Order.TYPE_SHOP); // 店铺专卖订单.
			// 根据店铺类型,记录订单幸运一折+类型.
			order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
		} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY) {
			order.set("order_type", Order.TYPE_SELL_BY_PROXY); // E趣代销订单.
		} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY_SEND) {
			order.set("order_type", Order.TYPE_SUPPLIER_SEND); // 厂家自发订单.
		} else if (orderSku.getInt("source") == Product.SOURCE_SELF_EXCLUSIVE) {
			order.set("order_type", Order.TYPE_SELF_SHOP); // 自营专卖订单.
			// 根据店铺类型,记录订单幸运一折+类型.
			order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
		} else {
			order.set("order_type", Order.TYPE_SELF_PUBLIC); // 自营公共订单.
		}
		order.set("merchant_id", orderSku.getStr("target_id")); // 商家ID.
		order.set("merchant_name", orderSku.getStr("target_name")); // 商家名称.
		order.set("merchant_no", orderSku.getStr("target_no")); // 商家编号.
		order.set("total", total); // 商品总金额(不含运费).

		// ==================================================================
		// 更新一折购明细表订单号.
		// ==================================================================
//		if (efunOrder != null && null != efunOrderDetailIdList) {
//			for (int i = 0, size = efunOrderDetailIdList.size(); i < size; i++) {
//				EfunOrderDetail.dao.findById(efunOrderDetailIdList.get(i)).set("order_id", order.getStr("id")).update();
//			}
//		}

		// ==================================================================
		// 配送方式 - 云店自提.
		// ==================================================================
		order.set("delivery_type", Order.DELIVERY_TYPE_SELF); // 配送方式.
		order.set("o2o_shop_no", o2oShopNo); // o2o门店编号.
		order.set("o2o_shop_name", Store.dao.getNameByNo(o2oShopNo)); // o2o门店名称.
		// 获取云店地址信息.
		Record store = Store.dao.findAddressNameByNo(o2oShopNo); 
		order.set("o2o_shop_address", store.getStr("provinceName") + store.getStr("cityName") + store.getStr("areaName") + store.getStr("address")); // o2o门店地址.
		order.set("concat", contact); // 收货人姓名.
		order.set("mobile", mobile); // 收货人手机号.

		// 更新订单.
		order.update();
		
		// 保存订单明细（假如为翻牌购订单则订单明细保存参与一折购时的e趣价）.
//		OrderDetail.dao.add(order.getStr("id"), orderSku.getInt("product_id"), orderSku.getStr("product_no"),
//				orderSku.getStr("product_name"), orderSku.getStr("product_img"), skuCode, orderSku.getStr("properties"),
//				orderSku.getInt("source"), orderSku.getBigDecimal("market_price"),
//				orderSku.getBigDecimal("supplier_price"), orderSku.getBigDecimal("sku_price"), orderQuantity, efunOrder.getStr("order_shop_id"), BigDecimal.ONE,
//				orderSku.getBigDecimal("sku_price"));
		
		map.put("order", order);
//		map.put("orderQuantity", orderQuantity);
		
		return map;
	}
	
	/**
	 * 生成订单.
	 * 
	 * @param userId
	 * @param seller
	 *            店铺/供货商Id.
	 * @param map
	 *            SkuCode - 数量.
	 * @param dataFrom
	 * @param remark
	 * 
	 * @author Chengyb
	 */
	private void createOrder(String userId, Map<String, Object> orderMap, Map<String, Integer> sourceMap, String dataFrom, String remark) {	
		Iterator<Entry<String, Object>> iterator = orderMap.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Entry<String, Object> entry = iterator.next();
//			
//			String key = entry.getKey(); // 商家Id.
//			Map<String, Object> map = (Map<String, Object>) entry.getValue();
//			List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("productList"); // 商家商品列表.
//			
//			// ==================================================================
//			// 订单初始化.
//			// ==================================================================
//			if(sourceMap.containsKey(key)) {
//				
//			}
//			Order order = new Order().init(userId, dataFrom, remark);
//			ProductSku.dao.getProductSource(skuCode);
//			// 订单类型.
//			if (orderSku.getInt("source") == Product.SOURCE_EXCLUSIVE) {
//				order.set("order_type", Order.TYPE_SHOP); // 店铺专卖订单.
//				// 根据店铺类型,记录订单幸运一折+类型.
//				order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
//			} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY) {
//				order.set("order_type", Order.TYPE_SELL_BY_PROXY); // E趣代销订单.
//			} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY_SEND) {
//				order.set("order_type", Order.TYPE_SUPPLIER_SEND); // 厂家自发订单.
//			} else if (orderSku.getInt("source") == Product.SOURCE_SELF_EXCLUSIVE) {
//				order.set("order_type", Order.TYPE_SELF_SHOP); // 自营专卖订单.
//				// 根据店铺类型,记录订单幸运一折+类型.
//				order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
//			} else {
//				order.set("order_type", Order.TYPE_SELF_PUBLIC); // 自营公共订单.
//			}
//			order.set("merchant_id", orderSku.getStr("target_id")); // 商家ID.
//			order.set("merchant_name", orderSku.getStr("target_name")); // 商家名称.
//			order.set("merchant_no", orderSku.getStr("target_no")); // 商家编号.
//			order.set("total", total); // 商品总金额(不含运费).
//
//			// ==================================================================
//			// 更新一折购明细表订单号.
//			// ==================================================================
//			if (efunOrder != null && null != efunOrderDetailIdList) {
//				for (int i = 0, size = efunOrderDetailIdList.size(); i < size; i++) {
//					EfunOrderDetail.dao.findById(efunOrderDetailIdList.get(i)).set("order_id", order.getStr("id")).update();
//				}
//			}
//
//			// ==================================================================
//			// 配送方式.
//			// ==================================================================
//			if (deliveryType == Order.DELIVERY_TYPE_EXPRESS) { // 快递.
//				createExpressOrder(order, addressId, orderSku.getBigDecimal("freight"));
//			} else if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 自提.
//				// 云店有库存的商品直接创建自提订单,云店无库存的商品等待快递配送至云店后自提.
//				Integer inventory = StoreSkuMap.dao.getStoreRealCount(skuCode, o2oShopNo);
//				if (inventory.intValue() >= orderQuantity) {
//					// 直接下单为自提订单.
//					order.set("delivery_type", Order.DELIVERY_TYPE_SELF); // 配送方式.
//					order.set("o2o_shop_no", o2oShopNo); // o2o门店编号.
//					order.set("o2o_shop_name", Store.dao.getNameByNo(o2oShopNo)); // o2o门店名称.
//					// 获取云店地址信息.
//					Record store = Store.dao.findAddressNameByNo(o2oShopNo);
//					order.set("o2o_shop_address", store.getStr("provinceName") + store.getStr("cityName")
//							+ store.getStr("areaName") + store.getStr("address")); // o2o门店地址.
//					order.set("concat", contact); // 收货人姓名.
//					order.set("mobile", mobile); // 收货人手机号.
//				} else {
//					// 将云店库存不足的部分转为快递自提订单.
//					orderQuantity = inventory;
//				}
//			} else if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 快递自提.
//				// 检查快递自提商品库存.
//				if (ProductSku.dao.enoughCount(skuCode, orderQuantity)) {
//					createO2OExpressOrder(order, o2oShopNo, contact, mobile, orderSku.getBigDecimal("freight"));
//				}
//			}
//			// 更新订单.
//			order.update();
//			// 保存订单明细（假如为翻牌购订单则订单明细保存参与一折购时的e趣价）.
//			OrderDetail.dao.add(order.getStr("id"), orderSku.getInt("product_id"), orderSku.getStr("product_no"),
//					orderSku.getStr("product_name"), orderSku.getStr("product_img"), skuCode, orderSku.getStr("properties"),
//					orderSku.getInt("source"), orderSku.getBigDecimal("market_price"),
//					orderSku.getBigDecimal("supplier_price"), orderSku.getBigDecimal("sku_price"), orderQuantity,
//					efunOrder.getStr("order_shop_id"), BigDecimal.ONE, orderSku.getBigDecimal("sku_price"));
//		}
	}
	
	/**
	 * 生成订单.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param efunOrder
	 *            一折购订单.
	 * @param total
	 *            商品总金额(不含运费).
	 * @param orderQuantity
	 *            下单数量.
	 * @param isWin
	 *            是否中奖.
	 * @author Chengyb
	 * @return Map<String, Object>
	 *          <br>Key: order    Value: 订单对象
	 *          <br>Key: orderQuantity Value: 下单成功数量
	 */
	private Map<String, Object> createOrder(
			String userId,
			EfunUserOrder efunOrder,
			List<String> efunOrderDetailIdList,
			BigDecimal total,
			boolean isWin,
			Record orderSku,
			int orderQuantity,
			int deliveryType,
			Integer addressId,
			String o2oShopNo,
			String contact,
			String mobile,
			String dataFrom,
			String remark) {
		Map<String, Object> map = new HashMap<String, Object>();
		// Sku编码.
		String skuCode = orderSku.get("sku_code");
		
		// 初始化订单.
		Order order = new Order().init(userId, dataFrom, remark);

		// 订单类型.
		if (orderSku.getInt("source") == Product.SOURCE_EXCLUSIVE) {
			order.set("order_type", Order.TYPE_SHOP); // 店铺专卖订单.
			// 根据店铺类型,记录订单幸运一折+类型.
			order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
		} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY) {
			order.set("order_type", Order.TYPE_SELL_BY_PROXY); // E趣代销订单.
		} else if (orderSku.getInt("source") == Product.SOURCE_FACTORY_SEND) {
			order.set("order_type", Order.TYPE_SUPPLIER_SEND); // 厂家自发订单.
		} else if (orderSku.getInt("source") == Product.SOURCE_SELF_EXCLUSIVE) {
			order.set("order_type", Order.TYPE_SELF_SHOP); // 自营专卖订单.
			// 根据店铺类型,记录订单幸运一折+类型.
			order.set("efun_plus_type", Shop.dao.getType(orderSku.getStr("target_id"))); // 幸运一折+类型.
		} else {
			order.set("order_type", Order.TYPE_SELF_PUBLIC); // 自营公共订单.
		}
		order.set("merchant_id", orderSku.getStr("target_id")); // 商家ID.
		order.set("merchant_name", orderSku.getStr("target_name")); // 商家名称.
		order.set("merchant_no", orderSku.getStr("target_no")); // 商家编号.
		order.set("total", total); // 商品总金额(不含运费).

		// ==================================================================
		// 更新一折购明细表订单号.
		// ==================================================================
		if (efunOrder != null && null != efunOrderDetailIdList) {
			for (int i = 0, size = efunOrderDetailIdList.size(); i < size; i++) {
				EfunOrderDetail.dao.findById(efunOrderDetailIdList.get(i)).set("order_id", order.getStr("id")).update();
			}
		}

		// ==================================================================
		// 配送方式.
		// ==================================================================
		if (deliveryType == Order.DELIVERY_TYPE_EXPRESS) { // 快递.
			createExpressOrder(order, addressId, orderSku.getBigDecimal("freight"));
		} else if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 自提.
			// 云店有库存的商品直接创建自提订单,云店无库存的商品等待快递配送至云店后自提.
			Integer inventory = StoreSkuMap.dao.getStoreRealCount(skuCode, o2oShopNo);
			if(inventory.intValue() >= orderQuantity) {
				// 直接下单为自提订单.
				order.set("delivery_type", Order.DELIVERY_TYPE_SELF); // 配送方式.
				order.set("o2o_shop_no", o2oShopNo); // o2o门店编号.
				order.set("o2o_shop_name", Store.dao.getNameByNo(o2oShopNo)); // o2o门店名称.
				// 获取云店地址信息.
				Record store = Store.dao.findAddressNameByNo(o2oShopNo); 
				order.set("o2o_shop_address", store.getStr("provinceName") + store.getStr("cityName") + store.getStr("areaName") + store.getStr("address")); // o2o门店地址.
				order.set("concat", contact); // 收货人姓名.
				order.set("mobile", mobile); // 收货人手机号.
			} else {
				// 将云店库存不足的部分转为快递自提订单.
				orderQuantity = inventory;
			}
		} else if(deliveryType == Order.DELIVERY_TYPE_SELF) { // 快递自提.
			// 检查快递自提商品库存.
			if(ProductSku.dao.enoughCount(skuCode, orderQuantity)) {
				createO2OExpressOrder(order, o2oShopNo, contact, mobile, orderSku.getBigDecimal("freight"));
			}
		}
		// 更新订单.
		order.update();
		// 保存订单明细（假如为翻牌购订单则订单明细保存参与一折购时的e趣价）.
		OrderDetail.dao.add(order.getStr("id"), orderSku.getInt("product_id"), orderSku.getStr("product_no"),
				orderSku.getStr("product_name"), orderSku.getStr("product_img"), skuCode, orderSku.getStr("properties"),
				orderSku.getInt("source"), orderSku.getBigDecimal("market_price"),
				orderSku.getBigDecimal("supplier_price"), orderSku.getBigDecimal("sku_price"), orderQuantity, efunOrder.getStr("order_shop_id"), BigDecimal.ONE,
				orderSku.getBigDecimal("sku_price"));
		
		map.put("order", order);
		map.put("orderQuantity", orderQuantity);
		
		return map;
	}
	
	/**
	 * 创建快递订单.
	 * 
	 * @param order
	 *            订单.
	 * @param addressId
	 *            收货地址Id.
	 * @param freight
	 *            运费.
	 * @author Chengyb
	 */
	private void createExpressOrder(Order order, int addressId, BigDecimal freight) {
		RecAddress address = RecAddress.dao.findById(addressId);
		order.set("delivery_type", Order.DELIVERY_TYPE_EXPRESS); // 配送方式(快递).
		order.set("concat", address.get("contact")); // 收货人姓名.
		order.set("mobile", address.get("mobile")); // 收货人手机号.
		order.set("tel", address.get("tel")); // 收货人固话.
		order.set("province", Address.dao.getNameByCode(address.getInt("province_code"))); // 收货人所在省.
		order.set("city", Address.dao.getNameByCode(address.getInt("city_code"))); // 收货人所在市.
		order.set("area", Address.dao.getNameByCode(address.getInt("area_code"))); // 收货人所在区.
		order.set("address", address.get("address")); // 收货人具体地址.
		order.set("zip", address.get("zip")); // 收货人邮编.
		order.set("freight", freight); // 运费.
		order.set("receive_address_id", addressId); // 收货地址id-用户自动分配仓库功能.
	}
	
	/**
	 * 创建云店自提订单.
	 * 
	 * @param order
	 *            订单.
	 * @param cloudStoreNo
	 *            云店编号.
	 * @param contact
	 *            联系人.
	 * @param mobile
	 *            手机号码.
	 * @param freight
	 *            运费.
	 * @author Chengyb
	 */
	private void createO2OExpressOrder(
			Order order,
			String cloudStoreNo,
			String contact,
			String mobile,
			BigDecimal freight) {
		Record store = Store.dao.findAddressNameByNo(cloudStoreNo);
		order.set("delivery_type", Order.DELIVERY_TYPE_EXPRESS_SELF); // 配送方式(快递自提).
		order.set("concat", contact); // 收货人姓名.
		order.set("mobile", mobile); // 收货人手机号.
		order.set("province", store.getStr("provinceName")); // 收货人所在省.
		order.set("city", store.getStr("cityName")); // 收货人所在市.
		order.set("area", store.getStr("areaName")); // 收货人所在区.
		order.set("address", store.getStr("address")); // 收货人具体地址.
		order.set("freight", freight); // 运费.
	}
	
	/**
	 * 支付生成的订单.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param order
	 *            订单.
	 * @param total
	 *            商品总金额.
	 * @author Chengyb
	 */
	private void pay4CreateOrder(
			String userId,
			Order order,
			String dataFrom,
			BigDecimal total,
			int deliveryType,
			BigDecimal useCash,
			Integer useIntegral,
			Record orderSku,
			int smsTypeFlag,
			boolean hasPayFlag,
			JsonMessage jsonMessage) {
		// 订单运费.
		BigDecimal freight = deliveryType == Order.DELIVERY_TYPE_EXPRESS ? orderSku.getBigDecimal("freight")
				: BigDecimal.ZERO;

		// 待支付金额 = 商品总额 + 运费.
		BigDecimal waittingPayCash = total.add(freight);
		if (useIntegral > 0) {
			// 使用积分抵扣的金额.（前面已经验证 最多只能50%）.
			BigDecimal integral2Cash = Integral.dao.getIntegralToCash(useIntegral);
			order.set("use_integral", useIntegral); // 使用积分数.
			order.set("integral_discount", integral2Cash); // 积分抵扣的金额.
			// 保存使用积分记录.
			IntegralUserRecord.dao.saveIntegralUserRecord4Pay(order.getStr("id"), userId, useIntegral,
					IntegralUserRecord.TYPE_SHOPPING_ORDER);
			// 待支付金额.
			waittingPayCash = total.subtract(integral2Cash);
		}

		// ==================================================================
		// 余额支付.
		// ==================================================================
		if (useCash.compareTo(waittingPayCash) >= 0) {
			order.set("cash", waittingPayCash); // 扣账户现金.
			order.set("pay_time", new Date()); // 付款时间.
			// 添加订单日志-支付.
			OrderLog.dao.add(order.getStr("id"), "order_log_pay", dataFrom);
			
			if (deliveryType == Order.DELIVERY_TYPE_SELF) {
				smsTypeFlag = STATUS_HAD_SEND; // 自提订单已发货.
				order.set("taking_code", StringUtil.getRandomNum(6)); // 自提码:随机生成6位数字.
				order.set("status", Order.STATUS_HAD_SEND); // 已发货.
			} else {
				smsTypeFlag = STATUS_WAIT_FOR_SEND; // 快递订单待发货.
				order.set("status", Order.STATUS_WAIT_FOR_SEND); // 待发货.
			}
			hasPayFlag = true;
		}
		// ==================================================================
		// 部分余额支付.
		// ==================================================================
		else {
			order.set("cash", useCash); // 扣账户现金
			order.set("cost", waittingPayCash.subtract(useCash)); // 待支付金额保存到在线待支付金额.
		}
	}
	
}