package com.buy.plugin.solr;

import java.util.List;
import java.util.Map;

/**
 * 基类page中包含公有翻页参数及保存查询到的结果以被页面遍历
 * 被子类继承后将增加不同的查询条件
 * 
 * @author chengyb
 */
public class Page {

	// 当前页码，从1开始计.
    private Integer pageNumber;

	// 每页条数.
	private Integer pageSize;
	
	// 总页数.
	private Integer totalPage;
	
	// 总条数.
	private Integer totalRow;

	// 查询参数.
	private Map<String, Object> conditions;
	
	// Facets数据.
	private Map<String, List<String>> facetFields;

	// 当前页数据.
	private List datas;

	public Page() {
	}
	
	public Page(Map<String, List<String>> facetFields, List datas, Integer pageNumber, Integer pageSize, Integer totalPage, Integer totalRow) {
		this.facetFields = facetFields;
		this.datas = datas;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalPage = totalPage;
		this.totalRow = totalRow;
		
//		getStart();
//		getHasPrevious();
//		getHasNext();
	}

	// 获取当前页码.
	public int getPageNumber() {
		return pageNumber;
	}

	// 设置当前页码.
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	// 获取每页显示条数.
	public int getPageSize() {
		return pageSize;
	}

	// 设置每页显示条数.
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	// 获取查询参数.
	public Map<String, Object> getConditions() {
		return conditions;
	}

	// 设置查询参数.
	public void setConditions(Map<String, Object> conditions) {
		this.conditions = conditions;
	}

	//======================================
	// Facet Fields数据.
	//======================================*/
	public Map<String, List<String>> getFacetFields() {
		return facetFields;
	}

	public void setFacetFields(Map<String, List<String>> facetFields) {
		this.facetFields = facetFields;
	}
	
	// 获取当前页数据.
	public List<Object> getDatas() {
		return datas;
	}

	// 设置当前页数据.
	public void setDatas(List<Object> datas) {
		this.datas = datas;
	}

	// 获取总条数.
	public long getTotalRow() {
		return totalRow;
	}

	// 设置总条数.
	public void setTotalRow(Integer totalRow) {
		this.totalRow = totalRow;
	}

	// 获取从第几条数据开始查询.
	public long getStart() {
		return (pageNumber - 1) * pageSize;
	}

	// 判断是否还有前一页.
	public boolean getHasPrevious() {
		return 1 == pageNumber ? false : true;
	}

	// 判断是否还有后一页.
	public boolean getHasNext() {
		return pageNumber + 1 <= getTotalPage() ? true : false;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

}