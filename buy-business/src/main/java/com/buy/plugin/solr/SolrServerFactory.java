package com.buy.plugin.solr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.buy.plugin.solr.opt.SolrSearcher;
import com.buy.plugin.solr.opt.SolrUpdate;

public class SolrServerFactory {
	private static Map<String,Object> map = new ConcurrentHashMap<String,Object>();
	public static final String SOLR_SEARCH = "1";
	public static final String SOLR_UPDATE = "2";
	
	public static class SolrServerFactoryHolder{
		private final static SolrServerFactory solrServerFactory = new SolrServerFactory();
	}
	
	public static SolrServerFactory getInstance(){
		return SolrServerFactoryHolder.solrServerFactory;
	}

	/**
	 * @param servers  {{name,url,type},{name,url,type}}
	 */
	public void loadingServers(String[]... servers){
		for(String[] server : servers){
			if(SOLR_SEARCH.equals(server[2])){
				map.put(server[0], new SolrSearcher(server[1]));
			}else if(SOLR_UPDATE.equals(server[2])){
				map.put(server[0], new SolrUpdate(server[1]));			
			}
		}
	}	
	
	public SolrSearcher getSolrSearcher(String serverName){
		Object obj = map.get(serverName);
		if(obj instanceof SolrSearcher){
			return (SolrSearcher)obj;
		}
		return 	null;
	}
	
	public SolrUpdate getSolrUpdate(String serverName){
		Object obj = map.get(serverName);
		if(obj instanceof SolrUpdate){
			return (SolrUpdate)obj;
		}
		return 	null;
	}

}