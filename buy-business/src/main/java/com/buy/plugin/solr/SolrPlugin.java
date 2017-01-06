package com.buy.plugin.solr;

import com.jfinal.plugin.IPlugin;

/**********************************************************************
 * First you need create this plugin class.
 **********************************************************************/
public class SolrPlugin implements IPlugin {
	private String[][] servers;

	public SolrPlugin(String[][] servers) {
		this.servers = servers;
	}

	@Override
	public boolean start() {
		try {
			SolrServerFactory factory = SolrServerFactory.getInstance();
			factory.loadingServers(servers);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean stop() {
		return false;
	}

}