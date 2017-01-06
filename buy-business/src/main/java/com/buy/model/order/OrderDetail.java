package com.buy.model.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.SysParam;
import com.buy.model.product.ProBackSort;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
/**
 * 订单详情
 * @author huangzq
 *
 */
public class OrderDetail extends Model<OrderDetail>{

	/** 折扣类型 - 普通折扣（翻牌） **/
	public final static int DISCOUNT_TYPE_NORMAL = 1;
	/** 折扣类型 - 一折购中奖 **/
	public final static int DISCOUNT_TYPE_WIN = 2;
	/** 折扣类型 - 门店折扣 **/
	public final static int DISCOUNT_TYPE_O2O = 3;

	private static final long serialVersionUID = 1L;
	
	public static final OrderDetail dao = new OrderDetail();
	
	/**
	 * 新增订单明细
	 * @param orderId 订单ID
	 * @param productId 商品ID
	 * @param productNo 商品编号
	 * @param productName 商品名称
	 * @param productImg 商品图片
	 * @param skuCode 商品SKU识别码
	 * @param productProperty 商品属性值
	 * @param source 商品来源
	 * @param marketPrice 市场价
	 * @param supplierPrice 结算价
	 * @param price e趣价
	 * @param count 购买数量
	 * @param orderShopId 下单店铺ID
	 * @author Jacob
	 * 2016年5月4日下午4:33:22
	 */
	public boolean add(
			String orderId,
			int productId,
			String productNo,
			String productName,
			String productImg,
			String skuCode,
			String productProperty,
			int source,
			BigDecimal marketPrice,
			BigDecimal supplierPrice,
			BigDecimal price,
			int count,
			String orderShopId,
			BigDecimal salesDiscount,
			BigDecimal originalPrice) {
		OrderDetail orderDetail = new OrderDetail();
		orderDetail.set("order_id", orderId);//订单ID
		orderDetail.set("product_id", productId);//商品ID
		orderDetail.set("product_no", productNo);//商品编号
		orderDetail.set("product_name", productName);//商品名称
		orderDetail.set("product_img", productImg);//产品图片（sku销售属性图或商品主图）
		orderDetail.set("sku_code", skuCode);//产品sku识别码
		orderDetail.set("product_property", productProperty);//商品销售属性
		orderDetail.set("market_price", marketPrice);//产品sku市场价格
		orderDetail.set("sales_discount", salesDiscount);//销售折扣
		orderDetail.set("original_price", originalPrice);//原价（用于记录线下订单打折商品打折前的价格）
		//判断商品是否为专卖商品（设置相应的结算价，结算时使用）
		if(source==Product.SOURCE_EXCLUSIVE||source==Product.SOURCE_SELF_EXCLUSIVE){
			//获取该商品的佣金率
			BigDecimal commissionRate = ProBackSort.dao.getCommissionRate(productId);
			if(null == commissionRate){
				commissionRate = BigDecimal.ZERO;
			}
			//判断当前商品SKU是否有加入幸运一折购
			int isEfun = ProductSku.dao.findByIdLoadColumns(skuCode, "is_efun").getInt("is_efun");
			if(isEfun==BaseConstants.YES){
				//产品供货价格(结算价）（商品SKU的e趣价*(1-佣金率-一折购佣金率)）
				BigDecimal efunRate = SysParam.dao.getBigDecimalByCode("efun_commission_rate");
				orderDetail.set("supplier_price", price.multiply(new BigDecimal("1").subtract(commissionRate).subtract(efunRate)).setScale(2, BigDecimal.ROUND_DOWN));
			}else{
				//产品供货价格(结算价）（商品SKU的e趣价*(1-佣金率)）
				orderDetail.set("supplier_price", price.multiply(new BigDecimal("1").subtract(commissionRate)).setScale(2, BigDecimal.ROUND_CEILING));
			}
			/******************EV值设置*************************/
			//EV值
			//判断是否为九折购订单
			BigDecimal ev = BigDecimal.ZERO;
			ev = price.multiply(commissionRate.multiply(new BigDecimal("0.5")).multiply(new BigDecimal(count)));
			orderDetail.set("ev", ev);//EV值
		}else{
			orderDetail.set("supplier_price", supplierPrice.multiply(salesDiscount));//供货价格(结算价)
			/******************EV值设置*************************/
			//EV值(公共商品的市场价*7%*购买数量)
			BigDecimal ev = marketPrice.multiply(new BigDecimal("0.07")).multiply(new BigDecimal(count));
			orderDetail.set("ev", ev);//EV值
		}
		orderDetail.set("price", price);//产品sku价格(不管是否幸运一折购九折购买都用商品SKU的e趣价)
		orderDetail.set("count", count);//购买数量
		if(StringUtil.notNull(orderShopId)){
			orderDetail.set("order_shop_id", orderShopId);//下单店铺ID;
		}
		orderDetail.set("create_time", new Date());//创建时间
		//保存
		return orderDetail.save();
	}
	
