package com.buy.service.product;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.buy.common.JsonMessage;
import com.buy.model.product.ProEvaluate;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

public class BaseProEvaluateService {

	/**
	 * 评论批量添加
	 * @param list
	 * @param userId
	 * @param isPublic
	 * @param orderId
	 * @param dataFrom
	 * @return
	 */
	@Before(Tx.class)
	public JsonMessage batchSaveOrdinary(List<JSONObject> list, String userId, Integer isPublic, String orderId, String dataFrom) {
		return ProEvaluate.dao.addOrdinaryEvaluate(list, userId, isPublic, orderId, dataFrom);
	}
	
	/**
	 * 评论批量添加
	 * @param list
	 * @param userId
	 * @param isPublic
	 * @param orderId
	 * @param dataFrom
	 * @return
	 */
	@Before(Tx.class)
	public JsonMessage batchSaveOrdinary(Integer[] detailIds, Integer[] proIds, Integer[] praiseRadios, 
			String[] contents, List<String[]> imgPathList, String userId, Integer isPublic, String orderId, String dataFrom) {
		return ProEvaluate.dao.addOrdinaryEvaluate(detailIds, proIds, praiseRadios, contents, imgPathList, userId, isPublic, orderId, dataFrom);
	}
}
