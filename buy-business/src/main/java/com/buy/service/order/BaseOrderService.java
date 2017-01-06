package com.buy.service.order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.common.MqUtil;
import com.buy.common.Ret;
import com.buy.common.constants.MqConstants;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.logistics.LogisticsCompany;
import com.buy.model.order.Order;
import com.buy.model.order.OrderDetail;
import com.buy.model.order.OrderLog;
import com.buy.model.order.OrderReturn;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.store.Store;
import com.buy.model.supplier.Supplier;
import com.buy.model.user.RecAddress;
import com.buy.model.user.User;
import com.buy.plugin.event.sms.user.ShopOrderApplyReturnEvent;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.dreamlu.event.EventKit;

/**
 *订单公共service
 * @author chenhg
 */
public class BaseOrderService {

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     *
     *    付款、扣库存
     *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 立即购买点击确认付款，扣库存
     * @param userId
     * @param orderIds
     * @return
     * @throws SQLException
     * @author huangzq
     * 2017年1月2日 上午10:52:07
     *
     */
    @Before(Tx.class)
    public JsonMessage comfirmPayAddLockCount(String userId,String... orderIds) throws SQLException{
        JsonMessage jsonMessage = new JsonMessage();
        
        //组装数据
  		List<Record> skuList = new ArrayList<Record>();
  		
  		for(String orderId : orderIds){
  			String sql = "select rd.sku_code from t_order_detail rd left join  t_order r on rd.order_id = r.id where r.status = ? and r.trade_status = ? and r.user_id = ? and r.id = ?";
  			Record order  = Db.findFirst(sql,Order.STATUS_WAIT_FOR_PAYMENT,Order.TRADE_NORMAL,userId,orderId);
  			if(order == null){
  				jsonMessage.setStatusAndMsg("1", "订单不存在");
  				return jsonMessage;
  			}
  			Record sku = ProductSku.dao.getSkuForSubmitOrder(order.getStr("sku_code"));	
  			skuList.add(sku);
  			
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
  		Map<String,Integer> succesOrderMap = new HashMap<String,Integer>();
  		for(String orderId : orderIds){
  			Order order = Order.dao.getOrderForUpdate(orderId);
  			int deliveryType = order.getInt("delivery_type");
  			//是否加了锁定库存
  			if(order.getInt("is_release_lock_count") == BaseConstants.YES){
  				jsonMessage  = ProductSku.dao.addLockCountForOrderId(deliveryType, order.getStr("o2o_shop_address"), orderId);
  				if(!jsonMessage.getStatus().equals("0")){
  					List<Record> skus = (List<Record>) jsonMessage.getData();
  					noStoreSkus.addAll(skus);
  				}else{
  					order.set("is_release_lock_count", BaseConstants.NO);
  	      			order.update();
  	      			succesOrderMap.put(orderId,deliveryType);
  				}
  			}else{
  				succesOrderMap.put(orderId,deliveryType);
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
  		
  		//订单锁定库存5分钟后释放
  		for(String orderId : succesOrderMap.keySet()){
			MqUtil.send(MqConstants.Queue.ORDER_LOCK_STORE_DELAY, orderId+","+succesOrderMap.get(orderId));
  		}
  		return jsonMessage;
       
   
    }

    /**
     * 我的订单立即付款（检查库存是否足够）
     * @param orderId
     * @return
     * @author chenhg
     * 2016年11月21日 上午11:53:42
     */
    public JsonMessage checkLockCount(String orderId){
        JsonMessage jm = new JsonMessage();
        Order order = Order.dao.findById(orderId);

        //九折购订单，一折购订单没有释放
        if(order.getInt("is_efun_nine") == BaseConstants.YES
                && EfunUserOrder.dao.getIsReleaseLockCountByOrderId(orderId) == EfunUserOrder.IS_RELEASE_LOCK_COUNT_N){
            return jm;
        }
        //锁定库存已经释放
        if(order.getInt("is_release_lock_count") == Order.IS_RELEASE_LOCK_COUNT_Y){
            List<Record> skuList= OrderDetail.dao.findSkuCodeList(orderId);
            for(Record sku : skuList){
                if(!ProductSku.dao.enoughCount(sku.getStr("skuCode"), sku.getInt("count"))){
                    jm.setStatusAndMsg("1", "库存不足，请联系商家补货");
                    jm.setData(ProductSku.dao.getSkuInventoryMessage(sku.getStr("skuCode")));
                    return jm;
                }
            }

        }

        return jm;
    }

     /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      取消订单
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 处理交易关闭订单（1.更新订单交易状态；2.退款；3.恢复库存）
     * @param orderIds 订单id(可能多个--购物车提交后取消订单)
     * @param tradeType 订单交易类型
     * @param refundType 退货订单类型
     * @param isAutoClose 是否自动关闭（用于自动取消订单）
     * @author chenhg
     * @throws SQLException
     */
    @Before(Tx.class)
    public void cancelOrder(String orderIds,Integer tradeType,Integer refundType ,boolean isAutoClose, String userId, String dataFrom) throws SQLException{
        String[] orderIdArr = orderIds.split(",");
        for(String orderId : orderIdArr){
            Order.dao.handleTransactionClosedOrder(orderId, tradeType, refundType, isAutoClose, userId, dataFrom);
        }
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      发货设置 / 发货
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 商品指定云店发货
     * @param order
     * @param appointStoreNo
     * @param skuCodeCountList
     * @return
     * @throws SQLException
     */
    @Before(Tx.class)
    public boolean productAppointStoreDeliver(Order order, String appointStoreNo, List<Record> skuCodeCountList) throws SQLException {
        // 转换锁定库存数   批量检查能否转换
        if (ProductSku.dao.transferLockCountForMany(appointStoreNo, skuCodeCountList)) {
            String orderId = order.getStr("id");
            // 发货云店地址
            Record storeAddress = Store.dao.getStoreAddressByNo(appointStoreNo);
            // 收货地址编码
            Record toAddress = Order.dao.getToAddressCodeById(orderId);
            // 产品Map 用于计算运费
            Map<String, Integer> productMap = new HashMap<String, Integer>();
            for(Record od : skuCodeCountList){
                productMap.put(od.getStr("skuCode"), od.getNumber("count").intValue());
            }

            order.set("o2o_shop_no", appointStoreNo);
            order.set("o2o_shop_name", storeAddress.getStr("name"));
            order.set("o2o_shop_address", storeAddress.getStr("address"));
            order.set("distribution_time", new Date());
            order.set("store_freight", FreightTemplate.dao.calculate(productMap,
                    appointStoreNo, toAddress.getInt("provinceCode"),
                    toAddress.getInt("cityCode"), toAddress.getInt("areaCode")));
            order.update();

            return true;
        } else { // 转换锁定库存数不成功，事务回滚，转到商家自行分配
            DbKit.getConfig().getConnection().rollback();
            order.update();
            return false;
        }
    }

    /**
     * 订单按照发货规则发货
     * @param orderId
     * 			订单ID
     * @param addressId
     * 			收货地址ID
     * @param is_efun_order
     * @throws SQLException
     */
    @Before(Tx.class)
    public boolean orderDeliverByRule(String orderId, Integer addressId, String is_efun_order) throws SQLException {
        Order order = new Order();
        order.set("id", orderId);

        // 订单sku和购买数量
        List<Record> skuCodeCountList = null;
        // 	TODO 后续一折购订单流程可能有变化，暂时不查询
        if (is_efun_order.equals("true")) {
            skuCodeCountList = EfunUserOrder.dao.findSkuCodeList(orderId);
        } else {
            skuCodeCountList = OrderDetail.dao.findSkuCodeList(orderId);
        }

        Integer deliverRule = 0;
        String appointStoreNo = "";
        // 产品Map 用于计算运费
        Map<String, Integer> productMap = new HashMap<String, Integer>();
        boolean isMix = false;// 标识是否混合规则（同一订单多个商品发货规则不一样）

        // 一张订单多个购买多个sku商品的时候，当多个商品发货规则不一致。则订单转到商家自行分配
        if (skuCodeCountList.size() > 1) {
            List<Integer> ruleList = new ArrayList<>();
            for (Record record : skuCodeCountList) {
                String skuCode = record.getStr("skuCode");
                productMap.put(skuCode, record.getNumber("count").intValue());
                ruleList.add(ProductSku.dao.getDeliverRuleByCode(skuCode));
            }

            /**
             * 四种混合规则
             * 1-2-3
             * 1-2
             * 1-3
             * 2-3
             */
            if (ruleList.contains(ProductSku.RULE_AUTO) && ruleList.contains(ProductSku.RULE_APPOINT) && ruleList.contains(ProductSku.RULE_SELF)) {
                isMix = true;
            } else if (ruleList.contains(ProductSku.RULE_AUTO) && ruleList.contains(ProductSku.RULE_APPOINT)) {
                isMix = true;
            } else if (ruleList.contains(ProductSku.RULE_AUTO) && ruleList.contains(ProductSku.RULE_SELF)) {
                isMix = true;
            } else if (ruleList.contains(ProductSku.RULE_APPOINT) && ruleList.contains(ProductSku.RULE_SELF)) {
                isMix = true;
            }
            // 当存在混合规则情况，转到商家自行分配，不再往下执行
            if (isMix) {
                return false;
            }

            // 当所有发货规则为指定云店发货，检查商品指定的发货云店是否统一
            if (!isMix && ruleList.contains(ProductSku.RULE_APPOINT)) {
                String compareStoreNo = "";
                boolean isSameStore = true;
                for (Record record : skuCodeCountList) {
                    appointStoreNo = ProductSku.dao.getAppointStoreNo(record.getStr("skuCode"));
                    if (StringUtil.isNull(compareStoreNo)) {
                        compareStoreNo = appointStoreNo;
                    } else {
                        if (!compareStoreNo.equals(appointStoreNo)) {
                            isSameStore = false;
                            break;
                        }
                    }
                }
                if (!isSameStore) {
                    return false;
                } else {
                    deliverRule = ProductSku.RULE_APPOINT;
                }
            } else {
                deliverRule = ruleList.get(0);
            }
        } else if (skuCodeCountList.size() == 1) {
            Record record = skuCodeCountList.get(0);
            String skuCode = record.getStr("skuCode");
            deliverRule = ProductSku.dao.getDeliverRuleByCode(skuCode);
            productMap.put(skuCode, record.getNumber("count").intValue());
        } else {
            return false;
        }

        boolean result = false;
        if (deliverRule == ProductSku.RULE_AUTO) {// 自动分配（系统分配）
            // 进入自动分配云店或商家发货方法
            result = Store.dao.autoDistributionDeliver(order, skuCodeCountList, addressId);

        } else if (deliverRule == ProductSku.RULE_APPOINT) {// 指定云店发货

            appointStoreNo = ProductSku.dao.getAppointStoreNo(skuCodeCountList.get(0).getStr("skuCode"));

            // 收货地址所在省编号
            int toProvinceCode = RecAddress.dao.findByIdLoadColumns(addressId, "province_code").getInt("province_code");
            // 指定发货云店所在省编号
            Integer appointProvCode = Store.dao.getStoreByNo(appointStoreNo).getInt("province_code");

            // 收货地址与指定发货云店属于同一省份
            if (appointProvCode == toProvinceCode) {
                // 转换锁定库存数   批量检查能否转换
                if (ProductSku.dao.transferLockCountForMany(appointStoreNo, skuCodeCountList)) {
                    // 发货云店地址
                    Record storeAddress = Store.dao.getStoreAddressByNo(appointStoreNo);
                    // 收货地址编码
                    Record toAddress = Order.dao.getToAddressCodeById(orderId);

                    order.set("o2o_shop_no", appointStoreNo);
                    order.set("o2o_shop_name", storeAddress.getStr("name"));
                    order.set("o2o_shop_address", storeAddress.getStr("address"));
                    order.set("distribution_time", new Date());
                    order.set("store_freight", FreightTemplate.dao.calculate(productMap,
                            appointStoreNo, toAddress.getInt("provinceCode"),
                            toAddress.getInt("cityCode"), toAddress.getInt("areaCode")));
                    order.update();
                    result = true;
                } else { // 转换锁定库存数不成功，事务回滚，转到商家自行分配
                    DbKit.getConfig().getConnection().rollback();
                    return false;
                }
            }
        } else if (deliverRule == ProductSku.RULE_SELF) {// 自行发货（自行分配）
            // 自行发货（不进行任何操作，让商家自己处理）
        }
        return result;
    }

    /**
     * 订单发货仓库设置
     * 分配成功：减去商品锁定库存数，增加仓库商品锁定库存数
     * @param orderId 订单
     * @param addressId 收货地址ID
     * @author Jacob
     * 2016年3月29日下午1:45:24
     * @throws SQLException
     */
    @Before(Tx.class)
    public boolean orderStore(String orderId, Integer addressId) throws SQLException{
        Order order = new Order();
        order.set("id", orderId);
        System.out.println("////////////////////商城快递商品，自动选择云店发货！");
        // 根据收货地址ID获取区、市、省编号
        RecAddress proCityArea = RecAddress.dao.findByIdLoadColumns(addressId, "province_code, city_code, area_code");
        // 获取订单明细的商品SKU识别码和购买数量列表
        List<Record> skuCodeCountList = OrderDetail.dao.findSkuCodeList(orderId);
        // 收货地址所在省编号
        int provinceCode = proCityArea.getInt("province_code");
        // 收货地址所在市编号
        int cityCode = proCityArea.getInt("city_code");
        // 收货地址所在区编号
        int areaCode = proCityArea.getInt("area_code");

        /****先判断收货地址所在省份是否有满足发货的分仓******/
        // 根据收货地址所在区编号获取该区县的所有分仓编号
        List<String> provinceStoreNoList = Store.dao.findNoListByProvinceCode(provinceCode, orderId);
        // 获取收货地址所在省份的其他省份的分仓编号
        List<String> otherProvinceStoreNoList = Store.dao.findNoListByProvinceCodeForOther(provinceCode, orderId);
        // 获取总仓编号
        String totolStoreNo = Store.dao.getTotalStoreNo();

        boolean result = false;
        if (StringUtil.notNull(provinceStoreNoList) && provinceStoreNoList.size() > 0) {
            // 云店同省发货
            result = Store.dao.storeDeliverByProv(provinceCode, cityCode, areaCode, orderId, totolStoreNo, skuCodeCountList, order);

            /********判断其它省份是否有发货的分仓*******/
        } else if (StringUtil.notNull(otherProvinceStoreNoList) && otherProvinceStoreNoList.size() > 0) {
            // 根据其他省份云店列表进行分配发货
            result = Store.dao.distributionDeliveryByStoreNoList(otherProvinceStoreNoList, totolStoreNo, skuCodeCountList, order);

            /********没有满足条件的分仓时最后用总仓进行发货********/
        } else {
            //不设置设置发货仓库，交给admin后台发货处理
        }
        // 更新订单
        order.update();
        return result;
    }

    /**
     * 订单发货
     * @param orderId				订单ID
     * @param merchantId			商家ID
     * @param merchantType			商家类型
     * @param logisticsId			物流单ID
     * @param logisticsNo			物流单号
     * @param logisticsCompany		物流公司
     * @param dataFrom				APP来源
     */
    @Before(Tx.class)
    public JsonMessage deliverGoods(String orderId, String merchantId, int merchantType,
                                    int logisticsId, String logisticsNo, String logisticsCompany, String dataFrom) throws SQLException {
        JsonMessage result = new JsonMessage();

		/*
			查询订单信息
		 */

        StringBuffer sql = new StringBuffer(" SELECT * FROM t_order ")
                .append(" WHERE order_type BETWEEN ? AND ? ")
                .append(" AND id = ? ")
                .append(" AND merchant_id = ? ")
                .append(" AND status = ? ")
                .append(" AND trade_status = ? ")
                .append(" AND delivery_type = ? ")
                .append(" AND (send_time IS NULL OR send_time = '0000-00-00 00:00:00') ")
                .append(" FOR UPDATE ");

        List<Object> paraList = new ArrayList<Object>();
        String sendUser = "";										// 发短信人
        if (User.FRONT_USER_SHOP == merchantType) {					// 参数 - 订单类型
            paraList.add(Order.TYPE_SHOP);
            paraList.add(Order.TYPE_SELF_SHOP);

            sendUser = Shop.dao.getShopKeeper(merchantId);
        } else if (User.FRONT_USER_SUPPLIER == merchantType) {
            paraList.add(Order.TYPE_SELF_PUBLIC);
            paraList.add(Order.TYPE_SUPPLIER_SEND);

            sendUser = Supplier.dao.findByIdLoadColumns(merchantId, "name").getStr("name");
        } else {
            return result.setStatusAndMsg("1", "非法提交");
        }
        paraList.add(orderId);                                      // 订单ID
        paraList.add(merchantId);									// 商家ID
        paraList.add(Order.STATUS_WAIT_FOR_SEND);					// 待发货
        paraList.add(Order.TRADE_NORMAL);							// 正常订单
        paraList.add(Order.DELIVERY_TYPE_EXPRESS);					// 配送订单

        Order order = Order.dao.findFirst(sql.toString(), paraList.toArray());
        if (StringUtil.isNull(order))
            return result.setStatusAndMsg("2", "订单不存在或订单处于非待发货状态");
        order.set("logistics_id", logisticsId).set("logistics_no", logisticsNo).set("logistics_company", logisticsCompany);

		/*
			操作
		 */

        // 扣取库存
        List<Record> skuCodeCountList = OrderDetail.dao.findSkuCodeList(orderId);

        // 超卖订单
        boolean flag = true;
        if(order.getInt("is_over_sell") == Order.IS_OVER_SELL){
            if(ProductSku.dao.subtractVirtualCountForManyAndOversold(skuCodeCountList)) {
                Order.dao.deliver(order, sendUser, dataFrom);		// 发货推送、发短信
                return result;
            } else{
                flag = false;
            }
        }

        // 非超卖订单
        else {
            if (ProductSku.dao.subtractVirtualCountForMany(skuCodeCountList)) {
                Order.dao.deliver(order, sendUser, dataFrom);		// 发货推送、发短信
                return result;
            } else {
                flag = false;
            }
        }

        // 事务回滚
        if (!flag) {
            DbKit.getConfig().getConnection().rollback();
            List<Record> skuList = new ArrayList<Record>();
            for (Record r :skuCodeCountList ) {
                String skuCode = r.getStr("skuCode");
                Integer count = r.getInt("count");
                if (!ProductSku.dao.enoughVirtualCountForSend(skuCode, count)) {
                    skuList.add(ProductSku.dao.getSkuInventoryMessageForSend(skuCode));
                }
            }
            result.setData(skuList);
            result.setStatusAndMsg("3", "虚拟库存不足，请增大虚拟库存再发货");
        }

        return result;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      会员确认收货
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     *  更新 - 会员确认收货
     * @param orderIds      订单ID集
     * @param userId        会员ID
     * @param dataFrom      APP来源
     */
    @Before(Tx.class)
    public JsonMessage batcheceiveGoodsByUser(String orderIds, String userId, String dataFrom) {
        JsonMessage result = new JsonMessage();

        /*
            查询订单信息
         */

        if (StringUtil.isNull(orderIds))
            return result.setStatusAndMsg("1", "非法提交");

        String[] orderIdList = orderIds.split(",");
        List<Object> lockParaList = new ArrayList<Object>();
        StringBuffer lock = new StringBuffer(" SELECT * FROM t_order WHERE id IN ( ");
        for (String id : orderIdList) {
            lock.append("?,");
            lockParaList.add(id);
        }
        lock.deleteCharAt(lock.length() -1);
        lock.append(" ) AND user_id = ? FOR UPDATE ");
        lockParaList.add(userId);

        List<Order> orderList = Order.dao.find(lock.toString(), lockParaList.toArray());
        if (StringUtil.isNull(orderList))
            return result.setStatusAndMsg("1", "订单不存在");

        /*
            验证订单是否全部可收货
         */

        for (Order o : orderList) {
            int status = o.getInt("status");
            if (StringUtil.isNull(status) || status != Order.STATUS_HAD_SEND) {
                return result.setStatusAndMsg("2", "部分订单状态已过期，请刷新订单后再次操作");
            }

            int tradeStatus = o.getInt("trade_status");
            if (StringUtil.isNull(tradeStatus) || tradeStatus!=Order.TRADE_NORMAL) {
                return result.setStatusAndMsg("2", "部分订单状态已过期，请刷新订单后再次操作");
            }
        }

        /*
            操作
        */

        Date now = new Date();
        for (Order o : orderList) {
            String id = o.get("id");

            // 添加订单日志-确认收货
            OrderLog.dao.add(id, "order_log_receive", dataFrom);

            // 改变订单状态
            o.set("status", Order.STATUS_WAIT_FOR_EVALUATION).set("comfirm_time", now).update();

            // 结算处理
            int hasSettle = Order.dao.settlement(id);

            // 增加商品销售量处理
            boolean isSettle = hasSettle == BaseConstants.YES;
            Product.dao.plusSalesSettle(id, isSettle);
        }

        return  result;
    }

    /**
     * 延长收货
     */
    @Before(Tx.class)
    public JsonMessage delayConfirmReceipt(String  orderId, String userId, String dataFrom) {
        JsonMessage result = new JsonMessage();
        String tipEnd = "，请点击确定进行刷新。";

        // 验证 - 待收货
        Order order = Order.dao.getRecievedInfo(orderId, userId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单状态已变更" + tipEnd);
            return result;
        }

        // 验证 - 配送订单
        int deliveryType = order.getInt("delivery_type");
        if (Order.DELIVERY_TYPE_EXPRESS != deliveryType) {
            result.setStatusAndMsg("2", "订单为非配送订单，不能延长收货" + tipEnd);
            return result;
        }

        // 验证 - 发货日期和最迟收货时间
        Date dateSend = order.get("send_time");
        Date dateRecieved = order.get("recieved_till_time");
        if (StringUtil.isNull(dateSend) && StringUtil.isNull(dateRecieved)) {
            result.setStatusAndMsg("3", "延长收货参数错误" + tipEnd);
            return result;
        }
        int days = DateUtil.getIntervalDays(dateSend, dateRecieved);

        // 验证 - 延期收货
        Integer recieved = SysParam.dao.getIntByCode("recevied_till_day_num");
        if (StringUtil.isNull(recieved)) {
            result.setStatusAndMsg("3", "延长收货参数错误" + tipEnd);
            return result;
        }
        else if (days > recieved) {
            result.setStatusAndMsg("4", "订单已延长收货，不能延长收货" + tipEnd);
            return result;
        }
        Integer delay = SysParam.dao.getIntByCode("delay_receipt_day");
        if (StringUtil.isNull(delay)) {
            result.setStatusAndMsg("3", "延长收货参数错误" + tipEnd);
            return result;
        }

        // 设置延长收货时间
        dateRecieved = DateUtil.addDay(dateRecieved, delay);
        order.set("recieved_till_time", dateRecieved);
        order.update();

        // 添加订单日志-延长收货时间
        OrderLog.dao.add(orderId, OrderLog.CODE_DELAY_RECEIVE, dataFrom);

        return result;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      退款退货
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 申请退款
     * @param orderId 订单ID
     * @param userId 会员ID
     * @param reason 退款原因
     * @author Jacob
     * 2015年12月24日下午4:53:52
     * @throws SQLException
     * 从Order迁移 Sylveon
     */
    @Before(Tx.class)
    public JsonMessage applyRefund(String orderId,String userId,String reason, String dataFrom) throws SQLException {
        JsonMessage jsonMessage = new JsonMessage();

        //获取订单(避免并发)
        Order order = Order.dao.getOrder4UserForUpdate(orderId, userId);

        if(order!=null){
            /***************hgchen****************** 1) 验  证  *******************hgchen***********************/

            int hasWin = OrderDetail.dao.hasWinPro(orderId);
            if (2 == hasWin)
                return jsonMessage.setStatusAndMsg("4", "中奖商品不能退货");


            //判断是否有在申请中的记录了
            if(order.getInt("trade_status")==Order.TRADE_RETURNING_MONEY){
                jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                return jsonMessage;
            }else{
                int status = order.getInt("status");
                int deliveryType = order.getInt("delivery_type");
                //判断当前订单如果是快递，是否为待发货状态，如果是自提，是否为已发货状态
                if(!((status == Order.STATUS_WAIT_FOR_SEND && deliveryType == Order.DELIVERY_TYPE_EXPRESS)
                        || (deliveryType == Order.DELIVERY_TYPE_SELF && status == Order.STATUS_HAD_SEND)
                        || (deliveryType == Order.DELIVERY_TYPE_SELF &&
                        status == Order.STATUS_WAIT_FOR_SEND &&
                        order.getInt("is_over_sell") == Order.IS_OVER_SELL))){
                    jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                    return jsonMessage;
                }
                //判断订单是否是当前会员的订单
                if(!order.getStr("user_id").equals(userId)){
                    jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                    return jsonMessage;
                }

                /******************【2.生成申请退款记录】*******************/
                OrderReturn orderReturn = new OrderReturn();
                orderReturn.set("no", StringUtil.getUnitCode(OrderReturn.NO_PREFIX));//生成编号
                orderReturn.set("user_id", userId);//会员ID
                orderReturn.set("order_id", orderId);//订单id
                orderReturn.set("merchant_id", order.getStr("merchant_id"));//商家id
                orderReturn.set("apply_time", new Date());//申请时间
                orderReturn.set("return_type", OrderReturn.TYPE_REFUND);//退款类型：1.退款，2.退货
                orderReturn.set("return_reason", reason);//退款原因
                orderReturn.set("cash", order.getBigDecimal("total").add(order.getBigDecimal("freight")));//退款金额(商品总额+运费)
                /**
                 * 自提订单：申请时就释放仓库锁定库存、直接退款成功、订单关闭
                 */
                if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
                    //订单交易关闭；释放仓库锁定库存；退款成功
                    order.set("trade_status", Order.TRADE_RETURN_MONEY_SUCCESS);//订单关闭
                    orderReturn.set("return_status", OrderReturn.RETURN_STATUS_SUCCESS);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））

                    //对于超单的，因为没有加锁定库存，所以不用扣回去
                    if(order.getInt("is_over_sell") == BaseConstants.NO){
                        String storeNo = order.getStr("o2o_shop_no");
                        List<Record> skuCodeList = OrderDetail.dao.findSkuCodeList(orderId);
                        for(Record od : skuCodeList){
                            boolean flag = ProductSku.dao.subtractStoreLockCount(storeNo, od.getStr("skuCode"), od.getInt("count"));
                            if(!flag){
                                //事务回滚
                                DbKit.getConfig().getConnection().rollback();
                                return jsonMessage.setStatusAndMsg("3", "库存异常，请刷新订单后再次操作");
                            }
                        }
                    }
                    //退款操作（积分、账户金额、第三方支付）
                    Order.dao.returnRefundForDeliverySelf(order);
                    //TODO 推送到pos

                }else{//快递
                    order.set("trade_status", Order.TRADE_RETURNING_MONEY);//退款中
                    orderReturn.set("return_status", OrderReturn.RETURN_STATUS_APPLY);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））
                }

                // 判断操作
                Integer returnId = OrderReturn.dao.getIdByOrderId(orderId);
                if (returnId == null) {
                    orderReturn.save();			// 添加
                } else {
                    orderReturn.set("id", returnId);
                    orderReturn.update();		// 修改
                }

                /******************【2.改变订单交易状态】*******************/

                order.update();
                /******************【3.生成订单日志】*******************/
                OrderLog.dao.add(orderId, "order_log_apply_refund", dataFrom);


                /******************发短信通知买家退款*******************/
                Integer orderType = order.getInt("order_type");// 订单类型
                // 店铺订单，自营专卖订单，厂家自发订单 才需要短信提醒
                if (orderType == Order.TYPE_SELF_SHOP || orderType == Order.TYPE_SHOP || orderType == Order.TYPE_SUPPLIER_SEND) {
                    String merchantId = order.getStr("merchant_id");// 商家ID
                    Ret ret = new Ret();
                    // 触发短信来源
                    ret.put("dataFrom", dataFrom);
                    // 卖家ID
                    ret.put("merchantId", merchantId);
                    // 订单编号
                    ret.put("orderNo", order.getStr("no"));
                    ret.put("orderType", orderType);
                    ret.put("SMSremark", "退款");
                    EventKit.postEvent(new ShopOrderApplyReturnEvent(ret));
                }
            }
        }else{
            jsonMessage.setStatusAndMsg("2", "订单不存在");
        }

        return jsonMessage;
    }

    /**
     * 申请退货
     * @param orderId 订单ID
     * @param userId 会员ID
     * @param reason 退货原因
     * @author Jacob
     * 2015年12月24日下午4:56:19
     * 从Order迁移 Sylveon
     */
    @Before(Tx.class)
    public JsonMessage applyReturn(String orderId,String userId,String reason, String dataFrom){

        JsonMessage jsonMessage = new JsonMessage();

        //获取订单//获取订单(避免并发)
        Order order = Order.dao.getOrder4UserForUpdate(orderId, userId);

        if(order!=null){
            int hasWin = OrderDetail.dao.hasWinPro(orderId);
            if (2 == hasWin)
                return jsonMessage.setStatusAndMsg("4", "中奖商品不能退款");

            Integer delivery_type = order.getInt("delivery_type");
            if (Order.DELIVERY_TYPE_SELF == delivery_type) {
                jsonMessage.setStatusAndMsg("3", "自提订单不能退货");
                return jsonMessage;
            }else{
                //判断是否有在申请中的记录了
                if(order.getInt("trade_status")==Order.TRADE_RETURNING_GOODS){
                    jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                    return jsonMessage;
                }else{

                    //判断当前订单是否待发货状态
                    if(order.getInt("status") != Order.STATUS_HAD_SEND){
                        jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                        return jsonMessage;
                    }
                    //判断订单是否是当前会员的订单
                    if(!order.getStr("user_id").equals(userId)){
                        jsonMessage.setStatusAndMsg("1", "订单状态已过期，请刷新订单后再次操作");
                        return jsonMessage;
                    }

                    /******************【1.改变订单交易状态】*******************/
                    order.set("trade_status", Order.TRADE_RETURNING_GOODS);//退货中
                    Date now = new Date();
                    Long remianRecievedmilliseconds = order.getDate("recieved_till_time").getTime()-now.getTime();
                    order.set("remian_recieved_milliseconds", remianRecievedmilliseconds);//设置剩余截至收货时间
                    order.set("recieved_stop_type", Order.RECIEVED_STOP_TYPE_RETURN);//设置订单截至收货时间暂停状态
                    order.update();
                    /******************【2.生成申请退款记录】*******************/
                    OrderReturn orderReturn = new OrderReturn();
                    orderReturn.set("no", StringUtil.getUnitCode(OrderReturn.NO_PREFIX));//生成编号
                    orderReturn.set("user_id", userId);//会员ID
                    orderReturn.set("order_id", orderId);//订单id
                    orderReturn.set("merchant_id", order.getStr("merchant_id"));//商家id
                    orderReturn.set("apply_time", new Date());//申请时间
                    orderReturn.set("return_type", OrderReturn.TYPE_RETURN_GOOD);//退款类型：1.退款，2.退货
                    orderReturn.set("return_reason", reason);//退款原因
                    orderReturn.set("return_status", OrderReturn.RETURN_STATUS_APPLY);//状态（0：申请中，1：等待买家寄回，2：等待卖家确认收货，3：退款成功(包括退换)，4：退款失败（包括退换））
                    orderReturn.set("cash", order.getBigDecimal("total"));//退款金额(商品总额，退货将不退还运费)
                    // 判断操作
                    Integer returnId = OrderReturn.dao.getIdByOrderId(orderId);
                    if (returnId == null) {
                        orderReturn.save();			// 添加
                    } else {
                        orderReturn.set("id", returnId);
                        orderReturn.update();		// 修改
                    }
                    /******************【3.生成订单日志】*******************/
                    OrderLog.dao.add(orderId, "order_log_return_good", dataFrom);


                    /******************发短信通知买家退货*******************/
                    Integer orderType = order.getInt("order_type");// 订单类型
                    // 店铺订单，自营专卖订单，厂家自发订单 才需要短信提醒
                    if (orderType == Order.TYPE_SELF_SHOP || orderType == Order.TYPE_SHOP || orderType == Order.TYPE_SUPPLIER_SEND) {
                        String merchantId = order.getStr("merchant_id");// 商家ID
                        Ret ret = new Ret();
                        // 触发短信来源
                        ret.put("dataFrom", dataFrom);
                        // 卖家ID
                        ret.put("merchantId", merchantId);
                        // 订单编号
                        ret.put("orderNo", order.getStr("no"));
                        ret.put("orderType", orderType);
                        ret.put("SMSremark", "退货");
                        EventKit.postEvent(new ShopOrderApplyReturnEvent(ret));
                    }
                }
            }
        }else{
            jsonMessage.setStatusAndMsg("2", "订单不存在");
        }

        return jsonMessage;
    }

    /**
     * 更新 - 同意退款
     */
    @Before(Tx.class)
    public JsonMessage accessRefund(String orderId, String merchantId, String dataFrom) {
        JsonMessage result = new JsonMessage();

        Order order = getOrderByReturn(orderId, merchantId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退款状态");
            return result;
        }

        OrderReturn orderReturn = getOrderReturn(orderId, merchantId, OrderReturn.TYPE_REFUND, OrderReturn.RETURN_STATUS_APPLY);
        if (StringUtil.isNull(orderReturn)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退款状态");
            return result;
        }

        Integer returnId = orderReturn.getInt("id");
        Order.dao.accessRefund(orderId, returnId, dataFrom);
        return result;
    }

    /**
     * 更新 - 拒绝退款
     */
    @Before(Tx.class)
    public JsonMessage refuseRefund(String orderId, String merchantId, String dataFrom) {
        JsonMessage result = new JsonMessage();

        Order order = getOrderByReturn(orderId, merchantId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退款状态");
            return result;
        }

        OrderReturn orderReturn = getOrderReturn(orderId, merchantId, OrderReturn.TYPE_REFUND, OrderReturn.RETURN_STATUS_APPLY);
        if (StringUtil.isNull(orderReturn)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退款状态");
            return result;
        }

        Integer returnId = orderReturn.getInt("id");
        Order.dao.refuseRefund(orderId, returnId, dataFrom);
        return result;
    }

    /**
     * 更新 - 同意退货
     */
    @Before(Tx.class)
    public JsonMessage sccessReturnGoods(String orderId, String merchantId, int isSendByStore, Record paras, String dataFrom) {
        JsonMessage result = new JsonMessage();

        Order order = getOrderByReturn(orderId, merchantId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退货状态");
            return result;
        }

        OrderReturn orderReturn = getOrderReturn(orderId, merchantId, OrderReturn.TYPE_RETURN_GOOD, OrderReturn.RETURN_STATUS_APPLY);
        if (StringUtil.isNull(orderReturn)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退货状态");
            return result;
        }

        if (isSendByStore == BaseConstants.YES) {
            String storeNo = Order.dao.findByIdLoadColumns(orderReturn.getStr("order_id"), "o2o_shop_no").getStr("o2o_shop_no");
            Record address = Store.dao.findAddressNameByNo(storeNo);
            orderReturn
                    .set("province",    address.getStr("provinceName"))
                    .set("city",        address.getStr("cityName"))
                    .set("area",        address.getStr("areaName"))
                    .set("address",     address.getStr("address"))
                    .set("concat",      address.getStr("principal"))
                    .set("mobile",      address.getStr("mobile"))
                    .set("store_no",    address.getStr("storeNo"));
        } else {
            orderReturn
                    .set("province",    paras.get("province"))
                    .set("city", 	    paras.get("city"))
                    .set("area", 	    paras.get("area"))
                    .set("address",     paras.get("address"))
                    .set("concat",	    paras.get("consignee"))
                    .set("mobile",	    paras.get("mobile"))
                    .set("zip",		    paras.get("zip"));
        }

        Order.dao.accessReturnGoods(orderReturn, merchantId, dataFrom);
        return result;
    }

    /**
     * 更新 - 拒绝退货
     */
    @Before(Tx.class)
    public JsonMessage refuseReturnGoods(String orderId, String shopId, String dataFrom) {
        JsonMessage result = new JsonMessage();

        Order order = getOrderByReturn(orderId, shopId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退货状态");
            return result;
        }

        OrderReturn orderReturn = getOrderReturn(orderId, shopId, OrderReturn.TYPE_RETURN_GOOD, OrderReturn.RETURN_STATUS_APPLY);
        if (StringUtil.isNull(orderReturn)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非申请退货状态");
            return result;
        }

        Integer returnId = orderReturn.getInt("id");
        Order.dao.refuseRefund(orderId, returnId, dataFrom);
        return result;
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      会员寄回商品 / 商家确认收货
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * 会员退货寄回商品
     * @param returnId
     * @param logisticsCompanyId
     * @param logisticsNo
     * @author Jacob
     * 2015年12月28日上午10:20:59
     * 从Order迁移 Sylveon
     */
    public JsonMessage saveReturnGoodsLogistics(String returnId,Integer logisticsCompanyId,String logisticsNo, String dataFrom){
        String logisticsCompany = LogisticsCompany.dao.findById(logisticsCompanyId).getStr("name");

        JsonMessage jm = new JsonMessage();
        //获取退货/退款订单
        OrderReturn orderReturn  = OrderReturn.dao.getOrderReturnForUpdate(Integer.valueOf(returnId));
        if(orderReturn.getInt("return_status") != OrderReturn.RETURN_STATUS_WAIT_BACK){
            jm.setStatusAndMsg("1", "您已填写过寄回商品物流信息");
            return jm;
        }
        orderReturn.set("return_status", OrderReturn.RETURN_STATUS_RETURNING);
        orderReturn.set("logistics_id", logisticsCompanyId);
        orderReturn.set("logistics_company", logisticsCompany);
        orderReturn.set("logistics_no", logisticsNo);
        orderReturn.set("return_goods_time", new Date());
        //更新退货订单信息
        orderReturn.update();

        // 添加订单日志-会员退货寄回商品
        String orderId = orderReturn.get("order_id");
        OrderLog.dao.add(orderId, OrderLog.CODE_RETURN_BACK, dataFrom);

        return jm;
    }

    /**
     * 更新 - 商家确认收货
     */
    @Before(Tx.class)
    public JsonMessage receiveGoodsByMerchant(String orderId, String merchantId, String dataFrom) {
        JsonMessage result = new JsonMessage();

        Order order = getOrderByReturn(orderId, merchantId);
        if (StringUtil.isNull(order)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非卖家确认收货状态");
            return result;
        }

        OrderReturn orderReturn = getOrderReturn(orderId, merchantId, OrderReturn.TYPE_RETURN_GOOD, OrderReturn.RETURN_STATUS_RETURNING);
        if (StringUtil.isNull(orderReturn)) {
            result.setStatusAndMsg("1", "订单不存在或该订单非卖家确认收货状态");
            return result;
        }

        Integer returnId = orderReturn.getInt("id");
        Order.dao.merchantSureGoods(orderId, returnId, dataFrom);
        return result;
    }

    /**
     * 刪除订单
     */
    public JsonMessage delOrder(String orderId, String userId, String dataFrom) {
        JsonMessage result = new JsonMessage();
        if(StringUtil.notNull(orderId)){
            //if (Order.dao.deleteOrder(orderId, userId)) {

                // 添加订单日志-会员删除订单
                OrderLog.dao.add(orderId, OrderLog.CODE_ORDER_DELETE, dataFrom);
                return result;
           // }
        }
       return result.setStatusAndMsg("1", "删除失败，请稍后尝试");
    }

    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
      *
      *      辅助方法
      *      1. 根据退款/退货查询订单
      *      2. 获取退单信息
      *
      * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


    /**
     * 根据退款/退货查询订单
     */
    Order getOrderByReturn(String orderId, String shopId) {
        return Order.dao.findFirst("SELECT * FROM t_order WHERE id = ? AND merchant_id = ? FOR UPDATE", orderId, shopId);
    }

    /**
     * 获取退单信息
     */
    OrderReturn getOrderReturn(String orderId, String shopId, Integer returnType, Integer returnStatus) {
        StringBuffer sql = new StringBuffer(" SELECT * FROM t_order_return WHERE 1 = 1 ");
        sql.append(" AND order_id = ? ");
        sql.append(" AND merchant_id = ? ");
        sql.append(" AND return_type = ? ");
        sql.append(" AND return_status = ? ");
        sql.append(" FOR UPDATE ");
        return OrderReturn.dao.findFirst(sql.toString(), orderId, shopId, returnType, returnStatus);
    }
    
   

}
