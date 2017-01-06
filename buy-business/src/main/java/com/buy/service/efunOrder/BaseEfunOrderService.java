package com.buy.service.efunOrder;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.common.MqUtil;
import com.buy.common.Ret;
import com.buy.common.constants.MqConstants;
import com.buy.model.account.Account;
import com.buy.model.efun.*;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.order.Order;
import com.buy.model.product.ProBackSort;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.store.Store;
import com.buy.model.store.StoreSkuMap;
import com.buy.model.supplier.Supplier;
import com.buy.model.user.RecAddress;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;
import com.buy.plugin.event.efun.EfunO2OTakingEvent;
import com.buy.service.cart.BaseEfunCartService;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Duang;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.dreamlu.event.EventKit;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * 幸运一折购参与订单公共service
 * @author chenhg
 */
public class BaseEfunOrderService {
	public BaseEfunCartService baseEfunOrderService = new  BaseEfunCartService();
	/**
	 * 幸运一折购参与，未支付完成，跳转到第三方支付，点击确认付款增加锁定库存
	 * @return
	 * @author chenhg
	 * 2016年11月18日 下午7:29:23
	 * @throws SQLException 
	 */
	@Before(Tx.class)
	public JsonMessage comfirmPayAddLockCount(String userId,String... orderIds) throws SQLException{
		JsonMessage jsonMessage = new JsonMessage();
		//组装数据
		List<Record> skuList = new ArrayList<Record>();
		Map<String,Record> skuMap = new HashMap<String, Record>();
		for(String orderId : orderIds){
			String sql = "select r.sku_code from t_efun_user_order r where r.status = ? and r.user_id = ? and r.id = ?";
			Record order  = Db.findFirst(sql,EfunUserOrder.STATUS_NOT_FINISH_PAY,userId,orderId);
			if(order == null){
				jsonMessage.setStatusAndMsg("1", "订单不存在");
				return jsonMessage;
			}
			Record sku = ProductSku.dao.getSkuForSubmitOrder(order.getStr("sku_code"));	
			skuList.add(sku);
			skuMap.put(orderId, sku);
		}
		//检查商品商家状态
		jsonMessage = ProductSku.dao.checkSku(skuList.toArray( new Record[]{}));
		if(jsonMessage.getStatus()!="0"){
			jsonMessage.setStatusAndMsg("2", "部分商品已下架");
			return jsonMessage;
		}
		//库存不足的商品
		List<Record> noStoreSkus = new ArrayList<Record>();
		//锁定库存成功的订单
		List<String> succesOrderIds = new ArrayList<String>();
		for(String orderId : orderIds){
			EfunUserOrder order = EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
			//是否加了锁定库存
			if(order.getInt("is_release_lock_count") == BaseConstants.YES){
				String skuCode = order.getStr("sku_code");
				Integer count  = order.getInt("count");
				boolean flag = ProductSku.dao.addLockCount(skuCode, count);
				if(flag==false){
					Record sku = skuMap.get(orderId);
					sku.keep("proName","properties","storeCount");
					noStoreSkus.add(sku);
				}else{
					succesOrderIds.add(orderId);
					order.set("is_release_lock_count", BaseConstants.NO);
					order.update();
				}
			}else{
				succesOrderIds.add(orderId);
			}
		}
		//存在库存不足的订单
		if(StringUtil.notNull(noStoreSkus)){
			//回滚
			DbKit.getConfig().getConnection().rollback();
			jsonMessage.setData(noStoreSkus);
			jsonMessage.setStatusAndMsg("3", "部分商品库存不足");
			return jsonMessage;
		}
		
		//发送mq锁定库存五分钟后释放。
		for(String orderId : succesOrderIds){
			MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_PAY_STORE_DELAY, orderId);
		}
		
