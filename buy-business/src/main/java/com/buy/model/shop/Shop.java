package com.buy.model.shop;

import com.buy.common.BaseConstants;
import com.buy.date.DateUtil;
import com.buy.model.SysParam;
import com.buy.model.efun.EfunSku;
import com.buy.model.product.Product;
import com.buy.model.trade.Trade;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Shop extends Model<Shop>{
	
	/** 未转出 */
	public final static int STATUS_UNTURNOUT = 0;
	/** 转出未审核 */
	public final static int STATUS_TURNOUT_AUDIT = 1;
	/** 未激活 */
	public final static int STATUS_UNACTIVATED = 2;
	/** 激活待审核 */
	public final static int STATUS_ACTIVATED_AUDIT = 3;
	/** 已激活 */
	public final static int STATUS_ACTIVATED = 4;
	/** 转出失败 */
	public final static int STATUS_TURN_FAIL = 5;
	/** 转出已退款 */
	public final static int STATUS_REFUND = 6;
	
	/** 禁用 - 正常 */
	public final static int FORBIDDEN_STATUS_NORMAL = 0;
	/** 禁用 - 到期  */
	public final static int FORBIDDEN_STATUS_DISABLE_UNPAY = 1;
	/** 禁用 - 违规  */
	public final static int FORBIDDEN_STATUS_STATUS_DISABLE_ILLEGAL = 2;
	
	/** 实名认证 - 未认证 */
	public final static int REALNAME_UNDO = 0;
	/** 实名认证 - 已认证 实名认证 - 已认证 */
	public final static int REALNAME_OK = 1;
	
	/** 开店渠道 - 自主申请 */
	public final static int OPEN_CHANNEL_SELF = 1;
	/** 开店渠道 - 代理商转让 */
	public final static int OPEN_CHANNEL_TURNOUT = 2;
	
	/**
	 * 店铺类型-专卖店铺
	 */
	public static final int BELONG_SELL = 0;
	/**
	 * 店铺类别-e趣自营
	 */
	public static final int BELONG_EFUN = 1;
	
	/**
	 * 总店id
	 */
	public static final int COMPANY_SHOP_ID = 1;
	
	/**
	 * 总店编号
	 */
	public static final String COMPANY_SHOP_NO = "zgs";
	
	/**
	 * 店铺是否过期-已过期
	 */
	public static final int TIME_OUT = 0;
	/**
	 * 店铺是否过期-未过期
	 */
	public static final int TIME_IN = 1;
	
	/**
	 * 店铺是否缴纳了保证金-否
	 */
	public static final int DEPOSIT_NO = 0;
	/**
	 * 店铺是否缴纳了保证金-是
	 */
	public static final int DEPOSIT_YES = 1;
	/**
	 * 店铺续费类型：手动
	 */
	public static final int RENEW_TYPE_MANUAL = 1;
	/**
	 * 店铺续费类型：自动
	 */
	public static final int RENEW_TYPE_AUTO = 2;
	
	/** 店铺生成类型 - 同步代理商 **/
	public final static int BUILD_SYNC_AGENT = 1;
	/** 店铺生成类型 - 赠送代理商 **/
	public final static int BUILD_GIFT_AGENT = 2;
	
	/** 店铺类型 - 普通店铺 **/
	public final static int TYPE_ORDINARY = 1;
	/** 店铺类型 - 幸运一折吃店铺 **/
	public final static int TYPE_EAT = 2;
	

	private static final long serialVersionUID = 1L;
	
	public final static Shop dao = new Shop();
	
	/**
	 * 店铺分配
	 * @param prov		省
	 * @param city		市
	 * @return			店铺ID
	 */
	public Shop getShopAssign(String prov, String city) {
		StringBuffer sql = new StringBuffer(" SELECT s.id, agent_id FROM t_shop s ");
		sql.append(" LEFT JOIN t_address t1 ON t1.code = s.province_code ");
		sql.append(" LEFT JOIN t_address t2 ON t2.code = s.city_code ");
		sql.append(" WHERE 1 = 1 ");
		List<Object> list = new ArrayList<>();
		sql.append(" AND s.status = ? ");			list.add(Shop.STATUS_ACTIVATED);
		sql.append(" AND s.forbidden_status = ? ");	list.add(Shop.FORBIDDEN_STATUS_NORMAL);
		sql.append(" AND DATEDIFF(NOW(),expire_date) <= 0 ");
		if(StringUtil.notNull(prov)) {
			sql.append(" AND t1.name = ? ");		list.add(prov);
		}
		if(StringUtil.notNull(city)) {
			sql.append(" AND t2.name = ? ");		list.add(city);
		}
		sql.append(" ORDER BY RAND() LIMIT 1 ");
		return Shop.dao.findFirst(sql.toString(), list.toArray());
	}
	
	/**
	 * 设置店铺申请信息
	 */
	public Shop setShopByApply(ShopApply shopApply, String shopId) {
		int monthAmount = shopApply.getInt("present_duration");
		Shop shop = Shop.dao.findByIdAndLock(shopId);
		
		Date now = new Date();
		String password = shop.getStr("password");
		monthAmount += shopApply.getInt("prepay_duration");
		
		return shop
			.set("name",			shop.get("no"))
			.set("pay_password",	password)
			.set("shop_keeper",		shopApply.get("real_name"))
			.set("idcard",			shopApply.get("idcard"))
			.set("service_tel",		shopApply.get("mobile"))
			.set("mobile",			shopApply.get("mobile"))
			.set("qq",				shopApply.get("qq"))
			.set("province_code",	shopApply.get("province_code"))
			.set("city_code",		shopApply.get("city_code"))
			.set("area_code",		shopApply.get("area_code"))
			.set("email",			shopApply.get("email"))
			.set("status",			Shop.STATUS_ACTIVATED)						// 激活状态
			.set("acvtivate_time", 	now)										// 激活时间
			.set("open_channel", 	Shop.OPEN_CHANNEL_SELF)						// 自主申请
			.set("expire_date", 	DateUtil.addMonth(now, monthAmount))		// 店铺有效时间
			.set("update_time", 	now);
	}
	
	/**
	 * 根据店铺id获得店铺全部信息
	 * @author HuangSx
	 * @date : 2015年9月24日 下午5:38:07
	 * @param shopId
	 * @return
	 */
	public Shop getShopByShopId(String shopId){
		return Shop.dao.findFirst("select * from t_shop where id =?",shopId);
	}
	/**
	 * 根据店铺id获得店铺的中文详细地址地址
	 * @author HuangSx
	 * @date : 2015年9月25日 上午10:26:42
	 * @param shopId
	 * @return
	 */
	public String getShopAddressById(String shopId){
		StringBuffer address = new StringBuffer();
		address.append(Db.queryStr("select a.name from t_address a,t_shop b where b.province_code = a.`code` and b.id = ?",shopId));
		address.append(Db.queryStr("select a.name from t_address a,t_shop b where b.city_code = a.`code` and b.id = ?",shopId));
		Record record =  Db.findFirst("select a.name as name,b.address as address from t_address a,t_shop b where b.area_code = a.`code` and b.id = ?",shopId);
		if(null == record){
			return "";
		}
		address.append(record.getStr("name"));
		address.append(record.getStr("address"));
		return address.toString();
	}
	
	/**
	 * 店铺是否拥有新品上架商品
	 * @param shopId	店铺id
	 * @return
	 * boolean	返回true表示有
	 * @author wangy
	 * @date 2015年10月7日 上午10:54:15
	 */
	public boolean hasNewArrivals(String shopId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT count(p.id) count");
		sql.append(" FROM t_product p");
		sql.append(" WHERE");
		sql.append(" p.shop_id=?");		//商品所属店铺id
		sql.append(" AND");
		sql.append(" p.id in(");	//商品id
		//获取所有新品上架商品的id
		sql.append(" SELECT product_id FROM t_pro_signboard_map sm left join t_pro_signboard s on(sm.signboard_id=s.id) WHERE s.name='新品上架')");
		Long count = Db.findFirst(sql.toString(), shopId).getLong("count");  //店铺新品上架商品种类数量
		return count>0;
	}
	
	/**
	 * 根据店铺编号查找ID
	 * @param shopNo	店铺编号
	 * @return			店铺ID
	 * @author Sylveon
	 */
	public String getIdByShopNo(String shopNo) {
		String sql = "SELECT id FROM t_shop WHERE no = ?";
		return Db.queryStr(sql, shopNo);
	}
	
	/**
	 * 根据店铺编号查找店铺信息
	 * @param shopNo	店铺编号
	 * @return			店铺信息
	 * @author Sylveon
	 */
	public Shop getShopByShopNo(String shopNo) {
		String sql = "SELECT id, agent_id FROM t_shop WHERE no = ?";
		return Shop.dao.findFirst(sql, shopNo);
	}
	
	
	/**
	 * 根据店铺ID查找手机号码
	 * @return 手机号码
	 * @author Sylveon
	 */
	public String getMobileByShopId(String shopId) {
		String sql = "SELECT mobile FROM t_shop WHERE id = ?";
		return Db.queryStr(sql, shopId);
	}
	
	/**
	 * 删除店铺
	 * @param shopIds	店铺ID集
	 * @author Sylveon
	 */
	public void deleteShop(String shopIds) {
		String sql = "Delete FROM t_shop WHERE id IN (" + shopIds + ")";
		Db.update(sql);
	}
	
	/**
	 * 验证店铺编号是否存在
	 * @param shopId	店铺ID
	 * @param shopNo	店铺编号
	 * @return			true 存在； false 不存在
	 */
	public boolean existShopNo(String shopId, String shopNo) {
		String sql = "SELECT count(*) FROM t_shop WHERE id != ? AND no = ?";
		Long result = Db.queryLong(sql, shopId, shopNo);
		return result > 0 ? true : false;
	}
	
	/**
	 * 根据店铺ID查找店铺编号
	 * @param shopId	店铺ID
	 * @return			店铺编号
	 */
	public String getShopNoByShopId(String shopId) {
		String sql = "SELECT no FROM t_shop WHERE id = ?";
		String shopNo = Db.queryStr(sql, shopId);
		return shopNo;
	}
	
	/**
	 * 是否缴纳了保证金
	 * @return
	 * @author Jacob
	 * 2015年12月25日下午5:37:29
	 */
	public boolean isPayDeposit(String shopId){
		String sql = "SELECT is_deposit FROM t_shop WHERE id = ?";
		if(Db.queryInt(sql, shopId)==DEPOSIT_YES){
			return true;
		}
		return false;
	}
	
	/**
	 * 查找店铺信息 - 锁行
	 */
	public Shop findByIdAndLock(String shopId) {
		return Shop.dao.findFirst("SELECT * FROM t_shop WHERE id = ? FOR UPDATE", shopId);
	}
	
	/**
	 * 更具id查找身份证
	 */
	public String getIdcardById(String shopId) {
		return Db.queryStr("SELECT idcard FROM t_shop WHERE id = ?", shopId);
	}
	
	
	/**
	 * 现金提取--验证支付密码是否正确
	 * @param payPassword(MD5)
	 * @param shopId
	 * @return true:密码正确；false：密码错误
	 */
	public boolean checkUserPayPassword(String payPassword, String shopId) {
		String sql = "SELECT id FROM t_shop WHERE id = ? AND pay_password = ?";
		String result = Db.queryStr(sql, shopId, payPassword);
		return StringUtil.notNull(result) ? true : false;
	}
	
	/**
	 * 获取店主姓名
	 */
	public String getShopKeeper(String shopId) {
		String result = "";
		Record shop = Db.findFirst("SELECT no, shop_keeper FROM t_shop WHERE id = ?", shopId);
		if (null == shop) {
			return result;
		} else {
			result = shop.getStr("shop_keeper");
			if (StringUtil.isNull(result))
				result = shop.getStr("no");
		}				
		return result;
	}
	
	/**
	 * 获取店铺冻结装袋
	 */
	public Integer getShopFrozenStatus(String shopId) {
		Integer forbiddenStatus = Db.queryInt("SELECT forbidden_status FROM t_shop WHERE id = ?", shopId);
		if (null == forbiddenStatus)
			forbiddenStatus = 0;
		return forbiddenStatus;
	}
	
	/**
	 * 获取可加入店铺
	 */
	public Shop getBelognShop(String shopNo) {
		StringBuffer sql = new StringBuffer(" SELECT id, agent_id FROM t_shop ");
		sql.append(" WHERE no = ? ");
		sql.append(" AND status = ? ");
		sql.append(" AND forbidden_status = ? ");
		return Shop.dao.findFirst(sql.toString(), shopNo, Shop.STATUS_ACTIVATED, Shop.FORBIDDEN_STATUS_NORMAL);
	}
	
	/**
	 * 是否主动冻结店铺
	 */
	public boolean isIllegalFrozen(String shopId) {
		String sql = "SELECT COUNT(1) FROM t_shop WHERE id = ? AND forbidden_status = ?";
		long count = Db.queryLong(sql, shopId, Shop.FORBIDDEN_STATUS_STATUS_DISABLE_ILLEGAL);
		return count > 0 ? true : false;
	}
	
	/**
	 * 更定店铺 - 是否具有商品进驻云店+是否具有商品加入幸运一折购
	 */
	public void updateByO2oEfun(String shopId) {
		String sql = "SELECT o2o, efun FROM v_shop_solr_o2oAndEfun WHERE shop_id = ?";
		Record record = Db.findFirst(sql, shopId);
		if (StringUtil.notNull(record)) {
			Integer o2o = record.getNumber("o2o").intValue();
			o2o = o2o > 0 ? BaseConstants.YES : BaseConstants.NO;
			
			Integer efun = record.getNumber("efun").intValue();
			efun = efun > 0 ? BaseConstants.YES : BaseConstants.NO;
			
			new Shop()
				.set("id", 		shopId)
				.set("is_o2o",	o2o)
				.set("is_efun",	efun)
				.update();
		}
	}
	
	/**
	 * 检查身份证是否已用
	 */
	/*public boolean idcardUsed(String shopId, String idcard) {
		List<Object> paraList1 = new ArrayList<Object>();
		StringBuffer sql1 = new StringBuffer(" SELECT COUNT(1) FROM t_shop WHERE 1 = 1 ");
		sql1.append(" AND idcard = ? ");			paraList1.add(idcard);
		sql1.append(" AND status = ? ");			paraList1.add(Shop.STATUS_ACTIVATED);
		if (StringUtil.notNull(shopId)) {
			sql1.append(" AND id = ? ");			paraList1.add(shopId);
		}
		long count = Db.queryLong(sql1.toString(), paraList1.toArray());
		
		if (count > 0)
			return true;
		
		List<Object> paraList2 = new ArrayList<Object>();
		StringBuffer sql2 = new StringBuffer(" SELECT COUNT(1) FROM t_shop_recycle WHERE 1 = 1 ");
		sql2.append(" AND idcard = ? ");		paraList2.add(idcard);
		sql2.append(" AND recycle_type = ? ");	paraList2.add(ShopRecycle.STATTUS_TIMEOUT);
		sql2.append(" ");
		if (StringUtil.notNull(shopId)) {
			sql2.append(" AND id = ? ");		paraList2.add(shopId);
		}
		count = Db.queryLong(sql2.toString(), paraList2.toArray());
		
		return count > 0 ? true : false;
	}*/
	
	/**
	 * 更定店铺 - 是否具有商品进驻云店
	 */
	public void updateByO2o(String shopId) {
		StringBuffer sql1 = new StringBuffer(" SELECT ");
		sql1.append(" COUNT(1) ");
		sql1.append(" FROM t_o2o_sku_map a, t_product b ");
		sql1.append(" WHERE a.product_id = b.id ");
		sql1.append(" AND b.source IN (?, ?) ");
		sql1.append(" AND b.shop_id = ? ");
		Long o2o = Db.queryLong(sql1.toString(), Product.SOURCE_EXCLUSIVE, Product.SOURCE_SELF_EXCLUSIVE, shopId);
		Integer is_o2o = o2o > 0 ? BaseConstants.YES : BaseConstants.NO;
		new Shop().set("id", shopId).set("is_o2o", is_o2o).update();
	}
	
	/**
	 * 更定店铺 - 是否具有商品加入幸运一折购
	 */
	public void updateByEfun(String shopId) {
		StringBuffer sql1 = new StringBuffer(" SELECT ");
		sql1.append(" COUNT(1) ");
		sql1.append(" FROM t_pro_sku a, t_product b ");
		sql1.append(" WHERE a.product_id = b.id ");
		sql1.append(" AND b.source IN (?, ?) ");
		sql1.append(" AND a.is_efun <> 0 ");
		sql1.append(" AND b.shop_id = ? ");
		Long efun = Db.queryLong(sql1.toString(), Product.SOURCE_EXCLUSIVE, Product.SOURCE_SELF_EXCLUSIVE, shopId);
		Integer is_efun = efun > 0 ? BaseConstants.YES : BaseConstants.NO; 
		new Shop().set("id", shopId).set("is_efun", is_efun).update();
	}
	
	
	/**
	 * 店铺激活--返回当前店铺的激活状态
	 */
	public String getActivationStatus(String shopId) {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT a.audit_status ");
		sql.append(" FROM ");
		sql.append(" t_shop_certification_record a ");
		sql.append(" WHERE a.shop_id = ? ");
		sql.append(" and a.type = ? ");
		sql.append(" and a.pay_status = ? ");
		sql.append(" ORDER BY a.id DESC ");
		sql.append(" LIMIT 1 ");
	
		Record record = Db.findFirst(sql.toString(), shopId, ShopCertification.TYPE_ACTIVATION, Trade.STATUS_SUCCESS);
		return record == null ?"3" :record.getInt("audit_status").toString();
	}

	
	/**
	 * 店铺激活--查询当前店铺：转出通过的店铺转出记录(id)
	 * @param shopId 店铺激活-店铺id
	 * @return
	 */
	public String findShopTransferRecord(String shopId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT id ");
		sql.append(" FROM t_shop_transfer_record a");
		sql.append(" WHERE 1=1");
		sql.append(" AND a.shop_id = ? ");
		sql.append(" AND a.audit_status = 1 ");
		sql.append(" AND a.audit_time IS NOT NULL ");
		//sql.append(" AND a.acvtivate_time IS NULL ");
		sql.append(" ORDER BY a.audit_time ");
		sql.append(" LIMIT 1 ");
		
		Record record = Db.findFirst(sql.toString(), shopId);
		return record == null ? "" : record.getInt("id").toString();
	}
	
	/**
	 * 验证开店 预缴费类型
	 */
	public boolean checkPrepaidType(Integer months) {
		return SysParam.dao.existChildren("shop_open_prepaid", months + "");
	}
	
	/**
	 * 店铺是否激活
	 */
	public boolean isActivated(String shopId) {
		String sql = "SELECT COUNT(1) FROM t_shop WHERE id = ? AND status = ?";
		long count = Db.queryLong(sql, shopId, STATUS_ACTIVATED);
		return count > 0 ? true : false;
	}
	

	/**
	 * 电话是否被使用
	 */
	public boolean existMobile(String mobile) {
		long count = Db.queryLong("SELECT COUNT(*) FROM t_shop WHERE mobile = ?", mobile);
		return count > 0 ? true : false; 
	}
	
	/**
	 * 验证邮箱是否被使用
	 */
	public boolean existEmail(String shopId, String email) {
		String sql = "SELECT email FROM t_shop WHERE id != ? AND email = ?";
		String find = Db.queryStr(sql, shopId, email);
		return StringUtil.notNull(find) ? true : false; 
	}
	
	/**
	 * 获取店铺类型
	 * @param shopId
	 * @return
	 */
	public int getType(String shopId){
		return dao.findByIdLoadColumns(shopId, "type").getInt("type");
	}

	//幸运一折吃--商品列表
	public Page<Record> eatList(Page<Object> page, String shopId){
		List<Object> params = new ArrayList<>();
		StringBuffer select = new StringBuffer();
		StringBuffer where = new StringBuffer();
		select.append(" SELECT ");
		select.append("  	p.id proId,");
		select.append("  	c.is_efun isEfun,");
		select.append("  	ROUND(CEIL(p.eq_price * "+ EfunSku.EFUN_PRICE_RATE+"*100)/100,2) eqPrice ,");
		select.append("  	p.market_price marketPrice,");
		select.append("  	CONCAT(p.`name`,' ',IFNULL(c.property_decs,''))  proName,");
		select.append("  	p.product_img proImg ");
		where.append(" FROM t_product p  ");
		where.append("  	LEFT JOIN t_pro_sku c ON p.id = c.product_id");
		where.append("  	LEFT JOIN t_shop s ON p.shop_id = s.id");
		where.append(" WHERE ");
		where.append("   s.type = ? ");
		params.add(Shop.dao.TYPE_EAT);
		where.append("   AND s.id = ? ");
		params.add(shopId);
		where.append("   AND p.`status` = ? ");
		params.add(Product.STATUS_SHELVE);
		where.append("   AND p.audit_status = ? ");
		params.add(Product.AUDIT_STATUS_SUCCESS);
		where.append("   AND p.lock_status = ? ");
		params.add(Product.LOCK_STATUS_ENABLE);
		return Db.paginate(page.getPageNumber(),page.getPageSize(),select.toString(),where.toString(),params.toArray());
	}


	//幸运一折吃--商家信息
	public Record marchantInfo(String shopId){
		StringBuffer sbsql = new StringBuffer();
		sbsql.append(" SELECT ");
		sbsql.append("  	CONCAT(s.service_begin_time,'-',s.service_end_time) serviceTime ,");
		sbsql.append("  	s.logo shopLogo,");
		sbsql.append("  	s.`name` shopName,");
		sbsql.append("  	IFNULL(s.service_tel,' ') mobile,");
		sbsql.append("  	s.introduction,");
		sbsql.append("  	s.description ");
		sbsql.append(" FROM t_shop s ");
		sbsql.append(" WHERE ");
		sbsql.append(" 	s.type = ? ");
		sbsql.append(" 	AND s.id = ? ");

		return Db.findFirst(sbsql.toString(), Shop.dao.TYPE_EAT,shopId);
	}
	
	/**
	 * 获取店铺已审核通过商品数量
	 * @author chenhg
	 * @param shopId
	 * @return
	 * @date 2016年12月23日 下午3:56:54
	 */
	public int getShopProduct(String shopId){
		String sql = "SELECT count(p.id) num FROM t_product p WHERE p.shop_id = ? AND p.audit_status = ?";
		Record rec = Db.findFirst(sql, shopId, Product.AUDIT_STATUS_SUCCESS);
		if(rec == null){
			return 0;
		}else{
			return rec.getNumber("num").intValue();
		}
	}
}
