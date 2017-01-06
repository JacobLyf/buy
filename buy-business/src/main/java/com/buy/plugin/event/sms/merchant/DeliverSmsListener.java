package com.buy.plugin.event.sms.merchant;

import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.model.SysParam;
import com.buy.model.order.Order;
import com.buy.model.shop.Shop;
import com.buy.model.sms.SMS;
import com.buy.model.supplier.Supplier;
import com.buy.string.StringUtil;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class DeliverSmsListener implements ApplicationListener<DeliverSmsEvent>
{
	Logger L = Logger.getLogger(DeliverSmsListener.class);
	
	@Override
	public void onApplicationEvent(DeliverSmsEvent e)
	{
		L.info("支付成功 - 处理数据准备发送短信");
		
		// 查询订单和会员信息
		List<String> orderIds = (List<String>) e.getSource();
		
		for (String o : orderIds)
		{
			String orderId = o;
			
			Order order = Order.dao.findByIdLoadColumns(orderId, "no, order_type, delivery_type, merchant_id");
			
			// 非配送订单
			Integer deliverType = order.getInt("delivery_type");
			if (null == deliverType || Order.DELIVERY_TYPE_EXPRESS != deliverType)
			{
				L.info("支付成功 - 没有配送方式");
				return;
			}
			
			// 查询商家信息
			String merchantNo = null;
			String mobile = null;
			Integer orderType = order.getInt("order_type");
			if (orderType!=null)
			{
				String merchantId = order.getStr("merchant_id");
				// 店铺
				if (Order.TYPE_SHOP == orderType)
				{
					Shop shop = Shop.dao.findByIdLoadColumns(merchantId, "no, mobile");
					merchantNo = shop.getStr("no");
					mobile = shop.getStr("mobile");
				}
				// 供货商
				else if (Order.TYPE_SUPPLIER_SEND == orderType)
				{
					Supplier supplier = Supplier.dao.findByIdLoadColumns(merchantId, "no, mobile");
					merchantNo = supplier.getStr("no");
					mobile = supplier.getStr("mobile");
				}
				// 其他情况不发短信
				else
				{
					L.info("支付成功 - 其他情况不发短信");
					return;
				}
			}
			
			// 发短信
			Integer isDev = SysParam.dao.getIntByCode("is_dev");
			if (isDev == 0)
			{
				String testMobileStr = SysParam.dao.getStrByCode("test_mobile");
				if (testMobileStr.indexOf(",") < 0)
					mobile = testMobileStr;
				else
					mobile = testMobileStr.split(",")[0];
			}
			if (StringUtil.notNull(mobile) || StringUtil.notNull(merchantNo))
			{
				String orderNo = order.getStr("no");
				String[] datas = {merchantNo, orderNo};
				SMS.dao.sendSMS("order_send_good", datas, mobile, "", "商家发货提醒", BaseConstants.DataFrom.PC);
			}
			// 不发短信
			else
			{
				L.info("支付成功 - 没有手机或者没有商家编号");
			}
		}
	}

}
