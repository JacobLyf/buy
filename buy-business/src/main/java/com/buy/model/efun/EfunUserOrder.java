package com.buy.model.efun;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.common.MqUtil;
import com.buy.common.constants.MqConstants;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.agent.AgentCashRecord;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.integral.IntegralUserRecord;
import com.buy.model.product.ProBackSort;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.user.User;
import com.buy.model.user.UserCashRecord;
import com.buy.numOprate.MathUtil;
import com.buy.plugin.event.efun.EfunWinNoticeEvent;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import net.dreamlu.event.EventKit;

/**
 * 会员参与幸运一折购记录（幸运一折购订单）表
 */
public class EfunUserOrder extends Model<EfunUserOrder>{
	
	private  Logger L = Logger.getLogger(EfunUserOrder.class);
	
	/**
	 * 配送方式：0.未选择配送方式；1.快递；2.自提；3.到店消费
	 */
	public static final int DELIVERY_TYPE_WAIT = 0;
	public static final int DELIVERY_TYPE_EXPRESS = 1;
	public static final int DELIVERY_TYPE_SELF = 2;
	public static final int DELIVERY_TYPE_SHOP_CONSUME  = 3;
	
	
	/**
	 * 是否真实数据-否
	 */
	public static final int IS_REAL_NO = 0;
	/**
	 * 是否真实数据-是
	 */
	public static final int IS_REAL_YES = 1;
	/**
	 * 是否真实数据-b类假数据
	 */
	public static final int IS_REAL_B = 2;
	/**
	 * 是否真实数据-c类假数据
	 */
	public static final int IS_REAL_C = 3;
	
	/************订单类型跟商品来源字段对应**************/
	/**
	 * 订单类型:专卖
	 */
	public final static int ORDER_TYPE_EXCLUSIVE = 1;
	/**
	 * 订单类型:自营专卖
	 */
	public final static int ORDER_TYPE__SELF_EXCLUSIVE = 2;
	/**
	 * 订单类型:自营公共
	 */
	public final static int ORDER_TYPE__SELF_PUBLIC = 3;
	/**
	 * 订单类型:e趣代售
	 */
	public final static int ORDER_TYPE__FACTORY = 4;
	/**
	 * 订单类型:厂家自发
	 */
	public final static int ORDER_TYPE__FACTORY_SEND = 5;
	
	/**
	 * 订单是否释放了锁定库存
	 */
	public static final int IS_RELEASE_LOCK_COUNT_Y = 1;  
	public static final int IS_RELEASE_LOCK_COUNT_N = 0;  
	
	public static final String NO_PREFIX = "ERD";  
//========================================================================//
//========================================================================//
//==========================幸运一折购人气营造区=========================//
//========================================================================//
//========================================================================//	
	//TODO 全部写着这里，方便以后撤销，pc，app可以公用
	public static final String SPECIAL_USER_ID = "2568088"; //会员id
	public static final int TIME_TWO = 2;                //2分钟
	public static final int TIME_FIVE = 5;               //5分钟
	public static final int BIG_RECORD = 15;             //已有记录数
	
	
	////////////////////幸运一折+标识///////////////////////
	/** 幸运一折+标识 - 普通订单 **/
	public final static int EFUN_PLUS_ORDINARY = 1;
	/** 幸运一折+标识 - 幸运一折吃订单 **/
	public final static int EFUN_PLUS_EAT = 2;
	
	
	/**
	 * 生成记录
	 * @param proId
	 */
	public void addC(int proId, int efunId, ProductSku sku){
		//概率
		int efunChance = EfunChance.dao.getNewestChance();
		int Y = 28/efunChance;
		int c = getAttendNum(efunId, proId, IS_REAL_C);
		Date date = new Date();
		Efun efun = Efun.dao.findById(efunId);
		if(Y>1){
			if(c<10){
				create(proId, efunId, efun, sku, date, IS_REAL_C);
			}
		}else{
			if(c<8){
				create(proId, efunId, efun, sku, date, IS_REAL_C);
			}
		}
	}
	
	/**
	 * 生成记录
	 * @param proId
	 */
	public void create(int proId, int efunId, Efun efun, ProductSku sku, Date date, int fakeType){
		//获取用户名
		String userName = getUserName();
		//获取地址
		Record record = getAddress();
		
		Product product = Product.dao.findById(proId);
		
		EfunUserOrder efunUserOrder = new EfunUserOrder();
		efunUserOrder.set("id", StringUtil.getUUID());//订单主键使用UUID
		efunUserOrder.set("efun_id", efunId);
		efunUserOrder.set("lottery_time", efun.getTimestamp("lottery_time"));
		efunUserOrder.set("user_id", SPECIAL_USER_ID);
		efunUserOrder.set("user_name", userName);//用户名
		efunUserOrder.set("product_id", proId);
		efunUserOrder.set("sku_code", sku.get("code"));
		efunUserOrder.set("product_name", product.getStr("name"));
		efunUserOrder.set("product_property",  "");
		efunUserOrder.set("eq_price", sku.getBigDecimal("eqPrice"));
		efunUserOrder.set("price", sku.getBigDecimal("efunPrice"));//参与幸运一折购购买价格
		efunUserOrder.set("product_img", product.getStr("product_img"));//商品图片
		//efunUserOrder.set("is_balance", IS_BALANCE_YES);//结算状态
		//efunUserOrder.set("status", EfunUserOrder.STATUS_HAD_EVALUATION); //已评价
		efunUserOrder.set("is_real", fakeType); //不是真实数据(2.b类假数据；3.c类假数据)
		
		//获取收货地址信息
		//efunUserOrder.set("province", record.getStr("province"));//收货人所在省
		//efunUserOrder.set("city", record.getStr("city"));//收货人所在市
		efunUserOrder.set("create_time", date);
		
		efunUserOrder.set("pay_time", date);
		//账户现金足够支付时生成一个从1到28的随机整数
		//获取已生成好的本期可能号码及相关数据的缓存数据
		Cache efunNumbersCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
		List<Integer> numberList = efunNumbersCache.lrange("efun_numbers", 0, efunNumbersCache.llen("efun_numbers"));
		numberList = getOtherNumbers(numberList);
		Random ran = new Random();
		Integer num = numberList.get(ran.nextInt(numberList.size()));
		efunUserOrder.set("number", num);
		
		//保存幸运一折购订单
		efunUserOrder.save();
		
	}
	
