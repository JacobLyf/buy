package com.buy.model.efun;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.model.order.Cart;
import com.buy.model.product.Product;
import com.buy.model.product.ProductSku;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
/**
 * 
 * @author huangzq
 *
 */
public class EfunCart extends Model<EfunCart>{
	
	/**
	 * 购物车类型-E趣自营店铺(自营专卖商品)
	 * 对应商品表的字段source（来源）的值2
	 */
	public static final int TYPE_SELF_EXCLUSIVE = 1;
	/**
	 * 购物车类型-E趣自营供货商(自营公共商品)
	 * 对应商品表的字段source（来源）的值3
	 */
	public static final int TYPE_SELF_PUBLIC = 2;
	/**
	 * 购物车类型-第三方店铺(第三方店铺专卖商品)
	 * 对应商品表的字段source（来源）的值1
	 */
	public static final int TYPE_SHOP = 3;
	/**
	 * 购物车类型-第三方供货商（E趣代销跟厂家自发）
	 * 对应商品表的字段source（来源）的值4跟5
	 */
	public static final int TYPE_SUPPLIER = 4;
	/**购物车删除状态*/
	public static final int STATUS_DELETE = 0;
	/**购物车有效状态*/
	public static final int STATUS_VALID = 1;

	private static final long serialVersionUID = 1L;
	
	public static final EfunCart dao = new EfunCart();
	
	
	/**
	 * 判断购物车是否属于用户
	 * @author chenhg
	 * @param userId
	 * @param cartId
	 * @return
	 * @date 2016年12月21日 上午10:42:17
	 */
	public boolean isBelongToUser(String userId, Integer cartId){
		String sql = "SELECT id FROM t_efun_cart WHERE user_id = ? AND id = ?";
		Record record = Db.findFirst(sql, userId, cartId);
		return StringUtil.notNull(record);
	}
	
	
	/**
	 * 根据商品sku识别码判断是否已经添加到购物车
	 * @author chenhg
	 * @param skuCode
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:22:55
	 */
	public boolean isInEfunCartBySkuCode(String skuCode,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select id from t_efun_cart where sku_code = ? and user_id = ? and status = ? ");
		return Db.findFirst(sql.toString(), skuCode, userId, EfunCart.STATUS_VALID) == null ? false:true; 
	}
	
	/**
	 * 根据商品sku识别码跟会员ID获取有效的购物车项
	 * @author chenhg
	 * @param skuCode
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:23:50
	 */
	public EfunCart getEfunCartBySkuCode(String skuCode,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from t_efun_cart where sku_code = ? and user_id = ? and status = ? ");
		return dao.findFirst(sql.toString(), skuCode, userId, EfunCart.STATUS_VALID);
	}
	
	/**
	 * 根据购物车id，会员id 获得购物车
	 * @author chenhg
	 * @param cartId
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:23:59
	 */
	public EfunCart getEfunCartById(Integer cartId, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from t_efun_cart where id = ? and user_id = ? and status = ? ");
		return dao.findFirst(sql.toString(), cartId, userId, EfunCart.STATUS_VALID);
	}
	
	
	/**
	 * 获取当前会员有效购物车的数量
	 * @author chenhg
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:24:08
	 */
	public Integer getEfunCartCount(String userId){
		String sql = " select count(id) cartCount  from t_efun_cart where user_id = ? and status = ? ";
		return Db.queryLong(sql, userId, EfunCart.STATUS_VALID).intValue();
	}
	
	/**
	 * 更新购物车数量缓存
	 * @author chenhg
	 * @param userId
	 * @date 2016年12月21日 上午10:24:14
	 */
	public void updateEfunCartCountRedis(String userId){
		/***********将购物车数量保存到redis*****************/
		// 获取名称为cartCount的Redis Cache对象.
		Cache cartCountCache = Redis.use(BaseConstants.Redis.CACHE_EFUN_CART_COUNT);
		cartCountCache.set("cartCount" + userId, dao.getEfunCartCount(userId));
		/***********将购物车数量保存到redis*****************/
	}
	
	/**
	 * 获取商品总额
	 * @author chenhg
	 * @param cartIds
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:24:22
	 */
	public BigDecimal getProductTotal(Integer[] cartIds, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	SUM(ps.eq_price * c.count) productTotal ");//实时商品sku价格*数量
		sql.append(" FROM ");
		sql.append(" 	t_efun_cart c,t_pro_sku ps ");
		sql.append(" WHERE ");
		sql.append(" 	c.sku_code = ps.`code` ");
		sql.append(" AND c.user_id = ? ");
		//购物车ID数组
		if(StringUtil.notNull(cartIds)&&cartIds.length>0){
			sql.append(" AND c.id in ("+StringUtil.arrayToString(",", cartIds)+") ");
		}
		return Db.queryBigDecimal(sql.toString(), userId);
	}
	
