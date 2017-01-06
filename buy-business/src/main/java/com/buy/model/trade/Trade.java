package com.buy.model.trade;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.common.MqUtil;
import com.buy.common.Ret;
import com.buy.common.constants.MqConstants;
import com.buy.date.DateUtil;
import com.buy.encryption.MD5Builder;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.agent.Agent;
import com.buy.model.agent.AgentCashRecord;
import com.buy.model.efun.Efun;
import com.buy.model.efun.EfunOrderDetail;
import com.buy.model.efun.EfunRefundApply;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.identification.PeishiApply;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.order.Order;
import com.buy.model.order.OrderLog;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopApply;
import com.buy.model.shop.ShopAssignment;
import com.buy.model.shop.ShopCashRechargeRecord;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.shop.ShopCertification;
import com.buy.model.shop.ShopDeposit;
import com.buy.model.shop.ShopRenew;
import com.buy.model.shop.ShopScore;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRechargeRecord;
import com.buy.model.user.UserCashRecord;
import com.buy.plugin.event.order.OrderStoreEvent;
import com.buy.plugin.event.pos.inventory.PushPosInventoryEvent;
import com.buy.plugin.event.shop.ShopUpdateEvent;
import com.buy.plugin.event.sms.merchant.DeliverSmsEvent;
import com.buy.plugin.event.sms.shop.ApplyUpdateEvent;
import com.buy.plugin.event.sms.user.DeliverFinishSmsEvent;
import com.buy.radomutil.RadomUtil;
import com.buy.service.pos.push.PushPosInventory;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.NestedTransactionHelpException;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;
/**
 * 交易信息表
 * @author eriol
 *
 */
public class Trade extends Model<Trade>{

	private static final long serialVersionUID = 1L;
	
	public static final Trade dao = new Trade();
	
	private Logger L = Logger.getLogger(Trade.class);
	
	///////////////////////支付平台////////////////////////////////
	/**
	 * 支付平台-微信
	 */
	public static final String PLAFORM_WX = "WXPAY";
	/**
	 * 支付平台-支付宝
	 */
	public static final String PLAFORM_ALIPAY = "ALIPAY";

	/**
	 * 支付平台-浦发银行（包括跨行）
	 */
	public static final String PLAFORM_SPDB = "SPDB";

	
    /////////////////////支付方式////////////////////////////////
	/**
	 * 微信手机网站
	 */
	public static final String PAY_TYPE_WX_JS = "WXPAY-JSAPI";
	/**
	 * 微信扫码支付
	 */
	public static final String PAY_TYPE_WX_NATIVE = "WXPAY-NATIVE";
	/**
	 * 微信APP支付
	 */
	public static final String PAY_TYPE_WX_APP = "WXPAY-APP";
	/**
	 * 支付宝移动
	 */
	public static final String PAY_TYPE_ALI_APP = "ALIPAY-APP";
	/**
	 * 支付宝在线支付
	 */
	public static final String PAY_TYPE_ALI_DIRECT = "ALIPAY-DIRECT";
	/**
	 * 浦发银行在线支付
	 */
	public static final String PAY_TYPE_SPDB_WEB = "SPDB-WEB";
	
	
	
	/**
	 * 类型:购物
	 */
	public static final int TYPE_SHOPING = 1;
	/**
	 * 类型:会员充值
	 */
	public static final int TYPE_USER_RECHARGE = 2;
	/**
	 * 类型:店铺充值
	 */
	public static final int TYPE_SHOP_RECHARGE = 3;
	/**
	 * 类型:店铺商品假一赔十认证
	 */
	public static final int TYPE_SHOP_PEISHI = 4;
	/**
	 * 类型:E趣购
	 */
	public static final int TYPE_EFUN_ORDER = 5;
	/**
	 * 类型:店铺续费
	 */
	public static final int TYPE_SHOP_RENEW = 6;
	/**
	 * 类型:店铺保证金
	 */
	public static final int TYPE_SHOP_DEPOSIT = 7;
	/**
	 * 类型:幸运一折购中奖支付运费
	 */
	public static final int TYPE_EFUN_FREIGHT = 8;
	/**
	 * 类型:店铺续费一年活动
	 */
	public static final int TYPE_SHOP_RENEW_ACTIVITY = 9;
	/**
	 * 类型:开店预缴
	 */
	public static final int TYPE_SHOP_OPEN = 10;
	/**
	 * 类型:店铺激活预缴费
	 */
	public static final int TYPE_SHOP_ACTIVITY = 11;
	
	

	
	
	
	
	
	/**
	 * 状态:提交
	 */
	public static final int STATUS_SUBMIT = 0;
	/**
	 * 状态:成功
	 */
	public static final int STATUS_SUCCESS = 1;
	/**
	 * 状态:失败
	 */
	public static final int STATUS_FAIL = 2;
	
	
	
	
	
