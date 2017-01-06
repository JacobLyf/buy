package com.buy.model.message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;

import com.buy.common.Ret;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * Model - 消息
 * @author Sylveon
 */
public class Message extends Model<Message> {
	
	/**
	 * 消息发布 - 未发布
	 */
	public static final int UN_RELEASE = 0;
	/**
	 * 消息发布 - 已发布
	 */
	public static final int IS_RELEASE = 1;
	
	/**
	 * 用户消息状态 - 未读
	 */
	public static final int STATUS_UNREAD = 0;
	/**
	 * 用户消息状态 - 已读
	 */
	public static final int STATUS_READ = 1;
	/**
	 * 用户消息状态 - 隐藏
	 */
	public static final int STATUS_HIDE = 2;
	
	/** 消息类型 - 商城公告 **/
	public static final int TYPE_EFUN_MESSAGE = 1;
	/** 消息类型 - 系统消息 **/
	public static final int TYPE_SYSTEM_MESSAGE = 2;
	/** 消息类型 - 订单消息 **/
	public static final int TYPE_ORDER_MESSAGE = 3;
	/** 消息类型 - 资产消息 **/
	public static final int TYPE_ACCOUNT_MESSAGE = 4;
	/** 消息类型 - 减价消息 **/
	public static final int TYPE_SALE_MESSAGE = 5;
	
	/** 消息标题 **/
	public static final String TITLE_BANKCARD = "银行卡修改审核结果";
	public static final String TITLE_REALNAME = "实名认证审核结果";
	public static final String TITLE_ORDER_UNPAY = "下单未支付提醒";
	public static final String TITLE_ORDER_CLOSE = "订单关闭提醒";
	public static final String TITLE_ORDER_REFUND = "退款申请结果";
	public static final String TITLE_ORDER_RETURN = "退货申请结果";
	public static final String TITLE_CASH = "现金变动提醒";
	public static final String TITLE_INTEGRAL = "积分变动通知";
	public static final String TITLE_PRODUCT_SALE = "商品降价通知";
	public static final String TITLE_SHOPOPEN_REWARD = "开店奖励发放";
	
	public static final String REDIS_USER = "redis_user";
	public static final String REDIS_SHOP = "redis_shop";
	public static final String REDIS_AGENT = "redis_agent";
	public static final String REDIS_SUPPLIER = "redis_supplier";
	
	///////////////消息标题模板//////////////////
	/**
	 * 消息标题-提醒发货
	 */
	public static final String TITLE_REMIND_DELIVERY = "提醒发货";

	private static final long serialVersionUID = 1L;
	public static Message dao = new Message();
	
	/**
	 * 发送消息 - 个人
	 * @param msgType
	 * @param typeTarId
	 * @param title
	 * @param content
	 * @param templateCode
	 * @param targetId
	 * @param targetType
	 */
	public void add4Private(int msgType, String typeTarId, String title, String content, String templateCode, String targetId, int targetType) {
		Date now = new Date();
		Message msg = new Message()
			.set("title",			title)				// 消息标题
			.set("content", 		content)			// 消息内容
			.set("type", 			msgType)			// 消息类型
			.set("type_target_id", 	typeTarId)			// 消息类型目标ID
			.set("template_code", 	templateCode)		// 消息模板编码
			.set("is_release",		Message.IS_RELEASE)	// 标题
	//		.set("send_admin_id",	null)				// 发送人ID
			.set("create_time", 	now)				// 创建时间
			.set("update_time", 	now)				// 修改时间
			.set("release_time", 	now)				// 发布时间
			.set("target_types", 	targetType);		// 目标类型
		if (StringUtil.notNull(typeTarId))
			msg.set("type_target_id", typeTarId);
		msg.save();
		
		int msgId = msg.get("id");
		MessageUser.dao.addMessageUser(msgId, targetId, targetType);
	}

	/**
	 * 批量添加已读信息 - 公共
	 * @param msgList		消息集合
	 * @param targetId		用户ID
	 * @param targetType	用户角色
	 * @return				添加记录
	 * @author Sylveon
	 */
	@Before(Tx.class)
	public void addMany4Public(List<Record> msgList, String targetId, int targetType) {
		// 查询已读消息SQL
		String search = "SELECT message_id FROM t_message_user_map WHERE 1 = 1 AND target_id = ? AND target_type = ? AND message_id = ?";
		// 处理已读消息数据
		List<String> sqlList = new ArrayList<String>();
		for(Record msg : msgList) {
			// 查询用户消息内容
			Integer msgId = msg.get("msgId");
			Record mgsUser = Db.findFirst(search.toString(), targetId, targetType, msgId);
			// 忽略已存在数据
			if (StringUtil.notNull(mgsUser))
				continue;
			// 生成添加sql
			StringBuffer add = new StringBuffer();
			add.append(" INSERT INTO t_message_user_map (message_id, target_id, target_type, status, create_time)");
			add.append(" VALUES (" + msgId + ", '" + targetId + "', " + targetType + ", " + Message.STATUS_READ + ", NOW())");
			sqlList.add(add.toString());
		}
		// 批量添加
		int size = sqlList.size();
		if (size > 0)
			Db.batch(sqlList, 50);
	}
	
