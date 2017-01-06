package com.buy.plugin.event.sms.user;

import org.apache.log4j.Logger;

import com.buy.common.Ret;
import com.buy.model.order.Order;
import com.buy.model.shop.Shop;
import com.buy.model.sms.SMS;
import com.buy.model.supplier.Supplier;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class ShopOrderApplyReturnListener implements ApplicationListener<ShopOrderApplyReturnEvent>{

	Logger L = Logger.getLogger(ShopOrderApplyReturnListener.class);
	
	@Override
	public void onApplicationEvent(ShopOrderApplyReturnEvent event) {
		L.info("店铺订单申请退货通知店主发送短信");
		Ret ret = (Ret) event.getSource();
		// 售后类型 （ 退款/退货）
		String aftermarket = ret.get("SMSremark");
		// 订单类型
		Integer orderType = ret.get("orderType");
		// 卖家ID
		String merchantId = ret.get("merchantId");
		// 项目来源
		Integer dataFrom = ret.get("dataFrom");
		// 订单编号
		String orderNo = ret.get("orderNo");
		// 手机号码
		String mobile = "";
		// 卖家类型
		String merchantType = "";
		if (orderType == Order.TYPE_SELF_SHOP || orderType == Order.TYPE_SHOP) {// 店铺专卖订单或者自营专卖订单
			mobile = Shop.dao.findByIdLoadColumns(merchantId, "mobile").getStr("mobile");
			merchantType = "店主";
		} else if (orderType == Order.TYPE_SUPPLIER_SEND) {// 厂家自发订单
			mobile = Supplier.dao.findByIdLoadColumns(merchantId, "mobile").getStr("mobile");
			merchantType = "供货商";
		}
		// 组装发送短信备注 example：提醒店主订单申请售后（退货）
		StringBuffer remark = new StringBuffer("提醒");
		remark.append(merchantType);
		remark.append("订单申请售后（");
		remark.append(aftermarket);
		remark.append("）");
		// 短信模板变量
		String[] datas = {merchantType, orderNo, merchantType};
		SMS.dao.sendSMS("shop_order_apply_return", datas, mobile, "", remark.toString(), dataFrom);
	}

}
