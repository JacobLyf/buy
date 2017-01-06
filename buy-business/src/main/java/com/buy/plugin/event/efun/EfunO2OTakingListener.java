package com.buy.plugin.event.efun;

import com.buy.model.order.Order;
import com.buy.model.sms.SMS;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;
import org.apache.log4j.Logger;

import java.util.Map;

@Listener (enableAsync = true)
public class EfunO2OTakingListener implements ApplicationListener<EfunO2OTakingEvent>
{
	 Logger L = Logger.getLogger(EfunO2OTakingListener.class);
	
	@Override
	public void onApplicationEvent(EfunO2OTakingEvent e)
	{
		L.info("一折购中奖-->提货方式-->选择自提 - 处理数据准备发送短信");
		
		// 查询订单和会员信息
		Map<String,Object> map = (Map<String, Object>) e.getSource();
		    int dataFrom = (int) map.get("APP");
			String orderId = (String) map.get("orderId");
			L.info("orderId:"+orderId);
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT ");
			sb.append("    euo.id,");
			sb.append("    euo.user_name userName,");
			sb.append("    euo.mobile,");
			sb.append("    euo.efun_id efunId,");
			sb.append("    euo.product_name proName,");
			sb.append("    euo.o2o_shop_name o2oShopName,");
			sb.append("    euo.taking_code takeCode,");
			sb.append("    euo.delivery_type deliveryType");
			sb.append(" FROM t_efun_user_order euo  ");
			sb.append(" WHERE ");
			sb.append("    euo.`status` = ? ");
			sb.append(" AND ");
			sb.append("    euo.id = ? ");
			Record record = Db.findFirst(sb.toString(),Order.STATUS_HAD_SEND, orderId);

			if(StringUtil.isNull(record)){
				L.info("处理数据准备发送短信 - 查无此订单");
				return;
			}
			
			// 没有手机号码
			String mobile = record.get("mobile");
			if (StringUtil.isNull(mobile))
			{
				L.info("处理数据准备发送短信 - 没有手机号码");
				return;
			}
			
			// 根据配送类型发短信
			String userName = record.get("userName");
			String efunId = record.getInt("efunId").toString();
			Integer deliveryType = record.getInt("deliveryType");
			String proName = record.getStr("proName");
			String o2oShopName = record.getStr("o2oShopName");

			if (Order.DELIVERY_TYPE_SELF == deliveryType)
			{
				L.info("处理数据准备发送短信 - 选择自提");
				String takingCode = record.getStr("takeCode");
				L.info("自提码：" + takingCode);
				if(userName.length() > 10){
					userName = userName.substring(0,10)+"...";
				}
				if(proName.length() > 15){
					proName = proName.substring(0,15)+"...";
				}

				String[] datas = {userName, efunId, proName,o2oShopName,takingCode};
				SMS.dao.sendSMS("efun_order_taking_self", datas, mobile, "", "提醒会员自提幸运一折购奖品", dataFrom);
			}
	}

}
