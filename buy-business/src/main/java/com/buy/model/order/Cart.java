package com.buy.model.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buy.common.BaseConstants;
import com.buy.common.JsonMessage;
import com.buy.model.efun.EfunProduct;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.product.Product;
import com.buy.model.product.ProductFavs;
import com.buy.model.product.ProductSku;
import com.buy.model.store.StoreSkuMap;
import com.buy.model.user.RecAddress;
import com.buy.string.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
/**
 * 购物车
 * @author huangzq
 *
 */
public class Cart extends Model<Cart>{
	
	/** 库存不足状态值 */
	public static final String LOW_STOCKS = "1";
	/**
	 * 购物车类型-E趣自营店铺(自营专卖商品)
	 * 对应商品表的字段source（来源）的值2
	 */
	public static final Integer TYPE_SELF_EXCLUSIVE = 1;
	/**
	 * 购物车类型-E趣自营供货商(自营公共商品)
	 * 对应商品表的字段source（来源）的值3
	 */
	public static final Integer TYPE_SELF_PUBLIC = 2;
	/**
	 * 购物车类型-第三方店铺(第三方店铺专卖商品)
	 * 对应商品表的字段source（来源）的值1
	 */
	public static final Integer TYPE_SHOP = 3;
	/**
	 * 购物车类型-第三方供货商（E趣代销跟厂家自发）
	 * 对应商品表的字段source（来源）的值4跟5
	 */
	public static final Integer TYPE_SUPPLIER = 4;
	/**购物车删除状态*/
	public static final Integer STATUS_DELETE = 0;
	/**购物车有效状态*/
	public static final Integer STATUS_VALID = 1;

	private static final long serialVersionUID = 1L;
	
	public static final Cart dao = new Cart();
	
	/**
	 * 根据商品sku识别码判断是否已经添加到购物车了
	 * @param skuCode 商品sku识别码
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月2日下午4:31:58
	 */
	public boolean isInCart(String skuCode,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(id) from t_cart where sku_code = ? and user_id = ? and status = ? ");
		return Db.queryLong(sql.toString(),new Object[]{skuCode,userId,STATUS_VALID})>0?true:false;
	}
	
	/**
	 * 根据商品sku识别码跟会员ID获取有效的购物车项
	 * @param skuCode 商品sku识别码
	 * @param userId 会员ID
	 * @return
	 * @author Jacob
	 * 2015年12月2日下午4:35:29
	 */
	public Cart getCart(String skuCode,String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from t_cart where sku_code = ? and user_id = ? and status = ? ");
		return dao.findFirst(sql.toString(),skuCode,userId,STATUS_VALID);
	}
	
