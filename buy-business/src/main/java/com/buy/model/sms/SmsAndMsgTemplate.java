package com.buy.model.sms;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 短信和消息模板
 * @author Sylveon
 *
 */
public class SmsAndMsgTemplate extends Model<SmsAndMsgTemplate> {

	private static final long serialVersionUID = 1L;
	public static final SmsAndMsgTemplate dao = new SmsAndMsgTemplate();
	
	/* ===== ===== ===== ===== 短信模板 ===== ===== ===== ===== */
	
	/** 短信模板 - 通用短信验证码 **/
	public static final String SMS_CODE = "sms_code";
	/** 短信模板 - 注册验证码 **/
	public static final String SMS_REGISTER_CODE = "register_code";
	/** 短信模板 - 注册成功通知 **/
	public static final String SMS_REGISTER_SUCESS = "register_sucess";
	/** 短信模板 - 找回密码（可能没用） **/
	public static final String SMS_FIND_PASSWORD_CODE = "find_password_code";
	/** 短信模板 - 店铺申请审核（通过） **/
	public static final String SMS_SHOP_APPLY_PASS = "shop_apply_pass";
	/** 短信模板 - 店铺申请审核（不通过） **/
	public static final String SMS_SHOP_APPLY_UNPASS = "shop_apply_unpass";
	/** 短信模板 - 手机绑定验证码（可能没用） **/
	public static final String SMS_BLIND_MOBILE_CODE = "blind_mobile_code";
	/** 短信模板 - 幸运一折购中奖 **/
	public static final String SMS_FUN_DARW_WIN = "efun_darw_win";
	/** 短信模板 - 店铺激活审核（通过） **/
	public static final String SMS_SHOP_ACTIVE_PASS = "shop_active_pass";
	/** 短信模板 - 开店预缴费退款通知 **/
	public static final String SMS_SHOP_OPEN_REFUND_PASS = "shop_open_refund_pass";
	/** 短信模板 - 开店申请支付完成通知 **/
	public static final String SMS_SHOP_APPLY_PAY_FINISH = "shop_apply_pay_finish";
	/** 短信模板 - 开店申请支付完成通知 **/
	public static final String SMS_SHOP_TURN_PASS = "shop_turn_pass";
	/** 短信模板 - 一折购中奖商品发货提醒 **/
	public static final String SMS_EFUN_ORDER_WIN_NOTICE = "efun_order_win_notice";
	/** 短信模板 - 开店预缴费成功通知代理商 **/
	public static final String SMS_AGNET_TURN_SHOP_PASS = "agent_turn_shop_success";
	/** 短信模板 - 到货通知 **/
	public static final String SMS_PRODUCT_ARRIVAL_NOTICE = "product_arrival_notice";

	/** 短信模板 - 一折吃中奖自提码通知 **/
	public static final String SMS_EFUN_EAT_TAKING_CODE = "efun_eat_taking_code";
	/** 短信模板 - 拼折吃中奖自提码通知 **/
	public static final String SMS_DISCOUNT_EAT_TAKING_CODE = "discount_eat_taking_code";

	/** 短信模板 - 快递自提到货通知 **/
	public static final String SMS_EXPRESS_SELF_TAKING_NOTICE = "express_self_taking_notice";
	
	
	
	
	/* ===== ===== ===== ===== 消息模板 ===== ===== ===== ===== */
	
	/** 消息模板 - 银行卡修改审核结果（通过） **/
	public static final String BANK_CARD_PASS = "bankCard_pass";
	/** 消息模板 - 银行卡修改审核结果（不通过） **/
	public static final String BANK_CARD_UNPASS = "bankCard_unpass";
	/** 消息模板 - 实名认证审核结果（通过） **/
	public static final String REAL_NAME_PASS = "realName_pass";
	/** 消息模板 - 实名认证审核结果（不通过） **/
	public static final String REAL_NAME_UNPASS = "realName_unpass";
	
	/** 消息模板 - 下单未支付提醒 **/
	public static final String ORDER_UNPAY = "order_unpay";
	/** 消息模板 - 订单关闭提醒 **/
	public static final String ORDER_CLOSE = "order_close";
	/** 消息模板 - 退款申请结果（通过） **/
	public static final String ORDER_REFUND_PASS = "order_refund_pass";
	/** 消息模板 - 退款申请结果（不通过） **/
	public static final String ORDER_REFUND_UNPASS = "order_refund_unpass";
	/** 消息模板 - 退货申请结果（通过） **/
	public static final String ORDER_RETURN_PASS = "order_return_pass";
	/** 消息模板 - 退货申请结果（不通过） **/
	public static final String ORDER_RETURN_UNPASS = "order_return_unpass";
	/** 消息模板 - 积分变动通知（增加） **/
	public static final String INTEGRAL_ADD = "integral_add";
	/** 消息模板 - 积分变动通知（减少） **/
	public static final String INTEGRAL_SUB = "integral_sub";
	/** 消息模板 - 现金变动提醒（增加） **/
	public static final String CASH_ADD = "cash_add";
	/** 现金变动提醒（减少） **/
	public static final String CASH_SUB = "cash_sub";
	/** 商品降价通知（购物车） **/
	public static final String PRODUCT_SALE_CART = "product_sale_cart";
	/** 商品降价通知（收藏） **/
	public static final String PRODUCT_SALE_FAVS = "product_sale_favs";
	
	/** 消息模板 - 开店奖励 **/
	public static final String SHOP_OPEN_REWARD = "shop_open_reward";
	
	/* ===== ===== ===== ===== 推送模板 ===== ===== ===== ===== */
	
	/** 订单发货推送 **/
	public final static String PUSH_ORDER_SEND = "push_order_send";
	/** 幸运一折购中奖消息推送 **/
	public final static String PUSH_EFUN_LUCK = "push_efun_luck";
	/** 购物车商品降价推送 **/
	public final static String PUSH_CART_SALE = "push_cart_sale";
	
	/* ===== ===== ===== ===== 占位符 ===== ===== ===== ===== */
	
	/**
	 * 短信和消息模板通用占位符
	 */
	public static final String PH_CODE = ">xxx<";
	
	/* ===== ===== ===== ===== 方法 ===== ===== ===== ===== */
	
	/**
	 * 获取模板内容
	 */
	public String getContentByType(String code) {
		return Db.queryStr("select content from t_sms_msg_template where code = ?", code);
	}
	
	public String dealContent(String templateCode, String[] datas) {
		String result = "";
		String content = getContentByType(templateCode);
		String[] contentArr = content.split(SmsAndMsgTemplate.PH_CODE);
		for (int i = 0; i < contentArr.length; i++)
			result += i == datas.length ? contentArr[i] : contentArr[i] + datas[i];
		return result;
	}
	
} 
