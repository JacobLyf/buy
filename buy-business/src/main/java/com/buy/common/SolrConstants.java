package com.buy.common;

public class SolrConstants {
	
	/**
	 * 类型：PC
	 */
	public static final Integer TYPE_PC = 1;
	
	/**
	 * 类型：APP
	 */
	public static final Integer TYPE_APP = 2;
	
	/**
	 * 【商品】默认排序规则-降序.
	 */
	public static final String DEFAULT_PRODUCT_SORT_DESC = "productScore(data_integrity_score, profit_score, new_product_score, new_shop_score, o2o_product_score, efun_product_score, shop_certification_score, shop_deposit_score, peishi_score, popular, depreciate_score, turnover_score, positive_ratio) desc";
	
	/**
	 * 【幸运一折购】默认排序规则-降序.
	 */
	public static final String DEFAULT_EFUN_SORT_DESC = "funScore(dataIntegrityScore, visitTimesScore, orderTimesScore, selfEfunScore, o2oSkuScore, addtimeScore, newEfunScore) desc";
	
	/**
	 * 【商品】排序规则-人气降序排序.
	 */
	public static final String PRODUCT_SORT_VIEWS_DESC = "view desc";
	
	/**
	 * 【商品】排序规则-销量降序排序.
	 */
	public static final String PRODUCT_SORT_SALES_DESC = "sales desc";
	
	/**
	 * 【商品】排序规则-评价降序排序.
	 */
	public static final String PRODUCT_SORT_EVALUATE_DESC = "evaluate desc";
	
	/**
	 * 【商品】排序规则-价格升序排序.
	 */
	public static final String PRODUCT_SORT_PRICE_ASC = "price asc";
	
	/**
	 * 【商品】排序规则-价格降序排序.
	 */
	public static final String PRODUCT_SORT_PRICE_DESC = "price desc";
	
	/**
	 * 【商品】排序规则-新品降序排序.
	 */
	public static final String PRODUCT_SORT_CREATE_DESC = "create desc";
	
	/**
	 * 【商品】排序规则-幸运一折购人气降序排序.
	 */
	public static final String PRODUCT_SORT_EFUN_VIEWS_DESC = "efunViews desc";
	
	/**
	 * 【商品】排序规则-幸运一折购历史开奖率.
	 */
	public static final String PRODUCT_SORT_EFUN_WINNING_RATE_DESC = "efunWinningRate desc";
	
	/**
	 * 【商品】排序规则-幸运一折购最新奖区排序.
	 */
	public static final String PRODUCT_SORT_EFUN_JOIN_TIME_DESC = "efunJoinTime desc";
	
	/**
	 * 【商品】排序规则-幸运一折购价格升序排序.
	 */
	public static final String PRODUCT_SORT_EFUN_PRICE_ASC = "efunPrice asc";
	
	/**
	 * 【商品】排序规则-幸运一折购价格降序排序.
	 */
	public static final String PRODUCT_SORT_EFUN_PRICE_DESC = "efunPrice desc";
	
	/**
	 * 【店铺】默认排序规则-降序.
	 */
	public static final String DEFAULT_SHOP_SORT_DESC = "shopScore(open_duration_score, release_products_score, sales_score, collect_count_score, reputably_score, deposit_score, shop_certification_score, enter_o2o_score, self_shop_score, support_efun_score, new_shop_score) desc";
	
	/**
	 * 分页时当前页数的参数Key
	 */
	public static final String PAGE_NO_PARAM = "pageNo";
	
	/**
	 * 分页时默认的当前页数
	 */
	public static final Integer PAGE_NO = 1;
	
	/**
	 * 分页时每页记录数的参数Key
	 */
	public static final String PAGE_SIZE_PARAM = "pageSize";
	
	/**
	 * 分页时默认的每页记录数
	 */
	public static final Integer PC_PAGE_SIZE = 50;
	/**
	 * app默认分页
	 */
	public static final Integer APP_PAGE_SIZE = 20;
	
	/**
	 * 店铺首页专卖商品推荐记录数
	 */
	public static final Integer SHOP_MONOPOLY_PRODUCT_SIZE = 12;
	
	/**
	 * 店铺首页公共商品推荐记录数
	 */
	public static final Integer SHOP_PUBLIC_PRODUCT_SIZE = 20;
	
	/**
	 * 店铺专卖商品的每页记录数
	 */
//	public static final Integer SHOP_MONOPOLY_PRODUCT_PAGE_SIZE = 20;
	
