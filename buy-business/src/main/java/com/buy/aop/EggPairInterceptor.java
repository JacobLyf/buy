package com.buy.aop;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.date.DateUtil;
import com.buy.model.account.Account;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.order.Order;
import com.buy.model.user.User;
import com.buy.numOprate.MathUtil;
import com.buy.radomutil.WeightData;
import com.buy.radomutil.WeightRandom;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * 双旦活动.
 * 
 * @author Chengyb
 */
public class EggPairInterceptor implements Interceptor {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	// Redis缓存前缀,避免同Db key覆盖问题.
	// 金蛋.
	public static String silverEggPrefix = "silver_egg_";
	// 银蛋.
	public static String goldenEggPrefix = "golden_egg_";
	// 已砸银蛋.
	public static String eggPrefix = "egg_";
	
	// 已砸金银蛋记录.
	public static String eggHitfix = "eggHit_";
	
	public static String remark = "2016双旦活动砸蛋";
	
	// ============================================
	// 队列一: 会员可砸银蛋次数.
	// 队列二: 会员已砸银蛋次数.
	// ==============================================
	String userCanHitMap = "userCanHitMap";
	String userAlreadyHitMap = "userAlreadyHitMap";
	
	static List<IntegralRecord> fakeList = new ArrayList<IntegralRecord>();
	/**
	 * 初始化假数据.
	 */
	static {
		IntegralRecord record1 = new IntegralRecord();
		record1.set("user_name", "Erduo");
		record1.set("integral", "188");
		fakeList.add(record1);
		
		IntegralRecord record2 = new IntegralRecord();
		record2.set("user_name", "tianyi14");
		record2.set("integral", "188");
		fakeList.add(record2);
		
		IntegralRecord record3 = new IntegralRecord();
		record3.set("user_name", "Rogers");
		record3.set("integral", "88");
		fakeList.add(record3);
		
		IntegralRecord record4 = new IntegralRecord();
		record4.set("user_name", "Liuer");
		record4.set("integral", "圣诞抱枕");
		fakeList.add(record4);
		
		IntegralRecord record5 = new IntegralRecord();
		record5.set("user_name", "XT163");
		record5.set("integral", "188");
		fakeList.add(record5);
		
		IntegralRecord record6 = new IntegralRecord();
		record6.set("user_name", "Mr.ing");
		record6.set("integral", "188");
		fakeList.add(record6);
		
		IntegralRecord record7 = new IntegralRecord();
		record7.set("user_name", "Marcyel");
		record7.set("integral", "88");
		fakeList.add(record7);
		
		IntegralRecord record8 = new IntegralRecord();
		record8.set("user_name", "chenchen28");
		record8.set("integral", "288");
		fakeList.add(record8);
		
		IntegralRecord record9 = new IntegralRecord();
		record9.set("user_name", "S.wong");
		record9.set("integral", "88");
		fakeList.add(record9);
		
		IntegralRecord record10 = new IntegralRecord();
		record10.set("user_name", "sara28");
		record10.set("integral", "188");
		fakeList.add(record10);
		
		IntegralRecord record11 = new IntegralRecord();
		record11.set("user_name", "Shopin");
		record11.set("integral", "拉杆箱");
		fakeList.add(record11);
		
		IntegralRecord record12 = new IntegralRecord();
		record12.set("user_name", "Suki");
		record12.set("integral", "88");
		fakeList.add(record12);
		
		IntegralRecord record13 = new IntegralRecord();
		record13.set("user_name", "sj123");
		record13.set("integral", "288");
		fakeList.add(record13);
		
		IntegralRecord record14 = new IntegralRecord();
		record14.set("user_name", "Summerha");
		record14.set("integral", "288");
		fakeList.add(record14);
		
		IntegralRecord record15 = new IntegralRecord();
		record15.set("user_name", "Ss zz");
		record15.set("integral", "88");
		fakeList.add(record15);
		
		IntegralRecord record16 = new IntegralRecord();
		record16.set("user_name", "Biubiu");
		record16.set("integral", "680");
		fakeList.add(record16);
		
		IntegralRecord record17 = new IntegralRecord();
		record17.set("user_name", "Tonyzhazha");
		record17.set("integral", "880");
		fakeList.add(record17);
		
		IntegralRecord record18 = new IntegralRecord();
		record18.set("user_name", "Chloe");
		record18.set("integral", "680");
		fakeList.add(record18);
		
		IntegralRecord record19 = new IntegralRecord();
		record19.set("user_name", "Trainbow");
		record19.set("integral", "288");
		fakeList.add(record19);
		
		IntegralRecord record20 = new IntegralRecord();
		record20.set("user_name", "Marcye");
		record20.set("integral", "880");
		fakeList.add(record20);
	}
	