		return jsonMessage;
	}
	
	/**
	 * 检查当前会员
	 * 1、是否已九折购买了该期次该幸运一折购商品
	 * 2、是否已经释放库存，如果释放了，需要验证库存是否充足
	 * @param userId
	 * @param efunOrderId
	 * @param checkCountFlag:true:验证库存；false:不验证库存
	 * @return
	 * @author chenhg
	 * 2016年11月19日 下午4:40:17
	 */
	public JsonMessage checkNineBuy(String userId,String efunOrderId, boolean checkCountFlag){
		JsonMessage jm = new JsonMessage();
		
		String sql = " SELECT sku_code,is_release_lock_count,is_efun_nine FROM t_efun_user_order WHERE id = ? AND user_id = ? ";
		Record euo = Db.findFirst(sql.toString(), efunOrderId, userId);
		if(euo.getInt("is_efun_nine") == BaseConstants.YES){
			jm.setStatusAndMsg("1", "您已通过幸运折扣购买了该期次该幸运一折购商品，请到我的订单查看。");
		}
		
		if(checkCountFlag){
			//库存释放了后要判断库存是否充足
			if(euo.getInt("is_release_lock_count") ==  EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
				if(!ProductSku.dao.enoughCount(euo.getStr("sku_code"), 1)){
					jm.setStatusAndMsg("2", "库存不足");
					jm.setData(ProductSku.dao.getSkuInventoryMessage(euo.getStr("sku_code")));
				}
			}
		}
		
		return jm;
	}
	
	/**
	 * 会员继续付款参与
	 * 1、判断库存是否充足（如果释放了库存）
	 * 2、判断是否已经支付
	 * @param userId
	 * @param efunOrderId
	 * @return
	 * @author chenhg
	 * 2016年11月20日 上午11:17:10
	 */
	public JsonMessage checkForAttend(String userId,String efunOrderId){
		JsonMessage jm = new JsonMessage();
		
		String sql = " SELECT sku_code,is_release_lock_count,status FROM t_efun_user_order WHERE id = ? AND user_id = ? ";
		Record euo = Db.findFirst(sql.toString(), efunOrderId, userId);
		if(euo.getInt("status") != EfunUserOrder.STATUS_NOT_FINISH_PAY){
			return jm.setStatusAndMsg("1", "该参与记录非待支付状态，请刷新后再操作");
		}
		
		String skuCode = euo.getStr("sku_code");
		
		//判断sku是否为幸运一折购商品
		boolean isEfunSkuExist = EfunSku.dao.isEfunSkuExist(skuCode);
		if(!isEfunSkuExist){
			return jm.setStatusAndMsg("3", "该商品已经退出幸运一折购");
		}
		
		//库存释放了后要判断库存是否充足
		if(euo.getInt("is_release_lock_count") ==  EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
			if(!ProductSku.dao.enoughCount(skuCode, 1)){
				jm.setStatusAndMsg("2", "库存不足");
				jm.setData(ProductSku.dao.getSkuInventoryMessage(skuCode));
				return jm;
			}
		}
		return jm;
	}
	
	
	/**
	 * 中奖后：选择自提
	 * @param ret
	 * @return
	 * @author chenhg
	 * 2016年11月22日 下午4:19:00
	 * @throws SQLException 
	 */
	@Before(Tx.class)
	public JsonMessage saveDeliveryTypeSelf(Ret ret) throws SQLException{
		JsonMessage jsonMessage = new JsonMessage();
		String orderId = ret.get("orderId");
		String userId = ret.get("userId");
		String o2oShopNo = ret.get("o2oShopNo");
		EfunUserOrder efunOrder = EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
		int efunId = efunOrder.getInt("efun_id");
		String skuCode = efunOrder.getStr("sku_code");
		boolean isWinPro = EfunUserOrder.dao.userIsWin(efunId,orderId,userId);
		if(isWinPro){
			if(StringUtil.isNull(efunOrder.getStr("taking_code")) && StringUtil.isNull(efunOrder.getStr("o2o_shop_no"))){
				boolean flag = true;
				//增加仓库锁定库存
				if(efunOrder.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
					if(!ProductSku.dao.addStoreLockCount(o2oShopNo, skuCode, 1)){
						flag = false;
					}
				}else{
					if(!ProductSku.dao.transferLockCount(o2oShopNo, skuCode, 1)){
						flag = false;
					}
				}
				if(flag){
					efunOrder.set("delivery_type",EfunUserOrder.DELIVERY_TYPE_SELF);
					efunOrder.set("user_name",ret.get("userName"));
					efunOrder.set("o2o_shop_no",o2oShopNo);
					efunOrder.set("o2o_shop_name",ret.get("o2oShopName"));
					efunOrder.set("o2o_shop_address",ret.get("o2oShopAddr"));
					efunOrder.set("status", Order.STATUS_HAD_SEND);//待收货
					efunOrder.set("taking_code", StringUtil.getRandomNum(6));//自提码
					efunOrder.update();
					/**发送自提短信驱动事件**/
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("orderId",orderId);
					map.put("APP", BaseConstants.DataFrom.PC);
					EventKit.postEvent(new EfunO2OTakingEvent(map));
					/**发送自提短信驱动事件**/
				}else{
					//事务回滚
					DbKit.getConfig().getConnection().rollback();
					jsonMessage.setStatusAndMsg("97", "库存不足");
				}
			}else{
				jsonMessage.setStatusAndMsg("-7", "请勿重复操作");
			}
		}else{
			jsonMessage.setStatusAndMsg("-8", "非法操作");
		}
		
		return jsonMessage;
	}
	
	
	/**
	 * 中奖后：选择快递
	 * @param ret
	 * @return
	 * @author chenhg
	 * 2016年11月22日 下午4:19:00
	 * @throws SQLException 
	 */
	@Before(Tx.class)
	public JsonMessage saveDeliveryTypeExpress(Ret ret){
		JsonMessage jsonMessage = new JsonMessage();
		String orderId = ret.get("orderId");
		String userId = ret.get("userId");
		
		EfunUserOrder efunOrder = EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
        
		boolean isWinPro = EfunUserOrder.dao.userIsWin(efunOrder.getInt("efun_id"),orderId,userId);
		if(isWinPro) {
			//24小时未选择配送方式的中奖订单会被释放锁定库存
			if(efunOrder.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y){
				if(!ProductSku.dao.addLockCount(efunOrder.getStr("sku_code"), 1)){
					jsonMessage.setStatusAndMsg("97", "库存不足");
				}
			}
			efunOrder.set("delivery_type", EfunUserOrder.DELIVERY_TYPE_EXPRESS);
			efunOrder.update();
			int efunId = efunOrder.getInt("efun_id");
			String skuCode = efunOrder.getStr("sku_code");
			/**发送提醒发货短信驱动事件**/
			if(efunOrder.getInt("is_pay_freight") == BaseConstants.YES){
				EfunUserOrder.dao.efunWinNoticeMerchant(skuCode,Order.DATA_FROM_APP,efunId,EfunUserOrder.EFUN_WIN_NOTICE_TWO);
			}
			/**发送提醒发货短信驱动事件**/
		}else{
			jsonMessage.setStatusAndMsg("-8", "非法操作");
		}
		
		return jsonMessage;
	}
	
	
	/**
	 * 判断发货
	 * @param efunUserOrderId
	 * @return
	 * @author chenhg
	 * 2016年11月24日 上午9:30:55
	 */
	public JsonMessage checkCountForSend(String efunUserOrderId){
		JsonMessage jsonMessage = new JsonMessage();
		EfunUserOrder euo = EfunUserOrder.dao.findById(efunUserOrderId);
		String skuCode = euo.getStr("sku_code");
		String o2o_shop_no = euo.getStr("o2o_shop_no");
		boolean flag = false;
		//商城发货
		if(StringUtil.notNull(o2o_shop_no)){
			if(!StoreSkuMap.dao.enoughStoreCountForSend(o2o_shop_no, skuCode, 1)){
				flag = true;
			}
		}else{
			if(!ProductSku.dao.enoughVirtualCountForSend(skuCode, 1)){
				flag = true;
			}
		}
		
		//库存不足
		if(flag){
			jsonMessage.setData(ProductSku.dao.findByIdLoadColumns(skuCode, "code,product_id"));
			jsonMessage.setStatusAndMsg("1", "库存不足！请补充库存后再发货。");
		}
		
		return jsonMessage;
	}
	
	
	
	/**
	 * 购物车提交一折购订单
	 * @param ret
	 * @param userId
	 * @return
	 * @throws Exception
	 * @author huangzq
	 * 2016年12月16日 下午3:23:14
	 *
	 */
	@Before(Tx.class)
	public JsonMessage submitFromCart(Ret ret,String userId) throws Exception{
		JsonMessage jsonMessage = new JsonMessage();
		Date now = new Date();
		BigDecimal zero = new BigDecimal(0);
		//使用金额
		BigDecimal useCash = ret.getBigDecimal("useCash");
		if(useCash==null){
			useCash = zero;
		}
		//使用积分
		BigDecimal useIntegral = ret.getBigDecimal("useIntegral");
		if(useIntegral==null){
			useIntegral = zero;
		}
		//一折购物车id
		Integer[] cartIds  = ret.get("cartIds");
		//来源
		String dataFrom = ret.getStr("dataFrom");
		
		
		
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		Integer integral = account.getInt("integral");
		BigDecimal cash = account.getBigDecimal("cash");
		//支付现金（使用金额+使用积分/100）
		BigDecimal payCash = useCash.add(useIntegral.divide(new BigDecimal(100)));
		
		//判断使用积分是否小于0
		if(useIntegral.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断使用现金是否小于0
		if(useCash.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断使用积分是否大于会员账户积分余额
		if(useIntegral.intValue() > integral){
			jsonMessage.setStatusAndMsg("2", "使用积分不能大于账户剩余积分！");
			return jsonMessage;
		}
		//判断使用现金是否大于会员账户现金余额
		if(useCash.compareTo(cash) == 1){
			jsonMessage.setStatusAndMsg("3", "使用现金不能大于账户剩余现金！");
			return jsonMessage;
		}
	
		List<Record> cartList = EfunCart.dao.getCartForOrder(cartIds, userId);
		//检查购物车商品状态
		jsonMessage = ProductSku.dao.checkSku(cartList.toArray( new Record[]{}));
		if(!jsonMessage.getStatus().equals("0")){
			return jsonMessage;
		}
		//应付总额
		BigDecimal total = zero;
		List<Record> noStoreSkus = new ArrayList<Record>();
		//判断库存是否充足
		for(Record cart : cartList){
			total = total.add(cart.getBigDecimal("efunPrice").multiply(new BigDecimal(cart.getInt("count"))));
			//库存不足，记录不足商品
			if((cart.getLong("storeCount")<cart.getInt("count"))){
				cart.keep("proName","properties","storeCount");
				noStoreSkus.add(cart);
			}
			
		}
		total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
		//库存不足，直接返回
		if(StringUtil.notNull(noStoreSkus)){
			jsonMessage.setData(noStoreSkus);
			jsonMessage.setStatusAndMsg("5", "部分商品库存不足");
			return jsonMessage;
			
		}

		//获取最新期次
		Efun efun = Efun.dao.getNewestEfun();
		//需要第三方支付订单id
		List<String> needPayorderIds = new ArrayList<String>();
		//支付成功的订单id
		List<String> orderIds = new ArrayList<String>();
		//生成订单
		for(Record cart : cartList){
			//订单总额
			BigDecimal orderTotal = cart.getBigDecimal("efunPrice").multiply(new BigDecimal(cart.getInt("count")));
			//订单使用现金
			BigDecimal orderCash = zero;
			//订单使用积分
			BigDecimal orderUseIntegral = zero;
			//剩余应付金额（循环使用）
			BigDecimal restCash = orderTotal;
			
			//优先积分抵扣
			if(useIntegral.compareTo(zero)>0){
				//使用积分大于订单总额
				if(useIntegral.compareTo(restCash.multiply(new BigDecimal(100)))>0){
					orderUseIntegral = restCash.multiply(new BigDecimal(100));
					useIntegral = useIntegral.subtract(restCash.multiply(new BigDecimal(100)));
					restCash = zero;
				}else{
					orderUseIntegral = useIntegral;
					restCash = orderTotal.subtract(useIntegral.divide(new BigDecimal(100)));
					useIntegral = zero;
				}
			}
			//现金抵扣
			if(restCash.compareTo(zero)>0&&useCash.compareTo(zero)>0){
				
				if(useCash.compareTo(restCash)>0){
					orderCash = restCash;
					useCash = useCash.subtract(orderCash);
					restCash = zero;
				}else{
					orderCash = useCash;
					restCash = restCash.subtract(useCash);
					useCash = zero;
				}
				
			}
			if(restCash.compareTo(zero)==0){
				boolean flag = ProductSku.dao.addLockCount(cart.getStr("sku_code"), cart.getInt("count"));
				//库存不足，记录不足商品
				if(flag==false){
					int storeCount = ProductSku.dao.getCount(cart.getStr("sku_code"));
					cart.keep("productName");
					cart.set("storeCount", storeCount);
					noStoreSkus.add(cart);
					break;
				}
				
			}
			//扣库存不足，返回
			if(StringUtil.notNull(noStoreSkus)){
				break;			
			}	
			//生成参与记录
			EfunUserOrder efunUserOrder = new EfunUserOrder();
			//生成订单编号
			String orderNo = StringUtil.getUnitCode(EfunUserOrder.NO_PREFIX);
			efunUserOrder.set("id", StringUtil.getUUID());
			efunUserOrder.set("no", orderNo);
			efunUserOrder.set("order_shop_id", cart.getStr("order_shop_id"));
			efunUserOrder.set("efun_id", efun.get("id"));
			efunUserOrder.set("lottery_time", efun.get("lottery_time"));
			efunUserOrder.set("user_id", userId);
			efunUserOrder.set("product_id", cart.get("productId"));
			efunUserOrder.set("sku_code", cart.get("sku_code"));
			efunUserOrder.set("product_name", cart.get("proName"));
			efunUserOrder.set("product_property", cart.get("properties"));
			efunUserOrder.set("product_img", cart.get("productImg"));
			efunUserOrder.set("eq_price", cart.get("eqPrice"));
			efunUserOrder.set("price", cart.get("efunPrice"));
			efunUserOrder.set("count", cart.getInt("count"));
			efunUserOrder.set("create_time", now);
			//根据来源计算结算价,填充商家信息
			int proSource = cart.getInt("proSource");
			efunUserOrder.set("order_type",proSource);
			if(proSource==Product.SOURCE_EXCLUSIVE||proSource==Product.SOURCE_SELF_EXCLUSIVE){
				BigDecimal rate = ProBackSort.dao.getCommissionRate(cart.get("productId"));
				efunUserOrder.set("supplier_price", cart.getBigDecimal("eqPrice").multiply(new BigDecimal(1).subtract(rate)));
				efunUserOrder.set("merchant_id",cart.getStr("shopId"));
				efunUserOrder.set("merchant_no",cart.getStr("shopNo"));
				efunUserOrder.set("merchant_name",cart.getStr("shopName"));
			}else{
				efunUserOrder.set("supplier_price", cart.get("supplierPrice"));
				efunUserOrder.set("merchant_id",cart.getStr("supplierId"));
				efunUserOrder.set("merchant_no",cart.getStr("supplierNo"));
				efunUserOrder.set("merchant_name",cart.getStr("supplierName"));
			}
			efunUserOrder.set("total", orderTotal);
			efunUserOrder.set("cash", orderCash);
			efunUserOrder.set("use_integral", orderUseIntegral);
			
			String userName = User.dao.getUserName(userId);
			efunUserOrder.set("user_name",userName);
			efunUserOrder.set("data_from",dataFrom);
			//抽奖号码
			List<Integer> numbers = new ArrayList<Integer>();
			//订单已支付完成
			if(restCash.compareTo(zero)==0){
				
				for(int i=0;i<cart.getInt("count");i++){
					int number = EfunUserOrder.dao.getNumber(efun.get("id"), cart.getStr("sku_code"), EfunUserOrder.IS_REAL_YES);
					numbers.add(number);
				}
				efunUserOrder.set("status",EfunUserOrder.STATUS_PAIED);
				efunUserOrder.set("number",StringUtil.listToString(",", numbers));
				efunUserOrder.set("pay_time",now);
			}else{
				efunUserOrder.set("cost",restCash);
			}
			efunUserOrder.save();
			//生成明细
			if(StringUtil.notNull(numbers)){
				for(Integer n : numbers){
					EfunOrderDetail.dao.add(efunUserOrder.getStr("id"), n,efun.getDate("lottery_time"));
				}
				///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
				EfunUserOrder.dao.rebate(userId, efunUserOrder.getBigDecimal("price").multiply(new BigDecimal(numbers.size())), efunUserOrder.getStr("id"));
				
				orderIds.add(efunUserOrder.getStr("id"));
			}else{
				needPayorderIds.add(efunUserOrder.getStr("id"));
			}
			
			//扣取账户现金
			if(orderCash.compareTo(zero)>0){
				account.set("cash", account.getBigDecimal("cash").subtract(orderCash));
				account.update();
				//备注
				String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
				UserCashRecord.dao.add(orderCash.multiply(new BigDecimal(-1)), account.getBigDecimal("cash"), UserCashRecord.TYPE_EUN_ORDER, userId, remark);
			}
			//扣取账户积分
			if(orderUseIntegral.compareTo(zero)>0){
				account.set("integral", account.getInt("integral")-orderUseIntegral.intValue());
				account.update();
				//备注
				String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
				IntegralRecord.dao.add(orderUseIntegral.multiply(new BigDecimal(-1)).intValue(), account.getInt("integral"), IntegralRecord.TYPE_EFUN_ORDER, userId, userName, remark);
			}
			
			
			
		}
		//扣库存不足，回滚返回
		if(StringUtil.notNull(noStoreSkus)){
			DbKit.getConfig().getConnection().rollback();
			jsonMessage.setData(noStoreSkus);
			jsonMessage.setStatusAndMsg("5", "部分商品库存不足");
			return jsonMessage;
			
		}	
		//使用金额不足，需第三方支付
		if(payCash.compareTo(total)<0){
			//删除购物车
			baseEfunOrderService.batchDelete(cartIds, userId);
			jsonMessage.setData(needPayorderIds);
			jsonMessage.setStatusAndMsg("6", "未完成支付，需跳往第三方支付");
			return jsonMessage;
		}
		
		jsonMessage.setData(orderIds);
		//删除购物车
		baseEfunOrderService.batchDelete(cartIds, userId);
		//支付成功,发送mq锁定库存，2小时后释放
		if(jsonMessage.getStatus().equals("0")){
			for(String orderId : orderIds){
				MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
			}
		}
		
		
		return jsonMessage;
	}
	
	
	
	/**
	 * 购物车提交一折购订单
	 * @param ret
	 * @param userId
	 * @return
	 * @throws Exception
	 * @author huangzq
	 * 2016年12月16日 下午3:23:14
	 *
	 */
	@Before(Tx.class)
	public JsonMessage submitFromSku(Ret ret,String userId) throws Exception{
		JsonMessage jsonMessage = new JsonMessage();
		Date now = new Date();
		BigDecimal zero = new BigDecimal(0);
		//使用金额
		BigDecimal useCash = ret.getBigDecimal("useCash");
		if(useCash==null){
			useCash = zero;
		}
		//使用积分
		BigDecimal useIntegral = ret.getBigDecimal("useIntegral");
		if(useIntegral==null){
			useIntegral = zero;
		}
		//skuCode
		String skuCode  = ret.getStr("skuCode");
		//数量
		Integer count  = ret.getInt("count");
		//来源
		String dataFrom = ret.getStr("dataFrom");
		//下单店铺id
		String orderShopId = ret.get("orderShopId");
		
		
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		Integer integral = account.getInt("integral");
		BigDecimal cash = account.getBigDecimal("cash");
		//判断使用积分是否小于0
		if(useIntegral.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断使用现金是否小于0
		if(useCash.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断购买数量是否大于0
		if(count<1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		
		//支付现金（使用金额+使用积分/100）
		BigDecimal payCash = useCash.add(useIntegral.divide(new BigDecimal(100)));
		
		//判断使用积分是否大于会员账户积分余额
		if(useIntegral.intValue() > integral){
			jsonMessage.setStatusAndMsg("2", "使用积分不能大于账户剩余积分");
			return jsonMessage;
		}
		//判断使用现金是否大于会员账户现金余额
		if(useCash.compareTo(cash) == 1){
			jsonMessage.setStatusAndMsg("3", "使用现金不能大于账户剩余现金");
			return jsonMessage;
		}
	
		Record sku = ProductSku.dao.getSkuForSubmitOrder(skuCode);
		//检查商品状态
		jsonMessage = ProductSku.dao.checkSku(sku);
		if(!jsonMessage.getStatus().equals("0")){
			return jsonMessage;
		}
		
		//判断库存是否充足
		if((sku.getLong("storeCount")<count)){
			sku.keep("proName","properties","storeCount");
			jsonMessage.setData(sku);
			jsonMessage.setStatusAndMsg("5", "商品库存不足");
			return jsonMessage;
		}
		
		//获取最新期次
		Efun efun = Efun.dao.getNewestEfun();
		
		//订单总额
		BigDecimal orderTotal = sku.getBigDecimal("efunPrice").multiply(new BigDecimal(count)).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		//订单已支付完成,扣库存
		if(payCash.compareTo(orderTotal)==0){
			boolean flag = ProductSku.dao.addLockCount(skuCode, count);
			//库存不足，记录不足商品
			if(flag==false){
				int storeCount = ProductSku.dao.getCount(skuCode);
				sku.keep("proName","properties");
				sku.set("storeCount", storeCount);
				jsonMessage.setData(sku);
				jsonMessage.setStatusAndMsg("5", "商品库存不足");
				return jsonMessage;
			}
		}else if(payCash.compareTo(orderTotal)==1){
			jsonMessage.dataException();
			return jsonMessage;
			
		}
		//生成参与记录
		EfunUserOrder efunUserOrder = new EfunUserOrder();
		//生成订单编号
		String orderNo = StringUtil.getUnitCode(EfunUserOrder.NO_PREFIX);
		efunUserOrder.set("id", StringUtil.getUUID());
		efunUserOrder.set("no", orderNo);
		efunUserOrder.set("order_shop_id", orderShopId);
		efunUserOrder.set("efun_id", efun.get("id"));
		efunUserOrder.set("lottery_time", efun.get("lottery_time"));
		efunUserOrder.set("user_id", userId);
		efunUserOrder.set("product_id", sku.get("productId"));
		efunUserOrder.set("sku_code", skuCode);
		efunUserOrder.set("product_name", sku.get("proName"));
		efunUserOrder.set("product_property", sku.get("properties"));
		efunUserOrder.set("product_img", sku.get("productImg"));
		efunUserOrder.set("eq_price", sku.get("eqPrice"));
		efunUserOrder.set("price", sku.get("efunPrice"));
		efunUserOrder.set("count", count);
		efunUserOrder.set("create_time", now);
		//根据来源计算结算价,填充商家信息
		int proSource = sku.getInt("proSource");
		efunUserOrder.set("order_type",proSource);
		if(proSource==Product.SOURCE_EXCLUSIVE||proSource==Product.SOURCE_SELF_EXCLUSIVE){
			BigDecimal rate = ProBackSort.dao.getCommissionRate(sku.get("productId"));
			efunUserOrder.set("supplier_price", sku.getBigDecimal("eqPrice").multiply(new BigDecimal(1).subtract(rate)));
			efunUserOrder.set("merchant_id",sku.getStr("shopId"));
			efunUserOrder.set("merchant_no",sku.getStr("shopNo"));
			efunUserOrder.set("merchant_name",sku.getStr("shopName"));
		}else{
			efunUserOrder.set("supplier_price", sku.get("supplierPrice"));
			efunUserOrder.set("merchant_id",sku.getStr("supplierId"));
			efunUserOrder.set("merchant_no",sku.getStr("supplierNo"));
			efunUserOrder.set("merchant_name",sku.getStr("supplierName"));
		}
		efunUserOrder.set("total", orderTotal);
		efunUserOrder.set("cash", useCash);
		efunUserOrder.set("use_integral", useIntegral);
		
		String userName = User.dao.getUserName(userId);
		efunUserOrder.set("user_name",userName);
		efunUserOrder.set("data_from",dataFrom);
		//抽奖号码
		List<Integer> numbers = new ArrayList<Integer>();
		//订单已支付完成
		if(payCash.compareTo(orderTotal)==0){
			
			for(int i=0;i<count;i++){
				int number = EfunUserOrder.dao.getNumber(efun.get("id"), skuCode, EfunUserOrder.IS_REAL_YES);
				numbers.add(number);
			}
			efunUserOrder.set("status",EfunUserOrder.STATUS_PAIED);
			efunUserOrder.set("pay_time",now);
			efunUserOrder.set("number",StringUtil.listToString(",", numbers));
		}else{
			efunUserOrder.set("cost",orderTotal.subtract(payCash));
		}
		efunUserOrder.save();
		//生成明显，返利
		if(StringUtil.notNull(numbers)){
			for(Integer n : numbers){
				EfunOrderDetail.dao.add(efunUserOrder.getStr("id"), n,efun.getDate("lottery_time"));
			}
			///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
			EfunUserOrder.dao.rebate(userId, efunUserOrder.getBigDecimal("price").multiply(new BigDecimal(numbers.size())), efunUserOrder.getStr("id"));
			
		}else{
			jsonMessage.setData(efunUserOrder.getStr("id"));
			jsonMessage.setStatusAndMsg("6", "未完成支付，需跳往第三方支付");
		}
		//扣取账户现金
		if(useCash.compareTo(zero)>0){
			account.set("cash", account.getBigDecimal("cash").subtract(useCash));
			account.update();
			//备注
			String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
			UserCashRecord.dao.add(useCash.multiply(new BigDecimal(-1)), account.getBigDecimal("cash").subtract(account.getBigDecimal("freeze_cash")), UserCashRecord.TYPE_EUN_ORDER, userId, remark);
		}
		//扣取账户积分
		if(useIntegral.compareTo(zero)>0){
			account.set("integral", account.getInt("integral")-useIntegral.intValue());
			account.update();
			//备注
			String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
			IntegralRecord.dao.add(useIntegral.multiply(new BigDecimal(-1)).intValue(), account.getInt("integral"), IntegralRecord.TYPE_EFUN_ORDER, userId, userName, remark);
		}
		
		jsonMessage.setData(efunUserOrder.getStr("id"));
		//支付成功,发送mq锁定库存，2小时后释放
		if(jsonMessage.getStatus().equals("0")){
			
			String orderId = (String) jsonMessage.getData();
			MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
		}
		return jsonMessage;
		
	}
	



	/***
	 * 一折购参与记录--参与记录
	 * @Author: Jekay
	 * @Date:   2016/12/20 11:52
	 ***/
	public Page<Record> allEfunRecord(Page<Object> page, String userId){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		select.append(" SELECT ");
		select.append("   euo.id efunOrderId,");
		select.append("   euo.product_id productId,");
		select.append("   euo.product_img productImg,");
		select.append("   euo.product_name productName,");
		select.append("   IFNULL(euo.product_property,'') productProperty,");
		select.append("   euo.sku_code skuCode,");
		select.append("   euo.efun_id efunId,");
		select.append("   euo.number myNumber,");
		select.append("   euo.cash useCash,");				//所付现金
		select.append("   euo.use_integral useIntegral,");	//已付积分
		select.append("   euo.eq_price eqPrice,");			//e趣展示价
		select.append("   euo.price ,");					//一折购价
		select.append("   e.win_number winNumber,");		//中奖号码
		select.append("   e.lottery_number lotteryNumber, ");//开奖三位数
		select.append("   euo.count joinCount,");			//参与份数
		select.append("   euo.status ,");					//订单状态:(0: 未完成付款 ,1：完成付款)'
		select.append("   euo.lottery_time lotteryTtime, ");
		select.append("   IF((unix_timestamp(euo.lottery_time) - unix_timestamp(now())) < 0,0");
		select.append("	  ,(unix_timestamp(euo.lottery_time) - unix_timestamp(now()))) tenMinute,");
		select.append("   IF(euo.lottery_time > NOW(),1,0) isDoing,");				//是否进行中的订单(1进行中,0往期)
		select.append("   IF(FIND_IN_SET(e.win_number,euo.number)>0,1,0) isWin,");	//是否进行中的订单(1中奖,0未中奖)
		select.append("   euo.create_time createTime ");
		where.append(" FROM ");
		where.append("   t_efun_user_order euo");
		where.append(" LEFT JOIN t_efun e ON euo.efun_id = e.id ");
		where.append(" WHERE ");
		where.append("   euo.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		where.append("  AND euo.user_id = ? ");
		paras.add(userId);
		where.append("  ORDER BY euo.efun_id DESC, euo.create_time DESC ");

		Page<Record> pageRecord = Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),paras.toArray());
		//根据抽奖份数处理订单中奖个数
		for(Record r : pageRecord.getList()){
			String numberList = r.getStr("myNumber");
			if(StringUtil.notNull(numberList)){
				int winNumber = r.getInt("winNumber");
				int winCount = getWinCount(winNumber,numberList);
				r.set("winCount",winCount);
			}
		}
		return pageRecord;
	}

	/***
	 * 一折购参与记录--进行中列表
	 * @Author: Jekay
	 * @Date:   2016/12/20 11:53
	 ***/
	public Page<Record> doingEfunList(Page page,String userId){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		select.append(" SELECT ");
		select.append("   euo.id efunOrderId,");
		select.append("   euo.product_id productId,");
		select.append("   euo.product_img productImg,");
		select.append("   euo.product_name productName,");
		select.append("   IFNULL(euo.product_property,'') productProperty,");
		select.append("   euo.sku_code skuCode,");
		select.append("   euo.efun_id efunId,");
		select.append("   euo.number myNumber,");
		select.append("   1 isDoing ,");
		select.append("   euo.cash useCash,");				//所付现金
		select.append("   euo.use_integral useIntegral,");	//已付积分
		select.append("   euo.eq_price eqPrice,");			//e趣展示价
		select.append("   euo.price ,");					//一折购价
		select.append("   euo.count joinCount,");			//参与份数
		select.append("   euo.status ,");					//订单状态:(0: 未完成付款 ,1：完成付款)'
		select.append("   IF((unix_timestamp(euo.lottery_time) - unix_timestamp(now())) < 0,0");
		select.append("	  ,(unix_timestamp(euo.lottery_time) - unix_timestamp(now()))) tenMinute,");
		select.append("   euo.create_time createTime ");
		where.append(" FROM ");
		where.append("   t_efun_user_order euo");
		where.append(" WHERE ");
		where.append("   euo.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		where.append("  AND euo.user_id = ? ");
		paras.add(userId);
		where.append("  AND (euo.lottery_time > NOW() OR euo.status = ?)");
		paras.add(EfunUserOrder.STATUS_NOT_FINISH_PAY);
		where.append("  AND euo.status <> ? ");
		paras.add(EfunUserOrder.STATUS_CANCEL);
		where.append("  ORDER BY euo.efun_id DESC,euo.create_time DESC ");

		return Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),paras.toArray());
	}

	/***
	 * 一折购参与记录--已揭晓--恭喜中奖列表
	 * @Author: Jekay
	 * @Date:   2016/12/20 11:54
	 ***/
	public Page<Record> winEfunList(Page<Object> page,String userId){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		select.append(" SELECT ");
		select.append("   euo.id efunOrderId,");
		select.append("   euo.product_id productId,");
		select.append("   euo.product_img productImg,");
		select.append("   euo.product_name productName,");
		select.append("   IFNULL(euo.product_property,'') productProperty,");
		select.append("   euo.sku_code skuCode,");
		select.append("   euo.efun_id efunId,");
		select.append("   euo.number myNumber,");
		select.append("   1 isWin ,");
		select.append("   0 isDoing ,");
		select.append("   euo.cash useCash,");				//所付现金
		select.append("   euo.use_integral useIntegral,");	//已付积分
		select.append("   euo.eq_price eqPrice,");			//e趣展示价
		select.append("   euo.price ,");					//一折购价
		select.append("   e.win_number winNumber,");		//中奖号码
		select.append("   euo.count joinCount,");			//参与份数
		select.append("   euo.status ,");					//订单状态:(0: 未完成付款 ,1：完成付款)'
		select.append("   euo.is_win_get isWinGet ,");		//是否已领取中奖的商品（0：否，1：是）
		select.append("   euo.lottery_time lotteryTime, ");
		select.append("   euo.create_time createTime ");
		where.append(" FROM ");
		where.append("   t_efun_user_order euo");
		where.append(" LEFT JOIN t_efun e ON euo.efun_id = e.id ");
		where.append(" WHERE ");
		where.append("   euo.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		where.append("  AND euo.status = ? ");
		paras.add(EfunUserOrder.STATUS_PAIED);
		where.append("  AND euo.user_id = ? ");
		paras.add(userId);
		where.append("  AND euo.lottery_time < NOW() ");
		where.append("  AND FIND_IN_SET(e.win_number,euo.number)>0 ");
		where.append("  ORDER BY euo.efun_id DESC , euo.create_time DESC ");

		Page<Record> pageRecord = Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),paras.toArray());
		//根据抽奖份数处理订单中奖个数
		for(Record r : pageRecord.getList()){
			String numberList = r.getStr("myNumber");
			if(StringUtil.notNull(numberList)){
				int winCount = getWinCount(r.getInt("winNumber"),numberList);
				r.set("winCount",winCount);
			}
			// 设置分享内容 - 标题
			String title = "幸运一折购：";
			String proName = r.getStr("productName");
			title += proName;
			// 设置分享内容 - 描述
			String desc = "没想到我真中奖了，e趣商城的幸运一折购真的好有趣哦~";
			String link = PropKit.use("global.properties").get("wap.domain");
			link += "/user/efun/efunShare?orderId=" + r.getStr("efunOrderId");

			r.set("title",title);
			r.set("desc",desc);
			r.set("link",link);

		}
		return pageRecord;
	}

	/***
	 * 一折购参与记录--已揭晓--折扣购物列表
	 * @Author: Jekay
	 * @Date:   2016/12/20 11:55
	 ***/
	public Page<Record> discountEfunList(Page page,String userId,int optType){
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		Integer efunNineDay = EfunUserOrder.dao.getEfunNineDay();
		select.append(" SELECT ");
		select.append("   a.id discountId,");
		select.append("   a.efun_order_id efunOrderId,");
		select.append("   a.order_id orderId,");
		select.append("   b.product_id productId,");
		select.append("   b.product_img productImg,");
		select.append("   b.product_name productName,");
		select.append("   IFNULL(b.product_property,'') productProperty,");
		select.append("   b.sku_code skuCode,");
		select.append("    b.efun_id efunId,");
		select.append("    b.eq_price eqPrice,");
		select.append("    b.price,");
		select.append("   b.cash useCash,");				//所付现金
		select.append("   b.use_integral useIntegral,");	//已付积分
		select.append("   b.number myNumber,");
		select.append("   0 isWin ,");
		select.append("   0 isDoing ,");
		select.append("   c.win_number winNumber,");
		select.append("   c.lottery_time lotteryTime,");
		select.append("   IFNULL(a.discount_val,'0') hasDiscount, ");//是否已经翻牌
		select.append("   FORMAT((IFNULL(a.discount_val,0.9) * b.eq_price),2) discountPrice,");
		select.append("   CAST((IFNULL(a.discount_val,0.9) *100) AS SIGNED ) discountVal,");
		select.append("   IF((unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now())) < 0,0");
		select.append("	  ,(unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now()))) nineOffTime ,");
		select.append("   date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR) nineOffBuyTime ");
		where.append(" FROM ");
		where.append("   t_efun_order_detail a ");
		where.append("    LEFT JOIN t_efun_user_order b ON a.efun_order_id = b.id ");
		where.append("    LEFT JOIN t_efun c ON b.efun_id = c.id ");
		where.append(" WHERE ");
		where.append("   a.number <> c.win_number ");
		where.append("  AND (a.order_id IS NULL OR a.order_id = '')  ");
		where.append("  AND b.user_id = ? ");
		paras.add(userId);
		where.append("  AND b.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		where.append("  AND b.`status` = ? ");
		paras.add(EfunUserOrder.STATUS_PAIED);
		where.append("  AND b.lottery_time < NOW() ");
		if(1 == optType){//翻牌
			where.append(" AND (a.discount_val IS NULL OR a.discount_val = '') ");
			where.append(" AND (unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now())) > 0 ");
		}else if(2 == optType){//合并领取
			where.append(" AND (a.discount_val IS NOT NULL OR a.discount_val <> '' ) ");
			where.append(" AND b.is_win_get = 0 ");
		}
		where.append("  ORDER BY b.efun_id DESC , a.create_time DESC ");
		return Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),paras.toArray());
	}

	/***
	 * 已揭晓--全部列表
	 * @Author: Jekay
	 * @Date:   2016/12/27 16:49
	 ***/
	public Page<Record> winAndDiscountList(Page<Object> page,String userId,int optType){
		StringBuffer select = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		StringBuffer where = new StringBuffer();
		Integer efunNineDay = EfunUserOrder.dao.getEfunNineDay();
		select.append(" SELECT ");
		select.append("   a.id discountId,");
		select.append("   a.efun_order_id efunOrderId,");
		select.append("   a.order_id orderId,");
		select.append("   b.product_id productId,");
		select.append("   b.product_img productImg,");
		select.append("   b.product_name productName,");
		select.append("   IFNULL(b.product_property,'') productProperty,");
		select.append("   b.sku_code skuCode,");
		select.append("    b.efun_id efunId,");
		select.append("    b.eq_price eqPrice,");
		select.append("    b.price,");
		select.append("   a.number myNumber,");
		select.append("   b.number numberList,");
		select.append("   b.cash useCash,");				//所付现金
		select.append("   b.use_integral useIntegral,");	//已付积分
		select.append("   c.win_number winNumber,");
		select.append("   c.lottery_time lotteryTime,");				//开奖时间
		select.append("   b.count joinCount,");							//参与份数
		select.append("   IFNULL(a.discount_val,'0') hasDiscount, ");	//是否已经翻牌
		select.append("   b.is_win_get isWinGet ,");					//是否已领取中奖的商品（0：否，1：是）
		select.append("   0 isDoing,");									//是否进行中的订单(1进行中,0往期)
		select.append("   IF(c.win_number = a.number,1,0) isWin,");		//是否进行中的订单(1中奖,0未中奖)
		select.append("    CAST((IFNULL(a.discount_val,0.9) *100) AS SIGNED ) discountVal,");
		select.append("   FORMAT((IFNULL(a.discount_val,0.9) * b.eq_price),2) discountPrice,");
		select.append("   IF((unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now())) < 0,0");
		select.append("	  ,(unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now()))) nineOffTime ,");
		select.append("   date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR) nineOffBuyTime");
		where.append(" FROM ");

		 where.append("   ((SELECT t1.id,t1.efun_order_id,t1.order_id,t1.number,t1.discount_val,t1.create_time,t1.begin_valid_time FROM t_efun_order_detail AS t1 ");
		 where.append("     LEFT JOIN t_efun_user_order AS b1 ON t1.efun_order_id = b1.id ");
		 where.append("     LEFT JOIN t_efun AS c1 ON c1.id = b1.efun_id");
		 where.append(" 	WHERE b1.user_id = '"+userId+"' AND t1.number = c1.win_number ");
		 where.append(" 	GROUP BY t1.efun_order_id)");
		 where.append(" UNION");
		 where.append("   (SELECT t2.id,t2.efun_order_id,t2.order_id,t2.number,t2.discount_val,t2.create_time,t2.begin_valid_time FROM t_efun_order_detail AS t2 ");
		 where.append(" 	LEFT JOIN t_efun_user_order AS b2 ON t2.efun_order_id = b2.id");
		 where.append(" 	LEFT JOIN t_efun AS c2 ON c2.id = b2.efun_id");
		 where.append(" 	WHERE b2.user_id = '"+userId+"' AND t2.number <> c2.win_number )) ");
		 where.append("   AS a ");

		where.append("    LEFT JOIN t_efun_user_order b ON a.efun_order_id = b.id ");
		where.append("    LEFT JOIN t_efun c ON b.efun_id = c.id ");
		where.append(" WHERE ");
		where.append("   (a.order_id IS NULL OR a.order_id = '')  ");
		where.append("  AND b.user_id = ? ");
		paras.add(userId);
		where.append("  AND b.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		where.append("  AND b.`status` = ? ");
		paras.add(EfunUserOrder.STATUS_PAIED);
		where.append("  AND b.lottery_time < NOW() ");
		if(1 == optType){//翻牌
			where.append(" AND (a.discount_val IS NULL OR a.discount_val = '') ");
			where.append(" AND c.win_number <> a.number ");
			where.append(" AND (unix_timestamp(date_add(a.begin_valid_time, interval "+efunNineDay+" HOUR)) - unix_timestamp(now())) > 0 ");
		}else if(2 == optType){//合并领取
			where.append(" AND (a.discount_val IS NOT NULL OR a.discount_val <> '' ) ");
			where.append(" AND b.is_win_get = 0 ");
		}

		where.append("  ORDER BY b.efun_id DESC , a.create_time DESC ");
		Page<Record> pageRecord = Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),paras.toArray());
		//根据抽奖份数处理订单中奖个数
		for(Record r : pageRecord.getList()){
			int winCount = 0;
			if(r.getInt("isWin") == 1){
				for(String str : r.getStr("numberList").split(",")){
					if(r.getInt("winNumber") == Integer.parseInt(str)){
						winCount +=1;
					}
				}
				// 设置分享内容 - 标题
				String title = "幸运一折购：";
				String proName = r.getStr("productName");
				title += proName;
				// 设置分享内容 - 描述
				String desc = "没想到我真中奖了，e趣商城的幸运一折购真的好有趣哦~";
				String link = PropKit.use("global.properties").get("wap.domain");
				link += "/user/efun/efunShare?orderId=" + r.getStr("efunOrderId");

				r.set("title",title);
				r.set("desc",desc);
				r.set("link",link);
			}
			r.set("winCount",winCount);
		}
		return pageRecord;
	}

	/***
	 * 一折购参与记录--订单详情
	 * @Author: Jekay
	 * @Date:   2016/12/20 11:55
	 ***/
	public Record efunOrderDetail(String efunOrderId){
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT ");
		sb.append("   euo.id efunOrderId,");
		sb.append("   euo.product_id productId,");
		sb.append("   euo.product_img productImg,");
		sb.append("   euo.product_name productName,");
		sb.append("   IFNULL(euo.product_property,'') productProperty,");
		sb.append("   euo.sku_code skuCode,");
		sb.append("   euo.efun_id efunId, ");
		sb.append("   euo.count joinCount,");
		sb.append("   euo.total,");
		sb.append("   euo.number myNumber,");
		sb.append("   e.win_number winNumber,");
		sb.append("   euo.price ,");
		sb.append("   euo.`status` ,");
		sb.append("   euo.create_time createTime,");
		sb.append("   euo.lottery_time lotteryTime");
		sb.append(" FROM  ");
		sb.append("  t_efun_user_order euo ");
		sb.append(" LEFT JOIN t_efun e ON e.id = euo.efun_id ");
		sb.append(" WHERE ");
		sb.append("  euo.is_real = ? ");
		sb.append("   AND euo.id = ? ");
		sb.append("  ORDER BY euo.create_time DESC ");
		Record record = Db.findFirst(sb.toString(),EfunUserOrder.IS_REAL_YES,efunOrderId);
		if(StringUtil.notNull(record) && StringUtil.notBlank(record.getStr("myNumber"))){
			if(record.getInt("status") > 0){
				int winCount = getWinCount(record.getInt("winNumber"),record.getStr("myNumber"));
				record.set("winCount",winCount);
				List<Integer> numberList = new ArrayList<>();
				String[] numberArray= record.getStr("myNumber").split(",");
				for(String str : numberArray){
					numberList.add(Integer.parseInt(str));
				}
				record.set("numberList",numberList);
			}else{
				record.set("winCount",0);
			}
		}
		return record;
	}

	/***
	 * 判断中奖个数
	 * @Author: Jekay
	 * @Date:   2016/12/21 10:54
	 ***/
	public int getWinCount(int winNumber,String myNumberStr){
		int winCount = 0;
		for(String str : myNumberStr.split(",")){
			if(winNumber == Integer.parseInt(str)){
				winCount +=1;
			}
		}
		return winCount;
	}

	/***
	 * 进行中提示数量
	 * @Author: Jekay
	 * @Date:   2016/12/22 17:48
	 ***/
	public Long doingCount(String userId){
		StringBuffer select = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		select.append(" SELECT ");
		select.append(" IFNULL(count(1),0) doingCount ");
		select.append(" FROM ");
		select.append("   t_efun_user_order euo");
		select.append(" WHERE ");
		select.append("   euo.is_real = ? ");
		paras.add(EfunUserOrder.IS_REAL_YES);
		select.append("  AND euo.user_id = ? ");
		paras.add(userId);
		select.append("  AND (euo.lottery_time > NOW() OR euo.status = ?)");
		paras.add(EfunUserOrder.STATUS_NOT_FINISH_PAY);
		select.append("  AND euo.status <> ?  ");
		paras.add(EfunUserOrder.STATUS_CANCEL);
		return Db.queryLong(select.toString(),paras.toArray());
	}
	/***
	 * 中奖提示数量
	 * @Author: Jekay
	 * @Date:   2016/12/22 17:48
	 ***/
	public Long winCount(String userId){
		StringBuffer select = new StringBuffer();
		select.append(" SELECT ");
		select.append(" IFNULL(count(1),0) winCount ");
		select.append(" FROM ");
		select.append("   t_efun_user_order euo");
		select.append(" LEFT JOIN t_efun e ON euo.efun_id = e.id ");
		select.append(" WHERE ");
		select.append("   euo.is_real = ? ");
		select.append("  AND euo.status = ? ");
		select.append("  AND euo.user_id = ? ");
		select.append("  AND euo.lottery_time < NOW() ");
		select.append("  AND FIND_IN_SET(e.win_number,euo.number)>0 ");
		return Db.queryLong(select.toString(),EfunUserOrder.IS_REAL_YES,EfunUserOrder.STATUS_PAIED,userId);
	}
	/***
	 * 折扣购物提示数量
	 * @Author: Jekay
	 * @Date:   2016/12/22 17:48
	 ***/
	public Long discountCount(String userId){
			StringBuffer select = new StringBuffer();
			select.append(" SELECT ");
			select.append(" IFNULL(count(1),0) discountCount ");
			select.append(" FROM ");
			select.append("   t_efun_order_detail a ");
			select.append("    LEFT JOIN t_efun_user_order b ON a.efun_order_id = b.id ");
			select.append("    LEFT JOIN t_efun c ON b.efun_id = c.id ");
			select.append(" WHERE ");
			select.append("   a.number <> c.win_number ");
			select.append("  AND (a.order_id IS NULL OR a.order_id = '')  ");
			select.append("  AND b.user_id = ? ");
			select.append("  AND b.is_real = ? ");
			select.append("  AND b.`status` = ? ");
			select.append("  AND b.lottery_time < NOW() ");
		return Db.queryLong(select.toString(),userId,EfunUserOrder.IS_REAL_YES,EfunUserOrder.STATUS_PAIED);
	}

	/**
	 * 计算立即参与的金额
	 * @param skuCode
	 * @param count
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2016年12月28日 下午3:56:27
	 *
	 */
	public Record calculateFromSku(String skuCode,Integer count,String userId){
		
		ProductSku sku = ProductSku.dao.findByIdLoadColumns(skuCode, "eq_price");
		Record r = new Record();
		//计算订单价格，保留两位小数
		BigDecimal price = sku.getBigDecimal("eq_price").divide(new BigDecimal(10)).setScale(2, BigDecimal.ROUND_HALF_UP);
		price = price.multiply(new BigDecimal(count));
		Account account = Account.dao.getAccountByUserId(userId, Account.TYPE_USER);
		BigDecimal userCash = account.getBigDecimal("cash");
		Integer userIntegral = account.getInt("integral");
		//推荐使用积分
		Integer recommonIntegral = 0;
		//推荐使用金额
		BigDecimal recommonCash = new BigDecimal(0);
		//还需支付金额
		BigDecimal needPayCash = new BigDecimal(0);
		
		BigDecimal integral = new BigDecimal(userIntegral).divide(new BigDecimal(100));
		
		if(integral.compareTo(price)==1){
			recommonIntegral =  price.multiply(new BigDecimal(100)).intValue();
			
		}else{
			recommonIntegral =  userIntegral;
			if(userCash.compareTo(price.subtract(integral))==1){
				recommonCash =  price.subtract(integral);
			}else{
				recommonCash =  userCash;
			}
			
		}
		needPayCash = price.subtract(new BigDecimal(recommonIntegral).divide(new BigDecimal(100))).subtract(recommonCash);
		r.set("orderPrice", price);
		r.set("userIntegral", userIntegral);
		r.set("userCash", userCash);
		r.set("recommonIntegral", recommonIntegral);
		r.set("recommonCash", recommonCash);
		r.set("needPayCash", needPayCash);
		
		return r;
	}
	
	/**
	 * 计算购物车提交结算的金额及推荐使用现金积分
	 * @param cartIds
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2016年12月28日 下午7:35:13
	 *
	 */
	public Record calculateFromCart(Integer[] cartIds,String userId){
		
		List<Record> cartList = EfunCart.dao.getCartForOrder(cartIds, userId);
		//计算订单价格
		BigDecimal total = new BigDecimal(0);
		for(Record cart : cartList){
			total = total.add(cart.getBigDecimal("efunPrice").multiply(new BigDecimal(cart.getInt("count"))));
			
		}
		total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
		Record r = new Record();
		Account account = Account.dao.getAccountByUserId(userId, Account.TYPE_USER);
		BigDecimal userCash = account.getBigDecimal("cash");
		Integer userIntegral = account.getInt("integral");
		//推荐使用积分
		Integer recommonIntegral = 0;
		//推荐使用金额
		BigDecimal recommonCash = new BigDecimal(0);
		//还需支付金额
		BigDecimal needPayCash = new BigDecimal(0);
		
		BigDecimal integral = new BigDecimal(userIntegral).divide(new BigDecimal(100));
		
		if(integral.compareTo(total)==1){
			recommonIntegral =  total.multiply(new BigDecimal(100)).intValue();
			
		}else{
			recommonIntegral =  userIntegral;
			if(userCash.compareTo(total.subtract(integral))==1){
				recommonCash =  total.subtract(integral);
			}else{
				recommonCash =  userCash;
			}
			
		}
		needPayCash = total.subtract(new BigDecimal(recommonIntegral).divide(new BigDecimal(100))).subtract(recommonCash);
		r.set("orderPrice", total);
		r.set("userIntegral", userIntegral);
		r.set("userCash", userCash);
		r.set("recommonIntegral", recommonIntegral);
		r.set("recommonCash", recommonCash);
		r.set("needPayCash", needPayCash);
		
		
		return r;
		
	}
	
	/**
	 * 订单预览.
	 * 
	 * @param prizeEfunOrderIdList
	 * 合并领取一折购订单Id列表.
	 * @param discountEfunOrderDetailIdList
	 * 合并购买一折购订单详情Id列表.
	 * @author Chengyb
	 */
	@SuppressWarnings("unchecked")
	public JsonMessage prepare4Order(String userId, List<String> prizeEfunOrderIdList, List<String> discountEfunOrderDetailIdList) {
		JsonMessage jsonMessage = new JsonMessage();
		
		//===================================================================
		// 用户所有的收货地址.
		//===================================================================
		List<Record> addressList = RecAddress.dao.list(userId);
		
		if(null != addressList && addressList.size() > 0) {
			jsonMessage = Order.dao.divisionOrder(userId, addressList.get(0).getInt("id"), prizeEfunOrderIdList, discountEfunOrderDetailIdList, null, jsonMessage);
			if(null != jsonMessage.getData()) {
				((Map<String, Object>) jsonMessage.getData()).put("addressList", addressList);
			}
		} else {
			jsonMessage = Order.dao.divisionOrder(userId, null, prizeEfunOrderIdList, discountEfunOrderDetailIdList, null, jsonMessage);
		}
		
		return jsonMessage;
	}
	
	/**
	 * 合并拼折扣.
	 * 
	 * @author Chengyb
	 */
	@Before(Tx.class)
	public void discount(String userId, String orderIds) {
		List<Record> orderList = new ArrayList<Record>();
		if(!StringUtil.isBlank(orderIds)) {
			if(orderIds.indexOf(",") != -1) { // 多个订单.
				String[] orderIdArray = orderIds.split(",");
				for (int i = 0, size = orderIdArray.length; i < size; i++) {
					String orderId = orderIdArray[i];
					checkEfunOrder4Discount(orderList, userId, orderId);
				}
			} else { // 单个订单.
				checkEfunOrder4Discount(orderList, userId, orderIds);
			}
		}
		
		if(orderList.size() > 0) {
			//========================================================
			// 牌获取一折购折扣.
			//========================================================
			BigDecimal discount = EfunDiscount.dao.getEfunDiscount();
			
			EfunUserOrder efunOrder = new EfunUserOrder();
			for (int i = 0, size = orderList.size(); i < size; i++) {
				efunOrder.set("id", orderList.get(i).getStr("id"));
				efunOrder.set("discount_val", discount);
				efunOrder.update();
			}
		}
	}
	
	/**
	 * 合并领取/购买(默认收货地址).
	 * 
	 * @author Chengyb
	 */
	@Before(Tx.class)
	public void mergeReceiveAndPurchaseByDefaultAddress(String userId, String orderIds) {
		//===================================================================
		// 店铺/供货商商品分组.
		// 数据结构:
		//         ---
		//            |___shopId : ed4e324c0e174d79b83350f82fa0ab57
		//            |   shopName : 优田品
		//            |   |___productSkuList
		//            |                     |___productId : 291659
		//            |                         productName : 小米5
		//            |
		//            |___shopId : 17a5a51bcb1e4fdc8205adf372f30f15
		//                shopName : 虾笼镇（兴盛店）
		//                |___productSkuList
		//                                  |___productId : 293123
		//                                      productName : 油焖大闸蟹 约一斤
		//                                      productImage : /2016/12/11/aa41cbc579ac42a6b18b7c2c5bf51acf.jpg
		//                                      skuCode : 1ec7ed99e5ef3b92416164c319f38203
		//                                      skuProperty : 
		//===================================================================
		Map<String, Object> shopOrSupplierMap = new HashMap<String, Object>();
		
		//===================================================================
		// 产品Map.【key】Sku识别码,【value】产品数量.
		// 供计算运费使用.
		//===================================================================
		
		//===================================================================
		// 商品下架.
		//===================================================================
		List<String> offShelvesList = new ArrayList<String>();
		
		//===================================================================
		// 商品删除.
		//===================================================================
		List<String> deletedList = new ArrayList<String>();
		
		//===================================================================
		// 商品冻结.
		//===================================================================
		List<String> lockedList = new ArrayList<String>();
		
		//===================================================================
		// 店铺冻结.
		//===================================================================
		List<String> shopFrozenList = new ArrayList<String>();
		
		//===================================================================
		// 商品无库存.
		//===================================================================
		List<String> soldOutList = new ArrayList<String>();
		
		List<Record> orderList = new ArrayList<Record>();
		if(!StringUtil.isBlank(orderIds)) {
			if(orderIds.indexOf(",") != -1) { // 多个订单.
				String[] orderIdArray = orderIds.split(",");
				for (int i = 0, size = orderIdArray.length; i < size; i++) {
					String orderId = orderIdArray[i];
					checkEfunOrder4ReceiveAndBuy(orderList, userId, orderId);
				}
			} else { // 单个订单.
				checkEfunOrder4ReceiveAndBuy(orderList, userId, orderIds);
			}
		}
		
		if(orderList.size() > 0) {
			for (int i = 0, size = orderList.size(); i < size; i++) {
				//===================================================================
				// 处理无法下单的商品.
				//===================================================================
				Record record = orderList.get(i);
				// 商品Id.
				Integer productId = record.getInt("product_id");
				// 商品图片.
				String productImage = record.getStr("product_img");
				// 商品名称.
				String productName = record.getStr("product_name");
				// Sku编码.
				String skuCode = record.getStr("sku_code");
				// 销售属性.
				
				if(shopOrSupplierMap.containsKey(skuCode) || offShelvesList.contains(skuCode) || deletedList.contains(skuCode) || lockedList.contains(skuCode) || soldOutList.contains(skuCode)) {
				} else { // 新商品.
					Record product = Product.dao.findProduct4EfunOrderByProId(productId);
					if(null != product) {
						// shop_id : 店铺Id
						// supplier_id : 供货商Id
						// status : 状态(0:下架,1:上架,2:已删除)
						// lock_status : 冻结状态(0:已冻结,1:正常)
						String shopId = product.getStr("shop_id");
						String supplierId = product.getStr("supplier_id");
						int status = product.getInt("status");
						int lockStatus = product.getInt("lock_status");
//						 商品名称.
//						String productName = record.getStr("product_name");
						if(status == 2) { // 商品已删除.
							deletedList.add(productName);
						}
						else if(status == 0) { // 商品已下架.
							offShelvesList.add(productName);
						}
						else if(lockStatus == 0) { // 商品已冻结.
							lockedList.add(productName);
						}
						else {
							canBuySkusGroupByExpress(shopOrSupplierMap, shopFrozenList, soldOutList, record, shopId, supplierId);
						}
					}
				}
			}
		}
	}

	/**
	 * 快递运输商品分组.
	 * 
	 * @author Chengyb
	 */
	private void canBuySkusGroupByExpress(Map<String, Object> shopOrSupplierMap, List<String> shopFrozenList,
			List<String> soldOutList, Record record, String shopId, String supplierId) {
		// 商品Id.
		Integer productId = record.getInt("product_id");
		// 商品图片.
		String productImage = record.getStr("product_img");
		// 商品名称.
		String productName = record.getStr("product_name");
		// Sku编码.
		String skuCode = record.getStr("sku_code");
		// 销售属性.
		String skuProperty = record.getStr("product_property");
		
		// 店铺可以被冻结,供货商目前没有冻结功能.
		if(!StringUtil.isBlank(shopId)) { // 店铺商品.
			Shop shop = Shop.dao.findByIdLoadColumns(shopId, "id, name, status, forbidden_status");
			if(null != shop) {
				//===================================================================
				// 店铺状态判断.
				//===================================================================
				if(shop.getInt("status") == 4 && shop.getInt("forbidden_status") == 0) {
				} else {
					shopFrozenList.add(productName);
				}
				inventoryGroup(shopOrSupplierMap, soldOutList, shopId, productId, productImage, productName, skuCode,
						skuProperty, shop);
			}
		} else if(!StringUtil.isBlank(supplierId)) {
			inventoryGroup(shopOrSupplierMap, soldOutList, shopId, productId, productImage, productName, skuCode,
					skuProperty, null);
		}
	}

	/**
	 * 按照店铺/供货商分组.
	 * 
	 * @author Chengyb
	 */
	private void inventoryGroup(Map<String, Object> shopOrSupplierMap, List<String> soldOutList, String shopOrSupplierId,
			Integer productId, String productImage, String productName, String skuCode, String skuProperty, Shop shop) {
		//===================================================================
		// 店铺商品库存判断.
		//===================================================================
		if(ProductSku.dao.hasCount(skuCode)) {
			//===================================================================
			// 按照店铺拆单.
			//===================================================================
			Map<String, Object> groupMap = null;
			List<Map<String, Object>> list = null;
			if(shopOrSupplierMap.containsKey(shopOrSupplierId)) {
				groupMap = (Map<String, Object>) shopOrSupplierMap.get(shopOrSupplierId);
				
				list = (List<Map<String, Object>>) groupMap.get("productSkuList");
			} else {
				groupMap = new HashMap<String, Object>();
				if(null != shop) { // 店铺.
					groupMap.put("shopId", shopOrSupplierId); // 店铺Id.
					groupMap.put("shopName", shop.getStr("name")); // 店铺名称.
				} else { // 供货商.
					groupMap.put("supplierId", shopOrSupplierId); // 供货商Id.
					groupMap.put("supplierName", Supplier.dao.findByIdLoadColumns(shopOrSupplierId, "name").getStr("name")); // 供货商名称.
				}
				list = new ArrayList<Map<String, Object>>();
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("productId", productId); // 商品Id.
			map.put("productName", productName); // 商品名称.
			map.put("productImage", productImage); // 商品图片.
			map.put("skuCode", productName); // 商品Sku编码.
			map.put("skuProperty", skuProperty); // 商品Sku销售属性.
			
			list.add(map);
			groupMap.put("productSkuList", list); // 店铺商品Sku列表.
			
			shopOrSupplierMap.put(shopOrSupplierId, groupMap);
		} else {
			soldOutList.add(productName);
		}
	}
	
	/**

	 * 计算订单支付金额及推荐使用现金积分
	 * @param userId
	 * @param orderIds
	 * @return
	 * @author huangzq
	 * 2016年12月31日 下午6:55:54
	 *
	 */
	public Record calculateOrder(String userId,String... orderIds ){
		
		Record r = new Record();
		//计算订单价格，保留两位小数
		BigDecimal price = EfunUserOrder.dao.calculateCost(userId, orderIds);
		Account account = Account.dao.getAccountByUserId(userId, Account.TYPE_USER);
		BigDecimal userCash = account.getBigDecimal("cash");
		Integer userIntegral = account.getInt("integral");
		//推荐使用积分
		Integer recommonIntegral = 0;
		//推荐使用金额
		BigDecimal recommonCash = new BigDecimal(0);
		//还需支付金额
		BigDecimal needPayCash = new BigDecimal(0);
		
		BigDecimal integral = new BigDecimal(userIntegral).divide(new BigDecimal(100));
		
		if(integral.compareTo(price)==1){
			recommonIntegral =  price.multiply(new BigDecimal(100)).intValue();
			
		}else{
			recommonIntegral =  userIntegral;
			if(userCash.compareTo(price.subtract(integral))==1){
				recommonCash =  price.subtract(integral);
			}else{
				recommonCash =  userCash;
			}
			
		}
		needPayCash = price.subtract(new BigDecimal(recommonIntegral).divide(new BigDecimal(100))).subtract(recommonCash);
		r.set("orderPrice", price);
		r.set("userIntegral", userIntegral);
		r.set("userCash", userCash);
		r.set("recommonIntegral", recommonIntegral);
		r.set("recommonCash", recommonCash);
		r.set("needPayCash", needPayCash);
		
		
		return r;
		
	}
	
	/**
	 * 合并领取/购买(选择云店收货地址).
	 * 
	 * @author Chengyb
	 */
	@Before(Tx.class)
	public void mergeReceiveAndPurchaseByO2oAddress(String userId, String orderIds, String storeNo) {
		//===================================================================
		// 店铺/供货商.
		//===================================================================
		Map<String, Record> shopOrSupplierMap = new HashMap<String, Record>();
		
		//===================================================================
		// 商品下架.
		//===================================================================
		List<String> offShelvesList = new ArrayList<String>();
		
		//===================================================================
		// 商品删除.
		//===================================================================
		List<String> deletedList = new ArrayList<String>();
		
		//===================================================================
		// 商品冻结.
		//===================================================================
		List<String> lockedList = new ArrayList<String>();
		
		//===================================================================
		// 店铺冻结.
		//===================================================================
		List<String> shopFrozenList = new ArrayList<String>();
		
		//===================================================================
		// 商品无库存.
		//===================================================================
		List<String> soldOutList = new ArrayList<String>();
		
		List<Record> orderList = new ArrayList<Record>();
		if(!StringUtil.isBlank(orderIds)) {
			if(orderIds.indexOf(",") != -1) { // 多个订单.
				String[] orderIdArray = orderIds.split(",");
				for (int i = 0, size = orderIdArray.length; i < size; i++) {
					String orderId = orderIdArray[i];
					checkEfunOrder4ReceiveAndBuy(orderList, userId, orderId);
				}
			} else { // 单个订单.
				checkEfunOrder4ReceiveAndBuy(orderList, userId, orderIds);
			}
		}
		
		if(orderList.size() > 0) {
			for (int i = 0, size = orderList.size(); i < size; i++) {
				//===================================================================
				// 处理无法下单的商品.
				//===================================================================
				Record record = orderList.get(i);
				// 商品Id.
				Integer productId = record.getInt("product_id");
				// 商品图片.
				String productImage = record.getStr("product_img");
				// 商品名称.
				// Sku编码.
				String skuCode = record.getStr("sku_code");
				// 销售属性.
				
				
				if(shopOrSupplierMap.containsKey(productId.toString()) || offShelvesList.contains(productId.toString()) || deletedList.contains(productId.toString()) || lockedList.contains(productId.toString()) || soldOutList.contains(productId.toString())) {
				} else { // 新商品.
					Record product = Product.dao.findProduct4EfunOrderByProId(productId);
					if(null != product) {
						// shop_id : 店铺Id
						// supplier_id : 供货商Id
						// status : 状态(0:下架,1:上架,2:已删除)
						// lock_status : 冻结状态(0:已冻结,1:正常)
						String shopId = product.getStr("shop_id");
						String supplierId = product.getStr("supplier_id");
						int status = product.getInt("status");
						int lockStatus = product.getInt("lock_status");
						// 商品名称.
						String productName = record.getStr("product_name");
						if(status == 2) { // 商品已删除.
							deletedList.add(productName);
						}
						else if(status == 0) { // 商品已下架.
							offShelvesList.add(productName);
						}
						else if(lockStatus == 0) { // 商品已冻结.
							lockedList.add(productName);
						}
						// 店铺可以被冻结,供货商目前没有冻结功能.
						else if(!StringUtil.isBlank(shopId)) { // 店铺商品.
							Shop shop = Shop.dao.findByIdLoadColumns(shopId, "status, forbidden_status");
							if(null != shop) {
								if(shop.getInt("status") == 4 && shop.getInt("forbidden_status") == 0) {
								} else {
									shopFrozenList.add(productName);
								}
							}
						} else {
							//===================================================================
							// 库存判断.
							//===================================================================
							ProductSku.dao.hasCount(skuCode);
							//===================================================================
							// 按照店铺/供货商拆单.
							//===================================================================
							
						}
					}
				}
			}
		}
	}

	/**
	 * 检查一个一折购订单是否可以牌.
	 * 
	 * @author Chengyb
	 */
	private void checkEfunOrder4Discount(List<Record> orderList, String userId, String efunOrderId) {
		List<Record> list = EfunOrderDetail.dao.findEfunOrderDetail(userId, efunOrderId, false);
		if(null != list && list.size() > 0) { // 订单存在.
			for (int j = 0, size = list.size(); j < size; j++) {
				if(null == list.get(j).getBigDecimal("discount_val")) { // 未牌.
					orderList.add(list.get(j)); // 添加当前订单到有效列表.
				}
			}
		}
	}
	/**
	 * 积分余额支付
	 * @param ret
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2017年1月1日 上午10:01:49
	 *
	 */
	@Before(Tx.class)
	public JsonMessage payWithCash(Ret ret ,String userId){
		JsonMessage jsonMessage = new JsonMessage();
		Date now = new Date();
		BigDecimal zero = new BigDecimal(0);
		//使用金额
		BigDecimal useCash = ret.getBigDecimal("useCash");
		if(useCash==null){
			useCash = zero;
		}
		//使用积分
		BigDecimal useIntegral = ret.getBigDecimal("useIntegral");
		if(useIntegral==null){
			useIntegral = zero;
		}
		if(useCash.add(useIntegral).compareTo(zero)<1){
			return jsonMessage;
		}
		//来源
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		Integer integral = account.getInt("integral");
		BigDecimal cash = account.getBigDecimal("cash");
		//支付现金（使用金额+使用积分/100）
		BigDecimal payCash = useCash.add(useIntegral.divide(new BigDecimal(100)));
		
		//判断使用积分是否小于0
		if(useIntegral.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断使用现金是否小于0
		if(useCash.compareTo(zero) == -1){
			jsonMessage.dataException();
			return jsonMessage;
		}
		//判断使用积分是否大于会员账户积分余额
		if(useIntegral.intValue() > integral){
			jsonMessage.setStatusAndMsg("2", "使用积分不能大于账户剩余积分！");
			return jsonMessage;
		}
		//判断使用现金是否大于会员账户现金余额
		if(useCash.compareTo(cash) == 1){
			jsonMessage.setStatusAndMsg("3", "使用现金不能大于账户剩余现金！");
			return jsonMessage;
		}
		
		//订单id
		String orderId = ret.getStr("orderId");
		EfunUserOrder efunUserOrder = EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
		
		String skuCode = efunUserOrder.getStr("sku_code");
		Record sku = ProductSku.dao.getSkuForSubmitOrder(skuCode);
		//检查商品状态
		jsonMessage = ProductSku.dao.checkSku(sku);
		if(!jsonMessage.getStatus().equals("0")){
			return jsonMessage;
		}
		//订单需支付金额
		BigDecimal cost = efunUserOrder.getBigDecimal("cost");
		Integer status = efunUserOrder.getInt("status");
		//判断状态
		if(status!=EfunUserOrder.STATUS_NOT_FINISH_PAY){
			jsonMessage.setStatusAndMsg("7", "订单已无需支付");
			return jsonMessage;
		}
		//使用金额过大
		if(payCash.compareTo(cost)==1){
			jsonMessage.dataException();
			return jsonMessage;
			
		}
		//订单已支付完成
		if(payCash.compareTo(cost)==0){
			
			Integer count = efunUserOrder.getInt("count");
			//是否是释放库存
			Integer isReleaseLockCount = efunUserOrder.getInt("is_release_lock_count");
			//库存已释放
			if(isReleaseLockCount==BaseConstants.YES){
				boolean flag = ProductSku.dao.addLockCount(skuCode, count);
				//库存不足，记录不足商品
				if(flag==false){
					jsonMessage.setStatusAndMsg("5", "商品库存不足");
					return jsonMessage;
				}
			}
			//获取最新期次
			Efun efun = Efun.dao.getNewestEfun();
			//抽奖号码
			List<Integer> numbers = new ArrayList<Integer>();
			
				
			for(int i=0;i<count;i++){
				int number = EfunUserOrder.dao.getNumber(efun.get("id"), skuCode, EfunUserOrder.IS_REAL_YES);
				numbers.add(number);
			}
			efunUserOrder.set("status",EfunUserOrder.STATUS_PAIED);
			efunUserOrder.set("pay_time",now);
			efunUserOrder.set("number",StringUtil.listToString(",", numbers));
			//生成明细，返利
			if(StringUtil.notNull(numbers)){
				for(Integer n : numbers){
					EfunOrderDetail.dao.add(efunUserOrder.getStr("id"), n,efun.getDate("lottery_time"));
				}
				///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
				EfunUserOrder.dao.rebate(userId, efunUserOrder.getBigDecimal("price").multiply(new BigDecimal(numbers.size())), efunUserOrder.getStr("id"));
				
			}
			
		}else{
			efunUserOrder.set("cash", efunUserOrder.getBigDecimal("cash").add(useCash));
			efunUserOrder.set("use_integral", efunUserOrder.getInt("use_integral")+useIntegral.intValue());
			jsonMessage.setStatusAndMsg("6", "未完成支付，需跳往第三方支付");
		}
		String userName = User.dao.getUserName(userId);
		String orderNo = efunUserOrder.getStr("no");
		//扣取账户现金
		if(useCash.compareTo(zero)>0){
			account.set("cash", account.getBigDecimal("cash").subtract(useCash));
			account.update();
			//备注
			String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
			UserCashRecord.dao.add(useCash.multiply(new BigDecimal(-1)), account.getBigDecimal("cash").subtract(account.getBigDecimal("freeze_cash")), UserCashRecord.TYPE_EUN_ORDER, userId, remark);
		}
		//扣取账户积分
		if(useIntegral.compareTo(zero)>0){
			account.set("integral", account.getInt("integral")-useIntegral.intValue());
			account.update();
			//备注
			String remark = "会员"+userName+"参与 幸运一折购-"+orderNo;
			IntegralRecord.dao.add(useIntegral.multiply(new BigDecimal(-1)).intValue(), account.getInt("integral"), IntegralRecord.TYPE_EFUN_ORDER, userId, userName, remark);
		}
		jsonMessage.setData(orderId);
		efunUserOrder.update();
		if(jsonMessage.getStatus().equals("0")){
			//发送mq标识锁定库存
			MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, orderId);
		}
		return jsonMessage;
	}
	/**
	 * 检查一个一折购订单是否可以领取/购买.
	 * 
	 * @author Chengyb
	 */
	private void checkEfunOrder4ReceiveAndBuy(List<Record> orderList, String userId, String efunOrderId) {
		List<Record> list = EfunOrderDetail.dao.findEfunOrderDetail(userId, efunOrderId, null);
		if(null != list && list.size() > 0) { // 订单存在.
			for (int j = 0, size = list.size(); j < size; j++) {
				if(null == list.get(j).getStr("order_id")) { // 未转为订单.
					orderList.add(list.get(j)); // 添加当前订单到有效列表.
				}
			}
		}
	}
	
}