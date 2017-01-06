package com.buy.model.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.model.SysParam;
import com.buy.model.efun.EfunOrderDetail;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.img.Image;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.order.Order;
import com.buy.model.order.OrderDetail;
import com.buy.model.order.OrderLog;
import com.buy.plugin.event.shop.ShopReputablyEvent;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.EventKit;

/**
 * 商品评价
 */
public class ProEvaluate extends Model<ProEvaluate> {

	private static final long serialVersionUID = 1L;

	public static final ProEvaluate dao = new ProEvaluate();
	/**
	 * 类型：晒单
	 */
	public static final Integer EFUN_TYPE = 2;
	/**
	 * 类型：评价
	 */
	public static final Integer ORDINARY_TYPE = 1;

	/**
	 * 评论奖励
	 *
	 * @param userId
	 * @param content
	 *            评论
	 */
	public void evaluateReward(String userId, String content) {
		if (content.length() >= 10) {
			// 添加积分
			new Integral().save(userId, SysParam.dao.getIntByCode("evaluate_integral"), "订单评价成功获取",
					IntegralRecord.TYPE_GET_INTEGRAL);
		}
	}

	/**
	 * 晒单评论奖励
	 *
	 * @param userId
	 * @param imgNum
	 *            晒单上传图片数量
	 */
	public void evaluateReward(String userId, Integer imgNum) {
		if (imgNum >= 2) {
			// 添加积分
			new Integral().save(userId, SysParam.dao.getIntByCode("evaluate_integral"), "一折购中奖晒单获取",
					IntegralRecord.TYPE_GET_INTEGRAL);
		}
	}

	/**
	 * 获取晒单默认评价
	 *
	 * @return
	 */
	public String getDefaultEvaluate() {
		return SysParam.dao.getStrByCode("evaluate_defalut");
	}

	/**
	 * 新增一折购晒单
	 *
	 * @param orderDetail
	 * @param userId
	 * @param evaluateContent
	 * @return
	 */
	public boolean addEfunEvaluate(String efunUserOrderId, String userId, String evaluateContent,
			String[] evaluateImgPaths, String dataFrom) {
		// 获取订单详情
		Record orderDetail = EfunOrderDetail.dao.getOrderDetailIdByEfunOrderId(efunUserOrderId);
		String proEvalId = StringUtil.getUUID();
		ProEvaluate efunEvaluate = new ProEvaluate();
		boolean result = efunEvaluate.set("id", proEvalId).set("product_id", orderDetail.getInt("proId"))
				.set("order_detail_id", orderDetail.getInt("detailId")).set("user_id", userId)
				.set("content", evaluateContent).set("create_time", new Date())
				// 类型(晒单)
				.set("type", EFUN_TYPE).set("is_public", BaseConstants.YES).save();

		// 更新晒单图片表
		if (evaluateImgPaths.length > 0) {
			for (String currImg : evaluateImgPaths) {
				ProEvaluateImg efunEvaluateImg = new ProEvaluateImg();
				efunEvaluateImg.set("pro_eval_id", proEvalId).set("img_path", currImg).set("create_time", new Date())
						.save();
				// 图片有效
				Image.dao.enable(currImg);
			}

			// 一折购晒单奖励积分(判断晒单图片数量)
			evaluateReward(userId, evaluateImgPaths.length);
		}

		String orderId = orderDetail.getStr("orderId");
		new Order().set("id", orderId).set("status", Order.STATUS_HAD_EVALUATION).update();
		// 订单日志
		OrderLog.dao.add(orderId, "order_log_evaluate", dataFrom);

		// 事件驱动 - 店铺好评评分
		EventKit.postEvent(new ShopReputablyEvent(orderId));
		return result;
	}

