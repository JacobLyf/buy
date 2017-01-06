package com.buy.model.efun;

import com.jfinal.plugin.activerecord.Model;


/**
 * 幸运一折购晒单（评价）表
 */
public class EfunEvaluate extends Model<EfunEvaluate>{

	private static final long serialVersionUID = 1L;
	
	public static final EfunEvaluate dao = new EfunEvaluate();
	
	//================幸运一折购热门奖区显示晒单的记录数=======================//
		public static final int EFUN_HOT_PRICE_SUN_NUM = 10;
   //================幸运一折购热门奖区显示晒单的记录数==========================//
	
	/**
	 * 评论奖励
	 * @param userId
	 * @param imgNum
	 */
	/*public void evaluateReward(String userId, Integer imgNum) {
		if (imgNum >= 2) {
			// 添加积分
			new Integral().save(userId, 
								SysParam.dao.getIntByCode("evaluate_integral"), 
								"一折购中奖晒单获取",  
								IntegralRecord.TYPE_GET_INTEGRAL);
		}
	}*/
	
	/**
	 * 获取默认评价语句
	 * @return
	 */
	/*public String getDefaultEvaluate() {
		return SysParam.dao.getStrByCode("evaluate_defalut");
	}*/
}