	/**
	 * 统计未读消息 - 公共
	 * @param msgType
	 * @param targetId
	 * @param targetType
	 * @return
	 */
	public long unreadCount4Public(int msgType, String targetId, int targetType) {
		// 公共消息未读总数
		StringBuffer sql1 = new StringBuffer(" SELECT COUNT(*) FROM t_message a ");
		sql1.append(" WHERE 1 = 1");
		sql1.append(" AND a.type = ?");
		sql1.append(" AND a.is_release = ?");
		sql1.append(" AND a.target_types LIKE CONCAT('%', ?, '%')");
		long unRead = Db.queryLong(sql1.toString(), msgType, Message.IS_RELEASE, targetType);
		// 公共消息已读总数
		StringBuffer sql2 = new StringBuffer(" SELECT COUNT(1) FROM t_message_user_map b, t_message a ");
		sql2.append(" WHERE b.message_id = a.id ");
		sql2.append(" AND a.type = ? ");
		sql2.append(" AND a.is_release = ? ");
		sql2.append(" AND a.target_types LIKE CONCAT('%', ?, '%') ");
		sql2.append(" AND b.target_id = ? ");
		sql2.append(" AND b.target_type = ? ");
		unRead -= Db.queryLong(sql2.toString(), msgType, Message.IS_RELEASE, targetType, targetId, targetType);
		// 未读数
		return unRead;
	}
	
	/**
	 * 统计当前用户未读消息数量 - 个人
	 * @param msgType
	 * @param targetId
	 * @param targetType
	 * @return
	 * @author Sylveon
	 */
	public long unreadCount4Private(int msgType, String targetId, int targetType) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT COUNT(*) FROM t_message a");
		sql.append(" LEFT JOIN t_message_user_map c ON c.message_id = a.id");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND a.type = ?");
		sql.append(" AND a.is_release = ?");
		sql.append(" AND c.target_id = ?");
		sql.append(" AND c.target_type = ?");
		sql.append(" AND a.target_types LIKE CONCAT('%', ?, '%')");
		sql.append(" AND c.status = ?");
		// 参数：消息类型、 已发布、 目标ID、  目标类型、 目标类型、 未读
		long unread = Db.queryLong(sql.toString(), msgType, Message.IS_RELEASE, targetId, targetType, targetType, Message.STATUS_UNREAD);
		return unread;
	}
	
	/**
	 * 发送消息初始化 - 个人 - 实名制
	 */
	public Ret init4UserRealName(int realNameId, String templateCode, String remark) {
		return new Ret()
			.put("realNameId",		realNameId)
			.put("templateCode",	templateCode)
			.put("remark",			remark)
		;
	}
	
	/**
	 * 发送消息初始化 - 个人 - 修改银行卡
	 */
	public Ret init4UserBank(int bankAccountId, String templateCode, String remark) {
		return new Ret()
			.put("bankAccountId",	bankAccountId)
			.put("templateCode",	templateCode)
			.put("remark",			remark)
		;
	}
	
	/**
	 * 发送消息初始化- 个人 - 订单消息
	 */
	public Ret init4UserOrder(int returnId, String title, String templateCode) {
		return new Ret()
			.put("returnId",		returnId)
			.put("title",			title)
			.put("templateCode",	templateCode)
		;
	}
	
	/**
	 * 发送消息初始化- 个人 - 现金变动
	 */
	public Ret init4UserCashRecord(String userId, String userName, BigDecimal cash, int cashRecordType, String remark) {
		return new Ret()
			.put("userId",			userId)
			.put("userName",		userName)
			.put("cash",			cash)
			.put("type",	cashRecordType)
			.put("remark",			remark)
		;
	}
	
	/**
	 * 发送消息初始化- 个人 - 积分变动
	 */
	public Ret init4UserIntgralRecord(String userId, String userName, int intgral, int intgralType, String remark) {
		return new Ret()
			.put("userId",			userId)
			.put("userName",		userName)
			.put("intgral",			intgral)
			.put("type",		intgralType)
			.put("remark",			remark)
		;
	}
	
	/**
	 * 发送消息初始化 - 个人（店主） - 现金变动
	 */
	public Ret init4ShopCashRecord(String shopId, String shopNo, BigDecimal cash, int cashRecordType, String remark) {
		return new Ret()
			.put("shopId",			shopId)
			.put("shopNo",			shopNo)
			.put("cash",			cash)
			.put("type",	cashRecordType)
			.put("remark",			remark)
		;
	}
	
	/**
	 * 发送消息初始化 - 个人 - 商品降价
	 * @param proSkuCode
	 * @param proPrice
	 */
	public Ret init4UserProSale(String proSkuCode, String proPrice) {
		return new Ret()
			.put("proSkuCode",		proSkuCode)
			.put("proPrice",		proPrice)
		;
	}
	
}