	/**
	 * 获取支付平台方式
	 * @param code
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年12月11日上午11:55:10
	 */
	public String getPlaformName(String code){
		if (StringUtil.isNull(code))
			return "";
		
		String name = "";
		switch(code){
			case Trade.PLAFORM_WX:
				name =  "微信支付";
				break;
			case Trade.PLAFORM_ALIPAY:
				name =  "支付宝支付";
				break;

			case Trade.PLAFORM_SPDB:
				name = "浦发银行支付";
				break;
		}
		return name;
	}
	
	/**
	 * 根据code获取支付方式
	 * @param payTypeCode
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2015年12月11日下午1:48:23
	 */
	public String getPayTypeName(String payTypeCode){
		String name = "在线支付";
		switch(payTypeCode){
			case Trade.PAY_TYPE_WX_NATIVE:
				name =  "微信扫码支付";
				break;
			case Trade.PAY_TYPE_ALI_APP:
				name =  "支付宝APP支付";
				break;			
			case Trade.PAY_TYPE_WX_APP:
				name =  "微信APP支付";
				break;
			case Trade.PAY_TYPE_WX_JS:
				name =  "微信手机网站";
				break;
			default:
				break;
		}
		return name;
	}
	/**
	 * 根据编号获取交易并锁住该交易
	 * @param no
	 * @return
	 * @author huangzq
	 */
	public Trade findByNoForUpdate(String no){
		return dao.findFirst("select t.* from t_trade t where t.no = ? for update",no);
	}
	/**
	 * 支付回调时检查交易状态与金额是否合法
	 * @param no
	 * @param total_fee
	 * @return
	 * @author huangzq
	 */
	public boolean checkTradeForCallBack(Trade trade,BigDecimal total_fee){
		
		if(trade!=null&&trade.getInt("status")==Trade.STATUS_SUBMIT
				&&trade.getBigDecimal("cash").compareTo(total_fee)==0){
			return true;
		}
		return false;
	}
	/**
	 * 处理交易
	 * @param trade
	 * @author huangzq
	 * @throws SQLException 
	 */
	public void handleTrade(Trade trade, String dataFrom) throws Exception {
		Integer type = trade.getInt("type");
		
		//购物订单
		if(type == Trade.TYPE_SHOPING){
			this.shoppingCallBack(trade, dataFrom);
		//会员充值	
		}else if(type == Trade.TYPE_USER_RECHARGE){
			this.userRechargeCallBack(trade);
		//店铺充值
		}else if(type == Trade.TYPE_SHOP_RECHARGE){
			this.shopRechargeCallBack(trade);
		//店铺假一赔十
		}else if(type == Trade.TYPE_SHOP_PEISHI){
			this.shopPeishiCallBack(trade);
		//店铺续费
		}else if(type == Trade.TYPE_SHOP_RENEW){
			this.shopRenewCallBack(trade);
		//店铺保证金
		}else if(type == Trade.TYPE_SHOP_DEPOSIT){
			this.shopDepositCallBack(trade);
		//乐趣购
		}else if(type == Trade.TYPE_EFUN_ORDER){
			this.efunOrderCallBack(trade);
		}else if(type==Trade.TYPE_SHOP_RENEW_ACTIVITY){
			this.shopRenewActivityCallBack(trade);
		//开店预缴费 - 自主开店
		}else if(type==TYPE_SHOP_OPEN){
			shopOpenCallBack(trade);
		//开店预缴费 - 店铺激活
		}else if(type==TYPE_SHOP_ACTIVITY){
			shopActivaityCallBack(trade);
		}
		//更新交易
		trade.set("update_time", new Date());
		trade.update();
	}
	
	
	/**
	 * 购物回调
	 * @param trade
	 * @author huangzq
	 * @throws SQLException 
	 */
	