	/**
	 * 店铺推荐商品的每页记录数
	 */
	public static final Integer SHOP_RECOMMEMD_PRODUCT_PAGE_SIZE = 20;
	
	/**
	 * 自动补全时默认的返回记录数
	 */
	public static final Integer AUTO_COMPLETE_LIMIT = 10;

/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/

	/**
	 * 人气排序.
	 */
	public static final String VIEW_DESC = "view desc";
	
	/**
	 * 新品排序.
	 */
	public static final String CREATE_DESC = "create desc";
	
	/**
	 * 价格升序排序.
	 */
	public static final String PRICE_ASC = "price asc";
	
	/**
	 * 价格降序排序.
	 */
	public static final String PRICE_DESC = "price desc";
	
	/**
	 * 销量排序.
	 */
	public static final String SALES_DESC = "sales desc";
	
	/**
	 * 评价排序.
	 */
	public static final String EVALUATE_DESC = "evaluate desc";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * 【商品/店铺】App广告Id字段.
	 */
	public static final String APP_AD_ID_FIELD = "appAdId";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/**
	 * 【商品】是否假一赔十.
	 */
	public static final String IS_PRODUCT_PEISHI = "com";
	
	/**
	 * 【商品】是否O2O.
	 */
	public static final String IS_O2O = "o2o";
	
	/**
	 * 【商品】是否e趣自营.
	 */
	public static final String IS_PROP = "prop";
	
	/**
	 * 【商品】是否参加幸运一折购.
	 */
	public static final String IS_EFUN = "efun";
	
	/**
	 * 【商品】App广告(手机Banner广告).
	 */
	public static final Integer APP_AD_BANNER_PRODUCT = 1;
	
	/**
	 * 【商品】App广告(e趣市场广告).
	 */
	public static final Integer APP_AD_EFUN_PRODUCT = 3;
	
	/**
	 * 【商品】App广告(品牌推荐).
	 */
	public static final Integer APP_AD_BRAND_PRODUCT = 5;
	
	/**
	 * 【商品】App广告(热门搜索).
	 */
	public static final Integer APP_AD_SEARCH_PRODUCT = 6;
	
	/**
	 * 【商品搜索基本信息字段】
	 */
	public static final String PRODUCT_BASE_FL = SolrConstants.ID + ',' + SolrConstants.PRODUCT_NAME + ','
			+ SolrConstants.PRODUCT_IMG + ',' + SolrConstants.PRICE_FIELD;
	
	/**
	 * 【App商品搜索返回字段】
	 */
	public static final String APP_PRODUCT_SEARCH_FL =
			SolrConstants.ID                                         // 商品Id.
			+ ',' + SolrConstants.PRODUCT_NAME       // 商品名称.
			+ ',' + SolrConstants.PRODUCT_IMG          // 商品图片.
			+ ',' + SolrConstants.PRICE_FIELD               // 商品价格.
			+ ',' + SolrConstants.IS_O2O                       // 是否O2O商品.
			+ ',' + SolrConstants.IS_PROP                     // 是否e趣自营.
			+ ',' + SolrConstants.IS_PRODUCT_PEISHI  // 是否假一赔十.
			+ ',' + SolrConstants.IS_EFUN                     // 是否e趣购商品.
			+ ',' + SolrConstants.PRODUCT_STATUS    // 商品状态.
			+ ',' + SolrConstants.PRODUCT_SOURCE    // 商品来源.
			+ ',' + SolrConstants.SHOP_ID                    // 店铺Id.
			+ ',' + SolrConstants.SHOP_NAME              // 店铺名称.
			+ ',' + SolrConstants.SUPPLIER_ID              // 供应商Id.
			+ ',' + SolrConstants.SUPPLIER_NAME;       // 供应商名称.
	
