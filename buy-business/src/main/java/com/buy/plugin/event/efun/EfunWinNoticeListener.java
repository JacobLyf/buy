package com.buy.plugin.event.efun;

import com.buy.model.order.Order;
import com.buy.model.product.Product;
import com.buy.model.shop.Shop;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.supplier.Supplier;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;
import org.apache.log4j.Logger;

import java.util.Map;

@Listener (enableAsync = true)
public class EfunWinNoticeListener implements ApplicationListener<EfunWinNoticeEvent>
{
	 Logger L = Logger.getLogger(EfunWinNoticeListener.class);
	
	@Override
	public void onApplicationEvent(EfunWinNoticeEvent e)
	{
		L.info("--一折购中奖--提醒店家发货--");
		
		// 查询商品所属店家/厂家
		Map<String,Object> map = (Map<String, Object>) e.getSource();
		    int dataFrom = (int) map.get("dataFrom");
			String proName = (String) map.get("proName");
			String property = (String) map.get("property");
			String shopId = (String) map.get("shopId");
			int efunId = (int) map.get("efunId");
			int winCount = (int) map.get("winCount");
			int unSend = (int) map.get("unSend");
			String userName = "";
			String mobile = "";
			L.info("中奖商品:"+proName);
			Shop shop = Shop.dao.findById(shopId);
			if(StringUtil.notNull(shop)){
				mobile = shop.getStr("mobile");
				userName = shop.getStr("name");
			}else{
				L.info("处理数据准备发送短信 - 找不到店铺了");
				return;
			}

			// 没有手机号码
			if (StringUtil.isNull(mobile))
			{
				L.info("处理数据准备发送短信 - 没有手机号码");
				return;
			}
			//处理空属性
			if(StringUtil.isNull(property)){
				property = "";
			}

		/**
		 * 发货提醒:
		 * 短信模板:
		 * 通知：
		 * 您的店铺E0017xxxx商品xxx（详细到sku）参与幸运一折购第xxx期共中奖xxx个，待发货 xxx个，请登录系统查看并发货。【 E趣商城】
		 */

		L.info("处理数据准备发送短信 - 发送提醒发货短信");

		String[] datas = {userName,proName+""+property,efunId+"", winCount+"",unSend+""};
		SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_EFUN_ORDER_WIN_NOTICE, datas, mobile, "", "一折购中奖商品-提醒店家发货", dataFrom);
	}

}