	/**
	 * 返回已参与幸运一折购sku
	 * @param proId
	 * @return
	 */
	public ProductSku getRandomSku(int proId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT sku.* ");
		sql.append(" FROM t_pro_sku sku ");
		sql.append(" WHERE sku.is_efun = ? ");
		sql.append(" AND product_id = ?  ");
		sql.append(" order by rand() LIMIT 1  ");
		
		return ProductSku.dao.findFirst(sql.toString(), ProductSku.IS_EFUN, proId);
	}
	/**
	 * 获取当前期次参与记录数
	 * @param efunId
	 * @return
	 */
	public int getHasAttendNum(int efunId,int proId){
		Record record = Db.findFirst("select count(id) num from t_efun_user_order where efun_id = ? and product_id = ? and status > 0", efunId, proId);
		return Integer.valueOf(record.get("num").toString());
	}
	/**
	 * 随机获取一个省市地址
	 * @return
	 */
	public Record getAddress(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  b.name province,a.name city ");
		sql.append(" FROM (SELECT * FROM t_address WHERE `level` = 2) a ");
		sql.append(" LEFT JOIN (SELECT * FROM t_address WHERE `level` = 1) b ");
		sql.append(" ON a.parent_code = b.code ");
		sql.append(" order by rand() LIMIT 1 ");
		
		return Db.findFirst(sql.toString());
	}
	
	/**
	 * 随机生成用户名（6-10 位的字母+数组的组合）
	 * @return
	 */
	public String getUserName(){
		//会员名称长度为 6-10
		int length = generateRandomLength(6, 11);
		return MathUtil.getRandomString(length);
	}
	
	/**
	 * 随机长度
	 * @param begin
	 * @param end
	 * @param size
	 * @return
	 */
	public int generateRandomLength(int begin, int end) {
		// 种子你可以随意生成，但不能重复
		int[] seed = new int[end - begin];

		for (int i = begin; i < end; i++) {
			seed[i - begin] = i;
		}
		Random ran = new Random();
		int j = ran.nextInt(seed.length - 0);
		return seed[j];
	}
//========================================================================//
//========================================================================//
//==========================幸运一折购人气营造区=========================//
//========================================================================//
//========================================================================//
	
	/**
	 * 中奖状态:等待开奖
	 */
	public static final int WIN_STATUS_WAIT = 0;
	/**
	 * 中奖状态:中奖
	 */
	public static final int WIN_STATUS_YES = 1;
	/**
	 * 中奖状态:未中奖
	 */
	public static final int WIN_STATUS_NO = 2;
	
	////////////////////是否已结算(0:否，1：是)//////////////
	/**
	* 是否已结算-否
	*/
	public static final int IS_BALANCE_NO = 0;
	/**
	* 是否已结算-是
	*/
	public static final int IS_BALANCE_YES = 1;
	
	////////////////////返利状态///////////////////////
	/**
	* 返利状态-待返利（等待订单完成）
	*/
	public static final int REBATE_WAIT = 0;
	/**
	* 返利状态-返利成功
	*/
	public static final int REBATE_SUCCESS = 1;
	/**
	* 返利状态-返利失败
	*/
	public static final int REBATE_FAIL = 2;
	
	private static final long serialVersionUID = 1L;
	
	public static final EfunUserOrder dao = new EfunUserOrder();
	
	/**
	 * 订单状态-未完成付款
	 */
	public static final int STATUS_NOT_FINISH_PAY = 0;
	/**
	 * 订单状态-已支付
	 */
	public static final int STATUS_PAIED = 1;
	/**
	 * 订单状态-取消订单
	 */
	public static final int STATUS_CANCEL = 2;
	/**
	 * 订单状态-已退款
	 */
	public static final int STATUS_REFUNDED = 3;
	
	////////////////////数据来源(1:PC,2:APP)//////////////
	/**
	* 数据来源-PC
	*/
	public static final int DATA_FROM_PC = 1;
	/**
	* 数据来源-APP
	*/
	public static final int DATA_FROM_APP = 2;

	/**
	 * 一折购中奖发货提醒,开奖后发
	 */
	public static final int EFUN_WIN_NOTICE_ONE = 1;
	/**
	 * 一折购中奖发货提醒,选择快递配送后发
	 */
	public static final int EFUN_WIN_NOTICE_TWO = 2;

	//================幸运一折购热门奖区显示中奖的记录数=======================//
	public static final int EFUN_HOT_PRICE_WIN_NUM = 15;
	//================幸运一折购热门奖区显示中奖的记录数==========================//
	
	
	/**
	 * 根据期次跟商品SKU标识码获取当前会员参与该期次商品SKU的分配号码
	 * @param efunId
	 * @param skuCode
	 * @return
	 * @author Jacob
	 * 2016年1月18日下午5:40:36
	 */
	@SuppressWarnings("null")
	public Integer getNumber(Integer efunId,String skuCode,Integer isRealType){
		
		//初始化返回抽奖号
		Integer number = 0;
		
		boolean result = false;
		while (result==false) {
			ProductSku proSku = ProductSku.dao.findById(skuCode);
			if(proSku!=null){
				StringBuffer updateSql = new StringBuffer();
				updateSql.append(" UPDATE t_pro_sku SET version = version + 1 ");
				updateSql.append(" WHERE");
				updateSql.append(" version = ?");
				updateSql.append(" AND code = ?");
				int resultCount = Db.update(updateSql.toString(),proSku.getInt("version"),skuCode);
				//更新成功
				if(resultCount!=0){
					//获取已生成好的本期可能号码及相关数据的缓存数据
					Cache efunNumbersCache = Redis.use(BaseConstants.Redis.CACHE_OTHER_DATA);
					
					List<Integer> numberList = efunNumbersCache.lrange("efun_numbers", 0, efunNumbersCache.llen("efun_numbers"));
					
					//判断是否为假数据
					if(StringUtil.notNull(numberList)&&(isRealType == IS_REAL_B||isRealType == IS_REAL_C)){
						numberList = getOtherNumbers(numberList);
					}
					
					//概率
					int efunChance = EfunChance.dao.getNewestChance();
					if(StringUtil.isNull(numberList)){
						//初始化1-28的号码列表
						for(int i=1;i<=28;i++){
							numberList.add(i);
						}
						efunChance = 28;
					}
					//获取参与该期次商品SKU已分配号码的列表
					StringBuffer sql = new StringBuffer();
					sql.append(" SELECT euo.number FROM t_efun_user_order euo WHERE euo.efun_id = ? AND euo.sku_code = ? AND euo.is_real = ? ORDER BY euo.create_time DESC ");
					List<Integer> userNumberList = Db.query(sql.toString(),efunId,skuCode,IS_REAL_YES);
					//已有人参加
					if(StringUtil.notNull(userNumberList)){
						int num = userNumberList.size()%efunChance;
						if(num>0){
							for(int i=0;i<num;i++){
								//剔除已排序的号码
								numberList.remove(userNumberList.get(i));
							}
							//打乱剩余的号码，然后获取第一个号码进行分配
							Collections.shuffle(numberList);
							number = numberList.get(0);
						}else{
							//打乱新奖区的号码，然后获取第一个号码进行分配
							Collections.shuffle(numberList);
							number = numberList.get(0);
						}
					}else{
						//打乱第一个奖区的号码，然后获取第一个号码进行分配
						Collections.shuffle(numberList);
						number = numberList.get(0);
					}
					result = true;
				}else{
					result = false;
				}
			}else{
				//数据已被删除
				throw new RuntimeException("分配抽奖号码数据异常");
				//result = false;
			}
		}
		return number;
	}
	
