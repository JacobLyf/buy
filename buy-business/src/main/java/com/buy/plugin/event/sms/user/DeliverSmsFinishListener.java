package com.buy.plugin.event.sms.user;

import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.model.order.Order;
import com.buy.model.sms.SMS;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class DeliverSmsFinishListener implements ApplicationListener<DeliverFinishSmsEvent>
{
	 Logger L = Logger.getLogger(DeliverSmsFinishListener.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(DeliverFinishSmsEvent e)
	{
		L.info("发货成功 - 处理数据准备发送短信");
		//等待事务提交，等待2秒
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		// 查询订单和会员信息
		List<String> orderIds = (List<String>) e.getSource();
		
		for (String o : orderIds)
		{
			String orderId = o;
			L.info("orderId:"+orderId);
			String sql = "SELECT o.no orderNo, o.delivery_type, o.taking_code, u.user_name userName, u.mobile, o.status FROM t_order o, t_user u WHERE o.user_id = u.id AND o.id = ?";
			Record record = Db.findFirst(sql, orderId);
			
			// 没有手机号码
			String mobile = record.get("mobile");
			if (StringUtil.isNull(mobile))
			{
				L.info("处理数据准备发送短信 - 没有手机号码");
				return;
			}
			
			// 根据配送类型发短信
			String userName = record.get("userName");
			String orderNo = record.get("orderNo");
			Integer deliveryType = record.getInt("delivery_type");
		
			if (null == deliveryType)
			{
				L.info("处理数据准备发送短信 - 没有配送方式");
				return;
			}
			// 配送订单
			else if (Order.DELIVERY_TYPE_EXPRESS == deliveryType)
			{
				L.info("处理数据准备发送短信 - 配送");
				String[] datas = {userName, orderNo};
				SMS.dao.sendSMS("order_send_good_finish", datas, mobile, "", "提醒会员发货完成（配送）", BaseConstants.DataFrom.PC);
			}
			// 自提
			else if (Order.DELIVERY_TYPE_SELF == deliveryType){
				
				L.info("处理数据准备发送短信 - 自提");
				String takingCode = record.getStr("taking_code");
				L.info("自提码：" + takingCode);
				Integer status = record.getInt("status");
				L.info("自提订单状态：" + status);
				if (Order.STATUS_HAD_SEND == status)
				{
					L.info("发送短信 - 自提订单已发货");
					String[] datas = {userName, orderNo, takingCode};
					SMS.dao.sendSMS("order_send_good_finish_self", datas, mobile, "", "提醒会员发货完成（自提）", BaseConstants.DataFrom.PC);
				}
				else
				{
					L.info("不发送短信 - 自提订单未发货");
				}
			}
		}
	}

}
