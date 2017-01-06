package com.buy.validator;

import java.math.BigDecimal;

import com.buy.common.Validator;
import com.buy.model.order.Order;
import com.buy.model.store.Store;
import com.buy.string.StringUtil;
import com.jfinal.core.Controller;

public class BaseOrderValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		
		String method = this.getActionMethod().getName();

		 if ("submitOrderFromPrize".equals(method)) {
			validateRequiredArray("prizeEfunOrderId"); // 一折购中奖订单Id列表.
			validateInteger("useIntegral"); // 使用积分.
			validateMoney("useCash"); // 使用余额.
			validateInteger("deliveryType"); // 运输方式.
			int deliveryType = c.getParaToInt("deliveryType");
			String payPassword = c.getPara("payPassword"); // 支付密码.
			Integer addressId = c.getParaToInt("addressId"); // 地址Id.

			/******************** 云店自提信息 ********************/
			String o2oShopNo = c.getPara("o2oShopNo"); // 云店编号.
			String contact = c.getPara("contact"); // 联系人.
			String mobile = c.getPara("mobile"); // 手机号.

			if (deliveryType != 1 && deliveryType != 2) {
				addError("1", "运输方式不能为空!");
			}

			if (deliveryType == Order.DELIVERY_TYPE_EXPRESS) { // 快递.
				if (addressId == null) {
					addError("2", "收货地址Id不能为空!");
				}
			} else if (deliveryType == Order.DELIVERY_TYPE_SELF) { // 自提.
				if (StringUtil.isBlank(o2oShopNo)) {
					addError("3", "自提云店编号不能为空!");
				} else {
					Store store = Store.dao.getStoreByNo(o2oShopNo);
					if (null == store) {
						addError("4", "无效的自提云店编号!");
					}
				}
				if (StringUtil.isBlank(contact)) {
					addError("5", "自提联系人不能为空!");
				}
				if (StringUtil.isBlank(mobile)) {
					addError("6", "自提手机号码不能为空!");
				} else {
					validateMobile("mobile", "7", "输入的手机号码格式不正确!");
				}
			}

			if (c.getParaToInt("useIntegral").intValue() > 0 || new BigDecimal(c.getPara("useCash")).signum() == 1) {
				if (StringUtil.isBlank(payPassword)) {
					addError("8", "支付密码不能为空!");
				}
			}
			
		//在线支付
		}else if(method.equals("onlinePayment")){
			
			validateRequiredArray("orderIds"); 
			validateRequired("payType");
			
		//锁定库存	
		}else if(method.equals("addLockCount")){
			validateRequiredArray("orderIds"); 
		}

	}

	@Override
	protected void handleError(Controller c) {
		this.returnJson();
	}

}
