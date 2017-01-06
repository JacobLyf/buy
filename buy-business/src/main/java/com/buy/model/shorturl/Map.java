package com.buy.model.shorturl;

public class Map {
	
	private Object id;
	private Object url;

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getUrl() {
		return url;
	}

	public void setUrl(Object url) {
		this.url = url;
	}

	public Map() {

	}

	public Map(Object id, Object url) {
		this.id = id;
		this.url = url;
	}

}