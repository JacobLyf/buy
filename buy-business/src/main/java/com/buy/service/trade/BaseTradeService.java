package com.buy.service.trade;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.solr.common.util.DateUtil;

import com.buy.common.BaseConstants;
import com.buy.common.MqUtil;
import com.buy.common.constants.MqConstants;
import com.buy.model.account.Account;
import com.buy.model.efun.Efun;
import com.buy.model.efun.EfunRefundApply;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.product.ProductSku;
import com.buy.model.trade.Trade;
import com.buy.model.user.UserCashRecord;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.tx.Tx;


/**
 * 交易处理
 * @author huangzq
 *
 */
public class BaseTradeService {
	private static final Logger L = Logger.getLogger(BaseTradeService.class);
	/**
	 * 第三方支付回调处理
	 * @param trade
	 * @author huangzq
	 * @throws SQLException 
	 */
	@Before(Tx.class)
	public void thirdPayCallBack(Ret ret, String dataFrom) throws Exception{
		//商户交易号
		String tradeNo = ret.get("tradeNo");
		//第三方交易号
		String third_bill_no = ret.get("third_bill_no");
		//金额
		BigDecimal totalFee = ret.get("totalFee");
		//计算日期
		String sett_date = ret.get("sett_date");
		//验证卖家账号和交易
		Trade trade = Trade.dao.findByNoForUpdate(tradeNo);		
		
		if(trade!=null&&Trade.dao.checkTradeForCallBack(trade,totalFee)){
			L.info("开始业务处理");
			trade.set("third_bill_no", third_bill_no);
			trade.set("status",Trade.STATUS_SUCCESS);
			if(StringUtil.notNull(sett_date)){
				trade.set("sett_date",sett_date);
			}
			
			if (StringUtil.isNull(dataFrom))
				dataFrom = trade.get("data_from");
			
			Trade.dao.handleTrade(trade, dataFrom);
			L.info("结束业务处理");
		}else{
			L.info("验证不通过，不做业务处理");
		}
	}
	
	
	/**
	 * 一折购参与回调
	 * @param trade
	 * @author huangzq
	 * 2016年12月29日 下午2:50:35
	 *
	 */
	public  void efunOrderCallBack(Trade trade){
		Date now = new Date();
		//更新订单
		String[] orderIds = trade.getStr("target").split(",");
		
		for(String orderId : orderIds){
			EfunUserOrder order =  EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
			if(order.getInt("status")==EfunUserOrder.STATUS_NOT_FINISH_PAY){
				Efun efun = Efun.dao.getNewestEfun();
				//order.set("status", EfunUserOrder.STATUS_WAIT_FOR_SEND);
				order.set("efun_id", efun.getInt("id"));//分配最新期次
				order.set("lottery_time", efun.getDate("lottery_time"));
				order.set("platform", trade.getStr("platform"));
				order.set("pay_way", trade.getStr("pay_way"));
				order.set("third_bill_no", trade.getStr("third_bill_no"));
				order.set("pay_time", now);
				//判断是否已经释放库存（mq在该订单五分钟内没有支付，会释放锁定库存）
				if(order.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
					//================增加锁定库存==========================//
					//根据配送类型，增加仓库商品锁定库存 或商品锁定库存
					boolean result = ProductSku.dao.addLockCount(order.getStr("sku_code"),1);
					//是否增加锁定库存成功
					if(result){
						//分配号码
						order.set("number", EfunUserOrder.dao.getNumber(order.getInt("efun_id"), order.getStr("sku_code"),EfunUserOrder.IS_REAL_YES));
						//重新锁定库存
						order.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_N);
						//更新返利状态
						order.set("rebate_status", EfunUserOrder.REBATE_SUCCESS);
						///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
						EfunUserOrder.dao.rebate(order.getStr("user_id"), order.getBigDecimal("price"), orderId);
						//发送mq
						MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
					}else{
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
				
				}else{
					//分配号码
					order.set("number", EfunUserOrder.dao.getNumber(order.getInt("efun_id"), order.getStr("sku_code"),EfunUserOrder.IS_REAL_YES));
					//重新锁定库存
					order.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_N);
					//更新返利状态
					order.set("rebate_status", EfunUserOrder.REBATE_SUCCESS);
					///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
					EfunUserOrder.dao.rebate(order.getStr("user_id"), order.getBigDecimal("price"), orderId);
					//发送mq
					MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
				}
		
			
				order.update();
			}
		}
		
	}



}