	/**
	 * 获取当前用户缓存中的购物车数量
	 * @author chenhg
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:24:34
	 */
	public int getCountByCache(String userId){
		int count = 0;
		if(StringUtil.notNull(userId)){
			Cache cartCountCache = Redis.use(BaseConstants.Redis.CACHE_EFUN_CART_COUNT);
			if(cartCountCache!=null){
				if(cartCountCache.get("cartCount"+userId) != null){
					count = cartCountCache.get("cartCount"+userId);
				}
			}
		}
		return count;
	}
	
	
	/**
	 * 获取商家列表
	 * @author chenhg
	 * @param userId
	 * @return
	 * @date 2016年12月21日 下午3:58:06
	 */
	public List<Record> findCartMerchant(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT  ");
		sql.append("  ec.merchant_id merchantId, ");     //商家id
		sql.append("  ec.merchant_name merchantName, ");  //商家名称
		sql.append("  ec.type cartType ");               //购物车类型
		sql.append(" FROM t_efun_cart ec  ");
		sql.append(" WHERE ec.`status` = ?  ");
		sql.append(" AND ec.user_id = ? ");
		sql.append(" GROUP BY ec.merchant_id,ec.merchant_name ");
		sql.append(" ORDER BY MAX(ec.create_time) DESC ");
		
		return Db.find(sql.toString(), EfunCart.STATUS_VALID, userId);
	}
	
	/**
	 * 获取某个商家的购车商品列表(pc)
	 * @author chenhg
	 * @param userId
	 * @param merchantId
	 * @return
	 * @date 2016年12月30日 下午4:07:01
	 */
	public List<Record> findCartProductListForPc(String userId, String merchantId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("   ec.id cartId, ");                               //购物车id
		sql.append("   ec.count cartCount, ");                         //购物车数量
		sql.append("   ec.price cartPrice, ");                         //加入购物车时的价格
		sql.append("   ps.eq_price skuPrice, ");                       //商品当前价格
		sql.append("   FORMAT(CEIL(IFNULL(ps.eq_price,ec.price)*10)/100,2) eqPrice, ");              //商品幸运一折购价格
		sql.append("   FORMAT(CEIL(IFNULL(ps.eq_price,ec.price)*10)/100*ec.count,2) eqPriceTotal, ");//商品幸运一折购价格*数量
		sql.append("   ec.sku_code skuCode, ");                        //skuCode
		sql.append("   ec.order_shop_id orderShopId, ");               //下单所在店铺id
		sql.append("   p.id productId, ");                             //商品id
		sql.append("   p.`name` productName, ");                       //商品名称
		sql.append("   ec.properties, ");                              //商品属性
		sql.append("   p.`status` productStatus, ");                   //商品上下架状态
		sql.append("   p.lock_status productLockStatus, ");            //商品锁定状态
		sql.append("   IFNULL(ps.sku_img,p.product_img) skuImg, ");    //商品图片
		sql.append("   IFNULL(ifnull(ps.real_count, 0) + ps.virtual_count - ps.lock_count - ps.store_lock_count,0) AS skuCount, ");//sku库存
		sql.append("   s.forbidden_status shopStatus  ");              //店铺状态 
		sql.append(" FROM t_efun_cart ec   ");
		sql.append("  LEFT JOIN t_product p ON ec.product_id = p.id  ");
		sql.append("  LEFT JOIN t_pro_sku ps ON ec.sku_code = ps.`code`  ");
		sql.append("  LEFT JOIN t_shop s ON ec.merchant_id = s.id  ");
		sql.append(" WHERE ec.`status` = ? ");
		sql.append(" AND ec.user_id = ? ");
		sql.append(" AND ec.merchant_id = ? ");
		
		return Db.find(sql.toString(), EfunCart.STATUS_VALID, userId, merchantId);
	}
	