	/**
	 * 【App店铺搜索返回字段】
	 */
	public static final String APP_SHOP_SEARCH_FL =
			SolrConstants.ID                                         // 店铺Id.
			+ ',' + SolrConstants.SHOP_NAME             // 店铺名称.
			+ ',' + SolrConstants.SHOP_LOGO              // 店铺Logo.
			+ ',' + SolrConstants.PROVINCE_NAME      // 省.
			+ ',' + SolrConstants.CITY_NAME                // 市.
			+ ',' + SolrConstants.AREA_NAME              // 区.
			+ ',' + SolrConstants.PRO_NUMS                // 商品数量.
			+ ',' + SolrConstants.GOOD_RATE               // 好评率.
			+ ',' + SolrConstants.FAV_COUNT               // 收藏次数.
			+ ',' + SolrConstants.SHOP_DEPOSIT         // 店铺保证金.
			+ ',' + SolrConstants.O2O                           // e趣云店商品.
			+ ',' + SolrConstants.PROP                         // e趣自营.
			+ ',' + SolrConstants.EFUN                         // 幸运一折购商品.
			+ ',' + SolrConstants.IS_CERTIFICATION      // 实名认证.
			+ ',' + SolrConstants.IS_PEISHI                    // 假一赔十.
			+ ',' + SolrConstants.IS_RETURN_CERTIFICATION  // 7天内退货认证.
			+ ',' + SolrConstants.IS_COMMUNICATION// 通讯保障认证.
			+ ',' + SolrConstants.IS_EXPRESS_SERVICES// 快速发货认证.
			+ ',' + SolrConstants.IS_TRAFFIC_SAFETY   // 货运安全及包装认证.
			+ ',' + SolrConstants.IS_RESPONSIBILITY;   // 卖家义务及违规处理.
	
	/**
	 * 【App接口店铺专卖商品搜索返回字段】
	 */
	public static final String APP_SHOP_MONOPOLY_PRODUCT_FL = SolrConstants.ID + ',' + SolrConstants.PRODUCT_NAME + ','
			+ SolrConstants.PRODUCT_IMG + ',' + SolrConstants.PRICE_FIELD + ',' + SolrConstants.IS_O2O + ','
			+ SolrConstants.IS_PROP + ',' + SolrConstants.IS_PRODUCT_PEISHI + ',' + SolrConstants.IS_EFUN;
	/**
	 * 【App接口店铺公共商品搜索返回字段】
	 */
	public static final String APP_SHOP_PUBLIC_PRODUCT_FL = SolrConstants.ID + ',' + SolrConstants.PRODUCT_NAME + ','
			+ SolrConstants.PRODUCT_IMG + ',' + SolrConstants.PRICE_FIELD + ',' + SolrConstants.IS_O2O + ',' + SolrConstants.IS_PROP + ','
			+ SolrConstants.IS_PRODUCT_PEISHI  + ',' + SolrConstants.IS_EFUN;
	/**
	 * 【App接口店铺公共商品推荐搜索返回字段】
	 */
	public static final String APP_SHOP_PUBLIC_PRODUCT_RECOMMEND_FL = SolrConstants.ID + ',' + SolrConstants.PRODUCT_NAME + ','
			+ SolrConstants.PRODUCT_IMG + ',' + SolrConstants.PRICE_FIELD + ',' + SolrConstants.IS_EFUN;

/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * e趣自营.
	 */
	public static final String  PROP = "prop";
	
	/**
	 * O2O商品.
	 */
	public static final String O2O = "o2o";
	
	/**
	 * 幸运一折购商品.
	 */
	public static final String EFUN = "efun";
	
	/**
	 * 假一赔十.
	 */
	public static final String COM = "com";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * Id字段.
	 * 【备注】搜索引擎的Core主键均为Id.
	 */
	public static final String  ID = "id";
	
	/**
	 * 商品搜索字段.
	 */
	public static final String SEARCH_FIELD = "search_field";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * Term推荐的最短长度.
	 */
	public static final Integer TERM_MIN_LENGTH = 2;
	
	/**
	 * Term推荐的最小词频.
	 */
	public static final Integer TERM_MIN_FREQUENCY = 1;
	
	/**
	 * 最大推荐的记录数.
	 */
	public static final Integer SUGGEST_MAX_LENGTH= 10;
	
	/**
	 * 【品牌】索引字段.
	 */
	public static final String BRAND_FIELD = "brand_name";
	
	/**
	 * 【品牌】状态字段.
	 */
	public static final String BRAND_STATUS = "brand_status";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * 【动态属性】名称前缀.
	 */
	public static final String DYNAMIC_PROPERTY_PREFIX = "property_name_";
	
	/**
	 * 【动态属性】值前缀.
	 */
	public static final String DYNAMIC_PROPERTY_VALUE_PREFIX = "property_value_";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * 【店铺】Id字段.
	 */
	public static final String  SHOP_ID = "shop_id";
	
	/**
	 * 【店铺】编号字段.
	 */
	public static final String  SHOP_NO = "shop_no";
	
	/**
	 * 【店铺】名称字段.
	 */
	public static final String  SHOP_NAME = "shop_name";
	
