package com.buy.service.cart;

import java.util.Date;
import java.util.List;

import com.buy.common.JsonMessage;
import com.buy.model.efun.EfunCart;
import com.buy.model.product.Product;
import com.buy.model.product.ProductFavs;
import com.buy.model.product.ProductSku;
import com.buy.model.shop.Shop;
import com.buy.model.supplier.Supplier;
import com.buy.string.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 幸运一折购购物车公用service
 * @author chenhg
 *
 */
public class BaseEfunCartService {

	/**
	 * 加入购物车
	 * @author chenhg
	 * @param userId
	 * @param skuCode
	 * @param proNum
	 * @param orderShopId
	 * @return
	 * @date 2016年12月21日 上午10:43:18
	 */
	public JsonMessage add(String userId,String skuCode,Integer proNum,String orderShopId){
		JsonMessage jsonMessage = new JsonMessage();
		
		if(ProductSku.dao.getProdcutStatusBySku(skuCode) != 0){
			return jsonMessage.setStatusAndMsg("1", "商品已下架，暂时不能加入");
		}
		
		if(proNum < 1){
			return jsonMessage.setStatusAndMsg("2", "加入数量不能小于0");
		}
		
		//判断库存
		int skuCount = ProductSku.dao.getCount(skuCode);
		if(skuCount < proNum){
			jsonMessage.setStatusAndMsg("3", "加入数量大于商品库存量");
		}else{
			ProductSku sku = ProductSku.dao.findById(skuCode); 
			Product product = Product.dao.findById(sku.getInt("product_id"));
			//判断该商品SKU是否已经存在购物车里面
			if(EfunCart.dao.isInEfunCartBySkuCode(skuCode,userId)){
				EfunCart cart = EfunCart.dao.getEfunCartBySkuCode(skuCode, userId);
				int curNum = cart.getInt("count")+proNum;
				if(skuCount < curNum){
					return jsonMessage.setStatusAndMsg("4", "该商品已加入购物车，当前可加入数量为："+(skuCount -cart.getInt("count") ));
				}
				cart.set("count", curNum);                       //更新数量
				cart.set("price", sku.getBigDecimal("eq_price"));//更新加入购物车时的商品SKU价格
				cart.set("update_time", new Date());
				cart.update();
			}else{
				//保存购物车
				EfunCart cart = new EfunCart();
				cart.set("user_id", userId);
				cart.set("sku_code", skuCode);
				cart.set("product_id", sku.getInt("product_id"));
				cart.set("order_shop_id", StringUtil.isNull(orderShopId)?"":orderShopId);
				cart.set("properties", sku.getStr("property_decs"));
				cart.set("count", proNum);
				cart.set("price", sku.getBigDecimal("eq_price"));
				cart.set("status", 	EfunCart.STATUS_VALID);
				cart.set("create_time", new Date());
				cart.set("update_time", new Date());
				
				int source = product.getInt("source");
				//确定商家信息
				if(source == Product.SOURCE_EXCLUSIVE
						|| source == Product.SOURCE_SELF_EXCLUSIVE){
					Shop shop = Shop.dao.findByIdLoadColumns(product.getStr("shop_id"), "id,no,name");
					cart.set("merchant_id", shop.getStr("id"));
					cart.set("merchant_no", shop.getStr("no"));
					cart.set("merchant_name", shop.getStr("name"));
				}else{
					Supplier supplier = Supplier.dao.findByIdLoadColumns(product.getStr("supplier_id"), "id,no,name");
					cart.set("merchant_id", supplier.getStr("id"));
					cart.set("merchant_no", supplier.getStr("no"));
					cart.set("merchant_name", supplier.getStr("name"));
				}
				//确定购物车类型
				switch (source) {
				//当商品来源为2.E趣专卖商品时，购物车类型保存为1.E趣自营(自营专卖商品)
				case Product.SOURCE_SELF_EXCLUSIVE:
					cart.set("type", EfunCart.TYPE_SELF_EXCLUSIVE);
					break;
				//当商品来源为3.E趣公共商品时，购物车类型保存为2.E趣自营(自营公共商品)
				case Product.SOURCE_SELF_PUBLIC:
					cart.set("type", EfunCart.TYPE_SELF_PUBLIC);
					break;
				//当商品来源为1.专卖商品时，购物车类型保存为3.第三方店铺
				case Product.SOURCE_EXCLUSIVE:
					cart.set("type", EfunCart.TYPE_SHOP);
					break;
				//当商品来源为4.E趣代销商品或者5.厂家自发商品时，购物车类型保存为3.第三方供货商
				case Product.SOURCE_FACTORY:
					cart.set("type", EfunCart.TYPE_SUPPLIER);
					break;
				case Product.SOURCE_FACTORY_SEND:
					cart.set("type", EfunCart.TYPE_SUPPLIER);
					break;

				default:
					break;
				}
				
				cart.save();
			}
			/****【更新商品加入购物车数量】******/
			
			product.set("cart_count", product.getInt("cart_count")+1);
			product.update();
			/***********将购物车数量保存到redis*****************/
			EfunCart.dao.updateEfunCartCountRedis(userId);
			/***********将购物车数量保存到redis*****************/
		}
		
		return jsonMessage;
	}
	
