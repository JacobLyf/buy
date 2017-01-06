package com.buy.plugin.event.product;

import java.util.Date;
import java.util.List;

import com.buy.common.BaseConfig;
import com.buy.common.BaseConstants;
import com.buy.model.product.Product;
import com.buy.model.product.nocargo.ProReplenishNote;
import com.buy.model.product.nocargo.ProductNoCargo;
import com.buy.model.shorturl.Mapper;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.buy.plugin.event.product.event.ProductArrivalNoticeEvent;
import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * 商品到货通知事件.
 * 
 * @author Chengyb
 */
@Listener(enableAsync = true)
public class ProductArrivalNoticeEventListener implements ApplicationListener<ProductArrivalNoticeEvent> {

	@Override
	public void onApplicationEvent(ProductArrivalNoticeEvent event) {
		Date date = new Date();
		// 商品Id.
		Integer productId = (Integer) event.getSource();
		// 更新到货库存.
		Integer skuCount = Product.dao.getCount(productId);
		ProductNoCargo productNoCargo = ProductNoCargo.dao.findCargoByProductId(productId);
		productNoCargo.set("status", 1);
		productNoCargo.set("replenish_time", date);
		productNoCargo.set("replenish_count", skuCount);
		productNoCargo.set("update_time", date);
		productNoCargo.update();
		
		// 商品名称.
		Product product = Product.dao.findByIdLoadColumns(productId, "name");
		// 商品短链接.
		String shortUrlKey = Mapper.createShortUrlIfNotExsit(BaseConfig.globalProperties.get("wap.product.url") + productId);
		String shortUrl = BaseConfig.globalProperties.get("wap.domain") + "/" + shortUrlKey;
		
		List<ProReplenishNote> list = ProReplenishNote.dao.findByNocargoId(productNoCargo.getInt("id"));
		for (int i = 0, size = list.size(); i < size; i++) {
			ProReplenishNote proReplenishNote = list.get(i);
			// 会员用户名.
			String userName = User.dao.getUserName(list.get(i).getStr("user_id"));
			// 会员手机号.
			String mobile = list.get(i).getStr("mobile");
			// 发送短信.
			SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_PRODUCT_ARRIVAL_NOTICE, new String[] { userName, product.get("name"), shortUrl }, mobile, "", "到货通知", BaseConstants.DataFrom.PC);
			// 更新记录.
			proReplenishNote.set("status", 1);
			proReplenishNote.set("update_time", new Date());
			proReplenishNote.update();
		}
	}
	
}