	/**
	 * 【店铺】状态字段.
	 */
	public static final String  SHOP_STATUS = "shop_status";
	
	/**
	 * 【店铺】禁用状态字段.
	 */
	public static final String  SHOP_FORBIDDEN_STATUS = "shop_forbidden_status";
	
	/**
	 * 【店铺】Logo字段.
	 */
	public static final String SHOP_LOGO = "shop_logo";
	
	/**
	 * 【店铺】商品数量字段.
	 */
	public static final String PRO_NUMS = "pro_nums";
	
	/**
	 * 【店铺】最近上新商品数量字段.
	 */
	public static final String RECENT_NEW_PRO_NUMS = "recent_new_pro_nums";
	
	/**
	 * 【店铺】好评率.
	 */
	public static final String GOOD_RATE = "good_rate";
	
	/**
	 * 【店铺】省.
	 */
	public static final String PROVINCE_NAME  = "province_name";
	
	/**
	 * 【店铺】市.
	 */
	public static final String CITY_NAME  = "city_name";
	
	/**
	 * 【店铺】区.
	 */
	public static final String AREA_NAME  = "area_name";
	
	/**
	 * 【店铺】关于店铺字段.
	 */
	public static final String DESCRIPTION = "description";
	
	/**
	 * 【店铺】店铺认证.
	 */
	public static final String SHOP_CERIFICATION = "shop_cerification";
	
	/**
	 * 【店铺】实名认证.
	 */
	public static final String IS_CERTIFICATION = "is_certification";
	
	/**
	 * 【店铺】假一赔十认证.
	 */
	public static final String IS_PEISHI = "is_peishi";
	
	/**
	 * 【店铺】7天内退货认证.
	 */
	public static final String IS_RETURN_CERTIFICATION = "is_return_certification";
	
	/**
	 * 【店铺】通讯保障认证.
	 */
	public static final String IS_COMMUNICATION = "is_communication";
	
	/**
	 * 【店铺】快速发货认证.
	 */
	public static final String IS_EXPRESS_SERVICES = "is_express_services";
	
	/**
	 * 【店铺】货运安全及包装.
	 */
	public static final String IS_TRAFFIC_SAFETY = "is_traffic_safety";
	
	/**
	 * 【店铺】卖家义务及违规处理.
	 */
	public static final String IS_RESPONSIBILITY ="is_responsibility";
	
	/**
	 * 【店铺】是否已缴纳店铺保证金.
	 */
	public static final String SHOP_DEPOSIT = "deposit";
	
	/**
	 * 【店铺】App广告(手机Banner广告).
	 */
	public static final Integer APP_AD_BANNER_SHOP = 2;
	
	/**
	 * 【店铺】App广告(发现好店).
	 */
	public static final Integer APP_AD_FOUND_SHOP = 4;
	
	/**
	 * 【店铺】App广告(热门搜索).
	 */
	public static final Integer APP_AD_SEARCH_SHOP = 7;
	
	/**
	 * 【店铺搜索返回字段】
	 */
	public static final String SHOP_FL = SolrConstants.ID + ',' + SolrConstants.SHOP_NAME + ','
			+ SolrConstants.SHOP_LOGO + ',' + SolrConstants.PROVINCE_NAME + ',' + SolrConstants.CITY_NAME + ','
			+ SolrConstants.AREA_NAME + ',' + SolrConstants.PRO_NUMS + ',' + SolrConstants.RECENT_NEW_PRO_NUMS + ','
			+ SolrConstants.GOOD_RATE + ',' + SolrConstants.IS_CERTIFICATION + ',' + SolrConstants.IS_PEISHI + ','
			+ SolrConstants.IS_RETURN_CERTIFICATION + ',' + SolrConstants.IS_RESPONSIBILITY + ','
			+ SolrConstants.IS_COMMUNICATION + ',' + SolrConstants.IS_EXPRESS_SERVICES + ','
			+ SolrConstants.IS_TRAFFIC_SAFETY;
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	/**
	 * 【供应商】Id字段.
	 */
	public static final String  SUPPLIER_ID = "supplier_id";
	
	/**
	 * 【供应商】名称字段.
	 */
	public static final String  SUPPLIER_NAME = "supplier_name";
	
	/**
	 * 【供应商】状态字段.
	 */
	public static final String  SUPPLIER_STATUS = "supplier_status";
	
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	
	/**
	 * 【幸运一折购】Sku Code.
	 */
	public static final String EFUN_SKU_CODE = "sku_code";
	
