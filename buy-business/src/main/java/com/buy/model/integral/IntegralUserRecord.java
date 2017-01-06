package com.buy.model.integral;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 用户积分使用记录表
 * 【说明】记录会员获取的积分使用和退回记录
 */
public class IntegralUserRecord extends Model<IntegralUserRecord>{
	
	/**
	 * 类型:购物订单
	 */
	public static final int TYPE_SHOPPING_ORDER = 1;
	/**
	 * 类型:幸运一折购订单
	 */
	public static final int TYPE_EFUN_ORDER = 2;

	private static final long serialVersionUID = 1L;
	
	public static final IntegralUserRecord dao = new IntegralUserRecord();
	
	/**
	 * 根据订单ID获取使用在该订单并且未过有效期的积分使用记录列表
	 * @param orderId 订单Id
	 * @param type 类型
	 * @return
	 * @author Jacob
	 * 2015年12月30日下午7:24:30
	 */
	public List<IntegralUserRecord> findListByOrderId(String orderId,Integer type){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	iur.* ");
		sql.append(" FROM ");
		sql.append(" 	t_integral_user_record iur ");
		sql.append(" WHERE ");
		sql.append(" 	iur.validity_period > DATE_FORMAT(NOW(), '%Y-%m-%d 00:00:00') ");
		sql.append(" AND ");
		sql.append(" 	iur.order_id = ? ");
		sql.append(" AND ");
		sql.append(" 	iur.type = ? ");
		return IntegralUserRecord.dao.find(sql.toString(), orderId,type);
	}
	
	/**
	 * 退回积分并保存订单使用积分退回记录
	 * @param orderId 订单ID
	 * @param type 类型
	 * @author Jacob
	 * 2015年12月30日下午7:18:15
	 */
	public void saveIntegralUserRecord4Return(String orderId,Integer type){
		/**********************【积分退回规则：超过有效期的积分不退回】************************/
		//根据订单ID获取使用在该订单并且未过有效期的积分使用记录列表
		List<IntegralUserRecord> integralList = IntegralUserRecord.dao.findListByOrderId(orderId,type);
		for(IntegralUserRecord integralUserRecord : integralList){
			//获取积分
			Integral integral = Integral.dao.findById(integralUserRecord.getInt("integral_id"));
			//获取购物抵扣的积分数额的绝对值
			int orderIntegral = Math.abs(integralUserRecord.getInt("integral"));
			//记录退回积分后剩余积分
			int remainIntegral = integral.getInt("remain_integral")+orderIntegral;
			integral.set("remain_integral", remainIntegral);
			//更新积分剩余值
			integral.update();
			//添加记录积分使用记录
			String userId = integral.getStr("user_id");
			IntegralUserRecord newIntegralUserRecord = new IntegralUserRecord();
			newIntegralUserRecord.set("user_id", userId);
			newIntegralUserRecord.set("order_id", orderId);
			newIntegralUserRecord.set("type", IntegralUserRecord.TYPE_SHOPPING_ORDER);
			newIntegralUserRecord.set("integral_id", integral.getInt("id"));
			newIntegralUserRecord.set("validity_period", integral.getTimestamp("validity_period"));
			newIntegralUserRecord.set("integral", orderIntegral);
			newIntegralUserRecord.set("remain_integral", remainIntegral);
			newIntegralUserRecord.set("remark", "退回积分");
			newIntegralUserRecord.set("create_time", new Date());
			newIntegralUserRecord.save();
		}
	}
	
	/**
	 * 使用积分并保存订单支付使用积分记录（同时更新积分条目剩余值）
	 * @param OrderId 订单Id
	 * @param userId 会员ID
	 * @param useIntegral 使用积分数量
	 * @param type 类型
	 * @author Jacob
	 * 2015年12月30日下午6:18:39
	 */
	public void saveIntegralUserRecord4Pay(String orderId,String userId,Integer useIntegral,Integer type){
		/**********************【积分扣除规则：按照积分有效期时间优先使用有效期快到期的积分】************************/
		//获取当前会员按按最早获取时间排序的积分列表
		List<Integral> integralList = Integral.dao.findListOrderByTimeAsc(userId);
		for(Integral integral : integralList){
			if(integral.getInt("remain_integral")-Math.abs(useIntegral)>0){
				//添加记录积分使用记录
				IntegralUserRecord integralUserRecord = new IntegralUserRecord();
				integralUserRecord.set("order_id", orderId);
				integralUserRecord.set("type", type);
				integralUserRecord.set("user_id", integral.getStr("user_id"));
				integralUserRecord.set("integral_id", integral.getInt("id"));
				integralUserRecord.set("validity_period", integral.getTimestamp("validity_period"));
				integralUserRecord.set("integral", -Math.abs(useIntegral));
				integralUserRecord.set("remain_integral", integral.getInt("remain_integral")-Math.abs(useIntegral));
				integralUserRecord.set("remark", "购物扣积分");
				integralUserRecord.set("create_time", new Date());
				integralUserRecord.save();
				//更新该条积分剩余数值
				integral.set("remain_integral", integral.getInt("remain_integral")-Math.abs(useIntegral));
				//更新剩余使用积分
				useIntegral = 0;
			}else{
				//添加记录积分使用记录
				IntegralUserRecord integralUserRecord = new IntegralUserRecord();
				integralUserRecord.set("order_id", orderId);
				integralUserRecord.set("type", type);
				integralUserRecord.set("user_id", integral.getStr("user_id"));
				integralUserRecord.set("integral_id", integral.getInt("id"));
				integralUserRecord.set("validity_period", integral.getTimestamp("validity_period"));
				integralUserRecord.set("integral", -integral.getInt("remain_integral"));
				integralUserRecord.set("remain_integral", 0);
				integralUserRecord.set("remark", "购物扣积分");
				integralUserRecord.set("create_time", new Date());
				integralUserRecord.save();
				//更新剩余使用积分
				useIntegral = Math.abs(useIntegral) - integral.getInt("remain_integral");
				//更新该条积分剩余数值
				integral.set("remain_integral", 0);
			}
			//更新积分
			integral.update();
			//当剩余使用积分小于0时，跳出循环
			if(useIntegral<=0){
				break;
			}
		}
	}

}
