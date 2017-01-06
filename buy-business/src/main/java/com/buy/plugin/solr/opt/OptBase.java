package com.buy.plugin.solr.opt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class OptBase {

	private static final int SO_TIMEOUT = 10000;
	private static final int CONNECTION_TIMEOUT = 5000;
	private static final int MAX_PER_HOST = 5000;
	private static final int MAX_CONNECTIONS = 200;

	public static HttpSolrClient createServer(String solrUrl) {
		HttpSolrClient solr = null;
		if (null == solr) {
			solr = new HttpSolrClient(solrUrl);
			solr.setSoTimeout(SO_TIMEOUT);
			solr.setConnectionTimeout(CONNECTION_TIMEOUT);
			solr.setDefaultMaxConnectionsPerHost(MAX_PER_HOST);
			solr.setMaxTotalConnections(MAX_CONNECTIONS);
			solr.setFollowRedirects(false);
			solr.setAllowCompression(true);
			solr.setMaxRetries(1);
			// SolrJ允许您以二进制格式上传内容而不是默认的XML格式。使用下面的代码来使用二进制格式上传,SolrJ用相同的格式来获取结果。
			solr.setRequestWriter(new BinaryRequestWriter());
		}
		return solr;
	}

	/**
	 * 处理Solr返回的高亮字段
	 * 
	 * @param documentList
	 *            Solr文档列表
	 * @param map
	 * @return
	 */
	public List<Map<String, Object>> convertDocumentList(SolrDocumentList documentList,
			Map<String, Map<String, List<String>>> map) {
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		Map<String, Object> item;
		for (SolrDocument document : documentList) {
			item = new HashMap<String, Object>();
			for (String fieldName : document.getFieldNames()) {
				item.put(fieldName, document.getFieldValue(fieldName));
			}
			// 将高亮字段放入map中
			if (null != map) {
				/*=============================================================
				  Solr返回的"highlighting"格式如下:
				  "1": {
                         "productName": [
                                                    "小米 4 2GB内存版 白色 移动4G<em>手机</em>"
                                                   ]
                        },
                 "2": {
                         "productName": [
                                                    "Apple iPhone 6 (A1586) 16GB 金色 移动联通电信4G<em>手机</em>"
                                                   ]
                       }
				=============================================================*/
				Map<String, List<String>> highLightMap = map.get(document.get("id"));
				for (Map.Entry<String, List<String>> entry : highLightMap.entrySet()) {
					 List<String> list = entry.getValue();
					 if(!list.isEmpty()) {
						 item.put(entry.getKey(), list.get(0));
					 }
				}
			}
			datas.add(item);
		}
		return datas;
	}
}