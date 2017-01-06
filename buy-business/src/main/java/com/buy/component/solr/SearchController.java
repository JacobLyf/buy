package com.buy.component.solr;
//package com.buy.component.solr;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.log4j.Logger;
//import com.buy.common.SolrConstants;
//import com.buy.model.product.ProductFrontSort;
//import com.buy.model.product.ProductFrontSortMap;
//import com.buy.plugin.solr.Page;
//import com.buy.plugin.solr.SolrServerFactory;
//import com.buy.plugin.solr.opt.SolrSearcher;
//import com.buy.plugin.solr.opt.SolrUpdate;
//import com.buy.string.StringUtil;
//import com.jfinal.core.Controller;
//
///**
// * Solr对外提供的接口服务.
// * 
// * @author chengyb
// *
// */
//public class SearchController extends Controller {
//	
//	private static final Logger L = Logger.getLogger(SearchController.class);
//	
//	SolrServerFactory factory = SolrServerFactory.getInstance();
//	
//	/**
//	 * PC前台分类数据映射.
//	 */
//	public static Map<Integer, List<Integer>> pcSortMap = null;
//	
//	/**
//	 * PC前台二级分类数据.
//	 */
//	public static Map<String, Object> secondSortMap = new HashMap<String, Object>();
//
//	// 添加/更新/删除文档服务器
//	SolrUpdate update = factory.getSolrUpdate("Update");
//	
//	// 搜索文档服务器
//	SolrSearcher search = factory.getSolrSearcher("Search");
//
//	/**
//	 * 商品搜索服务.
//	 */
//	public void shopPage() {
//	}
//	
//	/**
//	 * 【商城首页】--->点击【一/二/三级类目】.
//	 * 
//	 * @author Chengyb
//	 */
//	public void category() {
//		// 当前请求的前台商品分类Id.
//		Integer frontSortId = getParaToInt("id", 3937);
//		// 当前请求的前台商品分类级别.
//		Integer frontSortLevel = getParaToInt("level", 1);
//		// 一二级前台分类直接获取子集前台分类.
//		
//		
//		//=====================================
//		// 组装查询条件.
//		//=====================================*/
//		StringBuffer query = new StringBuffer();
//		if(frontSortLevel == ProductFrontSort.FIRST_LEVEL) { // 一级分类.
//			query.append("first_sort_id:").append(frontSortId);
//		} else if(frontSortLevel == ProductFrontSort.SECOND_LEVEL) { // 二级分类.
//			query.append("second_sort_id:").append(frontSortId);
//		} else if(frontSortLevel == ProductFrontSort.THIRD_LEVEL) { // 三级分类.
//			query.append("third_sort_id:").append(frontSortId);
//		}
//		
//		Page list = search.searchForPage(1, 20, query.toString(), null, new String[] { "eq_price asc" }, null, new String[] {"brand_name", "third_sort_name"});
//	    renderJson(list);
//	}
//	
//	/**
//	 * 商品搜索服务.
//	 */
//	public void productPage() {
//		String keyword = getPara("keyword"); // 搜索关键词【必填】
//		String[] filter = getParaValues("filter"); // 二次过滤条件【可选】,例如: brandName:OPPO
//		Integer pageNo = getParaToInt("pageNo", SolrConstants.PAGE_NO); // 当前页数【可选】
//		Integer pageSize = getParaToInt("pageSize", SolrConstants.PAGE_SIZE); // 每页大小【可选】
//		
////		if(!StringUtil.isNull(keyword)) {
////			String qParam = "productName:" + keyword;
////			renderJson(search.searchForPage(pageNo, pageSize, qParam, filter, new String[] { "_version_ desc" }, "productName"));
////		} else {
////			L.error("参数异常");
////		}
//	}
//	
//	/**
//	 * 自动补全服务. 
//	 */
//	public void autoComplete() {
//		// 【警告】前缀和正则表达式选择其中一个参数使用,切勿同时使用.
//		String prefix = getPara("prefix"); // 前缀【可选】
//		String regex = getPara("regex"); // 正则表达式【可选】
//		Integer limit = getParaToInt("limit", SolrConstants.AUTO_COMPLETE_LIMIT); // 返回的结果数【可选】
//		
//		renderJson(search.autoComplete(prefix, regex, limit));
//	}
//	
//}