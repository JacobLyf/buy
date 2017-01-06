package com.buy.plugin.solr.opt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.apache.solr.client.solrj.response.Suggestion;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;

import com.buy.common.BaseConstants;
import com.buy.common.SolrConstants;
import com.buy.plugin.solr.Page;
import com.buy.solr.SolrUtil;
import com.buy.string.StringUtil;
import com.buy.tool.ToolWeb;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class SolrSearcher extends OptBase {
	
	private static final Logger L = Logger.getLogger(SolrSearcher.class);
	
	public static Integer PC = 1;
	public static Integer APP = 2;
	
	private HttpSolrClient server;

	public SolrSearcher(String solrUrl) {
		System.out.println("Create a SolrSearcher ---> 【 " + solrUrl + " 】");
		this.server = createServer(solrUrl);
	}

	public HttpSolrClient getServer() {
		return server;
	}

	/**
	 * 根据参数查询
	 * 
	 * @param start
	 * @param rows
	 *            返回文档的最大数目.默认值为10.
	 * @param q
	 *            查询字符串【必须】
	 * @param fq
	 *            二次过滤条件
	 * @param sort
	 *            排序，对查询结果进行排序，例如：sort=date asc,price desc
	 * @param light
	 *            高亮的字段,逗号分割
	 * @param fl
	 *            指定返回那些字段内容，用逗号或空格分隔多个。
	 * @return
	 */
	public List<Map<String, Object>> searchByParams(Integer start, Integer rows, String q, String[] fq, String[] sort,
			String fl, String light) {
		try {
			SolrQuery solrQuery = commonParam(q, fq, null, sort, fl, light);
			
			if (null != start) {
				solrQuery.setStart(start);
			}
			if(null != rows) {
				solrQuery.setRows(rows); // 如果没有指定该参数时，使用默认值“10”。如果你想告诉Solr从查询返回所有可能结果没有上限，指定行为10000000或其他可能大得离谱值高于预期的行数。
			}

			QueryResponse response = server.query(solrQuery);
			
			logQueryTimeInfo(response);
			
			SolrDocumentList docList = response.getResults();
			// 获取高亮的字段
			Map<String, Map<String, List<String>>> map = null;
			if (null != light) {
				map = response.getHighlighting();
			}

			return convertDocumentList(docList, map);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据参数查询
	 * 
	 * @param start
	 * @param rows
	 * @param q
	 *            一次过滤条件.
	 * @param fq
	 *            二次过滤条件.
	 * @param sort
	 *            排序.
	 * @param light
	 *            高亮的字段，逗号分割.
	 * @param facet
	 *            聚合的字段.
	 * @param stats
	 *            统计的字段.
	 * @param needPriceRange
	 *            是否需要计算价格区间.
	 * @return
	 */
	public Page searchForPage(Integer pageNo, Integer pageSize, String q, String[] fq, String[] sort, String fl,
			String light, String[] facet, String[] stats, Boolean needPriceRange, Integer type) throws Exception {
		SolrQuery solrQuery = commonParam(q, fq, facet, sort, fl, light);

		// ===============================
		// 判断是否进行价格统计.
		// ===============================*/
		if (!StringUtil.isNull(stats)) {
			solrQuery.set("stats", true);
			solrQuery.set("stats.field", stats);
		}

		solrQuery.setStart((pageNo - 1) * pageSize);
		solrQuery.setRows(pageSize);

		QueryResponse response = server.query(solrQuery);
		
		String lineSeparator = (String) java.security.AccessController
				.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));
		
		L.info(lineSeparator);
		L.info("搜索: " + q + ", 排序: " + sort);
		
		logQueryTimeInfo(response);

		SolrDocumentList documentList = response.getResults();
		Long totalRow = documentList.getNumFound();

		// ===============================
		// Facet Field数据.
		// ===============================*/
		Map<String, List<String>> facetFields = null;
		if (!StringUtil.isNull(facet)) {
			facetFields = getFacetData(response);
		}

		// ===============================
		// Stats Field数据.
		// ===============================*/
		Map<String, FieldStatsInfo> statsFields = null;
		if (!StringUtil.isNull(stats)) {
			statsFields = response.getFieldStatsInfo();
		}

		// ===============================
		// 计算价格筛选区间.
		// ===============================*/
		if (needPriceRange && !StringUtil.isNull(facet) && !StringUtil.isNull(stats)
				&& statsFields.containsKey(SolrConstants.PRICE_FIELD)) {
			priceRange(q, totalRow, facetFields, statsFields, type);
		}

		// ========================================================================
		// 【警告】对于multiValued=true的字段不要设计高亮显示.
		// 为什么？因为Solr对于这个字段里面的多个值只会返回匹配高亮的那个值.
		// 例如: "colorName": ["金色", "银色", "深空灰"] 搜索"银色"只会返回 "colorName": ["<font
		// color='red'>银色</font>"]
		// ========================================================================
		// 获取高亮的字段.
		Map<String, Map<String, List<String>>> map = null;
		if (null != light) {
			map = response.getHighlighting();
		}
		Integer totalPage = totalRow.intValue() % pageSize > 0 ? totalRow.intValue() / pageSize + 1 : totalRow.intValue() / pageSize;
		return new Page(facetFields, convertDocumentList(documentList, map), pageNo, pageSize, totalPage, totalRow.intValue());
	}
	
	/**
	 * 根据参数统计.
	 * 
	 * @param start
	 * @param rows
	 *            返回文档的最大数目.默认值为10.
	 * @param q
	 *            查询字符串【必须】
	 * @param fq
	 *            二次过滤条件
	 * @param facet
	 *            聚合字段.
	 * @param light
	 *            高亮的字段,逗号分割
	 * @param fl
	 *            逗号分隔的列表，用来指定文档结果中应返回的 Field 集。默认为 “*”，指所有的字段。
	 * @return
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	public List<FacetField> searchForFacet(HttpSolrClient sortSolrClient, String q, String[] fq, String[] facet,
			String fl) throws Exception {
			SolrQuery solrQuery = commonParam(q, null, facet, null, fl, null);

			QueryResponse response = sortSolrClient.query(solrQuery);
			
			logQueryTimeInfo(response);
			
			return response.getFacetFields();
	}
	
	public Page shop(Integer pageNo, Integer pageSize, String q, String[] fq, String[] sort, String fl, String light,
			String[] facet) throws Exception {
		SolrQuery solrQuery = commonParam(q, fq, facet, sort, fl, light);

		// ===============================
		// 判断是否进行价格统计.
		// ===============================*/
		if (q.indexOf(SolrConstants.PRICE_FIELD) == -1) {
			// solrQuery.set("stats.field", SolrConstants.PRICE_FIELD);
		}

		solrQuery.setStart((pageNo - 1) * pageSize);
		solrQuery.setRows(pageSize);

		QueryResponse response = server.query(solrQuery);

		logQueryTimeInfo(response);

		SolrDocumentList documentList = response.getResults();
		Long totalRow = documentList.getNumFound();

		// ========================================================================
		// 【警告】对于multiValued=true的字段不要设计高亮显示.
		// 为什么？因为Solr对于这个字段里面的多个值只会返回匹配高亮的那个值.
		// 例如: "colorName": ["金色", "银色", "深空灰"] 搜索"银色"只会返回 "colorName": ["<font color='red'>银色</font>"]
		// ========================================================================
		// 获取高亮的字段.
		Map<String, Map<String, List<String>>> map = null;
		if (null != light) {
			map = response.getHighlighting();
		}
		Integer totalPage = totalRow.intValue() % pageSize > 0 ? totalRow.intValue() / pageSize + 1 : totalRow.intValue() / pageSize;
		return new Page(null, convertDocumentList(documentList, map), pageNo, pageSize, totalPage, totalRow.intValue());
	}
	
	/**
	 * 搜索公共参数.
	 * 
	 * @param q
	 * @param fq
	 * @param sort
	 * @return
	 */
	private SolrQuery commonParam(String q, String[] fq, String[] facet, String[] sort, String fl, String light) {
		SolrQuery solrQuery = new SolrQuery("*:*");
		solrQuery.set(CommonParams.QT, "/select"); // Request Handler
		solrQuery.set(CommonParams.Q, q);
		if(null != fl) solrQuery.set(CommonParams.FL, fl);
		if(null != fq && fq.length > 0) solrQuery.set(CommonParams.FQ, fq);
		if(null != sort && sort.length > 0) solrQuery.set(CommonParams.SORT, sort);
		
		//===============================
		// 聚合查询.
		//===============================*/
		if (!StringUtil.isNull(facet)) {
			// 设置通过facet查询为true，表示查询时使用facet机制.
			solrQuery.setFacet(true);
			// 设置facet的字段名称.
			solrQuery.set("facet.field", facet);
			// 设置统计后数量大于等于1才返回.
			solrQuery.setFacetMinCount(1);
			solrQuery.setIncludeScore(true);
		}
		
		//===============================
		// 高亮显示.
	    //===============================*/
		if (null != light) {
			// 设置高亮【相当于开启高亮功能】
			solrQuery.setHighlight(true);
			// solrQuery.addHighlightField(light); // 高亮字段
			solrQuery.setParam("hl.fl", light); // 高亮字段

			// 高亮显示字段前后添加html代码
			solrQuery.setHighlightSimplePre("<font color='red'>");
			solrQuery.setHighlightSimplePost("</font>");

			solrQuery.setHighlightSnippets(1); // 结果分片数，默认为1.
			solrQuery.setHighlightFragsize(100); // 每个分片的最大长度，默认为100.
		}
		return solrQuery;
	}

	/**
	 * 计算价格筛选区间.
	 * 
	 * @param q
	 *            查询条件
	 * @param totalRow
	 *            总记录数
	 * @param facetFields
	 *            聚合字段
	 * @param statsFields
	 *            统计字段
	 * @author Chengyb
	 * @throws Exception 
	 */
	private void priceRange(String query, Long totalRow, Map<String, List<String>> facetFields,
			Map<String, FieldStatsInfo> statsFields,Integer type) throws Exception {
		// 查询条件中【不含价格条件】时才统计价格区间.
		if(query.indexOf(SolrConstants.PRICE_FIELD) == -1 && totalRow > 0) {
			// 计算价格区间.
			Iterator<String> iterator = statsFields.keySet().iterator();
			if(iterator.hasNext()) {
				String fieldName = iterator.next();
				// FieldStatsInfo对象.
				FieldStatsInfo fieldStatsInfo = statsFields.get(fieldName);
				
				// 统计的总商品数.
				Long productNo = fieldStatsInfo.getCount();
				
				Long total = fieldStatsInfo.getCount(); // 总记录数.
				double max = (double) fieldStatsInfo.getMax(); // 最高价格.
				double min = (double) fieldStatsInfo.getMin(); // 最低价格.
				double mean = (double) fieldStatsInfo.getMean(); // 平均值.
				double stddev = (double) fieldStatsInfo.getStddev(); // 标准偏差.
				
				// 不显示价格筛选条件【1、商品数量不足一页；2、商品的价格】.
				int pageSize = SolrConstants.TYPE_PC ==  type? SolrConstants.PC_PAGE_SIZE : SolrConstants.APP_PAGE_SIZE;
				if(productNo > pageSize && max - min >= 10) {
					List<String> priceRange = new ArrayList<String>();
					
					int first = 0;
					int second = 0;
					int third = 0;
					int fourth = 0;
					int fifth = 0;
					
					if(stddev > 100*mean || max > 150*mean) { // 处理无搜索条件时,数据差异过大的情况.
						mean = mean/8;
						stddev = mean/6;
						
						first = (int) Math.floor(mean - 2*stddev); // 第一段. [0-first]
						second = (int) Math.floor(mean - stddev) > first + 1 ? (int) Math.floor(mean - stddev) : first + 1; // 第二段. [first - second]
						third = (int) Math.floor(mean) > second + 1 ? (int) Math.floor(mean) : second + 1; // 第三段. [second - third]
						fourth = (int) Math.floor(mean + stddev) > fourth + 1 ? (int) Math.floor(mean + stddev) : third + 1; // 第四段. [third - fourth]
						fifth = (int) Math.floor(mean + 2*stddev) > fourth + 1 ? (int) Math.floor(mean + 2*stddev) : fourth + 1; // 第五段. [fourth - fourth]
					
						priceRange.add("0-" + first);
						priceRange.add((first + 1) + "-" + second);
						priceRange.add((second + 1) + "-" + third);
						priceRange.add((third + 1) + "-" + fourth);
						priceRange.add((fourth + 1) + "-" + fifth);
						priceRange.add((fifth + 1) + "以上");
					} else {
						SolrQuery solrQuery = new SolrQuery("*:*");
						solrQuery.set(CommonParams.QT, "/select"); // Request Handler
						solrQuery.set(CommonParams.Q, query);
						
						solrQuery.set("facet", true);
						solrQuery.set("facet.range", SolrConstants.PRICE_FIELD);
						solrQuery.set("facet.range.start", (Math.floor(min/10)*10 - 1) + "");
						solrQuery.set("facet.range.end", Math.ceil(max/10)*10 + "");
						solrQuery.set("facet.range.gap", SolrUtil.priceRangeGap(max, min, mean));
						
						QueryResponse response = server.query(solrQuery);
						System.out.println("总记录数:" + total);
						SolrUtil.calculatePriceRange(total, response, priceRange, first, second, third, fourth, fifth);
					}
					
					if(priceRange.size() >= 3) { // 三个以上区间段才显示.
						facetFields.put(SolrConstants.PRICE_FIELD, priceRange);
					}
				}
			}
		}
	}

	/**
	 * 获取Facet Data数据.
	 * 
	 * @param response
	 * @author Chengyb
	 * @return
	 */
	private Map<String, List<String>> getFacetData(QueryResponse response) {
		Map<String, List<String>> facetFields = new HashMap<String, List<String>>();
		
		List<FacetField> oldFacetFields = response.getFacetFields();
		
		for (int i = 0, size = oldFacetFields.size(); i < size; i++) {
			FacetField facetField = oldFacetFields.get(i);

			List<Count> countList = facetField.getValues();
			
			if(countList.size() > 0) {
				List<String> facetValues = new ArrayList<String>();
				for (int j = 0, size2 = countList.size(); j < size2; j++) {
					facetValues.add(countList.get(j).getName());
				}
				facetFields.put(facetField.getName(), facetValues);
			}
		}
		return facetFields;
	}
	
	/**
	 * 商品搜索框自动提示. 
	 * 【备注】Solr中有FST-based和AnalyzingInfix二种suggesters，此处使用FST-based。
	 * 
	 * @param q
	 *            输入的搜索字符串.
	 * @param type
	 *            类型【1：PC；2：App】
	 * @author Chengyb
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	public List<Object> suggest(HttpSolrClient productSolrClient, HttpSolrClient shopSolrClient, String requestURI,
			String q, Integer type) throws Exception {
		List<Object> suggestList = new ArrayList<Object>();

		if (type == PC) {
			pcSuggest(productSolrClient, requestURI, suggestList, q);
		} else if (type == APP) {
			appSuggest(productSolrClient, shopSolrClient, suggestList, q);
		}

		return suggestList;
	}

	/**
	 * Pc Suggest.
	 * 
	 * @author Chengyb
	 */
	private void pcSuggest(HttpSolrClient productSolrClient, String requestURI,
			List<Object> suggestList, String q) throws Exception {
		// 一级分类、品牌的Suggest标志位.
		Boolean sortSuggestFlag = false, brandSuggestFlag = false;
		
		// =====================================
		// 一级分类逻辑处理.
		//【逻辑】一二三级包含当前搜索关键字，则返回当前商品的一级分类.
		// =====================================*/
		List<String> sortSuggestions = autoSuggest(productSolrClient, SolrConstants.SUGGEST_PC_FIRST_SORT, q);
		
		if(null != sortSuggestions) {
			Cache frontSortCache = Redis.use(BaseConstants.Redis.CACHE_PC_FRONT_SORT);
			
			if(sortSuggestions.size() > 0) {
				String sortSuggestion = sortSuggestions.get(0);
				
				Integer firstSortId = frontSortCache.get("pc:" + sortSuggestion + ":1");
					
				if(null != firstSortId) {
					sortSuggestion = "<a target='_blank' href='" + requestURI + "/product?cat=" + firstSortId + "&amp;key=" + q + "'>在 <font color='red'>" + sortSuggestion + "</font> 分类中搜索</a>";
						
					suggestList.add(sortSuggestion);
				}
				
				// 推荐关联一级分类.
				sortSuggestFlag = true;
			}
		}
		
		// =====================================
		// 品牌逻辑处理.
		// 【逻辑】品牌不进行分词，完全匹配才返回.
		// =====================================*/
		List<String> brandSuggestions = autoSuggest(productSolrClient, SolrConstants.SUGGEST_BRAND, q);
		if(null != brandSuggestions) {
			for (int i = 0, size = brandSuggestions.size(); i < size; i++) {
				String brandSuggestion = brandSuggestions.get(i).replace("<b>", "").replace("</b>", "");
				
				brandSuggestion = "<a href='" + requestURI + "/product?brand=" + brandSuggestion + "&key=" + q + "'>在 <font color='red'>" + brandSuggestion + "</font> 品牌下搜索</a>";
				
				suggestList.add(brandSuggestion);
				
				// 推荐关联品牌.
				brandSuggestFlag = true;
			}
		}
		
		// =====================================
		// 根据关键词匹配搜索字段【商品名称 + 属性 + 搜索关键词】，返回推荐的一级分类和品牌.
		// 【备注】优先推荐 分类、品牌完全匹配的记录.
		// =====================================*/
		if(sortSuggestFlag == false || brandSuggestFlag == false) {
			List<FacetField> facetFieldList = searchForFacet(productSolrClient, SolrConstants.SEARCH_FIELD + ":" + q, null, new String[] { SolrConstants.PC_FIRST_SORT_NAME, SolrConstants.BRAND_FIELD }, SolrConstants.PC_FIRST_SORT_NAME + ',' +SolrConstants.BRAND_FIELD);
			if(null != facetFieldList) {
				String sortSuggestion = null, brandSuggestion = null;
				long sortFacetCount = 0, brandFacetCount = 0;
				for (int i = 0, size = facetFieldList.size(); i < size; i++) {
					FacetField facetField = facetFieldList.get(i);

					// 处理一级分类数据.
					if(sortSuggestFlag == false && facetField.getName().equals(SolrConstants.PC_FIRST_SORT_NAME)) {
						List<Count> countList = facetField.getValues();
						
						Cache frontSortCache = Redis.use(BaseConstants.Redis.CACHE_PC_FRONT_SORT);
						
						for (int j = 0, size2 = countList.size(); j < size2; j++) {
							if(countList.get(j).getCount() > sortFacetCount) {
								sortFacetCount = countList.get(j).getCount();
								Integer firstSortId = frontSortCache.get("pc:" + countList.get(j).getName() + ":1");
									
								if(null != firstSortId) {
									sortSuggestion = "<a href='" + requestURI + "/product?cat=" + firstSortId + "&key=" + q + "'>在 <font color='red'>" + countList.get(j).getName() + "</font> 分类中搜索</a>";
								}
							}
						}
					}
					
					// 处理品牌数据.
					if(brandSuggestFlag == false && facetField.getName().equals(SolrConstants.BRAND_FIELD)) {
						List<Count> countList = facetField.getValues();
						
						for (int j = 0, size2 = countList.size(); j < size2; j++) {
							if(countList.get(j).getCount() > brandFacetCount) {
								brandFacetCount = countList.get(j).getCount();
								brandSuggestion = "<a href='" + requestURI + "/product?brand=" + countList.get(j).getName() + "&key=" + q + "'>在 <font color='red'>" + countList.get(j).getName() + "</font> 品牌下搜索</a>";
							}
						}
					}
				}
				
				if(null != sortSuggestion && brandSuggestFlag == false) {
					suggestList.add(sortSuggestion);
				}
				if(null != sortSuggestion && brandSuggestFlag == true) {
					suggestList.add(0, sortSuggestion);
				}
				if(null != brandSuggestion && sortSuggestFlag == false) {
					suggestList.add(brandSuggestion);
				}
				
			}
		}
		
		// =====================================
		// 属性逻辑处理.
		// 【逻辑】属性不进行分词，完全匹配才返回.
		// =====================================*/
		
		// =====================================
		// 自动提示处理.
		// 【逻辑】根据商品标题，返回相关提示词.
		// =====================================*/
		Map<String, List<Term>> termMap = term(productSolrClient, null, new String[] { SolrConstants.PRODUCT_NAME }, q + ".*", SolrConstants.SUGGEST_MAX_LENGTH - suggestList.size());
		// Term长度小于4个字符（2个汉字或4个英文字符）的不予以推荐.
		for (Map.Entry<String, List<TermsResponse.Term>> termsEntry : termMap.entrySet()) {
			List<TermsResponse.Term> termList = termsEntry.getValue();
			for (TermsResponse.Term term : termList) {
				System.out.println(term.getTerm());
				// 【1】判断Term长度；【2】判断Term词频；【3】suggestList最大为10.
				if(term.getTerm().length() >= SolrConstants.TERM_MIN_LENGTH && term.getFrequency() >= SolrConstants.TERM_MIN_FREQUENCY ) {
					// 推荐关联商品.
					suggestList.add("<a href='" + requestURI + "/product?key=" + term.getTerm() + "'>" + term.getTerm() + "</a>");
				}
			}
		}
	}

	/**
	 * App Suggest.
	 * 
	 * @author Chengyb
	 */
	private void appSuggest(HttpSolrClient productSolrClient, HttpSolrClient shopSolrClient, List<Object> suggestList,
			String q) throws Exception {
		// 一级分类、品牌的Suggest标志位.
		Boolean sortSuggestFlag = false, brandSuggestFlag = false;
		
		// =====================================
		// 一级分类逻辑处理.
		//【逻辑】一二三级包含当前搜索关键字，则返回当前商品的一级分类.
		// =====================================*/
		List<String> sortSuggestions = autoSuggest(productSolrClient, SolrConstants.SUGGEST_APP_FIRST_SORT, q + "*");
		
		Cache frontSortCache = Redis.use(BaseConstants.Redis.CACHE_APP_FRONT_SORT);
		
		if(null != sortSuggestions) {
			if(sortSuggestions.size() > 0) {
				String sortSuggestion = sortSuggestions.get(0);
				
				Map<String, Object> map = new HashMap<String, Object>();
					
				map.put("type", 3); // 某分类下的商品.
				map.put("condition", frontSortCache.get("app:" + sortSuggestion + ":1"));
				map.put("key", q); // 搜索关键词.
				map.put("tips", "在 " + sortSuggestion + " 分类下搜索"); // 提示.
				
				// 推荐关联一级分类.
				sortSuggestFlag = true;
			}
		}
		
		// =====================================
		// 品牌逻辑处理.
		// 【逻辑】品牌不进行分词，完全匹配才返回.
		// =====================================*/
		List<String> brandSuggestions = autoSuggest(productSolrClient, SolrConstants.SUGGEST_BRAND, q);
		if(null != brandSuggestions) {
			for (int i = 0, size = brandSuggestions.size(); i < size; i++) {
				String brandSuggestion = brandSuggestions.get(i).replace("<b>", "").replace("</b>", "");
				
				Map<String, Object> map = new HashMap<String, Object>();
				
				map.put("type", 4); // 某品牌下的商品.
				map.put("condition", brandSuggestion);
				map.put("key", q); // 搜索关键词.
				map.put("tips", "在 " + brandSuggestion + " 品牌下搜索"); // 提示.
				
				suggestList.add(map);
				
				// 推荐关联品牌.
				brandSuggestFlag = true;
			}
		}
		
		// =====================================
		// 根据关键词匹配搜索字段【商品名称 + 属性 + 搜索关键词】，返回推荐的一级分类和品牌.
		// 【备注】优先推荐 分类、品牌完全匹配的记录.
		// =====================================*/
		Map<String, Object> sortMap = new HashMap<String, Object>();
		Map<String, Object> brandMap = new HashMap<String, Object>();
		
		if(sortSuggestFlag == false || brandSuggestFlag == false) {
			List<FacetField> facetFieldList = searchForFacet(productSolrClient, SolrConstants.SEARCH_FIELD + ":" + q, null, new String[] { SolrConstants.APP_FIRST_SORT_NAME, SolrConstants.BRAND_FIELD }, SolrConstants.APP_FIRST_SORT_NAME + ',' +SolrConstants.BRAND_FIELD);
			if(null != facetFieldList) {
				String sortSuggestion = null, brandSuggestion = null;
				long sortFacetCount = 0, brandFacetCount = 0;
				for (int i = 0, size = facetFieldList.size(); i < size; i++) {
					FacetField facetField = facetFieldList.get(i);

					// 处理一级分类数据.
					if(sortSuggestFlag == false && facetField.getName().equals(SolrConstants.APP_FIRST_SORT_NAME)) {
						List<Count> countList = facetField.getValues();
						
						String sortSuggestionText = null;
						for (int j = 0, size2 = countList.size(); j < size2; j++) {
							if(countList.get(j).getCount() > sortFacetCount) {
								sortFacetCount = countList.get(j).getCount();
								sortSuggestionText = countList.get(j).getName();
								sortSuggestion = "在 " + countList.get(j).getName() + " 分类下搜索";
							}
						}
						
						if(null != sortSuggestion) {
							sortMap.put("type", 3); // 某分类下的商品.
							sortMap.put("condition", frontSortCache.get("app:" + sortSuggestionText + ":1"));
							sortMap.put("key", q); // 搜索关键词.
							sortMap.put("tips", sortSuggestion); // 提示.
						}
					}
					
					// 处理品牌数据.
					if(brandSuggestFlag == false && facetField.getName().equals(SolrConstants.BRAND_FIELD)) {
						List<Count> countList = facetField.getValues();
						
						String brandSuggestionText = null;
						for (int j = 0, size2 = countList.size(); j < size2; j++) {
							if(countList.get(j).getCount() > brandFacetCount) {
								brandFacetCount = countList.get(j).getCount();
								brandSuggestionText = countList.get(j).getName();
								brandSuggestion = "在 " + countList.get(j).getName() + " 品牌下搜索";
							}
						}
						
						if(null != brandSuggestion) {
							brandMap.put("type", 4); // 某品牌下的商品.
							brandMap.put("condition", brandSuggestionText);
							brandMap.put("key", q); // 搜索关键词.
							brandMap.put("tips", brandSuggestion); // 提示.
						}
					}
				}
				
				if(null != sortSuggestion && brandSuggestFlag == false) {
					suggestList.add(sortMap);
				}
				if(null != sortSuggestion && brandSuggestFlag == true) {
					suggestList.add(0, sortMap);
				}
				if(null != brandSuggestion && sortSuggestFlag == false) {
					suggestList.add(brandMap);
				}
				
			}
		}
		
		// =====================================
		// 店铺自动提示处理.
		// =====================================*/
//		List<Object> shopList = shopSuggest(shopSolrClient, null, q, APP);
//		suggestList.addAll(shopList);
		
		// =====================================
		// 自动提示处理.
		// 【逻辑】根据商品标题，返回相关提示词.
		// =====================================*/
		Map<String, List<Term>> termMap = term(productSolrClient, null, new String[] { SolrConstants.PRODUCT_NAME }, q + ".*", SolrConstants.SUGGEST_MAX_LENGTH - suggestList.size());
		// Term长度小于4个字符（2个汉字或4个英文字符）的不予以推荐.
		for (Map.Entry<String, List<TermsResponse.Term>> termsEntry : termMap.entrySet()) {
			List<TermsResponse.Term> termList = termsEntry.getValue();
			for (TermsResponse.Term term : termList) {
				// 【1】判断Term长度；【2】判断Term词频；【3】suggestList最大为10.
				if(term.getTerm().length() >= SolrConstants.TERM_MIN_LENGTH && term.getFrequency() >= SolrConstants.TERM_MIN_FREQUENCY ) {
					Map<String, Object> map = new HashMap<String, Object>();
					
					map.put("type", 1); // 商品.
					map.put("key", q); // 搜索关键词.
					map.put("tips", term.getTerm()); // 提示.
					
					// 推荐关联商品.
					suggestList.add(map);
				}
			}
		}
	}
	
	/**
	 * 店铺搜索框自动提示. 
	 * 【备注】Solr中有FST-based和AnalyzingInfix二种suggesters，此处使用FST-based。
	 * 
	 * @param inputQueryString
	 *            输入的搜索字符串.
	 * @author Chengyb
	 * @throws Exception 
	 */
	public List<Object> shopSuggest(HttpSolrClient shopSolrClient, String requestURI, String q, Integer type)
			throws Exception {
		List<Object> suggestList = new ArrayList<Object>();
		
		// 一级分类逻辑处理.
		//【逻辑】一二三级包含当前搜索关键字，则返回当前商品的一级分类.
		// =====================================*/
		List<String> shopSuggestions = autoSuggest(shopSolrClient, "shopSuggester", q + "*");
		if(null != shopSuggestions) {
			for (int i = 0, size = shopSuggestions.size(); i < size; i++) {
				String shopSuggestion = shopSuggestions.get(i);
				if(type == PC) {
					shopSuggestion = "<a target='_blank' href='" + requestURI + "/shop?key=" + ToolWeb.HtmltoText(shopSuggestion) + "'>" + shopSuggestion + "</a>";
					
					// 推荐关联店铺.
					suggestList.add(shopSuggestion);
				} else {
					Map<String, Object> map = new HashMap<String, Object>();
					
					map.put("type", 2); // 店铺.
					map.put("key", q); // 搜索关键词.
					map.put("tips", shopSuggestion); // 提示.

					// 推荐关联店铺.
					suggestList.add(map);
				}
			}
		}
		
		return suggestList;
	}
	
	/**
	 * Suggester组件.
	 * 
	 * @param solrClient
	 * @param dictionary
	 *            词典.
	 * @param q
	 *            关键词.
	 * @author Chengyb
	 * @return 
	 * @throws Exception
	 */
	public List<String> autoSuggest(HttpSolrClient solrClient, String dictionary, String q) throws Exception {

		SolrQuery query = new SolrQuery("*:*");
		query.set(CommonParams.QT, "/suggest");
		query.set("suggest", true);
		if(null != dictionary) {
			query.set("suggest.dictionary", dictionary);
		}
		query.set("suggest.q", q);
		
		QueryResponse queryResponse = solrClient.query(query);
		SuggesterResponse suggesterResponse = queryResponse.getSuggesterResponse();
	    Map<String, List<Suggestion>> suggestions = suggesterResponse.getSuggestions();
	    List<Suggestion> suggestionList = suggestions.get(dictionary);
	    
	    if(suggestionList.size() > 0) {
	    	List<String> list = new ArrayList<String>();
	    	Suggestion suggestion = suggestionList.get(0);
	    	list.add(suggestion.getTerm());
	    	return list;
	    }
	    return null;
	}
	
	/**
	 * Terms组件.
	 * 
	 * @param solrClient
	 * @param prefix
	 * @param fl
	 *            从哪些字段中获取Terms.
	 * @param regex
	 * @param limit
	 *            最大返回的Terms记录数.默认为10，如果<0，则返回所有Terms.
	 * @author Chengyb
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public Map<String, List<Term>> term(HttpSolrClient solrClient, String prefix, String[] fl, String regex, Integer limit) throws SolrServerException, IOException {
		// 创建查询参数以及设定的查询参数.
    	SolrQuery params = new SolrQuery("*:*");
    	params.set(CommonParams.QT, "/terms");

    	// parameters settings for terms requestHandler
    	// 参考（refer to）http://wiki.apache.org/solr/TermsComponent
    	// Turn on the TermsComponent
    	params.set(CommonParams.TERMS, true);
    	
    	// Required. The name of the field to get the terms from. May be specified multiple times as terms.fl=field1&terms.fl=field2...
    	if(fl != null) {
    		for (int i = 0, size = fl.length; i < size; i++) {
        		params.set("terms.fl", fl[i]);
        	}
    	}

    	// Optional. The term to start at. If not specified, the empty string is used, meaning start at the beginning of the field.
//    	params.set("terms.lower", ""); // term lower bounder开始的字符
    	
    	// Optional. Include the lower bound term in the result set. Default is true.
    	params.set("terms.lower.incl", true);
    	
    	// Optional. The minimum doc frequency to return in order to be included. Results are inclusive of the mincount (i.e. >= mincount)
    	params.set("terms.mincount", "1");
    	
    	// Optional. The maximum doc frequency. Default is -1 to have no upper bound. Results are inclusive of the maxcount (i.e. <= maxcount)
//    	params.set("terms.maxcount", "100");

    	//http://localhost:8983/solr/terms?terms.fl=text&terms.prefix=学 // using for auto-completing 
    	// Optional. Restrict matches to terms that start with the prefix.
    	if(!StringUtil.isBlank(prefix)) {
    		params.set("terms.prefix", prefix);
    	}
    	
    	// Optional. Restrict matches to terms that match the regular expression.
    	if(!StringUtil.isBlank(regex)) {
        	params.set("terms.regex", regex); // 示例: "小+.*"
    	}
    	
    	// Optional. Flags to be used when evaluating the regular expression defined in the "terms.regex" parameter. This parameter can be defined multiple times (each time with different flag)
    	params.set("terms.regex.flag", "case_insensitive"); 

    	// The maximum number of terms to return. The default is 10. If < 0, then include all terms.
    	params.set("terms.limit", limit);
    	
    	// The term to stop at. Either upper or terms.limit must be set.
//    	params.set("terms.upper", ""); //结束的字符 
    	
    	// Include the upper bound term in the result set. Default is false.
    	params.set("terms.upper.incl", false); 
    	
    	// If true, return the raw characters of the indexed term, regardless of if it is human readable. For instance, the indexed form of numeric numbers is not human readable. The default is false.
    	params.set("terms.raw", false);
    	
    	// If count, sorts the terms by the term frequency (highest count first). If index, returns the terms in index order. Default is to sort by count.
    	params.set("terms.sort", "count"); // 值可以为count或index. 

    	//查询并获取相应的结果！ 
    	QueryResponse response = solrClient.query(params); 

		// 获取相关的查询结果
		if (null != response) {
			L.info("查询耗时：" + response.getQTime() + "（ms）");
			// System.out.println(response.toString());

			TermsResponse termsResponse = response.getTermsResponse();
			if (null != termsResponse) {
				Map<String, List<TermsResponse.Term>> termMap = termsResponse.getTermMap();

				for (Map.Entry<String, List<TermsResponse.Term>> termsEntry : termMap.entrySet()) {
					System.out.println("Field Name: " + termsEntry.getKey());
					List<TermsResponse.Term> termList = termsEntry.getValue();
					System.out.println("Term : Frequency");
					for (TermsResponse.Term term : termList) {
						System.out.println(term.getTerm() + " : " + term.getFrequency());
					}
					System.out.println();
				}
				return termMap;
			}
		}
		return null;
    }
	
	/** 
	 * 拼写纠错
	 *   
	 * @param query
	 * @param spellRespose
	 * @return
	 * @throws SolrServerException
	 */
//    public String checkSpellKey(SolrQuery query, QueryResponse spellRespose) throws SolrServerException {  
//        String lastWord = "";  
//        SpellCheckResponse spellCheckResponse = spellRespose.getSpellCheckResponse();  
//        List<Suggestion> suggestionList = spellCheckResponse.getSuggestions();  
//  
//        if (suggestionList.size() > 0) {  
//            if (!spellCheckResponse.isCorrectlySpelled()) {  
//                lastWord = suggestionList.get(0).getAlternatives().toString().replace("[", "").replace("]", "");  
//                if (lastWord.indexOf(',') > 0) {  
//                    lastWord = lastWord.split(",")[0];  
//                }  
//            }  
//  
//            query.set("q", lastWord);  
//        }  
//        return lastWord;
//    }
    
	/**
	 * 记录Solr查询时间信息.
	 * 
	 * @param response
	 */
	private void logQueryTimeInfo(QueryResponse response) {
		int qTime = response.getQTime();
		long elapsedTime = response.getElapsedTime();
		L.info("Solr内查询的时间: " + qTime + " ms, 查询时间（还包含传输、序列化或反序列化等时间）: " + elapsedTime + " ms.");
	}

}