	/**
	 * 查找中奖人信息
	 * @param efunId
	 * @param winNumber
	 * @return
	 */
	public List<Record> getUserInfo4Efun(int efunId, int winNumber) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.mobile,");
		sql.append(" 	b.user_name userName,");
		sql.append(" 	a.product_name proName,");
		sql.append(" 	a.create_time createTime");
		sql.append(" FROM t_efun_user_order a");
		sql.append(" LEFT JOIN t_user b ON b.id = a.user_id");
		sql.append(" LEFT JOIN t_efun c ON c.id = a.efun_id");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND a.efun_id = ?");
		sql.append(" AND c.win_number = ?");
		return Db.find(sql.toString(), efunId, winNumber);
	}
	
	/**
	 * 判断会员是否是第一次参与幸运一折购
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年4月11日上午11:38:40
	 */
	public boolean isFirst(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	a.id");
		sql.append(" FROM t_efun_user_order a");
		sql.append(" WHERE ");
		sql.append(" 	a.user_id = ?");
		return null == Db.findFirst(sql.toString(), userId)?true:false;
	}
	
	/**
	 * 获取对应的商品SKU识别码
	 * @param efunUserOrderId
	 * @return
	 * @author Jacob
	 * 2016年4月27日下午3:00:47
	 */
	public String getSkuCode(String efunOrderId){
		return dao.findByIdLoadColumns(efunOrderId, "sku_code").getStr("sku_code");
	}
	
	/**
	 * 根据会员ID获取幸运一折购订单
	 * @param efunOrderId 幸运一折购订单ID
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2016年4月27日下午3:15:30
	 */
	public EfunUserOrder getEfunOrder(String efunOrderId,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append(" 	*");
		sql.append(" FROM t_efun_user_order a");
		sql.append(" WHERE ");
		sql.append(" 	a.user_id = ?");
		sql.append(" AND ");
		sql.append(" 	a.id = ?");
		sql.append(" AND ");
		sql.append(" 	a.status > ?");
		return dao.findFirst(sql.toString(), userId, efunOrderId, EfunUserOrder.STATUS_NOT_FINISH_PAY);
	}
	
	/**
	 * 检查会员是否九折购买了某一期次某个商品SKU的幸运一折购商品
	 * @param userId 会员ID
	 * @param efunOrderId 幸运一折购订单ID
	 * @return
	 * @author Jacob
	 * 2016年3月21日上午11:21:20
	 */
	public boolean checkNineBuy(String userId,String efunOrderId){
		String sql = " SELECT is_efun_nine FROM t_efun_user_order WHERE id = ? AND user_id = ? ";
		int isEfunNine = Db.queryInt(sql,efunOrderId,userId);
		if(isEfunNine==BaseConstants.YES){
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * 专卖商品结算
	 * @param orderId
	 * @author Jacob
	 * 2016年7月2日上午11:52:27
	 */
	public void monopolyPorductSettlement(String orderId){
		//获取订单
		EfunUserOrder order = EfunUserOrder.dao.findById(orderId);
		//获取商品
		Product product = Product.dao.findById(order.getInt("product_id"));
		//获取店铺账户
		Account account = Account.dao.getAccountForUpdate(product.getStr("shop_id"), Account.TYPE_SHOP);
		//获取店铺
		Shop shop = Shop.dao.findById(product.getStr("shop_id"));
		//结算总额（在生成幸运一折购订单时已对结算价进行处理：e趣价*(1-扣佣率-4%)）
		BigDecimal total = order.getBigDecimal("supplier_price");
		account.set("cash", account.getBigDecimal("cash").add(total).setScale(2,BigDecimal.ROUND_DOWN));
		account.set("update_time", new Date());
		account.update();
		//添加供货商现金对账记录
		String shopId = shop.getStr("id");
		BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
		ShopCashRecord.dao.add(total.setScale(2,BigDecimal.ROUND_DOWN), account.getBigDecimal("cash").setScale(2,BigDecimal.ROUND_DOWN).add(freezeCash), "", ShopCashRecord.TYPE_EFUN_BALANCE, shopId, "第三方店铺幸运一折购订单确认收货后商品结算，幸运一折购订单ID："+order.get("id"));
		//更新订单结算状态
		order.set("is_balance", EfunUserOrder.IS_BALANCE_YES);
		order.update();
	}
	
	/**
	 * 退回现金/积分
	 * @param orderId 幸运一折购订单ID
	 * @author Jacob
	 * 2016年1月20日下午3:17:43
	 */
	public void returnCashAndIntegral(String orderId){
		EfunUserOrder order = EfunUserOrder.dao.getEfunUserOrderForUpdate(orderId);
		//验证订单未完成付款状态
		if(order==null||order.getInt("status")!=EfunUserOrder.STATUS_NOT_FINISH_PAY){
			return;
		}
		
		Account account = Account.dao.getAccountForUpdate(order.getStr("user_id"), Account.TYPE_USER);
		//获取会员账号
		String userName = User.dao.getUserName(order.getStr("user_id"));
		//假如使用账户现金大于0，则退回现金
		if(order.getBigDecimal("cash").compareTo(BigDecimal.ZERO)==1){
			account.set("cash", account.getBigDecimal("cash").add(order.getBigDecimal("cash")).setScale(2,BigDecimal.ROUND_DOWN));
			account.set("update_time", new Date());
			account.update();
			//添加会员现金对账记录
			String userId = order.getStr("user_id");
			BigDecimal freezeCash = account.getBigDecimal("freeze_cash");
			UserCashRecord.dao.add(order.getBigDecimal("cash"), account.getBigDecimal("cash").setScale(2,BigDecimal.ROUND_DOWN).add(freezeCash),  UserCashRecord.TYPE_REFUND, userId, "幸运一折购未完成付款订单退回现金");
		}
		//假如使用账户积分大于0，则退回积分
		if(order.getInt("use_integral")>0){
			account.set("integral", account.getInt("integral")+order.getInt("use_integral"));
			account.set("update_time", new Date());
			account.update();
			//添加会员积分对账记录
			String userId = order.getStr("user_id");
			IntegralRecord.dao.add(order.getInt("use_integral"), account.getInt("integral"),  IntegralRecord.TYPE_CANCEL_ORDER, userId, userName, "幸运一折购未完成付款订单退回积分");
			/******************【幸运一折购未完成付款订单退回积分-积分退回记录】*******************/
			IntegralUserRecord.dao.saveIntegralUserRecord4Return(orderId,IntegralUserRecord.TYPE_EFUN_ORDER);
		}
		
		//回归库存
		if(order.getInt("is_release_lock_count") == EfunUserOrder.IS_RELEASE_LOCK_COUNT_N){
			ProductSku.dao.subtractLockCount(order.getStr("sku_code"), 1);
			order.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_Y);
			order.update();
		}

		//取消订单
		EfunUserOrder.dao.findById(orderId).set("status",EfunUserOrder.STATUS_CANCEL).update();

	}
	

	/**
	 * 提交幸运一折购订单（会员参与幸运一折购记录）
	 * @param userId 会员ID
	 * @param skuCode 商品SKU识别码
	 * @param efunId 幸运一折购期次
	 * @param useIntegral 使用账户积分数量
	 * @param useCash 使用账户现金金额
	 * @param addressId 收货地址
	 * @param payPassword 支付密码
	 * @param dataFrom 数据来源（1:PC,2:APP）
	 * @author Jacob
	 * 2016年1月16日下午3:07:58
	 */
	@Before(Tx.class)
	public JsonMessage submitEfunOrder(String userId,String skuCode,Integer efunId,Integer useIntegral,BigDecimal useCash,
			Integer addressId,String payPassword,int dataFrom){
		
		JsonMessage jsonMessage = new JsonMessage();
		
		//当使用了账户积分或现金时需要判断支付密码是否正确（WebEfunValidator.java已验证）
		Account account = Account.dao.getAccountForUpdate(userId, Account.TYPE_USER);
		Integer integral = account.getInt("integral");
		BigDecimal cash = account.getBigDecimal("cash");
		//判断使用积分是否大于会员账户积分余额
		if(useIntegral>integral){
			jsonMessage.setStatusAndMsg("100", "使用积分不能大于账户剩余积分！");
			return jsonMessage;
		}
		//判断使用现金是否大于会员账户现金余额
		if(useCash.compareTo(cash)==1){
			jsonMessage.setStatusAndMsg("200", "使用现金不能大于账户剩余现金！");
			return jsonMessage;
		}
		/*int proId = ProductSku.dao.getProIdBySku(skuCode);
		//判断是否已参与了该期该商品的幸运一折购
		if(EfunSku.dao.isPartake(userId, efunId, proId)){
			jsonMessage.setStatusAndMsg("2", "您已参与过该商品当前期次的幸运一折购！");
			return jsonMessage;
		}*/
		
		
		if(!ProductSku.dao.enoughCount(skuCode, 1)){
			jsonMessage.setData(ProductSku.dao.getSkuInventoryMessage(skuCode));
			jsonMessage.setStatusAndMsg("99", "库存不足");
			return jsonMessage;
		}
		
		/*******************【获取幸运一折购商品SKU相关信息】**********************/
		Record efunSkuInfo = EfunSku.dao.getEfunSkuInfo(skuCode, addressId, 1);
		//运费
		BigDecimal freight = efunSkuInfo.getBigDecimal("freight");
		/********************************************1.生成幸运一折购订单（会员参与幸运一折购记录）**********************************************************/
		
		EfunUserOrder efunUserOrder = new EfunUserOrder();
		efunUserOrder.set("id", StringUtil.getUUID());//订单主键使用UUID
		efunUserOrder.set("efun_id", efunId);
		efunUserOrder.set("lottery_time", Efun.dao.findByIdLoadColumns(efunId, "lottery_time").getTimestamp("lottery_time"));
		efunUserOrder.set("user_id", userId);
		efunUserOrder.set("user_name", User.dao.getUserName(userId));//by @author chenhg
		efunUserOrder.set("product_id", efunSkuInfo.getInt("productId"));
		efunUserOrder.set("sku_code", skuCode);
		efunUserOrder.set("product_name", efunSkuInfo.getStr("productName"));
		efunUserOrder.set("product_property",  efunSkuInfo.getStr("propertyValues"));
		efunUserOrder.set("eq_price", efunSkuInfo.getBigDecimal("eqPrice"));
		efunUserOrder.set("price", efunSkuInfo.getBigDecimal("efunPrice"));//参与幸运一折购购买价格
		efunUserOrder.set("order_type", efunSkuInfo.getInt("productSource"));//一折购订单类型
		efunUserOrder.set("merchant_id", efunSkuInfo.getStr("merchantId"));//商家id
		efunUserOrder.set("merchant_no", efunSkuInfo.getStr("merchantNo"));//商家编号
		efunUserOrder.set("merchant_name", efunSkuInfo.getStr("merchantName"));//商家名称
		

		//商品类型
		int productSource = efunSkuInfo.getInt("productSource");

		//判断商品是否为专卖商品（设置相应的结算价，结算时使用,并标识幸运一折+类型）
		if(productSource==Product.SOURCE_EXCLUSIVE||productSource==Product.SOURCE_SELF_EXCLUSIVE){
			//获取该商品的佣金率
			BigDecimal commissionRate = ProBackSort.dao.getCommissionRate(efunSkuInfo.getInt("productId"));
			if(null == commissionRate){
				commissionRate = BigDecimal.ZERO;
			}
			//产品供货价格(结算价）（商品SKU的e趣价*(1-佣金率-一折购佣金率)）
			BigDecimal efunRate = SysParam.dao.getBigDecimalByCode("efun_commission_rate");
			efunUserOrder.set("supplier_price", efunSkuInfo.getBigDecimal("eqPrice").multiply(new BigDecimal("1").subtract(commissionRate).subtract(efunRate)).setScale(2, BigDecimal.ROUND_CEILING));
			//根据店铺类型，记录订单幸运一折+类型
			efunUserOrder.set("efun_plus_type", Shop.dao.getType(efunSkuInfo.getStr("merchantId")));//幸运一折+类型
		}else{
			efunUserOrder.set("supplier_price", efunSkuInfo.getBigDecimal("supplierPrice"));//供货价格(结算价)
			efunUserOrder.set("efun_plus_type", this.EFUN_PLUS_ORDINARY);//幸运一折+类型
		}
		
		efunUserOrder.set("product_img", efunSkuInfo.getStr("productImg"));//商品图片	
		efunUserOrder.set("create_time", new Date());
		efunUserOrder.set("data_from", dataFrom);
		
		/********************************************2.根据支付规则对生成的订单进行支付**************************************************/
		
		//幸运一折购价
		BigDecimal efunPrice = efunSkuInfo.getBigDecimal("efunPrice");
		//积分抵扣金额
		BigDecimal remainingIntegralDiscount = Integral.dao.getIntegralToCash(useIntegral);
		String productPropertys = StringUtil.notNull(efunUserOrder.getStr("product_property"))?efunUserOrder.getStr("product_property").replace(",", " "):"";
		//备注
		String remark = "会员"+User.dao.getUserName(userId)+"参与 "+efunUserOrder.getStr("product_name")+" "+
				productPropertys+" 第"+efunId+"期幸运一折购";
		
		//保存订单总额
		efunUserOrder.set("total", efunPrice);
		
		boolean hasPayFlag = false;//是否已经完全支付（完全支付需要增加锁定库存）
		
		//当使用积分抵扣金额大于等于幸运一折购价时,全部用积分支付
		if (remainingIntegralDiscount.compareTo(efunPrice) == 1 
				|| remainingIntegralDiscount.compareTo(efunPrice) == 0) {
			
			efunUserOrder.set("use_integral", Integral.dao.getCashToIntegral(efunPrice));//抵扣的积分
			efunUserOrder.set("integral_discount", efunPrice);//积分抵扣金额
			//更新会员账户积分余额保存积分对账单（同时更新积分条目剩余值）
			Account.dao.updateAccountIntegralAddSaveRecord(userId,efunUserOrder.getStr("id"), Integral.dao.getCashToIntegral(efunPrice), IntegralRecord.TYPE_EFUN_ORDER,IntegralUserRecord.TYPE_EFUN_ORDER, remark);
			//付款时间
			efunUserOrder.set("pay_time", new Date());
			//生成一个从1到28的随机整数
			efunUserOrder.set("number", EfunUserOrder.dao.getNumber(efunId, skuCode, IS_REAL_YES));
			efunUserOrder.set("status", EfunUserOrder.STATUS_PAIED); //待发货
			
			///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
			this.rebate(userId, efunPrice, efunUserOrder.getStr("id"));
			efunUserOrder.set("rebate_status", EfunUserOrder.REBATE_SUCCESS);
			//设置默认的配送方式,如果购买商品不是入驻o2o云店:设置为快递配送

/*			if(efunUserOrder.getInt("efun_plus_type")==EfunUserOrder.EFUN_PLUS_EAT){
				//配送方式设置为到店消费
				efunUserOrder.set("delivery_type", EfunUserOrder.DELIVERY_TYPE_SHOP_CONSUME);
			}else{
				defaultDeliveryType(efunUserOrder,skuCode);
			}*/
			
			hasPayFlag = true;
		} else {
			if(useIntegral > 0){
				efunUserOrder.set("use_integral", useIntegral);//抵扣的积分
				efunUserOrder.set("integral_discount", remainingIntegralDiscount);//积分抵扣金额
				//更新会员账户积分余额保存积分对账单（同时更新积分条目剩余值）
				Account.dao.updateAccountIntegralAddSaveRecord(userId,efunUserOrder.getStr("id"), useIntegral, IntegralRecord.TYPE_EFUN_ORDER,IntegralUserRecord.TYPE_EFUN_ORDER, remark);
			}
			//商品幸运一折购价减去剩余积分抵扣金额后剩余待支付金额(第一次)
			BigDecimal remainingSum  = efunPrice.subtract(remainingIntegralDiscount);
			/**2.判断使用现金是否够**/
			if(useCash.compareTo(remainingSum)==1||useCash.compareTo(remainingSum)==0){
				efunUserOrder.set("cash", remainingSum);//保存使用现金金额
				//更新会员账户现金金额保存现金对账单
				Account.dao.updateAccountCashAddSaveRecord(userId, remainingSum, UserCashRecord.TYPE_EUN_ORDER,  remark);
				//付款时间
				efunUserOrder.set("pay_time", new Date());
				//账户现金足够支付时生成一个从1到28的随机整数
				efunUserOrder.set("number", EfunUserOrder.dao.getNumber(efunId, skuCode, IS_REAL_YES));
				efunUserOrder.set("status", EfunUserOrder.STATUS_PAIED); //待发货
				///////////////////////【幸运一折购返利（会员所属店铺和代理商）】//////////////////////////////
				this.rebate(userId, efunPrice, efunUserOrder.getStr("id"));
				efunUserOrder.set("rebate_status", EfunUserOrder.REBATE_SUCCESS);

/*				if(efunUserOrder.getInt("efun_plus_type")==EfunUserOrder.EFUN_PLUS_EAT){
					//配送方式设置为到店消费
					efunUserOrder.set("delivery_type", EfunUserOrder.DELIVERY_TYPE_SHOP_CONSUME);
				}else{
					//设置默认的配送方式
					defaultDeliveryType(efunUserOrder,skuCode);
				}*/

				hasPayFlag = true;
			}else{
				if(useCash.compareTo(new BigDecimal(0))==1){
					efunUserOrder.set("cash", useCash);//保存使用现金金额
					//更新会员账户现金金额保存现金对账单
					Account.dao.updateAccountCashAddSaveRecord(userId, useCash, UserCashRecord.TYPE_EUN_ORDER,  remark);
				}
				efunUserOrder.set("cost", remainingSum.subtract(useCash));//保存在线等待支付金额
				efunUserOrder.set("status", EfunUserOrder.STATUS_NOT_FINISH_PAY); //未完成支付
				//使用的积分跟使用的账户现金不够
				jsonMessage.setStatusAndMsg("3", "余额不足");
			}
		}
		
		
		
		/**完全支付，需要增加商品锁定库存**/
		if(hasPayFlag){
			if(!ProductSku.dao.addLockCount(skuCode, 1)){
				try {
					//事务回滚
					DbKit.getConfig().getConnection().rollback();
					jsonMessage.setData(ProductSku.dao.getSkuInventoryMessage(skuCode));
					jsonMessage.setStatusAndMsg("99", "库存不足");
					return jsonMessage;
				} catch (SQLException e) {
					L.error("参与幸运一折购,sku为："+skuCode+"扣减库存不成功,进行事务回滚失败");
				}
			}else{
				efunUserOrder.set("is_release_lock_count", EfunUserOrder.IS_RELEASE_LOCK_COUNT_N);
				
			}
			
		}
		
		//保存幸运一折购订单
		efunUserOrder.save();
				
		if(hasPayFlag){
			//参与并支付成功，锁定库存2个小时后释放
			MqUtil.send(MqConstants.Queue.EFUN_ORDER_LOCK_STORE_DELAY, efunUserOrder.getStr("id"));
		}
		
		
		Map<String,Object> dataMap = new HashMap<String,Object>();
		dataMap.put("orderId", efunUserOrder.getStr("id"));
		jsonMessage.setData(dataMap);
		
		return jsonMessage;
		
	}

	
	
	/**
	 * 幸运一折购返利（购买成功时）
	 * @param userId
	 * @param efunPrice
	 * @param efunOrderId
	 * @author Jacob
	 * 2016年1月22日下午3:00:29
	 */
	public void rebate(String userId,BigDecimal efunPrice,String efunOrderId){
		
		L.info("幸运一折购("+efunOrderId+")购买-返利");
		
		//店铺返利金额
		BigDecimal shopRebate = efunPrice.divide(new BigDecimal("28"),2,BigDecimal.ROUND_DOWN);
		//代理商返利金额
		BigDecimal agentRebate = efunPrice.divide(new BigDecimal("56"),2,BigDecimal.ROUND_DOWN);
		if(shopRebate.compareTo(new BigDecimal("0.01"))==-1){
			shopRebate = new BigDecimal("0.01");
		}
		if(agentRebate.compareTo(new BigDecimal("0.01"))==-1){
			agentRebate = new BigDecimal("0.01");
		}
		//获取会员所在店铺ID和代理商ID
		User user = User.dao.findByIdLoadColumns(userId, "shop_id,agent_id");
		String userShopId = user.getStr("shop_id");
		String userAgentId = user.getStr("agent_id");
		//获取会员所在店铺账户
		Account userShopAccount = Account.dao.getAccountForUpdate(userShopId, Account.TYPE_SHOP);
		//获取会员所属代理商账户
		Account userAgentAccount = Account.dao.getAccountForUpdate(userAgentId, Account.TYPE_AGENT);
		if(userShopAccount!=null){
			//更新会员所属店铺账户
			userShopAccount.set("cash", userShopAccount.getBigDecimal("cash").add(shopRebate).setScale(2,BigDecimal.ROUND_DOWN));
			userShopAccount.set("update_time", new Date());
			userShopAccount.update();
		}else{
			L.info("幸运一折购("+efunOrderId+")购买-会员所属店铺返利失败");
		}
		
		//添加店铺现金对账单记录
		BigDecimal freezeCashShop = userShopAccount.getBigDecimal("freeze_cash");
		ShopCashRecord.dao.addNotOrder(shopRebate.setScale(2,BigDecimal.ROUND_DOWN), userShopAccount.getBigDecimal("cash").add(freezeCashShop), efunOrderId, ShopCashRecord.TYPE_REBATE_SHOP_EFUN, userShopId, "店铺幸运一折购返利");
		
		if(userAgentAccount!=null){
			//更新会员所属代理商账户
			userAgentAccount.set("cash", userAgentAccount.getBigDecimal("cash").add(agentRebate).setScale(2,BigDecimal.ROUND_DOWN));
			userAgentAccount.set("update_time", new Date());
			userAgentAccount.update();
		}else{
			L.info("幸运一折购("+efunOrderId+")购买-会员所属代理商返利失败");
		}
		
		//添加代理商现金对账单记录
		BigDecimal freezeCashAgent = userAgentAccount.getBigDecimal("freeze_cash");
		AgentCashRecord.dao.add(agentRebate.setScale(2,BigDecimal.ROUND_DOWN), userAgentAccount.getBigDecimal("cash").add(freezeCashAgent), "",  AgentCashRecord.REBATE_EFUN, efunOrderId, userAgentId, "幸运一折购返利");
	}
	
	/**
	 * 
	 * 获取幸运一折购订单（锁定）
	 * @author huangzq 
	 * @date 2016年6月22日 上午11:43:52
	 * @param orderId
	 * @return
	 */
	public EfunUserOrder getEfunUserOrderForUpdate(String euoId){
		String sql = "select * from t_efun_user_order r where r.id = ? for update";
		return  dao.findFirst(sql,euoId);
	}
	
	/**
	 * 获取幸运一折购订单（锁定）
	 * @param orderId
	 * @return
	 */
	public EfunUserOrder getEfunUserOrderForUpdateWithOrderId(String orderId){
		String sql = "select * from t_efun_user_order r where r.order_id = ? for update";
		return  dao.findFirst(sql,orderId);
	}
	
	/**
	 * 获取幸运一折购商品九折优惠期限（小时） 配置参数
	 */
	public Integer getEfunNineDay(){
		String sql = "select sp.`value` efunNineDay from t_sys_param sp where sp.`code` = 'efun_nine_day' ";
		Record record = Db.findFirst(sql);
		Integer efunNineDay = 24;
		if(!StringUtil.isNull(record.getStr("efunNineDay"))){
			efunNineDay = Integer.valueOf(record.get("efunNineDay").toString());
		}
		return efunNineDay;
	}
	
	
	/***
	 * 判断会员订单参与的号码是否中奖
	 * @Author: Jekay
	 * @Date:   2016/9/19 14:01
	 ***/
	public boolean userIsWin(int efunId,String orderId,String userId){
		StringBuffer sbsql = new StringBuffer();
		sbsql.append(" SELECT ");
		sbsql.append(" 	 a.id ");
		sbsql.append(" FROM ");
		sbsql.append("   t_efun_user_order a");
		sbsql.append("   LEFT JOIN t_efun b ON a.efun_id = b.id");
		sbsql.append(" WHERE ");
		sbsql.append("   AND FIND_IN_SET(c.win_number, a.number) > 0");
		sbsql.append("   AND a.efun_id = ?");
		sbsql.append("   AND a.id = ?");
		sbsql.append("   AND a.user_id = ?");

		return StringUtil.isNull(Db.findFirst(sbsql.toString(),efunId,orderId,userId)) ? false : true ;
	}
	
	/**
	 * 获取非抽奖号码的号码列表
	 * @return
	 * @author Jacob
	 * 2016年10月10日下午7:55:07
	 */
	public List<Integer> getOtherNumbers(List<Integer> numbers){
		List<Integer> allNumbers = new ArrayList<Integer>();
		for(int i=0;i<28;i++){
			allNumbers.add(i);
		}
		if(!allNumbers.containsAll(numbers) || !numbers.containsAll(allNumbers)){
			allNumbers.removeAll(numbers);
		}
		return allNumbers;
	}
	
	/**
	 * 根据isReal获取当前期次参与记录数
	 * @param efunId
	 * @param proId
	 * @param isRealType(0.否，1.是，2.b类假数据，3.c类假数据)
	 * @return
	 * @author Jacob
	 * 2016年10月11日下午5:47:31
	 */
	public int getAttendNum(int efunId,int proId,int isRealType){
		String sql = " select count(id) num from t_efun_user_order where efun_id = ? and product_id = ? and status > 0 and is_real = ? ";
		return Db.queryLong(sql, efunId, proId, isRealType).intValue();
	}
	
	/**
	 * 获取当前期次一折购商品奖区数
	 * @param efunId
	 * @param proId
	 * @return
	 * @author Jacob
	 * 2016年10月11日下午6:12:13
	 */
	public int getAwardNum(int efunId,int proId){
		int awardNum = 0;
		String sql = " select count(id) num from t_efun_user_order where efun_id = ? and product_id = ? and status > 0";
		int attendNum  = Db.queryLong(sql, efunId, proId).intValue();
		int remainder = attendNum%28;
		if(remainder==0){
			awardNum = attendNum/28;
		}else{
			awardNum = attendNum/28+1;
		}
		return awardNum;
	}

	/**
	 * 一折购中奖订单-发送短信提醒发货
	 * 1、自提的订单，不需要发短信给店家或者厂家
	 * 2、等待选择提货方式的需等到选择快递发货的时候发短信提醒
	 * 3、如果只有快递的则是开完奖就提示店家或者厂家
	 * 4、提示的店家或者厂家的条件：商品属于专卖店铺、厂家自发的才需要发短信
	 * type : 1 开奖后如果是非O2O商品即发送, 2 用户选择自提方式,为快递(并且已支付运费的)时发送
	 * @Author: Jekay
	 * @Date:   2016/11/7 19:11
	 */
	public void efunWinNoticeMerchant(String skuCode,int dataFrom,int efunId,int type){
		StringBuffer sbsql = new StringBuffer();
		sbsql.append(" SELECT ");
		sbsql.append("   eu.sku_code skuCode,p.shop_id shopId,");
		sbsql.append("   eu.product_name proName,eu.product_property property,");
		sbsql.append("   eu.`status`,eu.delivery_type,is_pay_freight ");
		sbsql.append(" FROM ");
		sbsql.append("  t_efun_user_order eu");
		sbsql.append("  LEFT JOIN t_efun e ON eu.efun_id = e.id ");
		sbsql.append("  LEFT JOIN t_product p ON eu.product_id = p.id  ");
		sbsql.append(" WHERE ");
		sbsql.append("  1 = 1");
		sbsql.append(" AND FIND_IN_SET(e.win_number, eu.number) > 0");
		sbsql.append(" AND e.id = ? ");
		sbsql.append(" AND eu.is_real = ? ");
		sbsql.append(" AND p.source = ? ");
		sbsql.append(" AND eu.sku_code = ? ");
		List<Record> records = Db.find(sbsql.toString(),efunId,IS_REAL_YES ,Product.SOURCE_EXCLUSIVE,skuCode);
		if(StringUtil.notNull(records) && records.size()>0){
			/**发送提示发货短信驱动事件**/
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("skuCode",skuCode);
			map.put("dataFrom", dataFrom);
			map.put("shopId", records.get(0).getStr("shopId"));
			map.put("proName", records.get(0).getStr("proName"));
			map.put("property", records.get(0).getStr("property"));
			map.put("efunId", efunId);
			map.put("winCount",records.size());
			int unSend = 0;
			for(Record r : records){
				//提货方式选择 快递配送 时发提醒的条件:状态为待发货,配送方式为快递,邮费为已支付或包邮
				if(r.getInt("status") == 1 && r.getInt("delivery_type") == 1 && r.getInt("is_pay_freight") == 1){
					unSend +=1;
				}
			}
			if(type == 1){
				map.put("unSend",records.size());
			}else if(type == 2){
				map.put("unSend",unSend);
			}
			EventKit.postEvent(new EfunWinNoticeEvent(map));
			/**发送提示发货短信驱动事件**/
		}
	}
	
	/**
	 * 真实用户参与成功时增加相应量的假数据
	 * @param skuCode
	 * @param efunId
	 * @author Jacob
	 * 2016年10月11日下午6:24:54
	 */
	public void addB(String skuCode,int efunId){
		//概率
		int efunChance = EfunChance.dao.getNewestChance();
		int Y = 28/efunChance;
		int proId = ProductSku.dao.getProIdBySku(skuCode);
		int aNum = EfunUserOrder.dao.getAttendNum(efunId, proId, EfunUserOrder.IS_REAL_YES);
		int N = EfunUserOrder.dao.getAwardNum(efunId, proId);
		if(Y>1&&aNum==(N*efunChance+1)){
			int bNum = EfunUserOrder.dao.getAttendNum(efunId, proId, EfunUserOrder.IS_REAL_B);
			int cNum = EfunUserOrder.dao.getAttendNum(efunId, proId, EfunUserOrder.IS_REAL_C);
			int bNum2 = (aNum-1)*Y-(aNum-1)-cNum-bNum;
			if(bNum2>0){
				//随机生成sku
				ProductSku sku = EfunUserOrder.dao.getRandomSku(proId);
				Date date = new Date();
				Efun efun = Efun.dao.findById(efunId);
				for(int i=0;i<bNum2;i++){
					dao.create(proId, efunId, efun, sku, date, IS_REAL_B);
				}
			}
		}
	}


	/**
	 * 幸运购订单
	 * 如果购买商品不是入驻o2o云店
	 * 则把delivery_type 设置为1 默认为快递配送
	 */
	public void defaultDeliveryType(EfunUserOrder efunUserOrder,String skuCode){
		if(!ProductSku.dao.isO2o(skuCode)){
			efunUserOrder.set("delivery_type",DELIVERY_TYPE_EXPRESS);
		}
	}
	
	public List<Record> findSkuCodeList(String orderId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	ed.sku_code skuCode, ");
		sql.append(" 	ed.count ");
		sql.append(" FROM ");
		sql.append(" 	t_efun_user_order ed ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append("    ed.id = ? ");
		return Db.find(sql.toString(),orderId);
	}
	
	/**
	 * 返回参与记录是否已经释放锁定库存
	 * @param orderId
	 * @return
	 * @author chenhg
	 * 2016年11月28日 下午12:01:56
	 */
	public int getIsReleaseLockCountByOrderId(String orderId){
		return Db.queryInt("SELECT is_release_lock_count FROM t_efun_user_order WHERE order_id = ?", orderId);
	}
	
	/**
	 * 更新是否是否库存的标志
	 * @param orderId
	 * @param releaseLockCountFlag
	 */
	public void updateIsReleaseLockCountByOrderId(String orderId, int releaseLockCountFlag){
		String updateSql = "UPDATE t_efun_user_order SET is_release_lock_count = ? WHERE order_id = ? ";
		Db.update(updateSql, releaseLockCountFlag, orderId);
	}
	/**
	 * 是否超卖参与
	 * @param orderId
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2016年12月4日 上午10:52:57
	 *
	 */
	public boolean isOverSell(String orderId, String userId){
		String sql  = "select count(1) from t_efun_user_order r where r.is_over_sell = ? and r.id = ? and r.user_id = ?";
		long count = Db.queryLong(sql,BaseConstants.YES,orderId,userId);
		if(count>0){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取中奖后24小时仍未选择提货方式的记录
	 * @return
	 */
	public List<Record> getReleaseWinLockCountList(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("  euo.id");
		sql.append(" FROM t_efun_user_order euo ");
		sql.append("  LEFT JOIN t_efun e ON euo.efun_id = e.id");
		sql.append(" WHERE 1 = 1 ");
		sql.append("  AND FIND_IN_SET(e.win_number, euo.number) > 0");
		sql.append("  AND euo.delivery_type = ?");
		sql.append("  AND euo.lottery_time <= DATE_SUB(NOW(),INTERVAL 1 DAY)");
		sql.append("  AND is_real = ?");
		sql.append("  AND is_over_sell = ?");
		sql.append("  AND is_release_lock_count = ?");
		
		return Db.find(sql.toString(), DELIVERY_TYPE_WAIT, IS_REAL_YES, BaseConstants.NO, IS_RELEASE_LOCK_COUNT_N);
	}
	
	public EfunUserOrder getEatOrderMerchant(String orderId, String merchantId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT * FROM t_efun_user_order WHERE `status` = ? AND merchant_id = ? AND id = ? AND efun_plus_type = ?");
		return dao.findFirst(sql.toString(), STATUS_PAIED, merchantId, orderId, EFUN_PLUS_EAT);
	}
	
	/**
	 * 计算需要第三方支付的金额
	 * @param orderIds
	 * @param userId
	 * @author huangzq
	 * 2016年12月29日 上午10:34:54
	 *
	 */
	public BigDecimal calculateCost(String userId,String... orderIds){
		String sql =  "select ifnull(sum(r.cost),0) from t_efun_user_order r where r.status = ? and r.user_id = ? and r.id in("+StringUtil.arrayToStringForSql(",", orderIds)+")";
		return Db.queryBigDecimal(sql,EfunUserOrder.STATUS_NOT_FINISH_PAY,userId);
	}
	
	/**
	 * 统计该产品该期参与人数
	 * @author chenhj
	 * @param proId
	 * @param efunId
	 * @return
	 */
	public Integer countJoin(Integer proId, String efunId) {
		String joinSql = " select COUNT(id) joinCount from t_efun_user_order WHERE product_id = ? and efun_id = ? and status > ? ";
		return Db.queryLong(joinSql, proId, efunId, EfunUserOrder.STATUS_NOT_FINISH_PAY).intValue();
	}
	
	/**
	 * 是否含有历史中奖记录
	 * @author chenhj
	 */
	public int hasEfunId(int proId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT count(a.id) hasEfunId ");
		sql.append(" FROM t_efun_user_order a ");
		sql.append(" LEFT JOIN t_efun b on a.efun_id = b.id  ");
		sql.append(" WHERE a.product_id = ? ");
		sql.append(" AND FIND_IN_SET(b.win_number, a.number) > 0 ");
		sql.append(" AND a.is_real = ?  ");
		sql.append(" AND a.user_id <> ? ");
		sql.append(" AND b.lottery_time < now() ");
		Record record = Db.findFirst(sql.toString(), proId, EfunUserOrder.IS_REAL_YES, EfunUserOrder.SPECIAL_USER_ID);
		int hasEfunId = 1;
		if("0".equals(record.get("hasEfunId").toString())){
			hasEfunId = 0;
		}
		return hasEfunId;
	}
	
	/**
	 * 获取全部参与记录（返回记录长度可参数控制）
	 * @author chenhj
	 */
	public Page<Record> getJoinEfunList(Page<Object> page, String proId, Integer limit){
		StringBuffer selectSql =new StringBuffer();
		StringBuffer whereSql  = new StringBuffer();
		selectSql.append(" select * ");
		
		whereSql.append("  from ");
		whereSql.append("  (select ");
		whereSql.append("  date_format(a.create_time,'%Y-%m-%d %H:%i:%S') create_time, ");
		whereSql.append("  a.efun_id,  ");
		whereSql.append("  a.user_name, ");
		whereSql.append("  a.count, ");
		whereSql.append("  b.avatar, ");
		whereSql.append("  IFNULL(concat(t1.name,' ', t2.name), '') addr ");
		 
		whereSql.append("  from t_efun_user_order a   ");
		whereSql.append("   LEFT JOIN t_user b ON a.user_id = b.id ");
		whereSql.append("   LEFT JOIN t_address t1 ON b.province_code = t1.`code` ");
		whereSql.append("   LEFT JOIN t_address t2 ON b.city_code = t2.`code` ");
		whereSql.append("  WHERE a.product_id = ?  ");
		whereSql.append("  AND a.status > ?  ");
		whereSql.append("  order by a.create_time desc  ");
		whereSql.append("  limit ? ) t ");
		
		Page<Record> pageResult = Db.paginate(page.getPageNumber(), page.getPageSize(), selectSql.toString(), whereSql.toString(), proId, EfunUserOrder.STATUS_NOT_FINISH_PAY, limit);
		List<Record> list = pageResult.getList();
		for(Record rec : list){
			String userName = rec.getStr("user_name");
			if(StringUtil.notNull(userName)){
				userName = StringUtil.hideUserName(userName);
			}
			rec.set("user_name", userName);
		}
		return pageResult;
	}
	
	/**
	 * 获取某一期历史中奖记录的会员获奖信息
	 */
	public List<Record> getEfunWinnerMessage(Integer efunId, Integer proId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select ");
		sql.append("   a.user_name userName, ");
		sql.append("   a.number, ");
		sql.append("   a.count, ");
		sql.append("   b.avatar, ");
		sql.append("   IFNULL(concat(t1.`name`,' ',t2.`name`), '') address, ");
		sql.append("   a.create_time createTime, ");
		sql.append("   c.win_number winNumber, ");
		sql.append("   a.number joinNumber ");
		sql.append(" FROM t_efun_user_order a ");
		sql.append(" LEFT JOIN t_efun c on a.efun_id = c.id ");
		sql.append(" LEFT JOIN t_user b ON a.user_id = b.id ");
		sql.append(" LEFT JOIN t_address t1 ON b.province_code = t1.`code` ");
		sql.append(" LEFT JOIN t_address t2 ON b.city_code = t2.`code` ");
		sql.append(" WHERE c.id = ? ");
		sql.append("   AND FIND_IN_SET(c.win_number, a.number) > 0 ");
		sql.append("   AND a.product_id = ? ");
		sql.append("   AND a.is_real = ? ");
		sql.append("   AND a.user_id <> ? ");
		sql.append(" ORDER BY  a.id DESC ");
		
		List<Record> record = Db.find(sql.toString(), efunId, proId, IS_REAL_YES, EfunUserOrder.SPECIAL_USER_ID);
		for (Record r : record) {
			r.set("user_name", r.getStr("userName"))
			 .set("addr", r.getStr("address"))
			 .set("win_number", r.get("winNumber"))
			 .set("create_time", r.get("createTime"));
		}
		return record;
	}
	
	/**
	 * 支付成功后获取订单信息
	 * @param userId
	 * @param orderIds
	 * @return
	 * @author huangzq
	 * 2016年12月30日 上午10:10:56
	 *
	 */
	public Record getPaySuccInfo(String userId,String... orderIds){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		sql.append("	r.product_name productName,");
		sql.append("	r.product_property productProperty,");
		sql.append("	r.price,");
		sql.append("	r.count,");
		sql.append("	r.efun_id efunId,");
		sql.append("	r.lottery_time 	lotteryTime");
		sql.append(" FROM");
		sql.append("	t_efun_user_order r");
		sql.append(" WHERE");
		sql.append("	r.user_id = ?");
		sql.append(" AND r.STATUS = ?");
		sql.append(" AND r.id in(" + StringUtil.arrayToStringForSql(",", orderIds) + ")");
		List<Record> list = Db.find(sql.toString(), userId, EfunUserOrder.STATUS_PAIED);
		Record r = new Record();
		if(StringUtil.notNull(list)){
			r.set("orderList", list);
			r.set("efunId", list.get(0).getInt("efunId"));
			r.set("lotteryTime", list.get(0).getDate("lotteryTime"));
			r.set("productCount", list.size());
		}
		return r;
	}
}