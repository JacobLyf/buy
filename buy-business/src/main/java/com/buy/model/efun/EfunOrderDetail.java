package com.buy.model.efun;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import org.apache.log4j.Logger;

import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 一折购参与明细表.
 */
public class EfunOrderDetail extends Model<EfunOrderDetail> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6293124067239696803L;
	private  Logger L = Logger.getLogger(EfunOrderDetail.class);
	public static final EfunOrderDetail dao = new EfunOrderDetail();
	

	/*
	 * 根据一折购参与记录ID查询订单详情ID
	 */
	public Record getOrderDetailIdByEfunOrderId(String efunOrderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ")
		   .append("   od.id detailId, ")
		   .append("   od.product_id proId, ")
		   .append("   od.order_id orderId, ")
		   .append(" FROM t_efun_order_detail eod ")
		   .append(" LEFT JOIN t_order_detail od ON od.order_id = eod.order_id ")
		   .append(" WHERE eod.efun_order_id = ? ");
		return Db.findFirst(sql.toString(), efunOrderId);
	}

	/**
	 * 查询会员一折购订单明细.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param efunOrderId
	 *            一折购订单Id.
	 * @param WhetherOrNotWin
	 *            是否中奖(true: 中奖; false: 未中奖; null: 不限制是否中奖)
	 * @author Chengyb
	 * @return 以下字段:<br>
	 *         id : 一折购订单Id<br>
	 *         user_id : 会员Id<br>
	 *         number : 分配的抽奖号<br>
	 *         win_number : 开奖号<br>
	 *         discount_val : 折扣值<br>
	 *         product_id : 商品Id<br>
	 *         sku_code : Sku编码<br>
	 *         product_img : 商品图片<br>
	 *         product_name : 商品名称<br>
	 *         product_property : 商品销售属性<br>
	 *         eq_price : 一折购价格
	 */
	public List<Record> findEfunOrderDetail(String userId, String efunOrderId, Boolean whetherOrNotWin) {
		StringBuffer sql = new StringBuffer("SELECT t.id, s.id AS detail_id, s.number, o.win_number, s.discount_val, s.begin_valid_time, t.order_shop_id, t.product_id, t.sku_code, t.product_img, t.product_name, t.product_property, t.eq_price FROM t_efun_order_detail s");
		
		// 连接一折购订单表.
		sql.append(" LEFT JOIN t_efun_user_order t ON s.efun_order_id = t.id");
		
		// 连接一折购开奖表.
		sql.append(" LEFT JOIN t_efun o ON o.id = t.efun_id");
		
		sql.append(" WHERE ");
		sql.append(" t.id=?");
		sql.append(" AND ");
		sql.append(" t.user_id=?");

		if(null != whetherOrNotWin) {
			if(whetherOrNotWin) { // 中奖订单.
				sql.append(" AND s.number != 0");
				sql.append(" AND s.number = o.win_number"); // 分配号码 == 开奖号.			
			} else { // 未中奖订单.
				sql.append(" AND s.number != 0");
				sql.append(" AND s.number != o.win_number"); // 分配号码 != 开奖号.
			}
		}
		
		sql.append(" AND now() > t.lottery_time"); // 已经到了开奖时间.
		
		return Db.find(sql.toString(), efunOrderId, userId);
	}
	
	/**
	 * 查询会员一折购折扣订单明细.
	 * 
	 * @param userId
	 *            会员Id.
	 * @param efunOrderId
	 *            一折购订单详情Id.
	 * @param WhetherOrNotWin
	 *            是否中奖(true: 中奖; false: 未中奖; null: 不限制是否中奖)
	 * @author Chengyb
	 * @return 以下字段:<br>
	 *         id : 一折购订单Id<br>
	 *         merchant_id：商家Id<br>
	 *         merchant_name：商家名称<br>
	 *         user_id : 会员Id<br>
	 *         number : 分配的抽奖号<br>
	 *         win_number : 开奖号<br>
	 *         discount_val : 折扣值<br>
	 *         product_id : 商品Id<br>
	 *         sku_code : Sku编码<br>
	 *         product_img : 商品图片<br>
	 *         product_name : 商品名称<br>
	 *         product_property : 商品销售属性<br>
	 *         eq_price : 一折购价格
	 */
	public Record findDiscountEfunOrderDetail(String userId, String efunOrderDetailId) {
		StringBuffer sql = new StringBuffer("SELECT t.id, t.merchant_id, t.merchant_name, s.order_id, s.id AS detail_id, s.number, o.win_number, s.discount_val, s.begin_valid_time, t.order_shop_id, t.product_id, t.sku_code, t.product_img, t.product_name, t.product_property, t.eq_price FROM t_efun_order_detail s");
		
		// 连接一折购订单表.
		sql.append(" LEFT JOIN t_efun_user_order t ON s.efun_order_id = t.id");
		
		// 连接一折购开奖表.
		sql.append(" LEFT JOIN t_efun o ON o.id = t.efun_id");
		
		sql.append(" WHERE ");
		sql.append(" s.id=?");
		sql.append(" AND ");
		sql.append(" t.user_id=?");

		// 未中奖订单.
		sql.append(" AND s.number != 0");
		sql.append(" AND s.number != o.win_number"); // 分配号码 != 开奖号.
		
		sql.append(" AND now() > t.lottery_time"); // 已经到了开奖时间.
		
		return Db.findFirst(sql.toString(), efunOrderDetailId, userId);
	}
	
	/**
	 * 添加详情
	 * @param efunOrderId
	 * @param number
	 * @author huangzq
	 * 2016年12月30日 上午10:48:29
	 *
	 */
	public void add(String efunOrderId,int number,Date beginValidTime){
		EfunOrderDetail detail = new EfunOrderDetail();
		detail.set("id", StringUtil.getUUID());
		detail.set("efun_order_id", efunOrderId);
		detail.set("number", number);
		detail.set("create_time", new Date());
		detail.set("begin_valid_time", beginValidTime);
		detail.save();
	}

	/**
	 * 所有商品恢复待领取/购买状态
	 * @param orderId
	 */
	public  void restoreByCancelOrder(String orderId) {
		List<String> efunOrderIdList = Db.query("SELECT DISTINCT efun_order_id FROM t_efun_order_detail WHERE order_id = ?");
		if (StringUtil.notNull(efunOrderIdList)) {
			// 恢复订单 - t_efun_user_order
			String idsStr = StringUtil.listToStringForSql(",", efunOrderIdList);
			Db.update("UPDATE t_efun_user_order SET is_win_get = ? WHERE id IN (" + idsStr + ")", BaseConstants.NO);

			// 恢复订单详情 - t_efun_order_detail
			Db.update("UPDATE t_efun_order_detail SET order_id = '' WHERE order_id = ?", orderId);
		}
	}

	//检查是否可翻牌
	public Record checkCanFlop(String userId,String discountId){
		StringBuffer sql = new StringBuffer();
		List<Object> param = new ArrayList<>();
		sql.append(" SELECT ");
		sql.append("   a.id discountId,b.id efunOrderId");
		sql.append(" FROM t_efun_order_detail a");
		sql.append("   LEFT JOIN t_efun_user_order b ON a.efun_order_id = b.id ");
		sql.append(" WHERE 1=1 ");
		sql.append("  AND b.is_real = ? AND b.`status`= ? ");
		param.add(EfunUserOrder.IS_REAL_YES);
		param.add(EfunUserOrder.STATUS_PAIED);
		sql.append("  AND b.user_id = ? AND a.id = ? ");
		param.add(userId);
		param.add(discountId);
		sql.append("  AND (a.discount_val IS NULL OR a.discount_val = '') ");
		return Db.findFirst(sql.toString(),param.toArray());

	}

	//过滤不可翻牌的
	public String filterCanFlop(String userId,String discountIds){
		String[] discountIdArray = discountIds.split(",");
		String canFlopArray = "";
		for (int i = 0, size = discountIdArray.length; i < size; i++) {
			Record r = checkCanFlop(userId,discountIdArray[i]);
			if(StringUtil.notNull(r)){
				canFlopArray += r.getStr("discountId")+",";
			}
		}
		return canFlopArray;
	}
	
}