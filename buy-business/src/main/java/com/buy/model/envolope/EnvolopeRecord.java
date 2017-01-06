package com.buy.model.envolope;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

/** 
 * @author wangy
 * @date 2015年9月24日 下午4:54:50 
 */
public class EnvolopeRecord extends Model<EnvolopeRecord> {

	private static final long serialVersionUID = 1L;
	public final static EnvolopeRecord dao = new EnvolopeRecord();
	
	/**
	 * 根据红包类型获取用户抽红包记录（分页）
	 * @param page	
	 * @param type	//红包类型（1积分红包2,现金红包）
	 * @param userId	//用户id
	 * @return
	 * Page<EnvolopeRecord>	用户抽红包记录分页列表
	 * @author wangy
	 * @date 2015年9月24日 下午5:30:58
	 */
	public Page<EnvolopeRecord> envolopeRecordPage(Page page,int type,String userId){
		StringBuffer selectSql = new StringBuffer();
		StringBuffer whereSql = new StringBuffer();
		selectSql.append("SELECT");
		selectSql.append(" t1.order_no as orederNo");	//订单编号
		selectSql.append(" ,t1.create_time as createTime");	//红包创建时间
		if(1 == type){
			selectSql.append(" ,t1.envolope_amount as envolopeAmount");	//红包金额
		}else{
			selectSql.append(" ,t1.coupon_code couponCode");	//优惠券兑换码
			selectSql.append(" ,t2.rule couponRule");	//优惠券规则
		}
		whereSql.append(" FROM");
		whereSql.append(" t_envolope_record t1");
		if(2 == type){
			whereSql.append(" LEFT JOIN t_discount_coupon t2");
			whereSql.append(" ON");
			whereSql.append(" t1.coupon_code=t2.code");
		}
		whereSql.append(" WHERE");
		whereSql.append(" t1.user_id=?");	//用户编号
		whereSql.append(" AND");	
		whereSql.append(" t1.envolope_type=?");	//红包类型（1积分红包2,现金红包）
		whereSql.append(" ORDER BY");
		whereSql.append(" t1.create_time desc");	//按时间降序排序
		return EnvolopeRecord.dao.paginate(page.getPageNumber(), page.getPageSize(), 
				selectSql.toString(), whereSql.toString(),new Object[]{userId,type});
	} 
	
	/**
	 * 判断订单是否已经参与过抽红包活动
	 * @param orderId	订单id
	 * @return
	 * boolean	返回true表示订单已参与过抽红包活动
	 * @author wangy
	 * @date 2015年9月25日 上午9:45:26
	 */
	public boolean drawAlready(String orderId){
		String sql = "select id from t_envolope_record where order_id=?";
		EnvolopeRecord envolopeRecord = findFirst(sql,orderId);
		return null!=envolopeRecord;
	}
	
}
