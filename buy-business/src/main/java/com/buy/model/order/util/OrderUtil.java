package com.buy.model.order.util;

import java.util.Date;

import com.buy.model.order.Order;
import com.buy.model.order.OrderReturn;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Record;

public class OrderUtil {
	
	/**
	 * 订单类型 - 店铺订单
	 */
	public static final String ORDER_SHOP = Order.TYPE_SHOP + "," + Order.TYPE_SELF_SHOP;
	/**
	 * 订单类型 - 供货商订单
	 */
	public static final String ORDER_SUPPLIER = Order.TYPE_SELL_BY_PROXY + "," + Order.TYPE_SUPPLIER_SEND;
	/**
	 * 订单类型 - 公共订单
	 */
	public static final String ORDER_PUBLIC = Order.TYPE_SELF_PUBLIC + "," + Order.TYPE_SELL_BY_PROXY + "," + Order.TYPE_SUPPLIER_SEND;
	/**
	 * 订单类型 - 公共订单（自营+代发）
	 */
	public static final String ORDER_PUBLIC_EFUN = Order.TYPE_SELF_PUBLIC + "," + Order.TYPE_SELL_BY_PROXY;
	
	/**
	 * 订单类状态- 关闭订单
	 */
	public static final String ORDER_CLOSE = ""
		+ Order.TRADE_CANCLE + ","
		+ Order.TRADE_RETURN_MONEY_SUCCESS + ","
		+ Order.TRADE_RETURN_GOODS_SUCCESS + ","
		+ Order.TRADE_UNPAYMENT_IN_TIME;
	/**
	 * 订单状态  - 退款退货（t_order的trade_status）
	 */
	public static final String ORDER_TRADE_RETURN_APPLYE = Order.TRADE_RETURNING_MONEY + "," +  Order.TRADE_RETURNING_GOODS;
	/**
	 * 订单状态  - 退款退货
	 */
	public static final String ORDER_RETURN_APPLYE = OrderReturn.RETURN_STATUS_APPLY + "";
	/**
	 * 订单状态  - 退货中
	 */
	public static final String ORDER_RETURN_ING = OrderReturn.RETURN_STATUS_WAIT_BACK + "," + OrderReturn.RETURN_STATUS_RETURNING;
	
	/**
	 * 订单状态提示  - 店铺订单
	 */
	public static final String[] ORDER_SHOP_STATUS = {
		// 正常交易
		"等待买家付款", "等待卖家发货", "等待买家收货", "交易成功",
		// 非常交易
		"退款申请中", "退货申请中", "等待买家寄回商品", "等待卖家确认收货", "交易关闭"
	};
	
	/**
	 * 订单状态
	 * @param status
	 * @param tradeStatus
	 * @param returnStatus
	 * @param timeoutPay
	 * @param timeoutRec
	 * @return
	 * @author Sylveon
	 */
	public static int getMagOrderStatus(Integer status, Integer tradeStatus, Integer returnStatus, Integer timeoutPay, Integer timeoutRec) {
		switch (tradeStatus) {
			// 正常交易
			case Order.TRADE_NORMAL:
				switch (status) {
					case Order.STATUS_WAIT_FOR_PAYMENT:			// 等待买家付款(超时付款，订单自动关闭)
						return timeoutPay == 0 ?  0 : 8;
					case Order.STATUS_WAIT_FOR_SEND:			// 等待卖家发货
						return 1;
					case Order.STATUS_HAD_SEND:					// 等待买家收货(超时收货，订单自动完成)
						return timeoutRec == 0 ? 2 : 3;
					case Order.STATUS_WAIT_FOR_EVALUATION:		// 交易成功 - 待评价
					case Order.STATUS_HAD_EVALUATION:			// 交易成功 - 已评价
						return 3;
					default:									// 等待买家付款(超时付款，订单自动关闭)
						return timeoutPay == 0 ?  0 : 8;
				}
			// 退款退货中
			case Order.TRADE_RETURNING_MONEY:
				switch (returnStatus) {
					case OrderReturn.RETURN_STATUS_APPLY:		// 退款申请中
						return 4;
				}
			case Order.TRADE_RETURNING_GOODS:
				switch (returnStatus) {
					case OrderReturn.RETURN_STATUS_APPLY:		// 退货申请中
						return 5;
					case OrderReturn.RETURN_STATUS_WAIT_BACK:	// 等待买家寄回商品
						return 6;
					case OrderReturn.RETURN_STATUS_RETURNING:	// 等待卖家确认收货
						return 7;
				}
			// 交易关闭
			case Order.TRADE_CANCLE:
			case Order.TRADE_UNPAYMENT_IN_TIME:
			case Order.TRADE_RETURN_MONEY_SUCCESS:
			case Order.TRADE_RETURN_GOODS_SUCCESS:
				return 8;
			default:											// 等待买家付款(超时付款，订单自动关闭)
				return timeoutPay == 0 ?  0 : 8;
		}
	}
	
