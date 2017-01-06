package com.buy.model.efun;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.buy.date.DateUtil;
import com.buy.model.activity.Activity;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.user.User;
import com.buy.radomutil.RadomUtil;
import com.buy.radomutil.WeightData;
import com.buy.radomutil.WeightRandom;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

/**
 * 抽奖记录表为：t_efun_draw_record
 * @author efun
 *
 */
public class EfunDrawRecord extends Model<EfunDrawRecord>{

	private static final long serialVersionUID = 1L;
	
	public static final EfunDrawRecord dao = new EfunDrawRecord();
	
	/**
	 * 对手战绩,显示20条
	 */
	public static final int WIN_SHOW_NUM = 20;
	/**
	 * 中奖
	 */
	public static final int WIN_YES = 1;
	/**
	 * 没中奖
	 */
	public static final int WIN_NO = 0;
	/**
	 * 真实数据
	 */
	public static final int REAL_YES = 1;
	/**
	 * 假数据
	 */
	public static final int REAL_NO = 0;
	/**
	 * 活动开始时间
	 */
	public static final String LUCKY_START_TIME = "2016-07-01 00:00:00";
	/**
	 * 活动结束时间
	 */
	public static final String LUCKY_END_TIME = "2016-08-01 23:59:59";
	/**
	 * 里约奥运会活动开始时间
	 */
	//public static final String ORI_START_TIME = "2016-08-02 00:00:00";
	/**
	 * 里约奥运会活动结束时间
	 */
	//public static final String ORI_END_TIME = "2016-08-03 23:59:59";
	
	/**
	 * 我的幸运购中奖个数-我的战绩
	 * @return
	 */
	public Long myLuckyPrizeCount(String userId){
		String sql = "SELECT COUNT(1) AS count FROM t_efun_draw_record edr WHERE edr.user_id = ? AND edr.win_status = ? AND edr.is_real = ?";
		return Db.findFirst(sql,userId,WIN_YES,REAL_YES).getLong("count");
	}
	
	/**
	 * 我的幸运购中奖列表-我的战绩
	 * @return
	 */ 
	public List<Record> myLuckyPrizeList(String userId){
		String sql = "SELECT edr.prize_name prizeName ,date_format(edr.create_time,'%Y-%m-%d %H:%i:%S') createTime  FROM t_efun_draw_record edr WHERE edr.user_id = ? AND edr.win_status = ? AND edr.is_real = ? ORDER BY edr.create_time DESC";
		return Db.find(sql,userId,WIN_YES,REAL_YES);
	}
	
	/**
	 * 抽奖概率权重
	 * 前端轮盘定义:
	 */
	public List<WeightData> weightData(){
		List<WeightData>  categorys = new ArrayList<WeightData>();  
    	WeightData wc1 = new WeightData("5",50);  //未抽中
    	WeightData wc2 = new WeightData("2",35);  //20积分
    	WeightData wc3 = new WeightData("7",15);  //50积分
    	//按这个顺序加入，则c=0-14，b=15-49,c=50-99
    	categorys.add(wc3); 
        categorys.add(wc2);  
        categorys.add(wc1);
        return categorys;
	}
	
	/**
	 * 假数据抽奖概率
	 */
	public List<WeightData> fakeWeightData(){
		List<WeightData>  categorys = new ArrayList<WeightData>();  
    	WeightData wc1 = new WeightData("2",280); //20积分
    	WeightData wc2 = new WeightData("7",240); //50积分
    	WeightData wc3 = new WeightData("4",230); //5元现金红包 
    	WeightData wc4 = new WeightData("0",150); //50元现金红包
    	WeightData wc5 = new WeightData("3",80);  //九阳榨汁机
    	WeightData wc6 = new WeightData("6",15);  //小米5
    	WeightData wc7 = new WeightData("1",5);   //iphone6s
    	
    	categorys.add(wc7); 
    	categorys.add(wc6); 
    	categorys.add(wc5); 
    	categorys.add(wc4); 
    	categorys.add(wc3); 
        categorys.add(wc2);  
        categorys.add(wc1);
        return categorys;
	}
	
	/**
	 * 前端轮盘定义
	 */
	public HashMap<String,String> rouletteDefine(){
		HashMap<String,String> roulette = new HashMap<String,String>();
		roulette.put("0", " 50 元红包");
		roulette.put("1", "iPhone6s 64G");
		roulette.put("2", " 20 积分");
		roulette.put("3", "九阳榨汁机");
		roulette.put("4", " 5元 红包");
		roulette.put("5", "谢谢参与");
		roulette.put("6", "小米 5");
		roulette.put("7", " 50 积分");
		return roulette;
	}
	