	/**
	 * 批量保存订单评价
	 *
	 * @param list
	 * @param userId
	 * @param isPublic
	 * @param orderId
	 * @param dataFrom
	 * @return
	 */
	public JsonMessage addOrdinaryEvaluate(List<JSONObject> list, String userId, Integer isPublic, String orderId,
			String dataFrom) {
		// 生成评价数据 / 生成评价图片数据
		List<ProEvaluate> peList = new ArrayList<ProEvaluate>();
		List<ProEvaluateImg> peImageList = new ArrayList<ProEvaluateImg>();

		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				String peId = StringUtil.getUUID();
				
				int imgPathsLength = 0;
				// 生成评价图片数据
				JSONArray imgPaths = (JSONArray) list.get(i).get("imgPaths");// 晒图
				if (StringUtil.notNull(imgPaths)) {
					Object[] imgPathArray = imgPaths.toArray();
					imgPathsLength = imgPaths.toArray().length;
					for (int j = 0; j < imgPathsLength; j++) {
						ProEvaluateImg img = new ProEvaluateImg();
						img.set("pro_eval_id", peId)
						   .set("img_path", imgPathArray[j]);
						peImageList.add(img);
					}
				}
				
				
				Integer detailId = (Integer) list.get(i).get("detailId");
				// 生成评价数据
				ProEvaluate pe = new ProEvaluate();
				pe.set("id", peId)
				  .set("order_detail_id", detailId) // 订单详情ID
				  .set("product_id", list.get(i).get("proId")) // 产品ID
				  .set("user_id", userId)
				  .set("praise_radio", list.get(i).get("praiseRadio")) // 评价星级
				  .set("content", list.get(i).get("content")) // 评价内容
				  .set("is_public", isPublic);// 是否公开评价
				
				if (OrderDetail.dao.isEfunOrderDetail(detailId)) {
					pe.set("type", EFUN_TYPE);
				} else {
					pe.set("type", ORDINARY_TYPE);
				}
				pe.set("imgCount", imgPathsLength);
				peList.add(pe);
			}
		}

		return addMany(orderId, userId, peList, peImageList, dataFrom);
	}

	public JsonMessage addOrdinaryEvaluate(Integer[] detailIds, Integer[] proIds, Integer[] praiseRadios,
			String[] contents, List<String[]> imgPathList, String userId, Integer isPublic, String orderId,
			String dataFrom) {
		// 生成评价数据 / 生成评价图片数据
		List<ProEvaluate> peList = new ArrayList<ProEvaluate>();
		List<ProEvaluateImg> peImageList = new ArrayList<ProEvaluateImg>();
		for (int i = 0; i < detailIds.length; i++) {
			String peId = StringUtil.getUUID();
			
			int imgPathsLength = 0;
			// 生成评价图片数据
			String[] imgPaths = imgPathList.get(i);
			if (StringUtil.notNull(imgPaths)) {
				imgPathsLength = imgPaths.length;
				for (int j = 0; j < imgPathsLength; j++) {
					ProEvaluateImg img = new ProEvaluateImg();
					img.set("pro_eval_id", peId)
					   .set("img_path", imgPaths[j]);
					peImageList.add(img);
				}
			}
			
			// 生成评价数据
			ProEvaluate pe = new ProEvaluate();
			Integer detailId = detailIds[i];
			pe.set("id", peId)
			  .set("order_detail_id", detailId)
			  .set("product_id", proIds[i])
			  .set("user_id", userId)
			  .set("praise_radio", praiseRadios[i])
			  .set("content", contents[i])
			  .set("is_public", isPublic);
			if (OrderDetail.dao.isEfunOrderDetail(detailId)) {
				pe.set("type", EFUN_TYPE);
			} else {
				pe.set("type", ORDINARY_TYPE);
			}
			pe.set("imgCount", imgPathsLength);
			peList.add(pe);
		}

		return addMany(orderId, userId, peList, peImageList, dataFrom);
	}

	private JsonMessage addMany(String orderId, String userId, List<ProEvaluate> peList,
			List<ProEvaluateImg> imgUrlList, String dataFrom) {
		JsonMessage result = new JsonMessage();

		/*
		 * 验证订单
		 */
		// 验证部分，应该抽取
		Order order = Order.dao.getOrder4UserForUpdate(orderId, userId);
		if (StringUtil.isNull(order)) {
			// 订单不存在
			return result.setStatusAndMsg("1", "非法提交");
		} else {
			int status = order.getInt("status");
			int tradeStatus = order.getInt("trade_status");
			if (Order.STATUS_WAIT_FOR_EVALUATION == status && Order.TRADE_NORMAL == tradeStatus) {
			} else {
				// 订单不可评价
				return result.setStatusAndMsg("1", "该订单处于非待评价状态，不能评价");
			}
		}

		// 评论添加
		List<String> sqlList = new ArrayList<String>();
		for (ProEvaluate pe : peList) {
			// 拼接SQL语句
			StringBuffer sql = new StringBuffer();
			sql.append(" INSERT INTO t_pro_evaluate");
			sql.append(
					"	(id, product_id, order_detail_id, user_id, praise_radio, content, is_public, create_time, type)");
			sql.append(" VALUES ( ");
			sql.append(" 	'" + pe.getStr("id") + "', ");
			sql.append(pe.getInt("product_id") + ", ");
			sql.append(pe.getInt("order_detail_id") + ", ");
			sql.append(" 	'" + pe.getStr("user_id") + "', ");
			sql.append(pe.getInt("praise_radio") + ", ");
			sql.append(" 	'" + pe.getStr("content") + "', ");
			sql.append(pe.getInt("is_public") + ", ");
			sql.append("	NOW(), ");
			sql.append(pe.getInt("type"));
			sql.append(" )");
			sqlList.add(sql.toString());
		}
		int[] batchResult = Db.batch(sqlList, 50);
		for (int i = 0; i < batchResult.length; i++) {
			if (batchResult[i] > 0) {
				Integer evType = peList.get(i).getInt("type");
				// 奖励方法会自行判断是否符合奖励条件
				if (evType == EFUN_TYPE) {
					// 晒单奖励
					evaluateReward(userId, peList.get(i).getInt("imgCount"));
				} else if (evType == ORDINARY_TYPE) {
					// 评价奖励
					evaluateReward(userId, peList.get(i).getStr("content"));
				}
			}
		}
		// 评论图片添加
		if (StringUtil.notNull(imgUrlList)) {
			ProEvaluateImg.dao.addImgMany(imgUrlList);
		}
		// 修改订单状态
		order.set("status", Order.STATUS_HAD_EVALUATION).update();

		// 订单日志
		OrderLog.dao.add(orderId, "order_log_evaluate", dataFrom);

		// 事件驱动 - 店铺好评评分
		EventKit.postEvent(new ShopReputablyEvent(orderId));

		return result;
	}

	/**
	 * 会员订单评价
	 */
	public Record findUserOrderEvaluate(String orderId, String userId) {
		// 订单
		Record order = Db.findFirst("SELECT no orderNo, (total + freight) totalCost FROM t_order WHERE id = ? AND user_id = ?", orderId, userId);

		// 订单商品评价
		String sql = new StringBuffer(" SELECT ")
				.append(" od.product_id proId,")
				.append(" pe.id proEvaId, ")
				.append(" od.product_img proImg, ")
				.append(" pe.praise_radio praiseRadio, ")
				.append(" pe.content, ")
				.append(" pe.create_time createTime,")
				.append(" od.discount_type discountType, ")
				.append(" od.efun_id efunId ")
				.append(" FROM t_order_detail od")
				.append(" LEFT JOIN t_pro_evaluate pe ON pe.order_detail_id = od.id ")
				.append(" WHERE 1 = 1 ")
				.append(" AND od.order_id = ? ")
				.append(" AND pe.user_id = ? ")
				.append(" ORDER BY pe.create_time DESC ")
				.toString();
		List<Record> proEvaList = Db.find(sql.toString(), orderId, userId);

		// 晒单图片
		for (Record proEva : proEvaList) {
			String proEvaId = proEva.getStr("proEvaId");
			List<String> proEvaImgs = ProEvaluateImg.dao.findImgsByProEvaImgId(proEvaId);
			proEva.set("proEvaImgs", proEvaImgs).remove("proEvaId");
		}

		return order.set("proEvaList", proEvaList);
	}

	/**
	 * 中奖晒单(一折晒单)
	 * @param productId
	 * @param page
	 * @return
	 */
	public Page<Record> getEfunEvaluateList(String productId, Page<Object> page) {
		StringBuffer selectSql = new StringBuffer();
		StringBuffer whereSql =new StringBuffer();
		selectSql.append(" SELECT  ");
		selectSql.append("   c.user_name userName, ");
		selectSql.append("   c.avatar, ");
		selectSql.append("   date_format(a.create_time,'%Y-%m-%d %H:%i:%S') createTime, ");
		selectSql.append("   a.id, ");
		selectSql.append("   a.content evaluation, ");
		selectSql.append("   a.praise_radio, ");
		selectSql.append("   d.efun_id efunId ");

		whereSql.append("  FROM ");
		whereSql.append("  	 t_pro_evaluate a ");
		whereSql.append("  LEFT JOIN t_user c ON a.user_id = c.id ");
		whereSql.append("  LEFT JOIN t_order_detail d ON a.order_detail_id = d.id ");
		whereSql.append("  WHERE d.product_id = ? ");
		whereSql.append("    AND a.user_id <> ? ");
		whereSql.append("    AND a.type = ? ");
		whereSql.append("  ORDER BY d.efun_id desc ");
		
		Page<Record> pages = Db.paginate(page.getPageNumber(), 
				page.getPageSize(), selectSql.toString(), 
				whereSql.toString(), productId, 
				EfunUserOrder.SPECIAL_USER_ID, ProEvaluate.EFUN_TYPE);
		if (pages.getList().size() > 0) {
			for (Record record : pages.getList()) {
				String evaluateId = record.get("id").toString();
				List<Record> evaluateImgs = Db.find(
						" select a.img_path imgPath from t_pro_evaluate_img a WHERE a.pro_eval_id = ? ", evaluateId);
				record.set("evaluateImgs", evaluateImgs);
				// 处理用户名x**x
				String username = record.getStr("userName");
				if (username.length() > 4) {
					username = username.substring(0, 2) + "**"
							+ username.substring(username.length() - 2, username.length());
					record.set("userName", username);
				}
			}
		}
		return pages;
	}
}
