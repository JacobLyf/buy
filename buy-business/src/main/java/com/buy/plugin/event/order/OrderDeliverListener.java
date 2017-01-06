package com.buy.plugin.event.order;


import java.util.List;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.common.Ret;
import com.buy.common.SignUtil;
import com.buy.model.order.Order;
import com.buy.model.store.Store;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
/**
 * 后台订单发货触发点监听器
 * @author huangzq-
 *
 */
@Listener(enableAsync = true)
public class OrderDeliverListener implements ApplicationListener<OrderDeliverEvent> { 
	private static final Logger L = Logger.getLogger(OrderDeliverListener.class);
	@Override
	public void onApplicationEvent(OrderDeliverEvent orderDeliverEvent) {
		L.info("调用POS出库接口");
		
		// 获取参数
		Ret ret = (Ret) orderDeliverEvent.getSource();
		//订单id
		String orderId = ret.get("orderId");
		//操作人
		String operator = ret.get("operator");
		//发货云店或云店
		String storeNo = Order.dao.findByIdLoadColumns(orderId, "o2o_shop_no").getStr("o2o_shop_no");
		//获取秘钥
		String key = Store.dao.getSecretKeyByNo(storeNo);
		//调用Pos出库接口
		List<Record> skuCodeCountList = Db.find("SELECT sku_code skuCode, count FROM t_order_detail WHERE order_id = ?", orderId);
		
		String skuCountStr = JsonKit.toJson(skuCodeCountList);
		
		SignUtil sign = new SignUtil();
		sign.setKey(key);
		sign.setParameter("storeNo", storeNo);
		sign.setParameter("operator", operator);
		sign.setParameter("skuCountStr", skuCountStr);
		String result = sign.requestRemote(BaseConstants.Pos.POS_DOMAIN+"PutOutStore.aspx");
		L.info(result);
		
	}

}