	private void shoppingCallBack(Trade trade, String dataFrom) throws Exception {
		
		Date now = new Date();
		//更新订单
		String[] orderIds = trade.getStr("target").split(",");
		
		List<String> takeBySelfList = new ArrayList<String>();			// 自提订单ID集合
		List<String> waitForSendList = new ArrayList<String>();			// 发货订单ID集合
		
		for(String orderId: orderIds){
			boolean result = true;
			try{
				result = Db.tx(new IAtom() {				
				@Override
				public boolean run() throws SQLException {
					
					Order order = Order.dao.getOrderForUpdate(orderId);
					//订单处于未付款状态
					if(order!=null&&order.getInt("status")==Order.STATUS_WAIT_FOR_PAYMENT
							&&order.getInt("trade_status")==Order.TRADE_NORMAL){

						int deliveryType = order.getInt("delivery_type");
						Ret ret = new Ret();
						ret.put("now", now);
						ret.put("order", order);
						ret.put("trade", trade);
						ret.put("takeBySelfList", takeBySelfList);
						ret.put("waitForSendList", waitForSendList);
					
						//判断是否已经释放库存（mq在该订单五分钟内没有支付，会释放锁定库存）
						if(order.getInt("is_release_lock_count") == Order.IS_RELEASE_LOCK_COUNT_Y){
							//是否九折购订单
							if(order.getInt("is_efun_nine")==BaseConstants.YES){
								//参与记录是否是否库存
								int flag = EfunUserOrder.dao.getIsReleaseLockCountByOrderId(orderId);
								if(flag==BaseConstants.NO){
									//Trade.dao.orderHandleForaddCountSucc(ret);
									EfunUserOrder efunUserOrder = EfunUserOrder.dao.getEfunUserOrderForUpdateWithOrderId(orderId);
									efunUserOrder.set("is_release_lock_count", BaseConstants.YES);
									efunUserOrder.update();
									return true;
								}
								
							}
							
							//================增加锁定库存==========================//
							//根据配送类型，增加仓库商品锁定库存 或商品锁定库存
							JsonMessage jsonMessage = ProductSku.dao.addLockCountForOrderId(deliveryType, order.getStr("o2o_shop_no"), order.getStr("id"));
							//是否增加锁定库存成功
							if(jsonMessage.getStatus().equals("0")){
								Trade.dao.orderHandleForaddCountSucc(ret, dataFrom);
								return true;
							}
						
						}else{//未释放库存											
					
							Trade.dao.orderHandleForaddCountSucc(ret, dataFrom);
							return true;
						}
						
						
						
						
					}else{
						L.info("订单状态验证不通过");
					
					}
					return false;
				}
			});
			//库存不足情况	
			}catch(NestedTransactionHelpException e){
				L.info("库存不足");
				DbKit.getConfig().getConnection().rollback();
				result = false;
			}catch (Exception e) {
				L.info("程序异常");
				throw e;
			}
			
			//库存不足，超卖
			if(result==false){
				
				Db.tx(new IAtom() {					
					@Override
					public boolean run() throws SQLException {
						Order order = Order.dao.getOrderForUpdate(orderId);
						//订单处于未付款状态
						if(order.getInt("status")==Order.STATUS_WAIT_FOR_PAYMENT
								&&order.getInt("trade_status")==Order.TRADE_NORMAL){
							Ret ret = new Ret();
							ret.put("now", now);
							ret.put("order", order);
							ret.put("trade", trade);
							Trade.dao.orderHandleForaddCountFail(ret, dataFrom);
						}
						return true;
					}
				});
			}else{
				/*******************推送库存变化到POS @author chenhg ********************/
				/**
				 * false:正常订单、九折购订单
				 * true：一折购订单
				 */
				PushPosInventory source = new PushPosInventory().setOrderId(orderId, false);
				EventKit.postEvent(new PushPosInventoryEvent(source));
				/*******************推送库存变化到POS @author chenhg ********************/
				
				/***********************根据发货规则发货******************************/
				Order order = Order.dao.findById(orderId);
				Ret ret = new Ret();
				ret.put("addressId", order.getInt("receive_address_id"));
				ret.put("orderIds", orderId);
				ret.put("is_efun_order", order.get("is_efun_order"));
				EventKit.postEvent(new OrderStoreEvent(ret));
				/***********************根据发货规则发货******************************/
			}
			
			
		}
		trade.update();

		// 发短信 - 通知会员自提码
		if (takeBySelfList.size() > 0)
			EventKit.postEvent(new DeliverFinishSmsEvent(takeBySelfList));
		
		// 发短信 - 通知商家发货
		if (waitForSendList.size() > 0)
			EventKit.postEvent(new DeliverSmsEvent(waitForSendList));
		
		
		
	
	}
	/**
	 * 会员充值回调
	 * @param trade
	 * @param userType
	 * @author huangzq
	 */
	private void userRechargeCallBack(Trade trade){
		String userId = trade.getStr("target");
		User user = User.dao.findById(userId);
		Date now = new Date();
		//添加充值记录
		String no = StringUtil.getUnitCode(UserCashRechargeRecord.PREFIX_NO);
		UserCashRechargeRecord rechargeRecord = new UserCashRechargeRecord();
		rechargeRecord.set("no", no);
		rechargeRecord.set("recharge_time", now);
		rechargeRecord.set("cost_cash", trade.getBigDecimal("cash"));
		//计算充入金额
		BigDecimal rate =  SysParam.dao.getBigDecimalByCode("recharge_rate");
		BigDecimal money = trade.getBigDecimal("cash").multiply(rate);
		money.setScale(2);
		rechargeRecord.set("into_cash", money);
		rechargeRecord.set("pay_type", UserCashRechargeRecord.PAY_TYPE_ONLINE);
		rechargeRecord.set("pay_status", UserCashRechargeRecord.PAY_STATUS_WAITPAY);
		rechargeRecord.set("user_id", userId);
		rechargeRecord.set("user_no", user.get("user_name"));
		rechargeRecord.set("user_name", user.get("user_name"));
		rechargeRecord.set("audit_status", UserCashRechargeRecord.AUDIT_STATUS_PASS);
		rechargeRecord.set("trans_bill_no", trade.getStr("third_bill_no"));
		rechargeRecord.set("trade_plaform", trade.getStr("platform"));
		rechargeRecord.set("trade_pay_type", trade.getStr("pay_way"));
		rechargeRecord.set("pay_time", now);
		rechargeRecord.set("pay_status",UserCashRechargeRecord.PAY_STATUS_PAIED );
		rechargeRecord.set("audit_time", now);
		rechargeRecord.save();
		//更新账户余额
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		account.set("cash", account.getBigDecimal("cash").add(money));
		account.update();
		//添加现金记录
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		UserCashRecord.dao.add(money, account.getBigDecimal("cash").add(freezeCash), UserCashRecord.TYPE_RECHARGE, 
				userId, "现金充值："+money+"元");
		
		
	}
	/**
	 * 店铺充值回调
	 * @param trade
	 * @author huangzq
	 */
	private void shopRechargeCallBack(Trade trade){
		String shopId = trade.getStr("target");
		Shop shop = Shop.dao.findById(shopId);
		Date now = new Date();
		//添加充值记录
		String no = StringUtil.getUnitCode(ShopCashRechargeRecord.PREFIX_NO);
		ShopCashRechargeRecord rechargeRecord = new ShopCashRechargeRecord();
		rechargeRecord.set("no", no);
		rechargeRecord.set("recharge_time", now);
		rechargeRecord.set("cost_cash", trade.getBigDecimal("cash"));
		//计算充入金额
		BigDecimal rate =  SysParam.dao.getBigDecimalByCode("recharge_rate");
		BigDecimal money = trade.getBigDecimal("cash").multiply(rate);
		money.setScale(2);
		rechargeRecord.set("into_cash", money);
		rechargeRecord.set("pay_type", ShopCashRechargeRecord.PAY_TYPE_ONLINE);
		rechargeRecord.set("pay_status", ShopCashRechargeRecord.PAY_STATUS_WAIT);
		rechargeRecord.set("shop_id", shopId);
		rechargeRecord.set("shop_no", shop.get("no"));
		rechargeRecord.set("shop_name", shop.get("name"));
		rechargeRecord.set("audit_status", ShopCashRechargeRecord.AUDIT_STATUS_PASS);
		rechargeRecord.set("trans_bill_no", trade.getStr("third_bill_no"));
		rechargeRecord.set("trade_plaform", trade.getStr("platform"));
		rechargeRecord.set("trade_pay_type", trade.getStr("pay_way"));
		rechargeRecord.set("pay_time", now);
		rechargeRecord.set("pay_status",ShopCashRechargeRecord.PAY_STATUS_PASS );
		rechargeRecord.set("audit_time", now);
		rechargeRecord.save();
		//更新账户余额
		BigDecimal changeCash = rechargeRecord.getBigDecimal("into_cash");
		Account account = Account.dao.getAccountForUpdate(shopId, Account.TYPE_SHOP);
		account.set("cash", account.getBigDecimal("cash").add(changeCash));
		account.update();
		//添加现金记录
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		ShopCashRecord.dao.add(changeCash, account.getBigDecimal("cash").add(freezeCash), no, ShopCashRecord.TYPE_RECHARGE, 
				shopId, "现金充值："+changeCash+"元");
	
		
	}
	/**
	 * 店铺假一赔十回调
	 * @param trade
	 * @author huangzq
	 */
	private void shopPeishiCallBack(Trade trade){
		String productNo = trade.getStr("target");
		Date now = new Date();
		
		String sql = "select p.id ,shop_id from t_product p where p.no = ?";
		Product product = Product.dao.findFirst(sql,productNo);
		Integer productId = product.getInt("id");
		String shopId = product.getStr("shop_id");
		BigDecimal deposit = trade.getBigDecimal("cash");
		//获取倍数
		BigDecimal multiple =  SysParam.dao.getBigDecimalByCode("peishi_multiple");
		BigDecimal price = deposit.divide(multiple).setScale(2);
		//添加记录
		PeishiApply apply = new PeishiApply();
		apply.set("product_id", productId);
		apply.set("eq_price", price);
		apply.set("deposit", deposit);
		apply.set("pay_type", PeishiApply.PAY_TYPE_ONLINE);
		apply.set("shop_id",shopId);
		apply.set("pay_time", now);
		apply.set("create_time", now);
		apply.set("status", PeishiApply.STATUS_SUCCESS);
		apply.set("audit_status", PeishiApply.AUDIT_STATUS_SUCCESS);
		apply.set("pay_time", now);
		apply.set("audit_time", now);
		apply.set("platform", trade.getStr("platform"));
		apply.set("pay_way", trade.getStr("pay_way"));
		apply.set("third_bill_no", trade.getStr("third_bill_no"));
		apply.save();
		//更新商品状态
		product.set("is_peishi", BaseConstants.YES);
		product.update();
		//修改店铺假一赔十认证
		Shop shop = new Shop();
		shop.set("id", shopId);
		shop.set("is_peishi", BaseConstants.YES);
		shop.update();
		// 假一赔十评分
		ShopScore.dao.updateCertificationScore(shopId);
		
	}
	/**
	 * 店铺续费
	 * @param trade
	 * @author huangzq
	 */
	private void shopRenewCallBack(Trade trade){
		String shopId = trade.getStr("target");
		Date now = new Date();			
		//更新店铺到期时间
		Shop shop = Shop.dao.findByIdLoadColumns(shopId, "id,no,agent_id,expire_date,forbidden_status");
		Date expireDate = shop.getDate("expire_date");
		//获取3个月续费金额
		BigDecimal renewCash = SysParam.dao.getBigDecimalByCode("shop_renew_cash");
		//倍数
		int times = trade.getBigDecimal("cash").divide(renewCash).intValue();
		//计算续费后的日期
		if(expireDate.before(new Date())){
			expireDate = DateUtil.getMaxDate(now);
		}
		expireDate = DateUtil.addMonth(expireDate, ShopRenew.RENEW_MONTHS*times);
		shop.set("expire_date", expireDate);
		
		int forbiddenStatus = shop.getInt("forbidden_status");
		//判断店铺是否处于到期冻结状态，是则续费完改为正常状态
		if(Shop.FORBIDDEN_STATUS_DISABLE_UNPAY == forbiddenStatus){
			shop.set("forbidden_status", Shop.FORBIDDEN_STATUS_NORMAL);
			shop.update();
			EventKit.postEvent(new ShopUpdateEvent(shop.getStr("id")));//更新索引
		}else{
			shop.update();
		}
		
		//添加续费记录
		ShopRenew renew = new ShopRenew();
		renew.set("shop_id", shopId);
		renew.set("cash", trade.getBigDecimal("cash"));
		renew.set("type", ShopRenew.TYPE_MANUAL);
		renew.set("pay_type", ShopRenew.PAY_TYPE_ONLINE);
		renew.set("audit_status", ShopRenew.AUDIT_STATUS_SUCCESS);
		renew.set("audit_time", now);
		renew.set("expire_time", shop.getDate("expire_date"));
		renew.set("create_time", now);
		renew.set("platform", trade.getStr("platform"));
		renew.set("pay_way", trade.getStr("pay_way"));
		renew.set("third_bill_no", trade.getStr("third_bill_no"));
		renew.save();
		//e趣商城收取续费金额
		BigDecimal rentCash = SysParam.dao.getBigDecimalByCode("efun_shop_renew_cash").multiply(new BigDecimal(times));		
		//获取代理商账号
		Account agentAccount = Account.dao.getAccountForUpdate(shop.getStr("agent_id"), Account.TYPE_AGENT);
		//更新账户余额
		BigDecimal remainCash = agentAccount.getBigDecimal("cash");
		BigDecimal addCash = trade.getBigDecimal("cash").subtract(rentCash);
		agentAccount.set("cash", remainCash.add(addCash));
		agentAccount.update();
		//添加代理商返利记录
		BigDecimal freezeCash = agentAccount.getBigDecimal("freeze_cash");
		AgentCashRecord.dao.add(addCash, agentAccount.getBigDecimal("cash").add(freezeCash), "", AgentCashRecord.REBATE_SHOP_RENT,renew.getInt("id")+"", shop.getStr("agent_id"),AgentCashRecord.REMARK_REBATE_SHOP_RENT);
		
		
	}
	/**
	 * 店铺续费活动
	 * @param trade
	 * @author huangzq
	 */
	private void shopRenewActivityCallBack(Trade trade){
		String shopId = trade.getStr("target");
		Date now = new Date();			
		//更新店铺到期时间
		Shop shop = Shop.dao.findByIdLoadColumns(shopId, "id,no,agent_id,expire_date,forbidden_status");
		Date expireDate = shop.getDate("expire_date");
		//计算续费后的日期
		if(expireDate.before(new Date())){
			expireDate = DateUtil.getMaxDate(now);
		}
		expireDate = DateUtil.addMonth(expireDate, ShopRenew.RENEW_MONTHS*4);
		shop.set("expire_date", expireDate);
		int forbiddenStatus = shop.getInt("forbidden_status");
		//判断店铺是否处于到期冻结状态，是则续费完改为正常状态
		if(Shop.FORBIDDEN_STATUS_DISABLE_UNPAY == forbiddenStatus){
			shop.set("forbidden_status", Shop.FORBIDDEN_STATUS_NORMAL);
			shop.update();
			EventKit.postEvent(new ShopUpdateEvent(shop.getStr("id")));//更新索引
		}else{
			shop.update();
		}
		
		//添加续费记录
		ShopRenew renew = new ShopRenew();
		renew.set("shop_id", shopId);
		renew.set("cash", trade.getBigDecimal("cash"));
		renew.set("type", ShopRenew.TYPE_MANUAL);
		renew.set("pay_type", ShopRenew.PAY_TYPE_ONLINE);
		renew.set("audit_status", ShopRenew.AUDIT_STATUS_SUCCESS);
		renew.set("audit_time", now);
		renew.set("expire_time", shop.getDate("expire_date"));
		renew.set("create_time", now);
		renew.set("platform", trade.getStr("platform"));
		renew.set("pay_way", trade.getStr("pay_way"));
		renew.set("third_bill_no", trade.getStr("third_bill_no"));
		renew.save();
		//e趣商城收取续费金额
		BigDecimal rentCash = SysParam.dao.getBigDecimalByCode("efun_shop_renew_cash").multiply(new BigDecimal("4"));
		//获取代理商账号
		Account agentAccount = Account.dao.getAccountForUpdate(shop.getStr("agent_id"), Account.TYPE_AGENT);
		//更新账户余额
		BigDecimal remainCash = agentAccount.getBigDecimal("cash");
		BigDecimal addCash = trade.getBigDecimal("cash").subtract(rentCash);
		agentAccount.set("cash", remainCash.add(addCash));
		agentAccount.update();
		//添加代理商返利记录
		BigDecimal freezeCash = agentAccount.getBigDecimal("freeze_cash");
		AgentCashRecord.dao.add(addCash, agentAccount.getBigDecimal("cash").add(freezeCash), "", AgentCashRecord.REBATE_SHOP_RENT,renew.getInt("id")+"", shop.getStr("agent_id"),AgentCashRecord.REMARK_REBATE_SHOP_RENT);		
	}
	/**
	 * 店铺保证金缴纳
	 * @param trade
	 * @author huangzq
	 */
	private void shopDepositCallBack(Trade trade){
		String shopId = trade.getStr("target");
		Date now = new Date();
	
		//更新店铺缴纳标识
		Shop shop = new Shop();
		shop.set("id", shopId);
		shop.set("is_deposit", BaseConstants.YES);
		shop.update();
		//获取店铺信息
		Record r = Db.findFirst("SELECT s.`no`,s.`name`,a.`no` as agentNo from t_shop s LEFT JOIN t_agent a on s.agent_id = a.id where s.id = ?",shopId);
		//添加保证金记录
		ShopDeposit deposit = new ShopDeposit();
		deposit.set("shop_id", shopId);
		deposit.set("shop_no", r.getStr("no"));
		deposit.set("shop_name", r.getStr("name"));
		deposit.set("agent_no", r.getStr("agentNo"));
		deposit.set("cash", trade.getBigDecimal("cash"));
		deposit.set("type", ShopDeposit.TYPE_PAYMENT);
		deposit.set("pay_type", ShopDeposit.PAY_TYPE_ONLINE);
		deposit.set("audit_status", ShopDeposit.AUDIT_STATUS_SUCCESS);
		deposit.set("audit_time", now);
		deposit.set("create_time", now);
		deposit.set("platform", trade.getStr("platform"));
		deposit.set("pay_way", trade.getStr("pay_way"));
		deposit.set("third_bill_no", trade.getStr("third_bill_no"));
		deposit.save();
		// 店铺保证金缴纳评分
		ShopScore.dao.updateDepositScore(shopId);
		
	}
	/**
	 * 一折购参与回调
	 * @param trade
	 * @author huangzq
	 * 2016年12月29日 下午2:50:35
	 *
	 */
	private void efunOrderCallBack(Trade trade){
		Date now = new Date();
		//更新订单
		String[] orderIds = trade.getStr("target").split(",");
		
		for(String orderId : orderIds){
			
			EfunUserOrder order =  EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
			if(order.getInt("status")==EfunUserOrder.STATUS_NOT_FINISH_PAY){
				Efun efun = Efun.dao.getNewestEfun();				
				order.set("efun_id", efun.getInt("id"));//分配最新期次
				order.set("lottery_time", efun.getDate("lottery_time"));
				order.set("platform", trade.getStr("platform"));
				order.set("pay_way", trade.getStr("pay_way"));
				order.set("third_bill_no", trade.getStr("third_bill_no"));
				order.set("pay_time", now);
				int count = order.getInt("count");
				Ret ret = new Ret();
				ret.put("order", order);
				ret.put("now", now);
				ret.put("efun", efun);
				//判断是否已经释放库存（mq在该订单五分钟内没有支付，会释放锁定库存）
				if(order.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
					//================增加锁定库存==========================//
					//根据配送类型，增加仓库商品锁定库存 或商品锁定库存
					boolean result = ProductSku.dao.addLockCount(order.getStr("sku_code"),count);
					//是否增加锁定库存成功
					if(result){
						this.efunOrderHandleForAddCountSucc(ret);
					}else{
						
						this.efunOrderHandleForAddCountFail(ret);
					}
				
				}else{
					this.efunOrderHandleForAddCountSucc(ret);
				}
				order.update();
			}
		}
		
	}
	/**
	 * 一折购扣库存成功操作
	 * @param ret
	 * @author huangzq
	 * 2016年12月29日 下午5:34:50
	 *
	 */
	private void efunOrderHandleForAddCountSucc(Ret ret){
		EfunUserOrder order = ret.get("order");
		Efun efun = ret.get("efun");
		Date now = ret.get("now");
		int count = order.getInt("count");
		String orderId = order.getStr("id");
		//分配抽奖号码
		List<Integer> numbers = new ArrayList<Integer>();
		for(int i=0;i<count;i++){
			int number = EfunUserOrder.dao.getNumber(efun.get("id"), order.getStr("sku_code"), EfunUserOrder.IS_REAL_YES);
			numbers.add(number);
			EfunOrderDetail detail = new EfunOrderDetail();
			detail.set("efun_order_id", orderId);
			detail.set("number", number);
			detail.set("create_time", now);
			detail.save();
		}
		order.set("status",EfunUserOrder.STATUS_PAIED);
		order.set("number",StringUtil.listToString(",", numbers));
		//重新锁定库存
		order.set("is_release_lock_count", BaseConstants.NO);
		///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
		EfunUserOrder.dao.rebate(order.getStr("user_id"), order.getBigDecimal("price").multiply(new BigDecimal(numbers.size())), orderId);
		//发送mq
		MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
		
	}
	
