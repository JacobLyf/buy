package com.buy.plugin.event.order;


import java.sql.SQLException;

import com.buy.common.Ret;
import com.buy.model.order.Order;
import com.buy.model.store.Store;
import com.buy.plugin.event.pos.inventory.PushPosInventoryEvent;
import com.buy.service.order.BaseOrderService;
import com.buy.service.pos.push.PushPosInventory;
import com.buy.string.StringUtil;
import com.jfinal.aop.Duang;

import net.dreamlu.event.EventKit;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 订单扣库存跟自动分配发货仓库监听事件
 */
@Listener(enableAsync=true)
public class OrderStoreListener implements ApplicationListener<OrderStoreEvent> { 


	@Override
	public void onApplicationEvent(OrderStoreEvent orderStoreEvent) {
		
		
		// 获取参数
		Ret ret = (Ret) orderStoreEvent.getSource();
		//获取监控的订单对象
		String orderIds = ret.get("orderIds");
		//获取监控的收货地址ID
		Integer addressId = ret.get("addressId");
		
		String is_efun_order = ret.get("is_efun_order");
		try {
			//第三方支付回调事务提交问题，需要睡两秒
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			if(StringUtil.notNull(orderIds)){
				for(String orderId : orderIds.split(",")){
					//获取订单类型
					int orderType = Order.dao.findByIdLoadColumns(orderId, "order_type").getInt("order_type");
					int deliveryType = Order.dao.findByIdLoadColumns(orderId, "delivery_type").getInt("delivery_type");
					
					boolean result = false;
					if (deliveryType == Order.DELIVERY_TYPE_EXPRESS) {
						BaseOrderService baseOrderService = Duang.duang(BaseOrderService.class);
						//第三方订单
						if (orderType == Order.TYPE_SHOP 
								|| orderType == Order.TYPE_SUPPLIER_SEND) {
							// 根据云店和商家发货地址发货
							result = baseOrderService.orderDeliverByRule(orderId, addressId, is_efun_order);
							// POS推送
						} else {
							// 根据云店地址分配发货
							result = baseOrderService.orderStore(orderId, addressId);
						}
						
					}
					
					// 根据分配发货结果进行推送
					if (result) {
						/*******************推送库存变化到POS @author chenhg ********************/
						/**
						 * false:正常订单、九折购订单
						 * true：一折购订单
						 */
						PushPosInventory source = new PushPosInventory().setOrderId(orderId, false);
						EventKit.postEvent(new PushPosInventoryEvent(source));
						/*******************推送库存变化到POS @author chenhg ********************/
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