	/**
	 * 改变订单状态
	 */
	public static void handleStatusMsg(Record order)
	{
		Integer tradeStatus = order.getInt("tradeStatus");			// 订单交易状态
		Integer status = order.getInt("status");					// 订单状态
		Date payTillTime = order.getDate("payTillTime");			// 最迟付款时间
		Date recievedTillTime = order.getDate("recievedTillTime");	// 最迟收货时间
		Integer returnStatus = order.getInt("returnStatus");		// 退单状态
		
		if (StringUtil.isNull(returnStatus))
			returnStatus = 0;
		
		Date now = new Date();
		Integer orderStatus = -1;									// 目标订单状态
		switch (tradeStatus)
		{
			// 正常交易
			case Order.TRADE_NORMAL:
				
				switch (status) 
				{
					case Order.STATUS_WAIT_FOR_PAYMENT:				// 等待卖家发货
						orderStatus = StringUtil.notNull(payTillTime) && now.compareTo(payTillTime) >= 0 ? 8 : 0;
						break;
						
					case Order.STATUS_WAIT_FOR_SEND:				// 等待卖家发货
						orderStatus = 1;
						break;
					
					case Order.STATUS_HAD_SEND:						// 等待买家收货(超时收货，订单自动完成)
						orderStatus = StringUtil.notNull(recievedTillTime) && now.compareTo(recievedTillTime) >= 0 ? 3 : 2;
						break;
						
					case Order.STATUS_WAIT_FOR_EVALUATION:			// 交易成功 - 待评价
						orderStatus = 3;
						break;
						
					case Order.STATUS_HAD_EVALUATION:				// 交易成功 - 已评价
						orderStatus = 4;
						break;
						
					default:										// 等待买家付款(超时付款，订单自动关闭)
						orderStatus = StringUtil.notNull(payTillTime) && now.compareTo(payTillTime) >= 0 ? 8 : 0;
						break;
				}
				break;
				
			// 退款
			case Order.TRADE_RETURNING_MONEY:
				switch (returnStatus)
				{
					case OrderReturn.RETURN_STATUS_APPLY:			// 退款申请中
						orderStatus = 5;
						break;
				}
				break;
				
			// 退货
			case Order.TRADE_RETURNING_GOODS:
				switch (returnStatus)
				{
					case OrderReturn.RETURN_STATUS_APPLY:			// 退货申请中
						orderStatus = 6;
						break;
					case OrderReturn.RETURN_STATUS_WAIT_BACK:		// 等待买家寄回商品
					case OrderReturn.RETURN_STATUS_RETURNING:		// 等待卖家确认收货
					case OrderReturn.RETURN_STATUS_WAIT_STORE_BACK:	
						orderStatus = 7;
						break;
				}
				break;
				
			// 交易关闭
			case Order.TRADE_CANCLE:
				orderStatus = 8;
				order.set("returnReason", "买家取消订单");
				break;
			case Order.TRADE_RETURN_MONEY_SUCCESS:
				orderStatus = 8;
			case Order.TRADE_RETURN_GOODS_SUCCESS:
				orderStatus = 8;
				break;
				
			case Order.TRADE_UNPAYMENT_IN_TIME:
				orderStatus = 8;
				order.set("returnReason", "买家未按时付款");
				break;
				
			default:												// 等待买家付款(超时付款，订单自动关闭)
				orderStatus = StringUtil.notNull(payTillTime) && now.compareTo(payTillTime) >= 0 ? 8 : 0;
				break;
		}
		
		String tip = ""; 
		switch (orderStatus)
		{
			case 0:
				tip = "等待买家付款";
				break;
			case 1:
				tip =  "等待卖家发货";
				break;
			case 2:
				tip = "待买家收货";
				break;
			case 3:
				tip =  "交易成功";
				break;
			case 4:
				tip = "交易成功";
				break;
			case 5:
				tip = "退款申请中";
				break;
			case 6:
				tip = "退货申请中";
				break;
			case 7:
				tip = "退货中的订单";
				break;
			case 8:
				tip = "交易关闭";
				break;
			default:
				tip = "";
				break;
		}
		
		order.set("statusTip", tip).set("orderStatus",	orderStatus);
	}
	
}
