package com.buy.plugin.event.user;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.buy.common.Ret;
import com.buy.model.message.Message;
import com.buy.model.order.Cart;
import com.buy.model.push.Push;
import com.buy.model.push.PushUserMap;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.buy.plugin.jpush.Jpush;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

/**
 * Listener - 商品降价消息
 */
@Listener (enableAsync = true)
public class ProductSaleUpdateListener implements ApplicationListener<ProductSaleUpdateEvent> {
	
	Logger L = Logger.getLogger(ProductSaleUpdateListener.class);

	@Override
	public void onApplicationEvent(ProductSaleUpdateEvent event) {
		// 获取参数
		Ret ret = (Ret) event.getSource();
		String proSkuCode 	= ret.get("proSkuCode");
		String notCheck		= ret.get("isO2o");
		String proPrice		= ret.get("proPrice");
		
		// 记录会员ID，用于推送;
		Set<String> userIds = new HashSet<String>();
		
		// sku集合
		List<String> skuList = new ArrayList<String>();
		String skusStr = "";
		
		// 非云店完成标记
		if (StringUtil.isNull(notCheck)) {
			// 商品修改价格
			Double proPrice_d = 0D;
			if (StringUtil.notBlank(proPrice))
				proPrice_d = Double.valueOf(proPrice);
			
			// sku价格
			BigDecimal realEqPrice = Db.queryBigDecimal("SELECT eq_price FROM t_pro_sku WHERE code = ?", proSkuCode);
			Double realEqPrice_d = 0D;
			if (realEqPrice!=null)
				realEqPrice_d  = realEqPrice.doubleValue();
			
			if (realEqPrice_d <= proPrice_d)
				return;
			
			skuList.add(proSkuCode);
			
		// 云店完成标记
		} else {
			skuList = ret.get("skuList");
		}
		
		// 处理sku集合
		skusStr = StringUtil.listToStringForSql(",", skuList);
		
		// 查询商品降价，商品存在购物车的用户
		StringBuffer cartSql = new StringBuffer();
		cartSql.append(" SELECT");
		cartSql.append(" 	a.user_id userId,");
		cartSql.append(" 	b.user_name userName,");
		cartSql.append(" 	c.name proName,");
		cartSql.append(" 	(a.price - " + proPrice + ") subPrice");
		cartSql.append(" FROM t_cart a");
		cartSql.append(" LEFT JOIN t_user b ON b.id = a.user_id");
		cartSql.append(" LEFT JOIN t_product c ON c.id = a.product_id");
		cartSql.append(" WHERE 1 = 1");
		cartSql.append(" AND a.sku_code IN (" + skusStr + ")" );
		cartSql.append(" AND a.price > ? ");
		cartSql.append(" AND a.status = ? ");
		List<Record> cartList = Db.find(cartSql.toString(), proPrice, Cart.STATUS_VALID);
		// 发送消息
		String templateCode = SmsAndMsgTemplate.PRODUCT_SALE_CART;
		for (Record c : cartList) {
			// 设置参数：用户ID、用户名、商品名、减价价格
			String userId = c.getStr("userId");
			String userName = c.getStr("userName");
			String proName = c.getStr("proName");
			BigDecimal subPrice = c.getBigDecimal("subPrice");
			// 设置消息内容参数、消息内容
			String[] datas = new String[]{userName, proName, subPrice.toString()};
			String content = SmsAndMsgTemplate.dao.dealContent(templateCode, datas);
			// 添加消息
			Message.dao.add4Private(Message.TYPE_SALE_MESSAGE, proSkuCode, Message.TITLE_PRODUCT_SALE, content, templateCode, userId, User.FRONT_USER);

			userIds.add(userId);
		}
		
		// 推送
		if (StringUtil.notNull(userIds) && userIds.size() > 0)
		{
			List<String> userIdList = new ArrayList<String>(); 
			for (String id : userIds)
				userIdList.add(id);
			
			// 获取推送注册ID集合 
			List<String> regIds = PushUserMap.dao.findRegIds(userIdList);
			if (StringUtil.isNull(regIds))
				return;
			if (regIds.size() == 0)
				return;
			
			// 推送
			L.info("推送 - 商品降价消息");
			Push push = new Push();
			push.handleExtrasByApi(Push.JumpTo.CART, null);
			String content = SmsAndMsgTemplate.dao.getContentByType(SmsAndMsgTemplate.PUSH_CART_SALE);
			
			if (StringUtil.notNull(content))
			{
				push.set("content", content);
				push = Jpush.push4RegId(Jpush.PushPlatform.ALL, regIds, "购物车商品降价通知", push);
				push.set("create_time", new Date()).save();
			}
		}
		
	}
	
}