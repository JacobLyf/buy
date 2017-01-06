package com.buy.common;

import java.io.File;
import java.io.IOException;

import com.buy.model.Email;
import com.buy.model.Function;
import com.buy.model.MQSendError;
import com.buy.model.Menu;
import com.buy.model.Role;
import com.buy.model.SysParam;
import com.buy.model.account.Account;
import com.buy.model.account.AccountRecord;
import com.buy.model.account.Bank;
import com.buy.model.account.BankAccount;
import com.buy.model.account.BankAccountApprove;
import com.buy.model.account.CashTransferRecord;
import com.buy.model.account.WithdrawRecord;
import com.buy.model.activity.Activity;
import com.buy.model.activity.SnatchRedPacket;
import com.buy.model.ad.Ad;
import com.buy.model.ad.AdType;
import com.buy.model.ad.AppAd;
import com.buy.model.ad.Keyword;
import com.buy.model.address.Address;
import com.buy.model.address.ShopAddress;
import com.buy.model.address.SupplierAddress;
import com.buy.model.agent.Agent;
import com.buy.model.agent.AgentCashApplyRecord;
import com.buy.model.agent.AgentCashRecord;
import com.buy.model.agent.AgentGroup;
import com.buy.model.agent.AgentSupplierRecommend;
import com.buy.model.agent.LogAgentLogin;
import com.buy.model.agent.ShopRecycle;
import com.buy.model.auCodeRecord.AuCodeRecord;
import com.buy.model.bbs.BbsCategory;
import com.buy.model.bbs.BbsComment;
import com.buy.model.bbs.BbsTopic;
import com.buy.model.college.College;
import com.buy.model.college.CollegeType;
import com.buy.model.coupon.Coupon;
import com.buy.model.efun.Efun;
import com.buy.model.efun.EfunCart;
import com.buy.model.efun.EfunChance;
import com.buy.model.efun.EfunDiscount;
import com.buy.model.efun.EfunDiscountChangeDetail;
import com.buy.model.efun.EfunDiscountChangeRecord;
import com.buy.model.efun.EfunDrawRecord;
import com.buy.model.efun.EfunEvaluate;
import com.buy.model.efun.EfunEvaluateImg;
import com.buy.model.efun.EfunOrderDetail;
import com.buy.model.efun.EfunProduct;
import com.buy.model.efun.EfunProductScore;
import com.buy.model.efun.EfunRefundApply;
import com.buy.model.efun.EfunSku;
import com.buy.model.efun.EfunSkuApply;
import com.buy.model.efun.EfunUserOrder;
import com.buy.model.envolope.Envolope;
import com.buy.model.envolope.EnvolopeRecord;
import com.buy.model.error.ErrorLog;
import com.buy.model.exam.Exam;
import com.buy.model.exam.ExamOptions;
import com.buy.model.favorites.FavsProduct;
import com.buy.model.favorites.FavsShop;
import com.buy.model.feedback.Feedback;
import com.buy.model.file.EqFile;
import com.buy.model.freight.FreightRule;
import com.buy.model.freight.FreightTemplate;
import com.buy.model.holiday.Holiday;
import com.buy.model.identification.PeishiApply;
import com.buy.model.identification.PeishiCancelApply;
import com.buy.model.im.ImMerchant;
import com.buy.model.img.Image;
import com.buy.model.integral.Integral;
import com.buy.model.integral.IntegralRecord;
import com.buy.model.integral.IntegralUserRecord;
import com.buy.model.link.Link;
import com.buy.model.logistics.LogisticsCompany;
import com.buy.model.merchant.MerchantDeliverAddress;
import com.buy.model.message.Message;
import com.buy.model.message.MessageBak;
import com.buy.model.message.MessageUser;
import com.buy.model.message.MessageUserBak;
import com.buy.model.news.News;
import com.buy.model.news.NewsType;
import com.buy.model.notice.Notice;
import com.buy.model.notice.NoticeType;
import com.buy.model.order.Cart;
import com.buy.model.order.Order;
import com.buy.model.order.OrderDetail;
import com.buy.model.order.OrderLog;
import com.buy.model.order.OrderReturn;
import com.buy.model.order.RefundApply;
import com.buy.model.pos.PushPosRecord;
import com.buy.model.product.ProBackSort;
import com.buy.model.product.ProEvaluate;
import com.buy.model.product.ProEvaluateImg;
import com.buy.model.product.Product;
import com.buy.model.product.ProductBrand;
import com.buy.model.product.ProductFavs;
import com.buy.model.product.ProductFrontSort;
import com.buy.model.product.ProductGroup;
import com.buy.model.product.ProductImg;
import com.buy.model.product.ProductProperty;
import com.buy.model.product.ProductPropertyValue;
import com.buy.model.product.ProductSalesCount;
import com.buy.model.product.ProductScore;
import com.buy.model.product.ProductSignboardMap;
import com.buy.model.product.ProductSku;
import com.buy.model.product.SkuSalesCount;
import com.buy.model.product.SkuValueMap;
import com.buy.model.product.nocargo.ProReplenishNote;
import com.buy.model.product.nocargo.ProductNoCargo;
import com.buy.model.productApply.ImportantUpdateApply;
import com.buy.model.productApply.O2oProUpdateApply;
import com.buy.model.productApply.O2oProUpdateDetail;
import com.buy.model.productApply.ProductImgRecord;
import com.buy.model.productApply.PublicProductApply;
import com.buy.model.productApply.PublicSkuUpdateApply;
import com.buy.model.productApply.PublicSkuUpdateDetail;
import com.buy.model.productApply.PublicSkuUpdateProperty;
import com.buy.model.productApply.PublicSkuUpdateValueMap;
import com.buy.model.productApply.ShopProductApply;
import com.buy.model.productApply.ShopSkuUpdateApply;
import com.buy.model.productApply.ShopSkuUpdateDetail;
import com.buy.model.productApply.ShopSkuUpdateProperty;
import com.buy.model.productApply.ShopSkuUpdateValueMap;
import com.buy.model.push.Push;
import com.buy.model.push.PushUserMap;
import com.buy.model.shop.LogShopLogin;
import com.buy.model.shop.O2OProductApply;
import com.buy.model.shop.O2OProductUnshelveApply;
import com.buy.model.shop.Shop;
import com.buy.model.shop.ShopApply;
import com.buy.model.shop.ShopBuildRecord;
import com.buy.model.shop.ShopCashApplyRecord;
import com.buy.model.shop.ShopCashRechargeRecord;
import com.buy.model.shop.ShopCashRecord;
import com.buy.model.shop.ShopCertification;
import com.buy.model.shop.ShopDeposit;
import com.buy.model.shop.ShopFavs;
import com.buy.model.shop.ShopOpenRefund;
import com.buy.model.shop.ShopOpenReward;
import com.buy.model.shop.ShopProSort;
import com.buy.model.shop.ShopRenew;
import com.buy.model.shop.ShopScore;
import com.buy.model.shop.ShopSort;
import com.buy.model.shop.ShopTemplate;
import com.buy.model.shop.ShopTransfer;
import com.buy.model.sms.SMS;
import com.buy.model.sms.SmsAndMsgTemplate;
import com.buy.model.sms.SmsBatchRecord;
import com.buy.model.store.O2oSkuMap;
import com.buy.model.store.Store;
import com.buy.model.store.StoreImg;
import com.buy.model.store.StoreInOutRecord;
import com.buy.model.store.StorePosRecord;
import com.buy.model.store.StoreSkuMap;
import com.buy.model.supplier.LogSupplierLogin;
import com.buy.model.supplier.Supplier;
import com.buy.model.supplier.SupplierCashApplyRecord;
import com.buy.model.supplier.SupplierCashRecord;
import com.buy.model.supplier.SupplierContactApprove;
import com.buy.model.trade.Trade;
import com.buy.model.transfer.Transfer;
import com.buy.model.user.Admin;
import com.buy.model.user.LogLogin;
import com.buy.model.user.RealName;
import com.buy.model.user.RecAddress;
import com.buy.model.user.StockCertificateApply;
import com.buy.model.user.User;
import com.buy.model.user.UserCashApplyRecord;
import com.buy.model.user.UserCashRechargeRecord;
import com.buy.model.user.UserCashRecord;
import com.buy.model.user.UserCoupon;
import com.buy.model.user.UserSign;
import com.buy.model.user.UserSignRewardRecord;
import com.buy.model.user.UserStockAccount;
import com.buy.model.user.UserStockCertificate;
import com.buy.model.user.UserStockRecord;
import com.buy.model.version.AppVersion;
import com.buy.model.version.MerchantAppVersion;
import com.buy.plugin.rabbitmq.RabbitMQPlugin;
import com.buy.plugin.solr.SolrPlugin;
import com.buy.plugin.solr.SolrServerFactory;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.plugin.redis.serializer.JdkSerializer;
import com.jfinal.render.ViewType;