	/**
	 * 获取滚动数据.
	 */
	public static List<IntegralRecord> disposeData() {
		List<IntegralRecord> list = IntegralRecord.dao.find("SELECT user_name, integral FROM t_integral_record t WHERE t.remark='" + remark + "' ORDER BY t.create_time DESC LIMIT 0,20");
	    if(null != list) {
	    	if(list.size() < 20) {
	    		int size = list.size();
	    		for (int i = 0; i < fakeList.size() - size; i++) {
					list.add(fakeList.get(i));
				}
	    	} else {
	    		// 第4名（圣诞抱枕）、12名（拉杆箱）.
	    		IntegralRecord record1 = new IntegralRecord();
	    		record1.set("user_name", EfunUserOrder.dao.getUserName());
	    		record1.set("integral", "圣诞抱枕");
	    		list.add(3, record1);
	    		
	    		IntegralRecord record2 = new IntegralRecord();
	    		record2.set("user_name", EfunUserOrder.dao.getUserName());
	    		record2.set("integral", "拉杆箱");
	    		list.add(10, record2);
	    		
	    		list.remove(list.size() - 1);
	    		list.remove(list.size() - 1);
	    	}
	    } else {
	    	return fakeList;
	    }
	    return list;
	}
	
	/**
     * Session - 登录会员信息
     */
    public static final String SESSION_USER = "user_info";

	@Override
	public void intercept(Invocation inv) {
		Controller c = inv.getController();
		// PC、App、Wap.
		User user = null; // PC.
		user = c.getSessionAttr(SESSION_USER); // PC.
		if(null == user) {
			// App.
			// Wap.
		}
		
		// 确认收货.
		inv.invoke();
		
		// 订单编号(一折购/普通订单) 订单金额>=68元  不含运费.
		String orderId = c.getPara("orderId");
		confirmGoods(orderId);
	}

	public static void confirmGoods(String orderId) {
		
		// 优先查找普通订单.
		Order order = Order.dao.findByIdLoadColumns(orderId, "user_id,total,status");
		if(null != order) {
			BigDecimal total = order.getBigDecimal("total"); // 商品总金额(不含运费).
			int status = order.getInt("status"); // 订单状态.订单状态:(0：待付款，1：待发货，2：待收货（选择自提时付完款后直接跳转到此处），3：待评价，4：已评价)
		    // 交易完成并且商品金额>=68元.
			if(total.compareTo(new BigDecimal(68)) >= 0 &&status > 2) {
				// 将Redis内的用户数据+1.
				Cache eggPairCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
				eggPairCache.getJedis().eval(increaseSilverEggScript, 1, silverEggPrefix + order.getStr("user_id"));
			}
		} else {
			// 查找一折购订单.
			EfunUserOrder efunOrder = EfunUserOrder.dao.findByIdLoadColumns(orderId, "user_id,total,status");
			if(null != efunOrder) {
				BigDecimal total = efunOrder.getBigDecimal("total"); // 付款总金额(不含运费).
				int status = efunOrder.getInt("status"); // 订单状态:(0: 未完成付款 ,1：待发货，2：待收货，3：待评价，4：已评价).
				// 交易完成并且商品金额>=68元.
				if(total.compareTo(new BigDecimal(68)) >= 0 &&status > 2) {
					// 将Redis内的用户数据+1.
					// 将Redis内的用户数据+1.
					Cache eggPairCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
					eggPairCache.getJedis().eval(increaseSilverEggScript, 1, silverEggPrefix + efunOrder.getStr("user_id"));
				}
			}
		}
	}
	
	/**
	 * 会员砸银蛋.
	 */
	public static JsonMessage hitSilverEgg(String userId, String day, JsonMessage jsonMessage) {
		Cache eggPairCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		int hit = ((Long) eggPairCache.getJedis().eval(hitSilverEggScript, 4, silverEggPrefix + userId, eggPrefix + userId, eggPrefix + day + "_" + userId, goldenEggPrefix + userId)).intValue();
	    if(hit == 0) {
	    	jsonMessage.setStatusAndMsg("1", "您无法砸银蛋");
	    } else if(hit == 1) {
	    	// 抽奖.
	    	List<WeightData>  categorys = new ArrayList<WeightData>();  
	    	WeightData wc1 = new WeightData("88", 80);
	    	WeightData wc2 = new WeightData("188", 15);
	    	WeightData wc3 = new WeightData("288", 5);
	    	
	    	categorys.add(wc3);
	        categorys.add(wc2);
	        categorys.add(wc1);
	        
	        String integral = WeightRandom.getWeigthData(categorys);
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("integral", integral + "积分");
	        map.put("distance", 5 - Integer.parseInt(eggPairCache.getJedis().get(eggPrefix + day + "_" + userId)));
	        jsonMessage.setData(map);
	        
	        // 记录获奖记录(数据库).
	        integral(userId, integral);
	    } else if(hit == 2) {
	    	jsonMessage.setStatusAndMsg("2", "您今日已砸满5次银蛋!");
	    }
	    return jsonMessage;
	}
	
