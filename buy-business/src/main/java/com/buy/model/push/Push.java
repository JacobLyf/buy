package com.buy.model.push;

import org.apache.log4j.Logger;
import com.buy.plugin.jpush.Jpush;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Model;

/**
 * 推送记录
 */
public class Push extends Model<Push>
{
	Logger L = Logger.getLogger(Push.class);
	private static final long serialVersionUID = 1L;
	
	public static final Push dao = new Push();
	
	public Push pushHand()
	{
		/*
		 * 不推送情况
		 */

		if (null == this)
			return null;
		
		String content = this.get("content");			// 推送内容
		Integer platform = this.getInt("platform");		// 推送终端
		Integer jumpTo = this.getInt("jump_to");		// 跳转类型
		String item = this.get("item");					// 推送附带内容
		if (StringUtil.isNull(content) || null == platform || null == jumpTo)
			return null;
		
		/*
		 * 设置推送参数
		 */
		String title = "e趣商城推送";						// 标题
		if (JumpTo.HAND_URL == jumpTo)
			handleExtrasByHtml(jumpTo, item);
		else
			handleExtrasByApi(jumpTo, item);
		
		/*
		 * 推送
		 */
		return Jpush.pushAll(platform, title, this);
	}
	
	/**
	 * 额外参数
	 */
	private void handleExtras(int type, int to, String item)
	{
		this
			.set("jump_type",	type)
			.set("jump_to",		to)
			.set("item",		item);
	}
	
	/**
	 * 额外参数 - 内嵌web页面
	 */
	private void handleExtrasByHtml(int to, String item)
	{
		handleExtras(JumpType.HTML, to, item);
	}
	
	/**
	 * 额外参数 - app接口
	 */
	public void handleExtrasByApi(int to, String item)
	{
		handleExtras(JumpType.API, to, item);
	}
	
	/**
	 * 跳转类型
	 */
	public interface JumpType
	{
		/** 内嵌web页面 **/
		int HTML = 1;
		/** app接口 **/
		int API = 2;
	}
	
	/**
	 * 跳转位置
	 */
	public interface JumpTo
	{
		/** 手动推送内嵌页 **/
		int HAND_URL = 0;
		/** 所有订单 **/
		int ALL_ORDER = 1;
		/** 幸运一折购中奖记录 **/
		int EFUN_LUCK = 2;
		/** 购物车 **/
		int CART = 3;
		/** APP首页 **/
		int APP_HOME = 4;
		/** 幸运一折购专题 **/
		int EFUN_THEME = 5;
		/** 商品列表 **/
		int PRODUCT_LIST = 6;
		/** 商品详情 **/
		int PRODUCT_DETAIL = 7;
		/** 店铺列表 **/
		int SHOP_LIST = 8;
		/** 店铺详情 **/
		int SHOP_DETAIL = 9;
	}
	
	public interface JumpInfo
	{
		/** 幸运一折购中奖记录 **/
		String EFUN_LUCK = "/user/efun/myEfunWin";
	}

}