import net.dreamlu.event.EventPlugin;

public abstract class BaseConfig  extends JFinalConfig{

	/**
	 *classpath下的jdbc.properties
	 */
	public static Prop jdbcProperties;
	/**
	 * classpath下的global.properties
	 */
	public static Prop globalProperties;

	/**
	 * Solr配置.
	 */
	public static Prop solrProperties;

	/**
	 * Redis配置.
	 */
	public static Prop redisProperties;

	/**
	 * RabbitMQ配置.
	 */
	public static Prop rabbitmqProperties;

	/**
	 * 配置常量
	 * @throws IOException
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用getProperty(...)获取值

		//jdbc配置
		File jdbcFile = new File(BaseConstants.CONFIG_PATH+"jdbc.properties");
		File globalFile = new File(BaseConstants.CONFIG_PATH+"global.properties");
		File rabbitmpFile = new File(BaseConstants.CONFIG_PATH+"rabbitmq.properties");
		File redisFile = new File(BaseConstants.CONFIG_PATH+"redis.properties");
		File solrFile = new File(BaseConstants.CONFIG_PATH+"solr.properties");
		if(jdbcFile.exists()){
			System.out.println("加载服务器配置。。。。");
			jdbcProperties = PropKit.use(jdbcFile);
			globalProperties = PropKit.use(globalFile);
			rabbitmqProperties = PropKit.use(rabbitmpFile);
			redisProperties = PropKit.use(redisFile);
			solrProperties = PropKit.use(solrFile);
			this.loadPropertyFile(jdbcFile);

		}else{
			System.out.println("加载本地配置。。。。");
			jdbcProperties = PropKit.use("jdbc.properties");
			globalProperties = PropKit.use("global.properties");
			rabbitmqProperties = PropKit.use("rabbitmq.properties");
			redisProperties = PropKit.use("redis.properties");
			solrProperties = PropKit.use("solr.properties");
			this.loadPropertyFile("jdbc.properties");
		}


		me.setDevMode(getPropertyToBoolean("devMode", true));
		me.setViewType(ViewType.JSP);
		//设置文件上传的路径
		me.setUploadedFileSaveDirectory(globalProperties.get("file.upload.temp.path"));
		//最大上传大小30M
		me.setMaxPostSize(1024*1024*30);
	}


	/**
	 * model映射及插件配置
	 */
	public void configPlugin(Plugins me) {

		// 配置druid数据:主数据库(dev)
		DruidPlugin eqPlugin = new DruidPlugin(getProperty("jdbcUrl"),
				getProperty("user"), getProperty("password").trim());
		eqPlugin.setFilters("stat,wall");
		me.add(eqPlugin);

		// 配置druid数据库:仅仅是股权系统
		DruidPlugin stockPlugin = new DruidPlugin(getProperty("stock-jdbcUrl"),
				getProperty("user"), getProperty("password").trim());
		stockPlugin.setFilters("stat,wall");
		me.add(stockPlugin);

		// 初始化插件
		EventPlugin plugin = new EventPlugin();
		// 开启全局异步
		plugin.async(100);
		// 设置扫描jar包，默认不扫描
		//plugin.scanJar();
		// 设置监听器默认包，默认全扫描
		plugin.scanPackage("com.eq");
		me.add(plugin);
		// ======================================
		// 配置Redis Plugin
		// ======================================*/

		RedisPlugin footprint = new RedisPlugin(BaseConstants.Redis.CACHE_FOOT_PRINT,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.footprint.database"))); // 我的足迹.

		me.add(footprint);

		// PC商品前台分类数据.
		RedisPlugin pcFrontSort = new RedisPlugin(BaseConstants.Redis.CACHE_PC_FRONT_SORT,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.pc.frontsort.database")));
		me.add(pcFrontSort);

		// APP商品前台分类数据.
		RedisPlugin appFrontSort = new RedisPlugin(BaseConstants.Redis.CACHE_APP_FRONT_SORT,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.app.frontsort.database")));
		me.add(appFrontSort);

		//其他数据（邮件有效期，小神仙）
		RedisPlugin otherData = new RedisPlugin(BaseConstants.Redis.CACHE_OTHER_DATA,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.other.database"))); // 邮件有效期,小神仙.
		//最大的实例输
		otherData.getJedisPoolConfig().setMaxTotal(2000);
		//获取jedis实例时最大等待时间
		otherData.getJedisPoolConfig().setMaxWaitMillis(100);
		me.add(otherData);

		//TODO 购物车数据 此普通订单购物车会废弃
		RedisPlugin cartCount = new RedisPlugin(BaseConstants.Redis.CACHE_CART_COUNT,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.cartCount.database")));
		me.add(cartCount);
		
		//幸运购物车数据
		RedisPlugin efunCartCount = new RedisPlugin(BaseConstants.Redis.CACHE_EFUN_CART_COUNT,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.efunCartCount.database")));
		me.add(efunCartCount);

		//幸运一折购专区数据
		RedisPlugin efunData = new RedisPlugin(BaseConstants.Redis.CACHE_EFUN_DATA,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.efunData.database")));
		me.add(efunData);

		//幸运一折购专区数据
		RedisPlugin recentSearch = new RedisPlugin(BaseConstants.Redis.CACHE_RECENT_SEARCH_DATA,
				redisProperties.get("redis.host"),
				Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.recent.search.database")));
		me.add(recentSearch);

		//登录token
		RedisPlugin tokenProperty = new RedisPlugin(BaseConstants.Redis.CACHE_PC_APP_SESSION,
				redisProperties.get("redis.host"), Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")),
				redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.pc.session.database")));
		tokenProperty.setSerializer(JdkSerializer.me);
		me.add(tokenProperty);

		// 短链接.
		RedisPlugin shortURL = new RedisPlugin(BaseConstants.Redis.CACHE_SHORT_URL,
				redisProperties.get("redis.host"), Integer.parseInt(redisProperties.get("redis.port")),
				Integer.parseInt(redisProperties.get("redis.timeout")), redisProperties.get("redis.password"),
				Integer.parseInt(redisProperties.get("redis.short.url.database")));
		me.add(shortURL);


		// ======================================
		// 配置Solr Plugin
		// ======================================*/

		String[][] servers = { { "admin", solrProperties.get("admin"), SolrServerFactory.SOLR_SEARCH },
				{ "product_Search", solrProperties.get("product.core"), SolrServerFactory.SOLR_SEARCH },
				{ "shop_Search", solrProperties.get("shop.core"), SolrServerFactory.SOLR_SEARCH } };
		me.add(new SolrPlugin(servers));

		// ======================================
		// 配置RabbitMQ Plugin
		// ======================================*/

		RabbitMQPlugin rabbitMQPlugin = new RabbitMQPlugin(rabbitmqProperties.get("rabbitmq.host"),
				Integer.parseInt(rabbitmqProperties.get("rabbitmq.port")),
				rabbitmqProperties.get("rabbitmq.user"), rabbitmqProperties.get("rabbitmq.password"));
		me.add(rabbitMQPlugin);

		// 配置ActiveRecord插件(主数据库-dev)
		ActiveRecordPlugin arp = new ActiveRecordPlugin(eqPlugin);
		//打印sql语句
		arp.setDialect(new MysqlDialect());
		arp.setShowSql(getPropertyToBoolean("devMode", true));

		//系统模块
		arp.addMapping("t_menu",Menu.class);
		arp.addMapping("t_sys_param",SysParam.class);
		arp.addMapping("t_function",Function.class);

		// 用户模块
		arp.addMapping("t_admin", Admin.class);					// 用户（后台）
		arp.addMapping("t_user", User.class);					// 用户（前台）

		//角色模块
		arp.addMapping("t_role",Role.class);


		//商品模块
		arp.addMapping("t_product",Product.class);
		arp.addMapping("t_shop_pro_apply",ShopProductApply.class);
		arp.addMapping("t_pro_brand",ProductBrand.class);
		arp.addMapping("t_pro_favs",ProductFavs.class);
		arp.addMapping("t_pro_img",ProductImg.class);
		arp.addMapping("t_pro_property",ProductProperty.class);
		arp.addMapping("t_pro_property_value",ProductPropertyValue.class);
		arp.addMapping("t_pro_sku","code",ProductSku.class);
		arp.addMapping("t_sku_value_map","sku_code,value_id",SkuValueMap.class);
		arp.addMapping("t_shop_sku_update_value_map","detail_id,value_id",ShopSkuUpdateValueMap.class);
		arp.addMapping("t_pro_back_sort",ProBackSort.class);
		arp.addMapping("t_pro_front_sort", ProductFrontSort.class);
		arp.addMapping("t_pro_tag_map", ProductSignboardMap.class);
		arp.addMapping("t_pro_evaluate", ProEvaluate.class);			// 商品评价
		arp.addMapping("t_pro_evaluate_img", ProEvaluateImg.class);		// 商品评价图片
		arp.addMapping("t_public_pro_apply", PublicProductApply.class); // 公共商品申请表
		arp.addMapping("t_public_sku_update_apply", PublicSkuUpdateApply.class); // 公共商品sku信息修改申请
		arp.addMapping("t_public_sku_update_property", PublicSkuUpdateProperty.class);// 公共商品sku修改属性值
		arp.addMapping("t_public_sku_update_detail", PublicSkuUpdateDetail.class);// 公共商品sku修改详情
		arp.addMapping("t_public_sku_update_value_map", PublicSkuUpdateValueMap.class);// 供货商sku修改详情与属性值中间表

		//sku信息修改申请
		arp.addMapping("t_shop_sku_update_apply", ShopSkuUpdateApply.class);
		//o2o商品信息修改申请
		arp.addMapping("t_o2o_pro_update_apply", O2oProUpdateApply.class);
		//o2o商品修改云店完成情况
		arp.addMapping("t_o2o_pro_update_detail", O2oProUpdateDetail.class);
		//重要属性信息修改申请
		arp.addMapping("t_important_update_apply",ImportantUpdateApply.class);
		//商品图片记录
		arp.addMapping("t_pro_img_record",ProductImgRecord.class);
		//专卖商品sku修改详情
		arp.addMapping("t_shop_sku_update_detail",ShopSkuUpdateDetail.class);
		//专卖商品sku修改属性值
		arp.addMapping("t_shop_sku_update_property",ShopSkuUpdateProperty.class);
		//商品评分表
		arp.addMapping("t_product_score","pro_id", ProductScore.class);

		//O2O云店
		arp.addMapping("t_store",Store.class);
		arp.addMapping("t_store_img",StoreImg.class);
		//云店员工pos操作记录
		arp.addMapping("t_store_pos_record", StorePosRecord.class);
		//pos进出库记录
		arp.addMapping("t_store_in_out_record", StoreInOutRecord.class);

		//店铺模块
		arp.addMapping("t_shop",Shop.class);
		arp.addMapping("t_shop_sort", ShopSort.class);
		arp.addMapping("t_shop_apply",ShopApply.class);
		arp.addMapping("t_shop_deposit", ShopDeposit.class);
		arp.addMapping("t_shop_renew", ShopRenew.class);
		arp.addMapping("t_shop_pro_sort", ShopProSort.class);
		arp.addMapping("t_shop_favs", ShopFavs.class);
		arp.addMapping("t_shop_transfer_record", ShopTransfer.class);
		arp.addMapping("t_shop_certification_record", ShopCertification.class);
		arp.addMapping("t_o2o_pro_apply", O2OProductApply.class);
		arp.addMapping("t_shop_build_record", ShopBuildRecord.class);
		arp.addMapping("t_shop_template", ShopTemplate.class);
		// O2O云店下架申请.
		arp.addMapping("t_o2o_pro_unshelve_apply", O2OProductUnshelveApply.class);
		arp.addMapping("t_store_sku_map","sku_code,store_no", StoreSkuMap.class);
		arp.addMapping("t_o2o_sku_map","sku_code,store_no", O2oSkuMap.class);
		arp.addMapping("t_peishi_apply", PeishiApply.class);
		arp.addMapping("t_peishi_cancel_apply", PeishiCancelApply.class);
		//店铺评分表
		arp.addMapping("t_shop_score","shop_id", ShopScore.class);

		// 商品组合表.
		arp.addMapping("t_pro_group", "product_id, group_product_no", ProductGroup.class);

		//收藏模块
		arp.addMapping("t_shop_favs", FavsShop.class);
		arp.addMapping("t_pro_favs", FavsProduct.class);
		//购物车 模块
		arp.addMapping("t_cart", Cart.class);
		//活动
		arp.addMapping("t_activity", Activity.class);
		arp.addMapping("t_snatch_redPacket", SnatchRedPacket.class);
		// 记录模块
		arp.addMapping("t_log_user_login", LogLogin.class);
		arp.addMapping("t_log_agent_login", LogAgentLogin.class);
		arp.addMapping("t_log_shop_login", LogShopLogin.class);
		arp.addMapping("t_log_supplier_login", LogSupplierLogin.class);

		arp.addMapping("t_auth_code_record", AuCodeRecord.class);

		// 广告模块
		arp.addMapping("t_ad", Ad.class);					// PC广告
		arp.addMapping("t_ad_type", AdType.class);          //广告位管理
		arp.addMapping("t_keyword", Keyword.class);         //搜索关键词

		arp.addMapping("t_app_ad", AppAd.class);         //APP广告

		// 账户模块
		arp.addMapping("t_account", Account.class);
		arp.addMapping("t_account_record", AccountRecord.class);
		arp.addMapping("t_integral_record", IntegralRecord.class);
		arp.addMapping("t_withdraw", WithdrawRecord.class);
		arp.addMapping("t_user_cash_recharge_record", UserCashRechargeRecord.class);	// 现金充值
		arp.addMapping("t_user_cash_apply_record", UserCashApplyRecord.class);
		arp.addMapping("t_cash_transfer_record", CashTransferRecord.class);	// 现金转账
		arp.addMapping("t_shop_cash_apply_record", ShopCashApplyRecord.class);
		arp.addMapping("t_shop_cash_recharge_record", ShopCashRechargeRecord.class);
		arp.addMapping("t_shop_cash_record", ShopCashRecord.class);
		arp.addMapping("t_agent_cash_apply_record", AgentCashApplyRecord.class);
		arp.addMapping("t_agent_cash_record", AgentCashRecord.class);
		arp.addMapping("t_supplier_cash_apply_record", SupplierCashApplyRecord.class);
		arp.addMapping("t_supplier_cash_record", SupplierCashRecord.class);

		//股权模块
		arp.addMapping("t_user_stock_record", UserStockRecord.class);
		arp.addMapping("t_user_stock_certificate", UserStockCertificate.class);
		arp.addMapping("t_stock_certificate_apply", StockCertificateApply.class);
		arp.addMapping("t_user_stock_account", UserStockAccount.class);
		//银行卡
		arp.addMapping("t_bank_account", BankAccount.class);
		//银行卡审批记录
		arp.addMapping("t_bank_account_approve", BankAccountApprove.class);
		//银行
		arp.addMapping("t_bank", Bank.class);
		arp.addMapping("t_user_cash_record",UserCashRecord.class );

		// 短信模块
		arp.addMapping("t_sms_record", SMS.class);
		arp.addMapping("t_sms_batch_record", SmsBatchRecord.class);
		// 信息模块
		arp.addMapping("t_message", Message.class);
		arp.addMapping("t_message_bak", MessageBak.class);
		arp.addMapping("t_message_user_map", MessageUser.class);
		arp.addMapping("t_message_user_map_bak", MessageUserBak.class);
		// 短信和消息模板
		arp.addMapping("t_sms_msg_template", SmsAndMsgTemplate.class);


		//订单模块
		arp.addMapping("t_order", Order.class);
		arp.addMapping("t_order_detail", OrderDetail.class);
		arp.addMapping("t_order_return", OrderReturn.class);
		arp.addMapping("t_order_log", OrderLog.class);
		//交易表
		arp.addMapping("t_trade","no", Trade.class);
		//退款申请表
		arp.addMapping("t_refund_apply", RefundApply.class);

		// 资讯模块
		arp.addMapping("t_news", News.class);
		arp.addMapping("t_news_type", NewsType.class);

		// 幸运一折购模块
		arp.addMapping("t_efun", Efun.class);//幸运一折购表
		arp.addMapping("t_efun_chance", EfunChance.class);
		arp.addMapping("t_efun_sku_apply", EfunSkuApply.class);//幸运一折购商品sku申请表
		arp.addMapping("t_efun_cart", EfunCart.class);//幸运一折购商品sku申请表
		arp.addMapping("t_efun_sku", EfunSku.class);//幸运一折购商品sku关联表
		arp.addMapping("t_efun_user_order", EfunUserOrder.class);//会员参与幸运一折购记录（幸运一折购订单）表
		arp.addMapping("t_efun_order_detail", EfunOrderDetail.class); // 一折购参与明细表.
		arp.addMapping("t_efun_evaluate", EfunEvaluate.class);//幸运一折购商品晒单（评价）表
		arp.addMapping("t_efun_evaluate_img", EfunEvaluateImg.class);//幸运一折购商品晒单评价图片表
		arp.addMapping("t_efun_product", EfunProduct.class); // 幸运一折购商品表.
		arp.addMapping("t_efun_pro_score", "product_id", EfunProductScore.class); // 幸运一折购默认排序得分
		arp.addMapping("t_efun_draw_record", EfunDrawRecord.class);//幸运购抽奖记录表为
		arp.addMapping("t_efun_discount", EfunDiscount.class);//幸运翻牌购翻牌概率表
		arp.addMapping("t_efun_discount_change_record", EfunDiscountChangeRecord.class);//幸运翻牌购概率更改记录表
		arp.addMapping("t_efun_discount_change_detail", EfunDiscountChangeDetail.class);//幸运翻牌购概率更改详情表
		arp.addMapping("t_efun_refund_apply", EfunRefundApply.class);//幸运一折退款申请
		//物流模块
		arp.addMapping("t_logistics_company", LogisticsCompany.class);

		//供货商模块
		arp.addMapping("t_supplier", Supplier.class);
		arp.addMapping("t_supplier_contact_approve", SupplierContactApprove.class);//供货商联系人修改审批

		//代理商模块
		arp.addMapping("t_agent", Agent.class);
		arp.addMapping("t_shop_recycle", ShopRecycle.class);
		arp.addMapping("t_agent_supplier_recommend", AgentSupplierRecommend.class);


		//其它模块
		arp.addMapping("t_holiday", Holiday.class);

		//省、市、区、街道模块
		arp.addMapping("t_address", Address.class);

		//友情链接模块
		arp.addMapping("t_link", Link.class);
		// 仓库模块
		arp.addMapping("t_store", Store.class);
		//仓库调拨模块
		arp.addMapping("t_transfer", Transfer.class);

		//红包模块
		arp.addMapping("t_envolope", Envolope.class);
		arp.addMapping("t_envolope_record", EnvolopeRecord.class);

		//优惠卷模块
		arp.addMapping("t_coupon", Coupon.class);
		arp.addMapping("t_user_coupon_map", UserCoupon.class);

		// 邮箱模块
		arp.addMapping("t_email_home", Email.class);

		// 实名模块
		arp.addMapping("t_user_realname", RealName.class);
		// 图片模块
		arp.addMapping("t_image", Image.class);
		// 文件模块
		arp.addMapping("t_file","path" ,EqFile.class);
		// 运费模板.
		arp.addMapping("t_freight_template", FreightTemplate.class);

		// 运费规则.
		arp.addMapping("t_freight_rule", FreightRule.class);

		//积分模块
		arp.addMapping("t_integral", Integral.class);
		arp.addMapping("t_integral_record", IntegralRecord.class);
		arp.addMapping("t_integral_user_record", IntegralUserRecord.class);

		// 签到
		arp.addMapping("t_user_sign", UserSign.class);

		arp.addMapping("t_user_sign_reward_record", UserSignRewardRecord.class);

		// MQ消息发送异常登记表
		arp.addMapping("t_mq_send_error", MQSendError.class);

		//  公告
		arp.addMapping("t_notice", Notice.class);
		arp.addMapping("t_notice_type", NoticeType.class);

		// 考题
		arp.addMapping("t_exam", Exam.class);
		arp.addMapping("t_exam_options", ExamOptions.class);

		// e趣社区
		arp.addMapping("t_bbs_category", BbsCategory.class);
		arp.addMapping("t_bbs_topic", BbsTopic.class);
		arp.addMapping("t_bbs_comment", BbsComment.class);
		// e趣学院
		arp.addMapping("t_college", College.class);
		arp.addMapping("t_college_type", CollegeType.class);

		//意见反馈
		arp.addMapping("t_feedback", Feedback.class);

		// APP版本
		arp.addMapping("t_app_version", AppVersion.class);

		// 商家版APP版本
		arp.addMapping("t_merchant_app_version", MerchantAppVersion.class);

		// 代理商分组
		arp.addMapping("t_agent_group_map", AgentGroup.class);

		// 推送
		arp.addMapping("t_push_record", Push.class);
		arp.addMapping("t_push_user_map", "registration_id,user_id", PushUserMap.class);

		// 地址
		arp.addMapping("t_reciever_address", RecAddress.class);	// 用户收货地址
		arp.addMapping("t_shop_receiver_address", ShopAddress.class);
		arp.addMapping("t_supplier_receiver_address", SupplierAddress.class);

		// 商品/SKU(销量 + 结算)
		arp.addMapping("t_product_sales_count", "product_id", ProductSalesCount.class);
		arp.addMapping("t_sku_sales_count", "sku_code", SkuSalesCount.class);

		// 开店退款记录
		arp.addMapping("t_open_shop_refund", ShopOpenRefund.class);
		// 开店奖励
		arp.addMapping("t_open_shop_reward", ShopOpenReward.class);

		// IM账号
		arp.addMapping("t_im_merchant", ImMerchant.class);

		// 推送POS错误记录
		arp.addMapping("t_push_pos_record", PushPosRecord.class);

		// 商家发货地址表
		arp.addMapping("t_merchant_deliver_address", MerchantDeliverAddress.class);

		// 异常信息日志表
		arp.addMapping("t_error_log", ErrorLog.class);

		// 到货通知.
		arp.addMapping("t_pro_no_cargo", ProductNoCargo.class);
		arp.addMapping("t_pro_replenish_note", ProReplenishNote.class);

		me.add(arp);


		//配置股权数据库ActiveRecordPlugin
		//ActiveRecordPlugin stock = new ActiveRecordPlugin("stock", stockPlugin);
		//打印sql语句
		/*stock.setDialect(new MysqlDialect());
		stock.setShowSql(true);
		me.add(stock);*/

	}

	/**
	 * 配置处理器(这里添加了对html后缀的处理)如果不需要则默认为rest风格
	 */
	public void configHandler(Handlers me) {
		//configHandler 全局配置处理器，主要是记录日志和request域值处理"
		//me.add(new GlobalHandler());
	}
}
