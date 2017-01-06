package com.buy.model.shorturl;

import java.util.HashMap;
import java.util.Map;

public class Result {
	
	private Map<String, Object> map = new HashMap<String, Object>();

	public Result(Integer code) {
		map.put("code", code);
	}

	public Result(Integer code, Object data) {
		map.put("code", code);
		map.put("data", data);
	}

	public Map<String, Object> getResult() {
		return map;
	}
	
}