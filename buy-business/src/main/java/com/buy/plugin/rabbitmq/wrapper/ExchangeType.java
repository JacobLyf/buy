package com.buy.plugin.rabbitmq.wrapper;

public enum ExchangeType {
	CREATE("create"), UPDATE("update"), DELETE("delete"),
	
	// 商品上架.
	PRODUCT_SHELVES("productShelves"),
		
	// 商品下架.
	PRODUCT_UNSHELVES("productUnShelves"),
	
	// 商品评分更新.
    PRODUCT_SCORE_UPDATE("productScoreUpdate"),
    
    // 商品销量更新.
    PRODUCT_SALES_COUNT_UPDATE("productSalesCountUpdate"),
    
    // 幸运一折购商品评分更新.
    PRODUCT_EFUN_SCORE_UPDATE("productEfunScoreUpdate"),
    
    // 幸运一折购商品价格更新[加入幸运一折购的sku最低价].
    PRODUCT_EFUN_PRICE_UPDATE("productEfunPriceUpdate"),
    
    // 幸运一折购商品历史开奖率更新.
    PRODUCT_EFUN_WINNING_RATE_UPDATE("productEfunWinningRateUpdate"),
    
    // 商品加入幸运一折购.
    PRODUCT_JOIN_EFUN("productJoinEfun"),
    
    // 商品退出幸运一折购.
    PRODUCT_QUIT_EFUN("productQuitEfun"),
	
	// 品牌更新.
	BRAND_UPDATE("brandUpdate"),
	
	// 商品品牌更新.
	PRODUCT_BRAND_UPDATE("productBrandUpdate"),
	
	// 店铺更新.
	SHOP_UPDATE("shopUpdate"),
	
	// 店铺评分更新.
    SHOP_SCORE_UPDATE("shopScoreUpdate"),
    
    // 店铺商品数量更新.
    SHOP_PRODUCT_NO_UPDATE("shopProductNoUpdate"),
		
	// 供货商更新.
	SUPPLIER_UPDATE("supplierUpdate"),
	
	// PC前台二级分类数据更新.
	PC_FIRST_SORT_UPDATE("pcFirstSortUpdate"),
			
	// APP前台二级分类数据更新.
	APP_FIRST_SORT_UPDATE("appFirstSortUpdate"),
	
	// PC前台二级分类数据更新.
	PC_SECOND_SORT_UPDATE("pcSecondSortUpdate"),
		
    // APP前台二级分类数据更新.
	APP_SECOND_SORT_UPDATE("appSecondSortUpdate"),
	
	// PC前台三级分类数据更新.
	PC_THIRD_SORT_UPDATE("pcThirdSortUpdate"),
	
	// APP前台三级分类数据更新.
    APP_THIRD_SORT_UPDATE("appThirdSortUpdate"),
	
	// PC前后台分类关联更新.
	PC_SORT_MAPPING_UPDATE("pcSortMappingUpdate"),
	
	// APP前后台分类关联更新.
	APP_SORT_MAPPING_UPDATE("appSortMappingUpdate"),
	
	// 后台三级分类状态更新.
	BACK_SORT_STATUS_UPDATE("backSortStatusUpdate"),
	
	// APP广告商品列表更新.
    APP_AD_PRODUCT_UPDATE("appAdProductUpdate"),
    
    // APP广告店铺列表更新.
    APP_AD_SHOP_UPDATE("appAdShopUpdate"),
	
	// 删除店铺商品.
	DELETE_SHOP_PRODUCT("deleteShopProduct"),

	// O2O改价更新.
    PC_O2O_UPDATE("pcO2oUpdate");;
	
	private String value;

	private ExchangeType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	public static ExchangeType fromValue(String value) {
		for (ExchangeType type : ExchangeType.values()) {
			if (type.toString().equals(value)) {
				return type;
			}
		}
		return null;
	}
}