	/**
	 * 获取某个商家的购车商品列表(wap/app)
	 * @author chenhg
	 * @param userId
	 * @param merchantId
	 * @return
	 * @date 2016年12月21日 下午4:00:20
	 */
	public List<Record> findCartProductListForApp(String userId, String merchantId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append("   ec.id cartId, ");                               //购物车id
		sql.append("   ec.count cartCount, ");                         //购物车数量
//		sql.append("   CEIL(ec.price*10)/100 cartPrice, ");           //加入购物车时的价格
//		sql.append("   ec.merchant_name merchantName, ");              //商家名称
//		sql.append("   ec.merchant_id merchantId, ");                  //商家id
		sql.append("   ec.sku_code skuCode, ");                        //skuCode
		sql.append("   ec.properties, ");                              //商品属性
		sql.append("   ec.order_shop_id orderShopId, ");               //下单所在店铺id
//		sql.append("   ec.user_id userId, ");                          //会员id
		sql.append("   p.id productId, ");                             //商品id
//		sql.append("   p.source , ");                                  //商品来源
		sql.append("   p.`name` productName, ");                       //商品名称
		sql.append("   p.`status` productStatus, ");                   //商品上下架状态
		sql.append("   p.lock_status productLockStatus, ");            //商品锁定状态
		sql.append("   IFNULL(ps.sku_img,p.product_img) skuImg, ");    //商品图片
		sql.append("   IFNULL(ps.eq_price,ec.price) skuPrice, ");      //商品当前价格
		sql.append("   CEIL(IFNULL(ps.eq_price,ec.price)*10)/100 eqPrice, ");//商品幸运一折购价格
		sql.append("   ifnull(ps.real_count, 0) + ps.virtual_count - ps.lock_count - ps.store_lock_count AS skuCount, ");//sku库存
//		sql.append("   ps.market_price marketPrice, ");                //市场价
//		sql.append("   ps.supplier_price supplierPrice, ");            //供货价格
		sql.append("   s.forbidden_status shopStatus  ");              //店铺状态 
		sql.append(" FROM t_efun_cart ec   ");
		sql.append("  LEFT JOIN t_product p ON ec.product_id = p.id  ");
		sql.append("  LEFT JOIN t_pro_sku ps ON ec.sku_code = ps.`code`  ");
		sql.append("  LEFT JOIN t_shop s ON ec.merchant_id = s.id  ");
		sql.append(" WHERE ec.`status` = ? ");
		sql.append(" AND ec.user_id = ? ");
		sql.append(" AND ec.merchant_id = ? ");
		
		return Db.find(sql.toString(), EfunCart.STATUS_VALID, userId, merchantId);
	}
	
	/**
	 * 获取购物车(提交订单时需要)
	 * @param cartIds
	 * @param userId
	 * @return
	 * @author huangzq
	 * 2016年12月15日 下午3:54:56
	 *
	 */
	public List<Record> getCartForOrder(Integer[] cartIds,String userId){
	
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT");
		sql.append("	c.sku_code,");
		sql.append("	c.properties,");
		sql.append("	c.count,");
		sql.append("	c.order_shop_id,");
		sql.append("	p.`name` as proName,");
		sql.append("	p.`id` as productId,");
		sql.append("	p.`status` as proStatus,");
		sql.append("	p.lock_status as proLockStatus,");
		sql.append("	p.source as proSource ,");
		sql.append("	shop.id as shopId ,");
		sql.append("	shop.no as shopNo ,");
		sql.append("	shop.name as shopName ,");
		sql.append("	shop.status as shopStatus ,");
		sql.append("	shop.forbidden_status as shopForbiddenStatus ,");
		sql.append("	supplier.id as supplierId ,");
		sql.append("	supplier.no as supplierNo ,");
		sql.append("	supplier.name as supplierName ,");
		sql.append("	sku.eq_price/10 as efunPrice,");
		sql.append("	sku.sku_img as productImg,");
		sql.append("	sku.eq_price as eqPrice,");
		sql.append("	sku.eq_price/10 as efunPrice,");
		sql.append("	sku.supplier_price as supplierPrice,");
		sql.append("	sku.virtual_count+sku.real_count-sku.lock_count-sku.store_lock_count storeCount");
		sql.append(" FROM");
		sql.append("	t_efun_cart c");
		sql.append(" LEFT JOIN t_product p ON c.product_id = p.id");
		sql.append(" LEFT JOIN t_pro_sku sku ON c.sku_code = sku.code ");
		sql.append(" LEFT JOIN t_shop shop ON shop.id = p.shop_id ");
		sql.append(" LEFT JOIN t_supplier supplier ON supplier.id = p.supplier_id ");
		sql.append(" WHERE");
		sql.append("	c.`status` = ?");
		sql.append(" AND c.id IN ("+StringUtil.arrayToString(",", cartIds)+")");
		sql.append(" AND c.user_id = ?");
		sql.append(" order by sku.eq_price asc");
		return Db.find(sql.toString(),EfunCart.STATUS_VALID,userId);
		
	}
	
	/**
	 * 删除购物车
	 * @param ids
	 * @author huangzq
	 * 2016年12月21日 下午4:29:08
	 * efunCartService 有批量删除的方法batchDelete
	 
	public void deleteCartById(Integer... ids){
		String sql = "update t_efun_cart  set status = ? where id in("+StringUtil.arrayToString(",", ids)+")";
		Db.update(sql,EfunCart.STATUS_DELETE);
		
	}*/
	
	/**
	 * 获取购物车的商品id
	 * @author chenhg
	 * @param cartIds
	 * @param userId
	 * @return
	 * @date 2016年12月29日 下午6:02:18
	 */
	public List<Integer> findProductIdListByCartIds(Integer[] cartIds, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	distinct c.product_id ");
		sql.append(" FROM ");
		sql.append(" 	t_efun_cart c");
		sql.append(" WHERE ");
		sql.append(" c.user_id = ?");
		sql.append(" AND ");
		sql.append("    c.id in ( "+StringUtil.arrayToString(",", cartIds)+" ) ");
		return Db.query(sql.toString(), userId);
	}
	
}
