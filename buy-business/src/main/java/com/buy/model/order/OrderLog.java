package com.buy.model.order;

import java.util.Date;
import java.util.List;

import com.buy.model.SysParam;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
/**
 * 订单日志表
 * @author eriol
 *
 */
public class OrderLog extends Model<OrderLog>{

	private static final long serialVersionUID = 1L;
	
	public static final OrderLog dao = new OrderLog();
	
	/**
	 * 系统参数code：下单
	 */
	public static final String CODE_MAKE_ORDER = "order_log_order";
	/**
	 * 系统参数code：支付完成
	 */
	public static final String CODE_ORDER_PAY = "order_log_pay";
	/**
	 * 系统参数code：发货完成
	 */
	public static final String CODE_ORDER_SEND = "order_log_send";
	/**
	 * 系统参数code：确认收货
	 */
	public static final String CODE_ORDER_RECEIVE = "order_log_receive";
	/**
	 * 系统参数code：完成评价
	 */
	public static final String CODE_ORDER_EVALUATE = "order_log_evaluate";
	/**
	 * 系统参数code：买家申请退款
	 */
	public static final String CODE_APPLY_REFUND = "order_log_apply_refund";
	/**
	 * 系统参数code：卖家同意退款申请
	 */
	public static final String CODE_AGREE_REFUND = "order_log_agree_refund";
	/**
	 * 系统参数code：完成退款
	 */
	public static final String CODE_REFUND_SUCCESS = "order_log_refund_success";
	/**
	 * 系统参数code：卖家不同意退款
	 */
	public static final String CODE_REFUND_FAIL = "order_log_refund_fail";
	/**
	 * 系统参数code：买家申请退货
	 */
	public static final String CODE_APPLY_RETURN_GOOD = "order_log_return_good";
	/**
	 * 系统参数code：卖家同意退货申请
	 */
	public static final String CODE_AGREE_RETURN_GOOD = "order_log_agree_return";
	/**
	 * 系统参数code：买家完成商品寄回
	 */
	public static final String CODE_SEND_RETURN_GOOD = "order_log_send_return";
	/**
	 * 系统参数code：卖家确认收到商品，退货完成
	 */
	public static final String CODE_RETURN_GOOD_SUCCESS = "order_log_return_success";
	/**
	 * 系统参数code：卖家不同意退货申请
	 */
	public static final String CODE_RETURN_GOOD_FAIL = "order_log_return_fail";
	/**
	 * 系统参数code：买家删除订单
	 */
	public static final String CODE_ORDER_DELETE = "order_log_delete";
	/**
	 * 系统参数code：买家延长收货
	 */
	public static final String CODE_DELAY_RECEIVE = "order_log_delay_receive";

	/**
	 * 系统参数code：买家寄回商品
	 */
	public static final String CODE_RETURN_BACK = "order_log_return_back";
	/**
	 * 系统参数code：卖家门店代发
	 */
	public static final String CODE_O2O_SEND = "order_log_o2o_send";
	/**
	 * 系统参数code：卖家按照发货规则发货
	 */
	public static final String CODE_RULE_SEND = "order_log_rule_send";


	
	/**
	 * 查找订单流程
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public List<Record> findLogListByOrderId(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" op_time opTime,");
		sql.append(" op_content opContent");
		sql.append(" FROM t_order_log ol");
		sql.append(" WHERE ol.order_id = ?");
		return Db.find(sql.toString(), orderId);
	}
	/**
	 * 添加日志
	 * @param orderId 订单id
	 * @param code  订单日志内容系统参数编码
	 * @author huangzq
	 */
	public void add(String orderId,String code, String dataFrom){
		Date now  = new Date();
		//添加订单日志
		OrderLog orderLog = new OrderLog();
		orderLog.set("order_id", orderId);
		orderLog.set("op_time", now);
		
		orderLog.set("data_from", dataFrom);
		
		//日志内容
		String conent = SysParam.dao.getStrByCode(code );
		orderLog.set("op_content", conent);
		orderLog.save();
	}
	
	/**
	 * 根据店铺ID查找订单日志
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public List<Record> findByOrderId(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	op_time opTime,");
		sql.append(" 	op_content opContent");
		sql.append(" 	FROM t_order_log");
		sql.append(" WHERE order_id = ?");
		return Db.find(sql.toString(), orderId);
	}
	
	/**
	 * 获取订单日志最新记录
	 * @param orderId
	 * @return
	 * @author Sylveon
	 */
	public Record getLastLog(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	op_time opTime,");
		sql.append(" 	op_content opContent");
		sql.append(" FROM t_order_log");
		sql.append(" WHERE order_id = ?");
		sql.append(" ORDER BY op_time DESC");
		return Db.findFirst(sql.toString(), orderId);
	}
	
}
