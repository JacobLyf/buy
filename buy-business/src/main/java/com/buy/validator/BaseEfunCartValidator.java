package com.buy.validator;

import com.buy.common.Validator;
import com.buy.string.StringUtil;
import com.jfinal.core.Controller;

public class BaseEfunCartValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		String method = this.getActionMethod().getName();
		//加入购物车
		if ("add".equals(method)) {
			this.validateRequired("skuCode");
			this.validateInteger("proNum");
		//更新购物车	
		}else if ("updateCount".equals(method)){
			this.validateInteger("cartId","cartCount");
		//删除购物车
		}else if("delete".equals(method)){
			//非空
			String[] cartIdsStr = c.getParaValues("cartIds");
			if (cartIdsStr == null) {// 兼容苹果数组获取数据方式
				cartIdsStr = c.getParaValues("cartIds[]");
			}
			if(null == cartIdsStr||cartIdsStr.length<=0){
				addError("-3","cartIds参数不能为空");
			}
			
			for(String cartId : cartIdsStr){
				if(!StringUtil.isCount(cartId)){
					addError("-2","参数类型错误");
				}
			}
		//移入收藏夹
		}else if("moveToCollection".equals(method)){
			//非空
			String[] cartIdsStr = c.getParaValues("cartIds");
			if (cartIdsStr == null) {// 兼容苹果数组获取数据方式
				cartIdsStr = c.getParaValues("cartIds[]");
			}
			if(null == cartIdsStr||cartIdsStr.length<=0){
				addError("-3","cartIds参数不能为空");
			}
			for(String cartId : cartIdsStr){
				if(!StringUtil.isCount(cartId)){
					addError("-2","参数类型错误");
				}
			}
		}
	}

	@Override
	protected void handleError(Controller c) {
		returnJson();
	}

}