	/**
	 * 我的可抽奖次数
	 */
	public long myCanDrawCount(String userId){
		StringBuffer select1 = new StringBuffer();
		select1.append(" SELECT ");
		select1.append("  COUNT(1) ");
		select1.append(" FROM ");
		select1.append("  t_efun_user_order o ");
		select1.append(" WHERE ");
		select1.append("  o.is_real = ? ");
		select1.append("  AND o.`status` >= ? ");
		select1.append("  AND o.pay_time >= '"+LUCKY_START_TIME+"' ");
		select1.append("  AND o.pay_time <= '"+LUCKY_END_TIME+"' ");
		select1.append("  AND user_id = ? ");
		long joinCount = Db.queryLong(select1.toString(), REAL_YES,null,userId);
		
		//如果是在活动期间注册的好友都可以获得额外3次抽奖机会
		Date createTime = User.dao.findByIdLoadColumns(userId,"create_time").getDate("create_time");
		Date beginTime = DateUtil.StringToDate(LUCKY_START_TIME);
		Date endTime = DateUtil.StringToDate(LUCKY_END_TIME);
		if(createTime.after(beginTime) && createTime.before(endTime)){
			joinCount +=3;
		}
		
		
		StringBuffer select2 = new StringBuffer();
		select2.append(" SELECT ");
		select2.append("  COUNT(1) ");
		select2.append(" FROM ");
		select2.append("  t_efun_draw_record e ");
		select2.append(" WHERE ");
		select2.append("  e.is_real = ? ");
		select2.append("  AND e.create_time >= '"+LUCKY_START_TIME+"' ");
		select2.append("  AND e.create_time <= '"+LUCKY_END_TIME+"' ");
		select2.append("  AND user_id = ? ");
		long drawCount = Db.queryLong(select2.toString(), REAL_YES,userId);
		return (joinCount-drawCount) <= 0 ? 0 : (joinCount-drawCount);
	}
	
	/**
	 * 会员抽奖生成数据
	 */
	public void takeLucky(String userId,String myLuckyStar){
		EfunDrawRecord myRecord = new EfunDrawRecord();
		HashMap<String,String> roulette = rouletteDefine();
		String getPrize = roulette.get(myLuckyStar);
		String userName = User.dao.getUserName(userId);
		if(userName.length() > 4){
			userName = userName.substring(0, 2)+"**"+userName.substring(userName.length()-2, userName.length());
		}
		if("1" == myLuckyStar || "3" == myLuckyStar || "6" == myLuckyStar){
			myRecord.set("win_status", WIN_YES);
		}else{
			if("5" == myLuckyStar){
				myRecord.set("win_status", WIN_NO);
			}else{
				myRecord.set("win_status", WIN_YES);
			}
		}
		
		String drawDetail = "获得 "+getPrize;
		myRecord.set("user_id", userId);
		myRecord.set("user_name", userName);
		myRecord.set("prize_name", getPrize);
		myRecord.set("draw_detail", drawDetail);
		myRecord.set("is_real", REAL_YES);
		myRecord.set("create_time",new Date());
		myRecord.save();
		
		if(myRecord.getInt("win_status") == WIN_YES){
			//积分更新
			if("2" == myLuckyStar){
				new Integral().save(userId, 20, "抽中积分",  IntegralRecord.TYPE_GET_INTEGRAL);
			}else if("7" == myLuckyStar){
				new Integral().save(userId, 50, "抽中积分",  IntegralRecord.TYPE_GET_INTEGRAL);
			}
		}
		
		
		/**假数据**/
		EfunDrawRecord fakeRecord = new EfunDrawRecord();
		String fakeStar = WeightRandom.getWeigthData(fakeWeightData());
		String fakeName = RadomUtil.generate(3, RadomUtil.RADOM_LOWER)+"**"+RadomUtil.generate(2, RadomUtil.RADOM_NUMBER);
		String fakePrize = roulette.get(fakeStar);
		fakeRecord.set("user_id","010101");
		fakeRecord.set("user_name", fakeName);
		fakeRecord.set("prize_name", fakePrize);
		fakeRecord.set("draw_detail", "获得 "+fakePrize);
		fakeRecord.set("is_real", REAL_NO);
		fakeRecord.set("create_time",new Date());
		fakeRecord.set("win_status", WIN_YES);
		fakeRecord.save();
	}
	
	/**
	 * 统计用户火炬层数
	 * @param userId 用户ID
	 * @return 点击火炬次数
	 */
	public Long countUserDrawRecord(String userId){
		StringBuffer select = new StringBuffer();
		select.append(" SELECT ");
		select.append("  COUNT(1) ");
		select.append(" FROM ");
		select.append("  t_efun_draw_record e ");
		select.append(" WHERE ");
		select.append("  e.is_real = ? ");
		select.append("  AND e.create_time >= '" + Activity.ORI_START_TIME + "' ");
		select.append("  AND e.create_time <= '2016-08-23 23:59:59' ");
		select.append("  AND user_id = ? ");
		return Db.queryLong(select.toString(), REAL_YES, userId);
	}
	
	/**
	 * 保存用户奥运会活动中奖记录
	 * @param userId
	 * @param prizeName
	 * @return
	 */
	public boolean saveUserDrawRecord(String userId, String prizeName){
		String userName = User.dao.getUserName(userId);
		return new EfunDrawRecord().set("user_id", userId)
						    .set("user_name", userName)
						    .set("prize_name", prizeName)
						    .set("draw_detail", "获得" + prizeName)
						    .set("win_status", EfunDrawRecord.WIN_YES)
							.set("is_real", EfunDrawRecord.REAL_YES)
							.set("create_time", new Date()).save();
	}
}
