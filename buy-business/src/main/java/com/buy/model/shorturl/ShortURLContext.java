package com.buy.model.shorturl;

import java.io.File;

import com.buy.common.BaseConstants;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

public class ShortURLContext {

	/**
	 * Redis配置.
	 */
	public static Prop redisProperties;

	/**
	 * 每个ip的操作间隔，单位：秒
	 */
	public static Integer INTERVAL = null;

	/**
	 * id的长度
	 */
	public static Integer IDLEN = null;

	/**
	 * id的重试次数
	 */
	public static Integer IDCOUNT = null;

	/**
	 * id的默认寿命
	 */
	public static Integer IDLIFE = null;

	static {
		initConf();
	}

	private static void initConf() {
		File redisFile = new File(BaseConstants.CONFIG_PATH + "redis.properties");
		if (redisFile.exists()) {
			System.out.println("加载服务器配置。。。。");
			redisProperties = PropKit.use(redisFile);
		} else {
			System.out.println("加载本地配置。。。。");
			redisProperties = PropKit.use("redis.properties");
		}
		try {
			INTERVAL = Integer.valueOf(redisProperties.get("sys.interval"));
			IDLEN = Integer.valueOf(redisProperties.get("sys.id.len"));
			IDCOUNT = Integer.valueOf(redisProperties.get("sys.id.count"));
			IDLIFE = Integer.valueOf(redisProperties.get("sys.id.life"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}