	/**
	 * 更新购物车数量
	 * @author chenhg
	 * @param cartId
	 * @param cartCount
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:43:43
	 */
	public JsonMessage updateCount(Integer cartId,Integer cartCount, String userId){
		JsonMessage jsonMessage = new JsonMessage();
		if(cartCount < 1){
			return jsonMessage.setStatusAndMsg("1", "购物车数量不能小于0");
		}
		EfunCart cart = EfunCart.dao.getEfunCartById(cartId, userId);
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
		EfunCart.dao.updateEfunCartCountRedis(userId);
		/***********将购物车数量保存到redis*****************/
		return jsonMessage;
	}
	
	
	/**
	 * 更新购物车删除状态
	 * @author chenhg
	 * @param cartId
	 * @param status
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:43:34
	 */
	public JsonMessage updateDeleteStatus(Integer cartId,Integer status,String userId){
		JsonMessage jsonMessage = new JsonMessage();
		//更新购物车数据
		String sql = " UPDATE t_efun_cart c SET c.`status` = ?,c.update_time = NOW() WHERE c.user_id = ? AND c.id = ? ";
		int result = Db.update(sql, status, userId, cartId);
		if(result == 1){
			/***********将购物车数量保存到redis*****************/
			EfunCart.dao.updateEfunCartCountRedis(userId);
			/***********将购物车数量保存到redis*****************/
			return jsonMessage;
		}
		return jsonMessage.setStatusAndMsg("1", "操作失败，请刷新后再操作！");
	}
	
	/**
	 * 批量删除购物车
	 * @author chenhg
	 * @param ids
	 * @param userId
	 * @return
	 * @date 2016年12月21日 上午10:43:27
	 */
	public JsonMessage batchDelete(Integer[] ids, String userId){
		JsonMessage jsonMessage = new JsonMessage();
		//验证是否属于该会员
		for(Integer id : ids){
			if(!EfunCart.dao.isBelongToUser(userId, id)){
				jsonMessage.setStatusAndMsg("1", "购物车不存在");
				return jsonMessage;
			}
		}
		String sql = "UPDATE t_efun_cart SET status = 0  where user_id = '"+userId+"' and id in (";
		if(StringUtil.notNull(ids)&&ids.length>0){
			for(Integer id : ids){
				sql +="'"+id+"',";
			}
			sql = sql.substring(0, sql.length()-1);
			sql+=")";
			
			if(Db.update(sql)==ids.length){
				/***********将购物车数量保存到redis*****************/
				EfunCart.dao.updateEfunCartCountRedis(userId);
				/***********将购物车数量保存到redis*****************/
				return jsonMessage;
			}
			
		}
		return jsonMessage;
	}
	
	/**
	 * 批量将购车项移入到收藏夹
	 * @author chenhg
	 * @param userId
	 * @param cartIds
	 * @return
	 * @date 2016年12月29日 下午6:06:37
	 */
	@Before(Tx.class)
	public JsonMessage batchMoveToFavorites(String userId,Integer[] cartIds){
		//批量逻辑删除购物车
		JsonMessage jsonMessage = batchDelete(cartIds,userId);
		if("0".equals(jsonMessage.getStatus())){
			//根据购物车id数组获取相应的商品id数组
			List<Integer> productIdList = EfunCart.dao.findProductIdListByCartIds(cartIds, userId);
			//批量移入收藏夹
			ProductFavs.dao.collection(userId, productIdList);
		}
		
		return jsonMessage;
	}
}