	/**
	 * 一折购扣库存成功操作
	 * @param ret
	 * @author huangzq
	 * 2016年12月29日 下午5:34:50
	 *
	 */
	private void efunOrderHandleForAddCountFail(Ret ret){
		EfunUserOrder order = ret.get("order");
		Date now = ret.get("now");
		String orderId = order.getStr("id");
		order.set("is_over_sell",BaseConstants.YES);
		BigDecimal zero = new BigDecimal(0);
		//添加退款申请
		EfunRefundApply apply = new EfunRefundApply();
		apply.set("order_id", orderId);
		apply.set("user_id", order.get("user_id"));
		apply.set("integral", order.get("use_integral"));
		apply.set("cost", order.get("cost"));
		apply.set("cash", order.get("cash"));
		if(order.getBigDecimal("cost").compareTo(zero)==1){
			apply.set("status",EfunRefundApply.STATUS_UN_REFUND );//未退款
		}else{
			apply.set("status",EfunRefundApply.STATUS_REFUNDED );//已退款
		}
		apply.set("create_time", now);
		apply.save();
		Account account = Account.dao.getAccountForUpdate(order.getStr("user_id"), Account.TYPE_USER);
		//退回积分
		if(order.getInt("use_integral")>0){
			account.set("integral", account.getInt("integral")+order.getInt("use_integral"));						
			//添加积分记录
			IntegralRecord.dao.add(order.getInt("use_integral"), Math.abs(account.getInt("integral")), IntegralRecord.TYPE_EFUN_REFUND, order.getStr("user_id"), order.getStr("user_name"), "一折购退回积分");
			
		}
		//退回现金
		if(order.getBigDecimal("cash").compareTo(zero)==1){
			account.set("cash", account.getBigDecimal("cash").add(order.getBigDecimal("cash")));
			BigDecimal freezeCashUser = account.getBigDecimal("freeze_cash");
			//添加现金对账单
			UserCashRecord.dao.add(order.getBigDecimal("cash"), account.getBigDecimal("cash").add(freezeCashUser), UserCashRecord.TYPE_EFUN_REFUND, order.getStr("user_id"), "一折购退回现金");
		}
		account.update();
		
	}
	
	
	void shopOpenCallBack(Trade trade) {
		String applyIdStr = trade.getStr("target");					// 开店ID
		Integer applyId = Integer.valueOf(applyIdStr);
		Integer payStatus = trade.getInt("status");					// 支付状态
		
		Date now = new Date();
		
		// 是否邀请开店
		ShopApply apply = ShopApply.dao.getByIdAndLock(applyId);
		String agentId = "";
		String agentNo = apply.getStr("agent_no");
		Shop shop = null;
		if (StringUtil.notNull(agentNo)) {
			// 邀请开店
			shop = ShopAssignment.dao.assign(agentNo);
			agentId = shop.getStr("agent_id");
		} else {
			// 非邀请开店
			shop = ShopAssignment.dao.assign(now);
			agentId = shop.getStr("agent_id");
			agentNo = Agent.dao.getNoByAgentId(agentId);
		}
		
		// 设置店铺
		String initPasswird = RadomUtil.generate(6, RadomUtil.RADOM_NUMBER);
		String password = MD5Builder.getMD5Str(initPasswird);
		shop 
			.set("password",		password)
			.set("status",			Shop.STATUS_TURNOUT_AUDIT)
			.set("open_channel",	Shop.OPEN_CHANNEL_SELF);
		
		// 设置开店
		String shopId = shop.getStr("id");
		String shopNo = shop.getStr("no");
		apply
			.set("pay_status",			payStatus)
			.set("shop_id",				shopId)
			.set("shop_no",				shopNo)
			.set("agent_no",			agentNo);
		
		// 设置有效期
		int monthAmount = apply.getInt("prepay_duration");
		shop.set("expire_date", DateUtil.addMonth(now, monthAmount));
		
		// 操作
		apply.update();
		shop.update();
		
		// 发短信
		if (STATUS_SUCCESS == payStatus) {
			L.info("预缴费 - 支付成功");
			String mobile = apply.getStr("mobile");
			String[] datas1 = new String[] {shopNo, initPasswird};
			Ret source = SMS.dao.initSendSMS(SmsAndMsgTemplate.SMS_SHOP_APPLY_PAY_FINISH, datas1, mobile, "", "开店预缴费");
			EventKit.postEvent(new ApplyUpdateEvent(source));
			
			String agentMobile = Agent.dao.getMobileByAgentId(agentId);
			if (StringUtil.notNull(agentMobile)) {
				String[] datas2 = {shopNo};
				SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_AGNET_TURN_SHOP_PASS, datas2, agentMobile, "", "预缴费自主开店通知", BaseConstants.DataFrom.PC);
				L.info("预缴费通知代理商 - 发短信");
			} else{
				L.info("预缴费通知代理商 - 代理商没有电话");
			}
		} else {
			L.info("预缴费通 - 支付失败");
		}
	}
	
	void shopActivaityCallBack(Trade trade) {
		String activeIdStr = trade.getStr("target");
		Integer activeId = Integer.valueOf(activeIdStr);
		Integer payStatus = trade.getInt("status");
		ShopCertification cer = ShopCertification.dao.getForLock(activeId).set("pay_status", payStatus);
		cer.update();
		
		// 发短信
		if (STATUS_SUCCESS == payStatus) {
			L.info("预缴费 - 支付成功");
			// 发短信
			String agentId = cer.getStr("agent_id");
			// 查询代理商电话发短信
			String agentMobile = Agent.dao.getMobileByAgentId(agentId);
			if (StringUtil.notNull(agentMobile)) {
				String shopNo = cer.getStr("shop_no");
				String[] datas = {shopNo};
				SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_AGNET_TURN_SHOP_PASS, datas, agentMobile, "", "预缴费激活店铺通知", BaseConstants.DataFrom.PC);
				L.info("预缴费通知代理商 - 发短信");
			} else {
				L.info("预缴费通知代理商 - 代理商没有电话");
			}
		} else {
			L.info("预缴费通 - 支付失败");
		}
	}
	
	/**
	 * 验证支付平台
	 */
	public boolean checkPayForm(String payForm) {
		return SysParam.dao.existChildren("pay_form", payForm);
	}
	
	/**
	 * 普通订单扣库存成功操作
	 */
	private void orderHandleForaddCountSucc(Ret ret, String dataFrom){
		Order order = ret.get("order");
		List<String> takeBySelfList = ret.get("takeBySelfList");
		List<String> waitForSendList = ret.get("waitForSendList");
		Date now = ret.get("now");
		Trade trade = ret.get("trade");
		
		order.set("pay_time", now);
		order.set("platform", trade.getStr("platform"));
		order.set("pay_way", trade.getStr("pay_way"));
		order.set("third_bill_no", trade.getStr("third_bill_no"));
		int deliveryType = order.getInt("delivery_type");
		
		//判断是否自提或幸运一折购吃订单
		if(deliveryType==Order.DELIVERY_TYPE_SELF||order.getInt("efun_plus_type")==Order.EFUN_PLUS_EAT){
			// 自提
			String takingCode = StringUtil.getRandomNum(6);
			order.set("taking_code", takingCode);//自提码:随机生成6个数字
			order.set("status", Order.STATUS_HAD_SEND);
			takeBySelfList.add(order.getStr("id"));
			
			// 发短信通知
			String userId = order.get("user_id");
			User user = User.dao.findByIdLoadColumns(userId, "user_name, mobile");
			String mobile = order.get("mobile");
			String userName = user.get("user_name");
			String orderNo = order.get("no");
			String[] data = new String[]{userName, orderNo, takingCode};
			SMS.dao.sendSMS(SmsAndMsgTemplate.SMS_DISCOUNT_EAT_TAKING_CODE, data, mobile, "", "拼折吃自提码通知",6);
		}else{
			// 等待发货
			order.set("status", Order.STATUS_WAIT_FOR_SEND);
			waitForSendList.add(order.getStr("id"));
		}
		order.update();
		//添加订单日志
		OrderLog.dao.add(order.getStr("id"), OrderLog.CODE_ORDER_PAY, dataFrom);
	}
	/**
	 * 普通订单扣库存失败操作
	 */
	private void orderHandleForaddCountFail(Ret ret, String dataFrom){
		Order order = ret.get("order");
		Date now = ret.get("now");
		Trade trade = ret.get("trade");
		
		order.set("pay_time", now);
		order.set("platform", trade.getStr("platform"));
		order.set("pay_way", trade.getStr("pay_way"));
		order.set("third_bill_no", trade.getStr("third_bill_no"));
		if(order.getInt("delivery_type")== Order.DELIVERY_TYPE_EXPRESS){
			// 等待发货
			order.set("status", Order.STATUS_WAIT_FOR_SEND);
		}else{
			// 等待收货
			order.set("status", Order.STATUS_HAD_SEND);
		}
		//超卖
		order.set("is_over_sell", BaseConstants.YES);
		order.update();
		//添加订单日志
		OrderLog.dao.add(order.getStr("id"), OrderLog.CODE_ORDER_PAY, dataFrom);
	}
	
}
