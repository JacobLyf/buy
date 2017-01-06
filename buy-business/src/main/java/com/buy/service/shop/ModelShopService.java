package com.buy.service.shop;

import java.math.BigDecimal;

import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopScore;
import com.jfinal.plugin.activerecord.Record;

public class ModelShopService {

	/**
	 * 店铺好评更新
	 * @param orderId
	 * @author Sylveon
	 */
	public void updateShopReputably(String orderId) {
		// 店铺总体好评评分
		Record info = ShopScore.dao.updateReputablyScore(orderId);

		// 店铺好评率更新
		String shopId		= info.get("shopId");				// 店铺ID
		Double reputably	= info.getDouble("reputably");		// 店铺好评率
		new Shop()
			.set("id", 			shopId)
			.set("good_rate",	new BigDecimal(reputably))
			.update();
	}
	
}
