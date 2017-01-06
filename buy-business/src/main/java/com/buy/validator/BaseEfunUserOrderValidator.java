package com.buy.validator;

import java.math.BigDecimal;

import com.buy.common.JsonMessage;
import com.buy.common.Validator;
import com.buy.model.user.User;
import com.jfinal.core.Controller;

public class BaseEfunUserOrderValidator extends Validator {
	

	protected void validate(Controller c) {
		String method = this.getActionMethod().getName();
		//立即购买提交订单
		if("submitFromSku".equals(method)){
			//初始化使用金额,积分
			String useCashStr = "0";
			Integer useIntegral = 0;
			if(c.isParaExists("useCash")){
				this.validateMoney("useCash");
				useCashStr = c.getPara("useCash");
			}
			
			if(c.isParaExists("useIntegral")){
				this.validateInteger("useIntegral");
				useIntegral = c.getParaToInt("useIntegral");
			}
			
			this.validateInteger("count");
			this.validateRequired("skuCode");
			BigDecimal useCash = new BigDecimal(useCashStr);
			//有使用积分或现金
			if(useCash.compareTo(new BigDecimal(0))==1||useIntegral>0){
				this.validateRequired("payPassword");
				String payPassword = c.getPara("payPassword");
				if(!payPassword.equals(User.dao.findByIdLoadColumns(this.getCurrentUserId(), "pay_password").getStr("pay_password"))){
					this.addError("1", "支付密码错误");
				}
			}
			this.validateToken(JsonMessage.RESUBMIT_ERROR, "请勿重复提交");
			
			
		//购物车提交订单
		}else if("submitFromCart".equals(method)){
			//初始化使用金额,积分
			String useCashStr = "0";
			Integer useIntegral = 0;
			if(c.isParaExists("useCash")){
				this.validateMoney("useCash");
				useCashStr = c.getPara("useCash");
			}
			
			if(c.isParaExists("useIntegral")){
				this.validateInteger("useIntegral");
				useIntegral = c.getParaToInt("useIntegral");
			}
			validateIntArray("cartIds");
			BigDecimal useCash = new BigDecimal(useCashStr);
			//有使用积分或现金
			if(useCash.compareTo(new BigDecimal(0))==1||useIntegral>0){
				this.validateRequired("payPassword");
				String payPassword = c.getPara("payPassword");
				if(!payPassword.equals(User.dao.findByIdLoadColumns(this.getCurrentUserId(), "pay_password").getStr("pay_password"))){
					this.addError("1", "支付密码错误");
				}
			}
			
			
			this.validateToken(JsonMessage.RESUBMIT_ERROR, "请勿重复提交");
			
		//计算立即参与金额	
		}else if("calculateFromSku".equals(method)){
			this.validateInteger("count");
			this.validateRequired("skuCode");
			
		//计算购物车结算的金额
		}else if("calculateFromCart".equals(method)){

			this.validateIntArray("cartIds");
			
		//使用余额支付	
		}else if ("payWithCash".equals(method)){
			//初始化使用金额,积分
			String useCashStr = "0";
			Integer useIntegral = 0;
			if(c.isParaExists("useCash")){
				this.validateMoney("useCash");
				useCashStr = c.getPara("useCash");
			}
			
			if(c.isParaExists("useIntegral")){
				this.validateInteger("useIntegral");
				useIntegral = c.getParaToInt("useIntegral");
			}
			
			this.validateRequired("orderId");
		
			this.validateRequired("payPassword");
			String payPassword = c.getPara("payPassword");
			if(!payPassword.equals(User.dao.findByIdLoadColumns(this.getCurrentUserId(), "pay_password").getStr("pay_password"))){
				this.addError("1", "支付密码错误");
			}
			
			//this.validateToken(JsonMessage.RESUBMIT_ERROR, "请勿重复提交");
		}
		
		
	} 

	protected void handleError(Controller c) {
		this.returnJson();
	}

}