	/**
	 * 会员砸金蛋.
	 */
	public static JsonMessage hitGoldenEgg(String userId, JsonMessage jsonMessage) {
		Cache eggPairCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		int hit = ((Long) eggPairCache.getJedis().eval(hitGoldenEggScript, 1, goldenEggPrefix + userId)).intValue();
	    if(hit == 0) {
	    	jsonMessage.setStatusAndMsg("1", "您无法砸金蛋");
	    } else if(hit == 1) {
	    	// 抽奖.
	    	List<WeightData>  categorys = new ArrayList<WeightData>();  
	    	WeightData wc1 = new WeightData("680", 90);
	    	WeightData wc2 = new WeightData("880", 10);
	    	
	        categorys.add(wc2);
	        categorys.add(wc1);
	        
	        String integral = WeightRandom.getWeigthData(categorys);
	        Map<String, Object> map = new HashMap<String, Object>();
	        map.put("integral", integral + "积分");
	        map.put("distance", 5 - Integer.parseInt(eggPairCache.getJedis().get(eggPrefix + sdf.format(new Date())  + "_" + userId)));
	        jsonMessage.setData(map);
	        
	        // 记录获奖记录.
	        integral(userId, integral);
	    }
	    return jsonMessage;
	}
	
	/**
	 * 记录会员抽奖积分.
	 */
	private static void integral(String userId, String integral) {
		// 增加积分.
		Account.dao.rewardUserIntegral(userId, Integer.parseInt(integral));
		// 积分记录.
		Integral userIntegral = new Integral();
		userIntegral.set("user_id", userId);
		userIntegral.set("integral", integral);
		userIntegral.set("remain_integral", integral);
		userIntegral.set("source", remark);
		userIntegral.set("validity_period", DateUtil.StringToDate("2017-04-05 23:59:59"));
		userIntegral.set("create_time", new Date());
		userIntegral.save();
		// 积分事项记录.
		IntegralRecord integralRecord = new IntegralRecord();
		integralRecord.set("integral", integral); // 变动金额（可正可负）.
		integralRecord.set("remain_integral", integral); // 账户余额
		integralRecord.set("type", 9); // 积分消费类型
		integralRecord.set("user_id", userId); // 用户id
		String userName = User.dao.getUserName(userId);
		integralRecord.set("user_no", userName); // 用户账号
		integralRecord.set("user_name", userName); // 用户名（这里也存用户账号）
		integralRecord.set("remark", remark); // 备注
		integralRecord.set("create_time", new Date());
		integralRecord.save();
	}
	
	// 增加用户可以砸银蛋的次数.
	static String increaseSilverEggScript = "if redis.call('EXISTS', KEYS[1]) == 0 then\n"
			+ " redis.call('SETEX', KEYS[1], 2160000, 1);\n"
			+ "else\n"
			+ " redis.call('INCR', KEYS[1]);\n"
			+ "end\n";

    // 用户砸银蛋.
    static String hitSilverEggScript = "if redis.call('EXISTS', KEYS[1]) == 0 then\n"
    		  + "return 0;\n" // 【0】不可砸银蛋.
    		  + "else\n"
    		  + "if tonumber(redis.call('GET', KEYS[1])) > 0 then\n"
    		  + "local dayHit = false;\n"
    		  + "if redis.call('EXISTS', KEYS[3]) == 0 then\n"
    		  + "redis.call('SETEX', KEYS[3], 2160000, 1);\n"
    		  + "dayHit = true;\n"
    		  + "else\n"
    		  + "if tonumber(redis.call('GET', KEYS[3])) < 5 then\n"
    		  + "redis.call('INCR', KEYS[3]);\n"
    		  + "dayHit = true;\n"
    		  + "else\n"
    		  + "return 2;\n" // 【2】今日砸银蛋次数已满.
    		  + "end;\n"
    		  + "end;\n" 
    		  + "if dayHit then\n"
    		  + "redis.call('DECR', KEYS[1]);\n"
    		  + "if redis.call('EXISTS', KEYS[2]) == 0 then\n"
    		  + "redis.call('SETEX', KEYS[2], 2160000, 1);\n"
    		  + "redis.call('SETEX', KEYS[4], 2160000, 0);\n"
    		  + "else\n"
    		  + "redis.call('INCR', KEYS[2]);\n"
    		  + "if tonumber(redis.call('GET', KEYS[2])) % 5 == 0 then\n"
              + "redis.call('INCR', KEYS[4]);\n"
              + "end;\n"
    		  + "end;\n"
    		  + "return 1;\n" // 【1】砸银蛋成功.
    		  + "end;\n"
    		  + "else\n"
    		  + "return 0;\n" // 【0】不可砸银蛋.
    		  + "end;\n"
    		  + "end;\n";
    
    // 用户砸银蛋.
    static String hitGoldenEggScript = "if redis.call('EXISTS', KEYS[1]) == 0 then\n"
    		  + "return 0;\n"
    		  + "else\n"
    		  + "if tonumber(redis.call('GET', KEYS[1])) == 1 then\n"
    		  + "redis.call('DECR', KEYS[1]);\n"
    		  + "return 1;\n"
    		  + "else\n"
    		  + "return 0;\n"
    		  + "end;\n"
    		  + "end;\n";
}