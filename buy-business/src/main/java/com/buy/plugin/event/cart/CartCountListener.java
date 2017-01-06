package com.buy.plugin.event.cart;


import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import com.buy.model.order.Cart;

/**
 * 购物车数量监听事件
 */
@Listener(enableAsync=false)
public class CartCountListener implements ApplicationListener<CartCountEvent> { 


	@Override
	public void onApplicationEvent(CartCountEvent cartCountEvent) {
		
		String userId = (String) cartCountEvent.getSource();
		/***********将购物车数量保存到redis*****************/
		Cart.dao.updateCartCountRedis(userId);
		/***********将购物车数量保存到redis*****************/
		
	}

}