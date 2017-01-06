package com.buy.model.integral;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.user.User;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 积分(会员获取积分记录)
 * 【说明】记录会员获取积分的记录
 */
public class Integral extends Model<Integral>{

	private static final long serialVersionUID = 1L;
	
	public static final Integral dao = new Integral();
	
	/**
	 * 积分跟现金转换率
	 */
	public static final int transformationRate = 100;

	/**
	 * 获取用户有效积分总数
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月30日下午3:23:33
	 */
	public Integer getTotal(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	SUM(i.remain_integral) total ");
		sql.append(" FROM ");
		sql.append(" 	t_integral i ");
		sql.append(" WHERE ");
		sql.append(" 	i.validity_period > DATE_FORMAT(NOW(), '%Y-%m-%d 00:00:00') ");
		sql.append(" AND ");
		sql.append(" 	i.remain_integral > 0 ");
		sql.append(" AND ");
		sql.append(" 	i.user_id = ? ");
		return Db.queryInt(sql.toString(),userId);
	}
	
	/**
	 * 查询有效期内剩余积分大于0并且按最早获取时间排序的积分列表
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月30日下午2:44:45
	 */
	public List<Integral> findListOrderByTimeAsc(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	* ");
		sql.append(" FROM ");
		sql.append(" 	t_integral i ");
		sql.append(" WHERE ");
		sql.append(" 	i.validity_period > DATE_FORMAT(NOW(), '%Y-%m-%d 00:00:00') ");
		sql.append(" AND ");
		sql.append(" 	i.remain_integral > 0 ");
		sql.append(" AND ");
		sql.append(" 	i.user_id = ? ");
		sql.append(" ORDER BY ");
		sql.append(" 	i.create_time ASC ");
		return Integral.dao.find(sql.toString(), userId);
	}
	
	/**
	 * 保存积分获取记录（同时更新会员账户积分数并生成账户积分变动记录）
	 * @param userId 会员ID
	 * @param integral 获取积分数
	 * @param source 来源
	 * @param orderNo 订单编号
	 * @return
	 * @author Jacob
	 * 2016年1月3日下午4:22:29
	 */
	public boolean save(String userId,Integer integral,String source){
		return save(userId, integral, source, IntegralRecord.TYPE_SHOPPING_RETURN);
	}
	
	/**
	 * 保存积分获取记录（同时更新会员账户积分数并生成账户积分变动记录）
	 * @author Sylveon
	 */
	public boolean save(String userId, Integer integral, String source,  Integer integralRecordType) {
		Date now = new Date();
		//获取会员账户
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		account.set("integral", account.getInt("integral")+integral);
		account.set("update_time", now);
		//更新账户
		account.update();
		//获取用户账号
		User user = User.dao.findByIdLoadColumns(userId, "user_name");
		//生成会员账号积分变动记录
		IntegralRecord.dao.add(integral, account.getInt("integral"), integralRecordType, userId, user.getStr("user_name"), source);
		//新增积分获取
		Integral newIntegral = new Integral();
		newIntegral.set("user_id", userId);
		newIntegral.set("integral", integral);
		newIntegral.set("remain_integral", integral);
		newIntegral.set("source", source);
		newIntegral.set("validity_period", DateUtil.addYear(DateUtil.StringToDate(DateUtil.DateToString(new Date(), DateUtil.pattern_ymd)+" 23:59:59"), 100));//有效期为100个自然年
		newIntegral.set("create_time", now);
		return newIntegral.save();
	}
	
	/**
	 * 积分兑换成现金
	 * @param integral 积分数值
	 * @return
	 * @author Jacob
	 * 2015年12月17日下午7:52:38
	 */
	public BigDecimal getIntegralToCash(Integer integral){
		//获取系统参数配置的积分兑换率
		Integer exchangeRate = SysParam.dao.getIntByCode("exchange_rate");
		/**【根据积分兑换现金的相应规则进行转换，直接除以积分兑换率】**/
		BigDecimal integralDiscount = new BigDecimal(integral).divide(new BigDecimal(exchangeRate));
		return integralDiscount;
	}
	
	/**
	 * 现金兑换成积分
	 * @return
	 * @author Jacob
	 * 2015年12月17日下午8:34:32
	 */
	public Integer getCashToIntegral(BigDecimal cash){
		//获取系统参数配置的积分兑换率
		Integer exchangeRate = SysParam.dao.getIntByCode("exchange_rate");
		/**【根据积分兑换现金的相应规则进行转换，直接将现金乘以积分兑换率】**/
		Integer integral =  (cash.multiply(new BigDecimal(exchangeRate))).intValue();
		return integral;
	}
	
	/**
	 * 更新积分余额
	 * @param id
	 * @param remainIntegral
	 * @return
	 * @author Sylveon
	 */
	public String updateSql(Integer id, Integer remainIntegral) {
		return "UPDATE t_integral SET remain_integral = " + remainIntegral + " WHERE id = " + id;
	}
	
}