	/**
	 * 根据购物车id，会员id 获得购物车
	 * @param cartId
	 * @param userId
	 * @return
	 */
	public Cart getCartById(Integer cartId, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" select * from t_cart where id = ? and user_id = ? and status = ? ");
		return dao.findFirst(sql.toString(),cartId,userId,STATUS_VALID);
	}
	
	/**
	 * 根据条件获取购物车明细信息列表
	 * @param userId 会员ID
	 * @param targetId 商家ID
	 * @param cartType 购物车类型
	 * @param cartIds[] 购物车ID数组
	 * @param suppllierProductType 代理商商品代售或自配（对应商品来源的4跟5）
	 * @return
	 * @author Jacob
	 * 2015年12月2日下午3:37:32
	 */
	public List<Record> findCartList(String userId,String targetId,Integer cartType,Integer cartIds[],Integer suppllierProductType){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	cd.cart_id, ");
		sql.append(" 	cd.cart_price, ");//加入购车时的商品sku价格
		sql.append(" 	cd.count, ");//数量
		sql.append(" 	cd.product_name, ");
		sql.append(" 	IFNULL(cd.sku_img,cd.product_img) product_img, ");//商品图片
		sql.append(" 	ABS(cd.sku_price) sku_price, ");//实时商品sku价格
		sql.append(" 	cd.product_id, ");
		sql.append(" 	cd.product_no, ");
		sql.append(" 	cd.product_status, ");//商品上下架状态
		sql.append(" 	cd.product_lock_status, ");//商品锁定状态
		sql.append(" 	cd.properties, ");//加入购车时的商品sku属性（包含属性名称跟属性值，多个属性以";"隔开）
		sql.append(" 	cd.store_count, ");//商品sku库存(根据商品来源字段souce进行判断取出真实库存或虚拟库存)
		sql.append(" 	cd.sku_code, ");//商品sku识别码
		sql.append(" 	cd.shop_id, ");
		sql.append(" 	cd.supplier_id, ");
		sql.append(" 	cd.source, ");
		sql.append(" 	cd.create_time, ");
		sql.append(" 	cd.user_id, ");
		sql.append(" 	cd.order_shop_id, ");//下单店铺
		sql.append(" 	ABS(cd.market_price) market_price, ");//商品SKU市场价
		sql.append(" 	ABS(cd.supplier_price) supplier_price, ");//商品SKU供货价（结算价）
		sql.append(" 	cd.forbidden_status ");//商品所属店铺冻结状态
		sql.append(" FROM ");
		sql.append(" 	v_web_cart_detail cd ");
		sql.append(" WHERE ");
		sql.append(" 	1 = 1 ");
		sql.append(" AND ");
		sql.append(" 	cd.status = ? ");
		params.add(Cart.STATUS_VALID);
		sql.append(" AND ");
		sql.append(" 	cd.user_id = ? ");
		params.add(userId);
		//购物车类型
		if(cartType!=null){
			//E趣自营店铺(自营专卖商品)
			if(cartType==Cart.TYPE_SELF_EXCLUSIVE){
				sql.append(" AND cd.source = ? ");
				params.add(Product.SOURCE_SELF_EXCLUSIVE);//自营专卖商品
				sql.append(" AND cd.shop_id = ? ");
				params.add(targetId);
			}
			//E趣自营供货商(自营公共商品)
			if(cartType==Cart.TYPE_SELF_PUBLIC){
				sql.append(" AND cd.source = ? ");
				params.add(Product.SOURCE_SELF_PUBLIC);//自营公共商品
				sql.append(" AND cd.supplier_id = ? ");
				params.add(targetId);
			}
			//第三方店铺(第三方店铺专卖商品)
			if(cartType==Cart.TYPE_SHOP){
				sql.append(" AND cd.source = ? ");
				params.add(Product.SOURCE_EXCLUSIVE);//专卖商品
				sql.append(" AND cd.shop_id = ? ");
				params.add(targetId);
			}
			//第三方供货商（E趣代销跟厂家自发）
			if(cartType==Cart.TYPE_SUPPLIER){
				sql.append(" AND (cd.source = ? || cd.source = ?) ");
				params.add(Product.SOURCE_FACTORY);//e趣代售商品
				params.add(Product.SOURCE_FACTORY_SEND);//厂家自发商品
				sql.append(" AND cd.supplier_id = ? ");
				params.add(targetId);
			}
		}
		//代理商商品代售或自配（对应商品来源的4跟5）
		if(suppllierProductType!=null){
			if(suppllierProductType==Product.SOURCE_FACTORY){
				sql.append(" AND cd.source = ? ");
				params.add(Product.SOURCE_FACTORY);//e趣代售商品
			}
			if(suppllierProductType==Product.SOURCE_FACTORY_SEND){
				sql.append(" AND cd.source = ? ");
				params.add(Product.SOURCE_FACTORY_SEND);//厂家自发商品
			}
		}
		//购物车ID数组
		if(StringUtil.notNull(cartIds)&&cartIds.length>0){
			sql.append(" AND cd.cart_id in ("+StringUtil.arrayToString(",", cartIds)+") ");
		}
		sql.append(" ORDER BY ");
		sql.append(" 	cd.update_time DESC ");
		List<Record> list = Db.find(sql.toString(),params.toArray());
		//增加是否一折购商品标识
		for(Record r : list){
			if(EfunProduct.dao.isEfunProduct(r.getInt("product_id"))){
				r.set("isEfun",1);
			}else{
				r.set("isEfun",0);
			}
		}
		return list;
	}
	
	public List<Record> findCartList4SubmitOrder(String userId,String targetId,Integer cartType,Integer cartIds[],Integer suppllierProductType,
			Integer addressId,List<Record> pickUpInfoList,String[] deliveryTypes){
		//获取跟当前会员和商家target_id相关的购车明细信息列表
		List<Record> cartDetailList = findCartList(userId,targetId,cartType,cartIds,suppllierProductType);
		//处理配送方式数组(cartId-Order.DELIVERY_TYPE_EXPRESS或者cartId-Order.DELIVERY_TYPE_SELF)
		Map<Integer,Integer> deliveryTypeMap = stringArrayToMap(deliveryTypes);
		for(Record cartDetail : cartDetailList){
			//获取相应的自提信息
			Record pickUpInfo = this.getPickUpInfo(pickUpInfoList, cartDetail.getInt("cart_id"));
			cartDetail.set("o2o_shop_id", pickUpInfo!=null?pickUpInfo.getStr("o2oShopId"):"");//o2o门店ID
			cartDetail.set("o2o_shop_no", pickUpInfo!=null?pickUpInfo.getStr("o2oShopNo"):"");//o2o门店编号
			cartDetail.set("o2o_shop_name",  pickUpInfo!=null?pickUpInfo.getStr("o2oShopName"):"");//o2o门店名称
			cartDetail.set("o2o_shop_address", pickUpInfo!=null?pickUpInfo.getStr("o2oShopAddress"):"");//o2o门店地址
			cartDetail.set("pick_up_name", pickUpInfo!=null?pickUpInfo.getStr("pickUpName"):"");//收货人姓名
			cartDetail.set("pick_up_mobile", pickUpInfo!=null?pickUpInfo.getStr("pickUpMobile"):"");//收货人手机号
			//运费所需参数map
			Map<String, Integer> skuCodeCountMap = new HashMap<String, Integer>();
			skuCodeCountMap.put(cartDetail.getStr("sku_code"), cartDetail.getInt("count"));
			//初始化该商品SKU运费
			BigDecimal freight = new BigDecimal(0);
			//判断该商品SKU在选中的收货地址的所在城市是否满足购买量的O2O门店可自提
			boolean isPickUp = StoreSkuMap.dao.isPickUp(cartDetail.getInt("count"), cartDetail.getStr("sku_code"));
			if(addressId!=null){
				RecAddress recAddress = RecAddress.dao.findByIdLoadColumns(addressId, "province_code,city_code,area_code");
				if(deliveryTypeMap.size()==0||deliveryTypeMap.get(cartDetail.getInt("cart_id"))==1){
					//计算该商品SKU运费
					freight = FreightTemplate.dao.calculate(skuCodeCountMap, recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
				}
			}
			cartDetail.set("delivery_type", deliveryTypeMap.size()==0?1:deliveryTypeMap.get(cartDetail.getInt("cart_id")));//保存快递方式
			cartDetail.set("is_pick_up", isPickUp);//保存是否可自提
			cartDetail.set("freight", freight.setScale(2,BigDecimal.ROUND_UP));//保存运费
		}
		return cartDetailList;
	}
	
	/**
	 * 将数组转换成MAP
	 * @param stringArray  格式（key-value）
	 * @return
	 * @author Jacob
	 * 2015年12月15日下午3:31:06
	 */
	public Map<Integer,Integer> stringArrayToMap(String[] stringArray){
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		if(StringUtil.notNull(stringArray)){
			for(String s : stringArray){
				map.put(Integer.parseInt(s.split("-")[0]), Integer.parseInt(s.split("-")[1]));
			}
		}
		
		return map;
	}
	
	/**
	 * 将数组转换成MAP
	 * @param stringArray  格式（key-value）
	 * @return
	 * @author Jacob
	 * 2015年12月15日下午3:31:06
	 */
	public Map<String,String> stringArrayToMap2(String[] stringArray){
		Map<String,String> map = new HashMap<String,String>();
		
		if(StringUtil.notNull(stringArray)){
			for(String s : stringArray){
				map.put(s.split("-")[0], s.split("-").length==2?s.split("-")[1]:"");
			}
		}
		
		return map;
	}
	
	/**
	 * 根据购物车ID从自提信息记录列表找出相应的自提信息
	 * @param recordList 自提信息记录列表
	 * @param cartId 购物车ID
	 * @return
	 * @author Jacob
	 * 2016年3月4日上午10:06:26
	 */
	public Record getPickUpInfo(List<Record> recordList,Integer cartId){
		Record pickUpInfo = new Record(); 
		if(StringUtil.notNull(recordList)){
			for(Record record : recordList){
				if(record.get("cartId").toString().equals(cartId.toString())){
					pickUpInfo = record;
				}
			}
		}
		return pickUpInfo;
	}
	
	/**
	 * 根据选中的购物车项跟会员ID获取对应的商家列表
	 * @param userId  会员ID
	 * @param cartIds  购物车Id数组
	 * @return
	 * @author Jacob
	 * 2015年12月10日上午11:02:54
	 */
	public List<Record> findCartMerchant4OrderConfirmation(String userId,Integer[] cartIds){
		List<Object> params = new ArrayList<Object>();
		String cartIdsString = StringUtil.arrayToString(",", cartIds);
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	m.target_id, ");
		sql.append(" 	m.user_id, ");
		sql.append(" 	m.target_name, ");
		sql.append(" 	m.target_no, ");
		sql.append(" 	m.cart_type, ");
		sql.append(" 	m.order_time, ");
		sql.append(" 	m.product_id ");
		sql.append(" FROM ");
		sql.append(" 	( ");
		sql.append(" 		( ");
		sql.append(" 			SELECT ");
		sql.append(" 				p.shop_id target_id, ");
		sql.append(" 				s.`name` target_name, ");
		sql.append(" 				s.`no` target_no, ");
		sql.append(" 				c.type cart_type, ");
		sql.append(" 				MAX(c.create_time) order_time, ");
		sql.append(" 				c.user_id, ");
		sql.append(" 				CAST(SUBSTRING_INDEX(group_concat(c.product_id order by c.update_time desc),',',1) AS signed) product_id ");
		sql.append(" 			FROM ");
		sql.append(" 				t_cart c ");
		sql.append(" 			LEFT JOIN t_product p ON c.product_id = p.id ");
		sql.append(" 			LEFT JOIN t_shop s ON s.id = p.shop_id ");
		sql.append(" 			WHERE ");
		sql.append(" 				1 = 1 ");
		sql.append(" 			AND p.source = 1 ");
		sql.append(" 			AND c.`status` = 1 ");
		sql.append(" 			AND c.id IN ("+cartIdsString+") ");
		sql.append(" 			AND c.user_id = ? ");
		params.add(userId);
		sql.append(" 			GROUP BY ");
		sql.append(" 					target_id, ");
		sql.append(" 					target_name, ");
		sql.append(" 					cart_type, ");
		sql.append(" 					c.user_id ");
		sql.append(" 		) ");
		sql.append(" 		UNION ");
		sql.append(" 			( ");
		sql.append(" 				SELECT ");
		sql.append(" 				    p.shop_id target_id, ");
		sql.append(" 				    s.`name` target_name, ");
		sql.append(" 				    s.`no` target_no, ");
		sql.append(" 					c.type cart_type, ");
		sql.append(" 					MAX(c.create_time) order_time, ");
		sql.append(" 					c.user_id, ");
		sql.append(" 					CAST(SUBSTRING_INDEX(group_concat(c.product_id order by c.update_time desc),',',1) AS signed) product_id ");
		sql.append(" 				FROM ");
		sql.append(" 					t_cart c ");
		sql.append(" 				LEFT JOIN t_product p ON c.product_id = p.id ");
		sql.append(" 			    LEFT JOIN t_shop s ON s.id = p.shop_id ");
		sql.append(" 				WHERE ");
		sql.append(" 					1 = 1 ");
		sql.append(" 				AND p.source = 2 ");
		sql.append(" 				AND c.`status` = 1 ");
		sql.append(" 				AND c.id IN ("+cartIdsString+") ");
		sql.append(" 				AND c.user_id = ? ");
		params.add(userId);
		sql.append(" 				GROUP BY ");
		sql.append(" 					target_id, ");
		sql.append(" 					target_name, ");
		sql.append(" 					cart_type, ");
		sql.append(" 					c.user_id ");
		sql.append(" 			) ");
		sql.append(" 		UNION ");
		sql.append(" 			( ");
		sql.append(" 				SELECT ");
		sql.append(" 					p.supplier_id target_id, ");
		sql.append(" 					s.`name` target_name, ");
		sql.append(" 					s.`no` target_no, ");
		sql.append(" 					c.type cart_type, ");
		sql.append(" 					MAX(c.create_time) order_time, ");
		sql.append(" 					c.user_id, ");
		sql.append(" 					CAST(SUBSTRING_INDEX(group_concat(c.product_id order by c.update_time desc),',',1) AS signed) product_id ");
		sql.append(" 				FROM ");
		sql.append(" 					t_cart c ");
		sql.append(" 				LEFT JOIN t_product p ON c.product_id = p.id ");
		sql.append(" 				LEFT JOIN t_supplier s ON s.id = p.supplier_id ");
		sql.append(" 				WHERE ");
		sql.append(" 					1 = 1 ");
		sql.append(" 				AND p.source = 3 ");
		sql.append(" 				AND c.`status` = 1 ");
		sql.append(" 				AND c.id IN ("+cartIdsString+") ");
		sql.append(" 				AND c.user_id = ? ");
		params.add(userId);
		sql.append(" 				GROUP BY ");
		sql.append(" 					target_id, ");
		sql.append(" 					target_name, ");
		sql.append(" 					cart_type, ");
		sql.append(" 					c.user_id ");
		sql.append(" 			) ");
		sql.append(" 		UNION ");
		sql.append(" 			( ");
		sql.append(" 				SELECT ");
		sql.append(" 					p.supplier_id target_id, ");
		sql.append(" 					s.`name` target_name, ");
		sql.append(" 					s.`no` target_no, ");
		sql.append(" 					c.type cart_type, ");
		sql.append(" 					MAX(c.create_time) order_time, ");
		sql.append(" 					c.user_id, ");
		sql.append(" 					CAST(SUBSTRING_INDEX(group_concat(c.product_id order by c.update_time desc),',',1) AS signed) product_id ");
		sql.append(" 				FROM ");
		sql.append(" 					t_cart c ");
		sql.append(" 				LEFT JOIN t_product p ON c.product_id = p.id ");
		sql.append(" 				LEFT JOIN t_supplier s ON s.id = p.supplier_id ");
		sql.append(" 				WHERE ");
		sql.append(" 					1 = 1 ");
		sql.append(" 				AND p.source IN (4, 5) ");
		sql.append(" 				AND c.`status` = 1 ");
		sql.append(" 				AND c.id IN ("+cartIdsString+") ");
		sql.append(" 				AND c.user_id = ? ");
		params.add(userId);
		sql.append(" 				GROUP BY ");
		sql.append(" 					target_id, ");
		sql.append(" 					target_name, ");
		sql.append(" 					cart_type, ");
		sql.append(" 					c.user_id ");
		sql.append(" 			) ");
		sql.append(" 	) m ");
		sql.append(" ORDER BY ");
		sql.append(" 	m.order_time DESC ");
		List<Record> merchantList = Db.find(sql.toString(), params.toArray());
		return merchantList;
	}
	
	/**
	 * 获取当前会员有效购物车的数量
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2015年12月14日下午8:09:51
	 */
	public Integer getCartCount(String userId){
		String sql = " select count(cart_id) cartCount from v_web_cart_detail where user_id = ? and status = ? ";
		return Db.queryLong(sql,userId,Cart.STATUS_VALID).intValue();
	}
	
	/**
	 * 更新购物车数量缓存
	 * @param userId
	 * @author Jacob
	 * 2016年4月5日下午7:49:55
	 */
	public void updateCartCountRedis(String userId){
		/***********将购物车数量保存到redis*****************/
		// 获取名称为cartCount的Redis Cache对象.
		Cache cartCountCache = Redis.use(BaseConstants.Redis.CACHE_CART_COUNT);
		cartCountCache.set("cartCount"+userId, dao.getCartCount(userId));
		
		/***********将购物车数量保存到redis*****************/
	}
	
	/**
	 * 判断购物车订单提交时商品库存
	 * @param cartIds
	 * @param deliveryTypes
	 * @param pickUpInfoList
	 * @return
	 */
	public JsonMessage checkOrderSubmitCount(Integer[] cartIds, String[] deliveryTypes,List<Record> pickUpInfoList, String userId){

		JsonMessage jsonMessage = new JsonMessage();
		
		//处理配送方式数组(cartId-Order.DELIVERY_TYPE_EXPRESS或者cartId-Order.DELIVERY_TYPE_SELF)
		Map<Integer,Integer> deliveryTypeMap = stringArrayToMap(deliveryTypes);
		List<Record> skuInventory = new ArrayList<Record>();
		boolean flag = false;
		for(Integer id : cartIds){
			Cart cart = Cart.dao.getCart(id, userId);
			String skuCode = cart.getStr("sku_code");
			Integer proNum = cart.getInt("count");
			
			int deliveryType = deliveryTypeMap!=null&&deliveryTypeMap.size()>0?deliveryTypeMap.get(id):1;
			//判断是否为自提
			if(deliveryType == Order.DELIVERY_TYPE_SELF){//自提
				//获取相应的自提信息
				String o2oShopNo = this.getPickUpInfo(pickUpInfoList, id).getStr("o2oShopNo");
				
				if(!StoreSkuMap.dao.enoughStoreCount(o2oShopNo, skuCode, proNum)){
					flag = true;
					skuInventory.add(ProductSku.dao.getSkuInventoryMessageForPickUp(skuCode, o2oShopNo));
				}
			}else{//快递
				if(!ProductSku.dao.enoughCount(skuCode, proNum)){
					flag = true;
					skuInventory.add(ProductSku.dao.getSkuInventoryMessage(skuCode));
				}
			}
			
		}
		if(flag){
			jsonMessage.setData(skuInventory);
			jsonMessage.setStatusAndMsg("10", "库存不足");
		}
		return jsonMessage;
	}
	
	/**
	 * 结算（购物车结算按钮）
	 * @return
	 * @author Jacob
	 * 2015年12月8日下午8:43:07
	 */
	public JsonMessage settlement(Integer[] cartIds, String userId){
		JsonMessage jsonMessage = new JsonMessage();
		String skuCodesStr = "";
		String skuCodesStr4Lock = "";
		String skuCodesStr4Store = "";
		for(Integer id : cartIds){
			Cart cart = Cart.dao.getCart(id, userId);
			//商品SKU识别码
			String skuCode = cart.getStr("sku_code");
			//商品ID
			Integer productId = cart.getInt("product_id");
			Product product = Product.dao.findByIdLoadColumns(productId, "status,lock_status");
			if(product.getInt("status")!=Product.STATUS_SHELVE){
				skuCodesStr += skuCode+",";
			}
			if(product.getInt("lock_status")!=Product.LOCK_STATUS_ENABLE){
				skuCodesStr4Lock += skuCode+",";
			}
			//分别判断购买数量是否大于库存
			if(cart.getInt("count")>ProductSku.dao.getCount(cart.getStr("sku_code"))){
				skuCodesStr4Store += cart.getStr("sku_code")+",";
			}
		}
		if(StringUtil.notBlank(skuCodesStr4Store)){
			skuCodesStr4Store = skuCodesStr4Store.substring(0, skuCodesStr4Store.length()-1);
			jsonMessage.setData(skuCodesStr4Store);
			jsonMessage.setStatusAndMsg(Cart.LOW_STOCKS, "部分商品库存不足，请到购物车修改");
		}
		if(StringUtil.notBlank(skuCodesStr)){
			skuCodesStr = skuCodesStr.substring(0, skuCodesStr.length()-1);
			jsonMessage.setData(skuCodesStr);
			jsonMessage.setStatusAndMsg("34", "部分商品已下架，请到购物车修改");
			return jsonMessage;
		}
		if(StringUtil.notBlank(skuCodesStr4Lock)){
			skuCodesStr4Lock = skuCodesStr4Lock.substring(0, skuCodesStr4Lock.length()-1);
			jsonMessage.setData(skuCodesStr4Lock);
			jsonMessage.setStatusAndMsg("35", "部分商品已锁定，请到购物车修改");
			return jsonMessage;
		}
		return jsonMessage;
	}
	
	/**
	 * 根据选中的购物车项跟会员ID获取对应的商家列表和商家分组下的商品列表
	 * @param userId
	 * @param cartIds
	 * @param deliveryTypes 快递方式
	 * @param pickUpInfoList 自提信息
	 * @return
	 * @author Jacob
	 * 2015年12月10日上午11:46:22
	 */
	public List<Record> findOrderConfirmationList(String userId,Integer[] cartIds,Integer addressId,String[] deliveryTypes,List<Record> pickUpInfoList){
		//获取购物车商家视图信息列表
		List<Record> merchantList = findCartMerchant4OrderConfirmation(userId, cartIds);
		
		if(StringUtil.notNull(merchantList)){
			//处理配送方式数组(cartId-Order.DELIVERY_TYPE_EXPRESS或者cartId-Order.DELIVERY_TYPE_SELF)
			Map<Integer,Integer> deliveryTypeMap = stringArrayToMap(deliveryTypes);
			//循环购物车商家视图信息列表
			for(Record record : merchantList){
				//是否只有快递运输
				boolean isOnlyExpress = true;
				//获取跟当前会员和商家target_id相关的购车明细信息列表
				List<Record> cartDetailList = findCartList(userId,record.getStr("target_id"),record.getInt("cart_type"),cartIds,null);
				//运费所需参数map
				Map<String, Integer> allSkuCodeCountMap = new HashMap<String, Integer>();
				for(Record cartDetail : cartDetailList){
					//获取相应的自提信息
					Record pickUpInfo = this.getPickUpInfo(pickUpInfoList, cartDetail.getInt("cart_id"));
					boolean pickUpInfoFlag = pickUpInfo!=null;
					cartDetail.set("o2o_shop_id", pickUpInfoFlag?pickUpInfo.getStr("o2oShopId"):"");//o2o门店ID
					cartDetail.set("o2o_shop_no", pickUpInfoFlag?pickUpInfo.getStr("o2oShopNo"):"");//o2o门店编号
					cartDetail.set("o2o_shop_name",  pickUpInfoFlag?pickUpInfo.getStr("o2oShopName"):"");//o2o门店名称
					cartDetail.set("o2o_shop_address",pickUpInfoFlag?pickUpInfo.getStr("o2oShopAddress"):"");//o2o门店地址
					cartDetail.set("pick_up_name", pickUpInfoFlag?pickUpInfo.getStr("pickUpName"):"");//收货人姓名
					cartDetail.set("pick_up_mobile", pickUpInfoFlag?pickUpInfo.getStr("pickUpMobile"):"");//收货人手机号
					//判断快递方式
					if(deliveryTypeMap.size()==0||deliveryTypeMap.get(cartDetail.getInt("cart_id"))==1){
						allSkuCodeCountMap.put(cartDetail.getStr("sku_code"), cartDetail.getInt("count"));
					}
					//运费所需参数map
					Map<String, Integer> skuCodeCountMap = new HashMap<String, Integer>();
					skuCodeCountMap.put(cartDetail.getStr("sku_code"), cartDetail.getInt("count"));
					//初始化该商品SKU运费
					BigDecimal freight = new BigDecimal(0);
					//判断该商品SKU在选中的收货地址的所在城市是否满足购买量的O2O门店可自提
					boolean isPickUp = StoreSkuMap.dao.isPickUp(cartDetail.getInt("count"), cartDetail.getStr("sku_code"));
					if(addressId!=null){
						RecAddress recAddress = RecAddress.dao.findByIdLoadColumns(addressId, "province_code,city_code,area_code");
						if(deliveryTypeMap.size()==0||deliveryTypeMap.get(cartDetail.getInt("cart_id"))==1){
							//计算该商品SKU运费
							freight = FreightTemplate.dao.calculate(skuCodeCountMap, recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
						}
					}
					cartDetail.set("delivery_type", deliveryTypeMap.size()==0?1:deliveryTypeMap.get(cartDetail.getInt("cart_id")));//保存快递方式
					cartDetail.set("is_pick_up", isPickUp);//保存是否可自提
					cartDetail.set("freight", freight.setScale(2,BigDecimal.ROUND_UP));//保存运费
					if(isPickUp) isOnlyExpress = false;
				}
				//初始化该组运费
				BigDecimal freight = new BigDecimal(0);
				if(addressId!=null&&!(-1 == addressId)){
					RecAddress recAddress = RecAddress.dao.findByIdLoadColumns(addressId, "province_code,city_code,area_code");
					//计算该组运费
					freight = FreightTemplate.dao.calculate(allSkuCodeCountMap, recAddress.getInt("province_code"), recAddress.getInt("city_code"), recAddress.getInt("area_code"));
				}
				record.set("cartDetailList", cartDetailList);
				record.set("cartDetailSize", cartDetailList.size());
				record.set("isOnlyExpress", isOnlyExpress);
				record.set("freight", freight.setScale(2,BigDecimal.ROUND_UP));
			}
		}
		
		return merchantList;
	}
	
	
	/**
	 * 获取商品总额
	 * @param cartIds
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2016年4月28日下午4:00:48
	 */
	public BigDecimal getProductTotal(Integer[] cartIds,String userId){
		List<Object> params = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	SUM(ps.eq_price * c.count) productTotal ");//实时商品sku价格*数量
		sql.append(" FROM ");
		sql.append(" 	t_cart c,t_pro_sku ps ");
		sql.append(" WHERE ");
		sql.append(" 	c.sku_code = ps.`code` ");
		sql.append(" AND c.user_id = ? ");
		params.add(userId);
		//购物车ID数组
		if(StringUtil.notNull(cartIds)&&cartIds.length>0){
			sql.append(" AND c.id in ("+StringUtil.arrayToString(",", cartIds)+") ");
		}
		return Db.queryBigDecimal(sql.toString(), params.toArray());
	}
	
	/**
	 * 判断购物车结算配送方式数组是否全部为自提订单
	 * @param deliveryTypes（cartId-1或cartId-2）
	 * @return
	 * @author Jacob
	 * 2016年5月7日下午3:07:35
	 */
	public boolean isAllPickUpOrder(String[] deliveryTypes){
		boolean isSuccess = true;
		//处理配送方式数组(cartId-Order.DELIVERY_TYPE_EXPRESS或者cartId-Order.DELIVERY_TYPE_SELF)
		Map<Integer,Integer> deliveryTypeMap = stringArrayToMap(deliveryTypes);
		Collection<Integer> values = deliveryTypeMap.values();
		for(Integer value : values){
			if(value==1){
				isSuccess = false;
				break;
			}
		}
		return isSuccess;
	}
	
	/**
	 * 检查购买的购物车商品状态（上下架状态、锁定状态）
	 * @param cartIds
	 * @return
	 * @author Jacob
	 * 2016年6月1日下午1:50:50
	 */
	public JsonMessage checkProductStatus(Integer[] cartIds){
		JsonMessage jsonMessage = new JsonMessage();
		String skuCodesStr = "";
		String skuCodesStr4Lock = "";
		for(Integer id : cartIds){
			Cart cart = Cart.dao.findById(id);
			//商品SKU识别码
			String skuCode = cart.getStr("sku_code");
			//商品ID
			Integer productId = cart.getInt("product_id");
			Product product = Product.dao.findByIdLoadColumns(productId, "status,lock_status");
			if(product.getInt("status")!=Product.STATUS_SHELVE){
				skuCodesStr += skuCode+",";
			}
			if(product.getInt("lock_status")!=Product.LOCK_STATUS_ENABLE){
				skuCodesStr4Lock += skuCode+",";
			}
		}
		if(StringUtil.notBlank(skuCodesStr)){
			skuCodesStr = skuCodesStr.substring(0, skuCodesStr.length()-1);
			jsonMessage.setData(skuCodesStr);
			jsonMessage.setStatusAndMsg("34", "部分商品已下架，请到购物车修改");
			return jsonMessage;
		}
		if(StringUtil.notBlank(skuCodesStr4Lock)){
			skuCodesStr4Lock = skuCodesStr4Lock.substring(0, skuCodesStr4Lock.length()-1);
			jsonMessage.setData(skuCodesStr4Lock);
			jsonMessage.setStatusAndMsg("35", "部分商品已锁定，请到购物车修改");
		}
		return jsonMessage;
	}
	
	/**
	 * 根据会员id获取购物车商家视图信息
	 * @param userId
	 * @return
	 * @author Jacob
	 * 2015年12月2日下午2:47:02
	 */
	public List<Record> findCartMerchant(String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	cm.target_id, ");
		sql.append(" 	cm.target_name, ");
		sql.append(" 	cm.cart_type, ");
		sql.append(" 	cm.user_id, ");
		sql.append(" 	cm.order_time ");
		sql.append(" FROM ");
		sql.append(" 	v_web_cart_merchant cm ");
		sql.append(" WHERE ");
		sql.append(" 	cm.user_id = ? ");
		sql.append(" ORDER BY cm.order_time DESC ");
		return Db.find(sql.toString(),userId);
	}
	/**
	 * 获取单个购物车
	 *
	 * @author huangzq 
	 * @date 2016年9月10日 下午5:25:51
	 * @param cartId
	 * @param userId
	 * @return
	 */
	public Cart getCart (Integer cartId,String userId){
		return Cart.dao.findFirst("select * from t_cart c where c.id = ? and c.user_id = ?", cartId,userId);
		
	}
	
	/**
	 * 获取当前用户缓存中的购物车数量
	 * @author Jacob
	 * 2016年9月9日下午5:13:35
	 */
	public int getCountByCache(String userId){
		int count = 0;
		if(StringUtil.notNull(userId)){
			// 获取名称为cartCount的Redis Cache对象.
			Cache cartCountCache = Redis.use(BaseConstants.Redis.CACHE_CART_COUNT);
			if(cartCountCache!=null){
				if(cartCountCache.get("cartCount"+userId) != null){
					count = cartCountCache.get("cartCount"+userId);
				}
			}
		}
		return count;
	}
	
	//===============验证===============================//
	/**
	 * cartId 是否属于该用户
	 * @param userId
	 * @param cartId
	 * @return true:是 false:不是
	 * @author chenhg
	 * 2016年4月28日 下午6:00:07
	 */
	public boolean isBelongToUser(String userId, String cartId){
		String sql = "SELECT id FROM t_cart WHERE user_id = ? AND id = ?";
		Record record = Db.findFirst(sql, userId, cartId);
		return StringUtil.notNull(record);
	}
	//===============验证===============================//
	
	/**
	 * 加入购物车
	 * @param userId
	 * @param skuCode
	 * @param proNum
	 * @param orderShopId
	 * @return
	 */
	public JsonMessage add(String userId,String skuCode,Integer proNum,String orderShopId){
		JsonMessage jsonMessage = new JsonMessage();
		
		ProductSku proSku = ProductSku.dao.findById(skuCode);
		if(StringUtil.isNull(proSku)){
			return jsonMessage.setStatusAndMsg("1", "未找到该商品");
		}
		
		if(ProductSku.dao.getProdcutStatusBySku(skuCode) != 0){
			return jsonMessage.setStatusAndMsg("1", "商品处于非正常状态，暂时不能加入！");
		}
		if(proNum < 1){
			return jsonMessage.setStatusAndMsg("1", "加入数量不能小于0！");
		}
		//获取商品sku
		ProductSku sku = ProductSku.dao.findById(skuCode);
		//判断库存
		int skuCount = ProductSku.dao.getCount(skuCode);
		if(skuCount < proNum){
			jsonMessage.setStatusAndMsg(Cart.LOW_STOCKS, "加入数量大于商品库存量！");
		}else{
			//判断该商品SKU是否已经存在购物车里面
			if(Cart.dao.isInCart(skuCode,userId)){
				//根据sku识别码获取购物车项
				Cart cart = Cart.dao.getCart(skuCode, userId);
				int curNum = cart.getInt("count")+proNum;
				if(skuCount < curNum){
					return jsonMessage.setStatusAndMsg(Cart.LOW_STOCKS, "该商品已加入购物车，当前可加入数量为："+(skuCount -cart.getInt("count") ));
				}
				cart.set("count", curNum);//更新数量
				cart.set("price", sku.getBigDecimal("eq_price"));//更新加入购物车时的商品SKU价格
				cart.set("update_time", new Date());
				cart.update();
			}else{
				//保存购物车
				Cart cart = new Cart();
				cart.set("user_id", userId);
				cart.set("sku_code", skuCode);
				cart.set("product_id", sku.getInt("product_id"));
				cart.set("order_shop_id", StringUtil.isNull(orderShopId)? "" :orderShopId);
				cart.set("properties", sku.getStr("property_decs"));
				cart.set("count", proNum);
				cart.set("price", sku.getBigDecimal("eq_price"));
				cart.set("status", 	Cart.STATUS_VALID);
				cart.set("create_time", new Date());
				cart.set("update_time", new Date());
				//获取商品来源
				Product product = Product.dao.findByIdLoadColumns(sku.getInt("product_id"), "source");
				//当商品来源为2.E趣专卖商品时，购物车类型保存为1.E趣自营(自营专卖商品)
				if(product.getInt("source")==Product.SOURCE_SELF_EXCLUSIVE){
					cart.set("type", Cart.TYPE_SELF_EXCLUSIVE);
				}
				//当商品来源为3.E趣公共商品时，购物车类型保存为2.E趣自营(自营公共商品)
				if(product.getInt("source")==Product.SOURCE_SELF_PUBLIC){
					cart.set("type", Cart.TYPE_SELF_PUBLIC);
				}
				//当商品来源为1.专卖商品时，购物车类型保存为2.第三方店铺
				if(product.getInt("source")==Product.SOURCE_EXCLUSIVE){
					cart.set("type", Cart.TYPE_SHOP);
				}
				//当商品来源为4.E趣代销商品或者5.厂家自发商品时，购物车类型保存为3.第三方供货商
				if(product.getInt("source") == Product.SOURCE_FACTORY || product.getInt("source")==Product.SOURCE_FACTORY_SEND){
					cart.set("type", Cart.TYPE_SUPPLIER);
				}
				cart.save();
			}
			/****【更新商品加入购物车数量】******/
			Product product = Product.dao.findById(sku.getInt("product_id"));
			product.set("cart_count", product.getInt("cart_count")+1);
			product.update();
			/***********将购物车数量保存到redis*****************/
			updateCartCountRedis(userId);
			/***********将购物车数量保存到redis*****************/
		}
		
		return jsonMessage;
	}
	
	/**
	 * 批量删除购物车
	 * @param ids
	 * @param userId
	 * @return
	 */
	public JsonMessage batchDelete(String[] ids, String userId){
		JsonMessage jsonMessage = new JsonMessage();
		String sql = "UPDATE t_cart SET status = 0  where user_id = '"+userId+"' and id in (";
		if(StringUtil.notNull(ids)&&ids.length>0){
			for(String id : ids){
				sql +="'"+id+"',";
			}
			sql = sql.substring(0, sql.length()-1);
			sql+=")";
			
			if(Db.update(sql)==ids.length){
				/***********将购物车数量保存到redis*****************/
				updateCartCountRedis(userId);
				/***********将购物车数量保存到redis*****************/
				return jsonMessage;
			}
			
		}
		return jsonMessage.setStatusAndMsg("1", "批量删除购物车，请刷新后操作！");
	}
	
	/**
	 * 更新购物车删除状态
	 * @param cartId
	 * @param status
	 * @return
	 * @author Jacob
	 * 2015年12月3日上午10:30:58
	 */
	public JsonMessage updateDeleteStatus(Integer cartId,Integer status,String userId){
		JsonMessage jsonMessage = new JsonMessage();
		//更新购物车数据
		String sql = " UPDATE t_cart c SET c.`status` = ?,c.update_time = NOW() WHERE c.user_id = ? AND c.id = ? ";
		int result = Db.update(sql, status, userId, cartId);
		if(result == 1){
			/***********将购物车数量保存到redis*****************/
			updateCartCountRedis(userId);
			/***********将购物车数量保存到redis*****************/
			return jsonMessage;
		}
		return jsonMessage.setStatusAndMsg("1", "更新失败，请刷新后再操作！");
	}
	
	/**
	 * 更新购物车数量
	 * @param cartId
	 * @param cartCount
	 * @return
	 * @author Jacob
	 * 2015年12月8日下午5:34:15
	 */
	public JsonMessage updateCount(Integer cartId,Integer cartCount, String userId){
		JsonMessage jsonMessage = new JsonMessage();
		if(cartCount < 1){
			return jsonMessage.setStatusAndMsg("1", "购物车数量不能小于0");
		}
		Cart cart = Cart.dao.getCartById(cartId, userId);
		//防止非法操作别人的购物车
		if(cart == null){
			return jsonMessage.setStatusAndMsg("2", "购物车商品已经失效");
		}
		//库存不足
		int total = ProductSku.dao.getCount(cart.getStr("sku_code"));
		if(total < cartCount){
			jsonMessage.setData(total);
			return jsonMessage.setStatusAndMsg("3", "已超过商品库存");
		}
		
		cart.set("count", cartCount);
		cart.set("update_time", new Date());
		cart.update();
		/***********将购物车数量保存到redis*****************/
		updateCartCountRedis(userId);
		/***********将购物车数量保存到redis*****************/
		return jsonMessage;
	}
	
	
	/**
	 * 根据购物车id数组获取相应的商品id数组
	 * @param cartIds 购物车IDs
	 * @return
	 * @author Jacob
	 * 2015年12月3日下午1:49:39
	 */
	public List<Integer> findProductIdListByCartIds(String[] cartIds, String userId){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" 	ps.product_id ");
		sql.append(" FROM ");
		sql.append(" 	t_cart c,t_pro_sku ps ");
		sql.append(" WHERE ");
		sql.append("    c.sku_code = ps.code ");
		sql.append(" AND c.user_id = ?");
		sql.append(" AND ");
		sql.append("    c.id in ( "+StringUtil.arrayToStringForSql(",", cartIds)+" ) ");
		return Db.query(sql.toString(), userId);
	}
	
	
	/**
	 * 批量将购车项移入到收藏夹
	 * @param userId 会员ID
	 * @param cartIds 购车id数组
	 * @author Jacob
	 * 2015年12月3日下午1:53:08
	 */
	public JsonMessage batchMoveToFavorites(String userId,String[] cartIds){
		//根据购物车id数组获取相应的商品id数组
		List<Integer> productIdList = findProductIdListByCartIds(cartIds, userId);
		//批量移入收藏夹
		ProductFavs.dao.collection(userId, productIdList);
		//批量逻辑删除购物车
		return batchDelete(cartIds,userId);
	}
	
}
