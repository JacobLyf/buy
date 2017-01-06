package com.buy.plugin.solr.opt;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class SolrUpdate extends OptBase {
	private HttpSolrClient server;

	public SolrUpdate(String solrUrl) {
		System.out.println("Create a SolrUpdate ---> 【 " + solrUrl + " 】");
		this.server = createServer(solrUrl);
	}

	public void addOrUpdateDocuments(List<Map<String, Object>> docList) {
		try {
			SolrInputDocument document;
			for (Map<String, Object> map : docList) {
				document = new SolrInputDocument();
				for (String key : map.keySet()) {
					document.addField(key, map.get(key));
				}
				server.add(document);
			}
			server.commit();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 添加或者更新文档
	 * 
	 * @param map
	 */
	public void addOrUpdateDocument(Map<String, Object> map) {
		try {
			SolrInputDocument document = new SolrInputDocument();

			for (String key : map.keySet()) {
				document.addField(key, map.get(key));
			}
			server.add(document);
			server.commit();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteDocumentByQuery(String query) {
		try {
			server.deleteByQuery(query);
			server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteDocumentByIds(List<String> ids) {
		try {
			server.deleteById(ids);
			server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteDocumentById(String id) {
		try {
			server.deleteById(id);
			server.commit();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
