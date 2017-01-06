package com.buy.common.constants;


/**
 * mq常量
 * @author zhuoqi
 *
 */
public class MqConstants {
	/**
	 * 队列
	 * @author zhuoqi
	 *
	 */
	public interface Queue {
		/**
		 * 普通订单锁跳去支付扣存（5分钟）
		 */
		public static final String ORDER_LOCK_STORE_DELAY = "order_lock_store_delay";
		/**
		 * 一折购订单跳去支付锁扣存（5分钟）
		 */
		public static final String EFUN_ORDER_LOCK_PAY_STORE_DELAY = "efun_order_lock_pay_store_delay";		
		/**
		 * 一折购订单锁扣存（2小时）
		 */
		public static final String EFUN_ORDER_LOCK_STORE_DELAY = "efun_order_lock_store_delay";		
		/**
		 * 普通订单锁自动取消（24小时）
		 */
		public static final String ORDER_CANCEL_DELAY = "order_cancel_delay";
	}
	
	
	
}