	/**
	 * 【幸运一折购】奖区人气.
	 */
	public static final String EFUN_VIEWS_COUNT = "view";
	
	/**
	 * 【幸运一折购】历史开奖率.
	 */
	public static final String EFUN_RATE = "rate";
	
	/**
	 * 【幸运一折购】最新奖区.
	 */
	public static final String EFUN_JOIN_TIME = "joinTime";
	
	/**
	 * 【幸运一折购】审核状态.
	 */
	public static final String EFUN_STATUS = "efun_status";
/*-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/	

	/**
	 * 【商品】名称字段.
	 */
	public static final String PRODUCT_NAME = "product_name";
	
	/**
	 * 【商品】名称编号.
	 */
	public static final String PRODUCT_NO = "product_no";
	
	/**
	 * 【商品】状态字段.
	 */
	public static final String PRODUCT_STATUS = "product_status";
	
	/**
	 * 【商品】锁定状态字段.
	 */
	public static final String LOCK_STATUS = "lock_status";
	
	/**
	 * 【商品】审核状态字段.
	 */
	public static final String AUDIT_STATUS = "audit_status";
	
	/**
	 * 【商品】图片字段.
	 */
	public static final String PRODUCT_IMG = "product_img";
	
	/**
	 * 【商品】价格字段.
	 */
	public static final String PRICE_FIELD = "price";
	
	/**
	 * 【商品】是否为店铺推荐商品字段.
	 */
	public static final String IS_SHOP_COMMEND = "is_shop_commend";
	
	/**
	 * 【PC】【商品】一级分类Id字段.
	 */
	public static final String PC_FIRST_SORT_ID = "pc_first_sort_id";
	
	/**
	 * 【PC】【商品】一级分类Name字段.
	 */
	public static final String PC_FIRST_SORT_NAME = "pc_first_sort_name";
	
	/**
	 * 【PC】【商品】二级分类Id字段.
	 */
	public static final String PC_SECOND_SORT_ID = "pc_second_sort_id";
	
	/**
	 * 【PC】【商品】二级分类名称字段.
	 */
	public static final String PC_SECOND_SORT_NAME = "pc_second_sort_name";
	
	/**
	 * 【PC】【商品】三级分类Id字段.
	 */
	public static final String PC_THIRD_SORT_ID = "pc_third_sort_id";
	
	/**
	 * 【PC】【商品】三级分类名称字段.
	 */
	public static final String PC_THIRD_SORT_NAME = "pc_third_sort_name";
	
	/**
	 * 【APP】【商品】一级分类Id字段.
	 */
	public static final String APP_FIRST_SORT_ID = "app_first_sort_id";
	
	/**
	 * 【APP】【商品】一级分类Name字段.
	 */
	public static final String APP_FIRST_SORT_NAME = "app_first_sort_name";
	
	/**
	 * 【APP】【商品】二级分类Id字段.
	 */
	public static final String APP_SECOND_SORT_ID = "app_second_sort_id";
	
	/**
	 * 【APP】【商品】二级分类名称字段.
	 */
	public static final String APP_SECOND_SORT_NAME = "app_second_sort_name";
	
	/**
	 * 【APP】【商品】三级分类Id字段.
	 */
	public static final String APP_THIRD_SORT_ID = "app_third_sort_id";
	
	/**
	 * 【APP】【商品】三级分类名称字段.
	 */
	public static final String APP_THIRD_SORT_NAME = "app_third_sort_name";
	
	/**
	 * 【商品】后台三级分类Status字段.
	 */
	public static final String SORT_STATUS = "sort_status";
	
	/**
	 * 【商品】来源字段.
	 */
	public static final String PRODUCT_SOURCE = "product_source";
	
	/**
	 * 【商品】浏览次数字段.
	 */
	public static final String VIEW_COUNT = "view_count";
	
	/**
	 * 【商品/店铺】收藏次数字段.
	 */
	public static final String FAV_COUNT = "fav_count";
	
	/**
	 * 【商品】销量字段.
	 */
	public static final String SALES_COUNT = "sales";
	
	/**
	 * 【词典】PC一级分类词典.
	 */
	public static final String SUGGEST_PC_FIRST_SORT = "pcSortSuggester";
	
	/**
	 * 【词典】APP一级分类词典.
	 */
	public static final String SUGGEST_APP_FIRST_SORT = "appSortSuggester";
	
	/**
	 * 【词典】品牌词典.
	 */
	public static final String SUGGEST_BRAND = "brandSuggester";

}
