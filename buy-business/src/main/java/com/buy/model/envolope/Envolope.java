package com.buy.model.envolope;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/** 
 * @author wangy
 * @date 2015年9月24日 下午4:52:29 
 */
public class Envolope extends Model<Envolope> {
	
	private static final long serialVersionUID = 1L;
	public final static Envolope dao = new Envolope(); 

	/**
	 * 获得给予抽奖的红包，随机排序取前9个
	 * @return
	 * List<Envolope>
	 * @author wangy
	 * @date 2015年9月24日 下午4:59:15
	 */
	public List<Envolope> envolopeList(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" t1.id as envolopeId");	//红包id
		sql.append(" FROM");
		sql.append(" t_envolope t1");
		sql.append(" ,t_discount_coupon t2");
		sql.append(" WHERE");
		sql.append(" t1.type=1 or");	//积分红包
		sql.append(" (t1.target_id=t2.id");
		sql.append(" AND t2.status=1");			//优惠券已发布
		sql.append(" AND t2.begin_date<=date(now())");	//优惠券已开始使用
		sql.append(" AND t2.end_date>=date(now()))");	//优惠券未过期
		sql.append(" ORDER BY RAND()");		
		sql.append(" limit 0,9");
		return Envolope.dao.find(sql.toString());
	}
	
	/**
	 * 根据红包id获取部分红包信息
	 * @param id
	 * @return
	 * Envolope
	 * @author wangy
	 * @date 2015年10月13日 下午4:56:47
	 */
	public Envolope findEnvolopeById(int id){
		String sql = "SELECT t1.id envolopeId,t1.type type,t1.amount amount,t1.nums nums,t1.target_id targetId,t2.code code,"
				    + "t2.rule rule from t_envolope t1 LEFT JOIN t_discount_coupon t2 ON(t1.target_id=t2.id) where t1.id=?";
		Envolope envolope = Envolope.dao.findFirst(sql, id);
		return envolope;
	}
	
}