	/**
	 * 查找订单明细表
	 * @param orderId 订单ID
	 * @return
	 * @author Jacob
	 * 2015年12月17日下午2:58:17
	 */
	public List<Record> findOrderDetailList(String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	od.id, ");
		sql.append(" 	od.product_id, ");
		sql.append(" 	od.product_name, ");
		sql.append(" 	od.count, ");
		sql.append(" 	od.price, ");
		sql.append("    od.sku_code,");
		sql.append(" 	od.product_img, ");
		sql.append(" 	od.product_property, ");
		sql.append(" 	od.sales_discount, ");
		sql.append(" 	od.original_price ");
		sql.append(" FROM ");
		sql.append(" 	t_order_detail od ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append("    od.order_id = ? ");
		return Db.find(sql.toString(),orderId);
	}
	
	public List<Record> findOrderDetailList(String orderId, String userId){
		Order order = Order.dao.findFirst("SELECT id FROM t_order WHERE id = ? AND user_id = ?", orderId, userId);
		if (StringUtil.isNull(order))
			return null;
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	od.id, ");
		sql.append(" 	od.product_id, ");
		sql.append(" 	od.product_name, ");
		sql.append(" 	od.count, ");
		sql.append(" 	od.price, ");
		sql.append(" 	od.product_img, ");
		sql.append(" 	od.product_property ");
		sql.append(" FROM ");
		sql.append(" 	t_order_detail od ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append("    od.order_id = ? ");
		return Db.find(sql.toString(),orderId);
	}
	
	/**
	 * 获取订单明细的商品SKU识别码和购买数量列表
	 * @param orderId
	 * @return
	 * @author Jacob
	 * 2016年4月7日下午3:46:11
	 */
	public List<Record> findSkuCodeList(String orderId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	od.sku_code skuCode, ");
		sql.append(" 	od.count ");
		sql.append(" FROM ");
		sql.append(" 	t_order_detail od ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append("    od.order_id = ? ");
		return Db.find(sql.toString(),orderId);
	}

	/**
	 * 根据商品编号获取订单ID集合
	 * @param productNo 商品编号
	 * @return 订单ID集合
	 */
	public String getOrderIdsByProductName(String productNo) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT o.id FROM t_order_detail d LEFT JOIN t_order o ON o.id = d.order_id");
		sql.append(" WHERE d.product_no LIKE CONCAT ('%', ?, '%')");
		List<String> oidList = Db.query(sql.toString(), productNo);
		if (StringUtil.notNull(oidList) && oidList.size() > 0) {
			return StringUtil.listToStringForSql(",", oidList);
		} else {
			return "-1";
		}
	}
	
	/**
	 * 获取订单明细销售量
	 * @return
	 */
	public List<OrderDetail> findOrderDetailSaleCount(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	product_id, ");
		sql.append(" 	sku_code, ");
		sql.append(" 	count ");
		sql.append(" FROM t_order_detail ");
		sql.append(" WHERE order_id = ? ");
		return OrderDetail.dao.find(sql.toString(), orderId);
	}

	/**
	 * 订单详情中，是否所有商品中奖
	 * 0 没有中奖商品； 1 有中奖商品； 2 所有商品为中奖商品
	 */
	public int hasWinPro(String orderId) {
		long all = Db.queryLong(" SELECT COUNT(1) FROM t_order_detail WHERE order_id = ? ", orderId);
		long win = Db.queryLong(" SELECT COUNT(1) FROM t_order_detail WHERE order_id = ? AND discount_type = ? ", orderId, DISCOUNT_TYPE_WIN);
		if (win == 0) 			return 0;		// 没有中奖商品
		else if (all != win) 	return 1;		// 有中奖商品
		else if (all == win) 	return 2;		// 所有商品为中奖商品
		else 					return 0;		// 没有中奖商品
	}
	
	/**
	 * 查看详情是否属于一折购中奖
	 * @param detailId
	 * @return
	 */
	public boolean isEfunOrderDetail(Integer detailId) {
		return StringUtil.notNull(OrderDetail.dao.findByIdLoadColumns(detailId, "efun_id")) ? true : false;
	}
}
