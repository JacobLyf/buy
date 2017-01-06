package com.buy.model.shorturl;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import com.buy.common.BaseConstants;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import redis.clients.jedis.Jedis;

public class Mapper {

	/**
	 * 当前redis服务器的Cache对象
	 */
	private static Cache CACHE = Redis.use(BaseConstants.Redis.CACHE_SHORT_URL);

	/**
	 * 生成ID时的种子（小写）
	 */
	private static final String SEED = "0123456789abcdefghijklmnopqrstuvwxyz";

	/**
	 * 从池中获取一个jedis对象
	 * 
	 * @return jedis对象
	 */
	public static Jedis getJedis() {
		return CACHE.getJedis();
	}

	/**
	 * 将一个jedis对象还给连接池
	 * 
	 * @param jedis
	 *            jedis对象
	 */
	public static void closeJedis(Jedis jedis) {
		CACHE.close(jedis);
	}

	/**
	 * 判断字符串不是空白的
	 * 
	 * @param s
	 *            被判断的字符串
	 * @return 不是空白的返回true，空白的返回false
	 */
	public static Boolean isNotBlank(String s) {
		return (s != null) && (s.trim().length() > 0);
	}

	/**
	 * 判断两个字符串是否相等、相同
	 * 
	 * @param s1
	 *            字符串1
	 * @param s2
	 *            字符串2
	 * @return 相等返回true，不相等返回false
	 */
	public static Boolean equals(String s1, String s2) {
		return (s1 != null) && (s1.equals(s2));
	}

	public static java.util.Map<String, Object> interesting() {
		java.util.Map<String, Object> map = new HashMap<String, Object>();
		Random random = new Random();
		map.put("code", random.nextInt(900) + 100);
		map.put("data", UUID.randomUUID().toString().replace("-", ""));
		return map;
	}

	/**
	 * 判断是否是URL地址，注意URL地址必须携带“http://”或“https://”
	 * 
	 * @param url
	 *            URL地址(大小写不敏感)
	 * @return 判断结果
	 */
	public static Boolean isUrl(String url) {
		Pattern pattern = Pattern.compile("(?i)^((https|http)?:\\/\\/)[^\\s]+");
		return pattern.matcher(url).matches();
	}

	/**
	 * id生成器，根据指定的长度和重试次数生成不重复的id
	 * 
	 * @param len
	 *            id的长度
	 * @param count
	 *            重试次数
	 * @return url的id（小写）
	 */
	private static String idGenerater(int len, int count) {
		StringBuilder builder = new StringBuilder();
		Jedis jedis = getJedis();
		Random random = new Random();
		int seedlen = SEED.length();

		for (int i = 0; i < count; ++i) {
			builder.setLength(0);
			for (int j = 0; j < len; ++j) {
				builder.append(SEED.charAt(random.nextInt(seedlen)));
			}
			if (!isNotBlank(jedis.get(builder.toString()))) {
				// 在redis中没找到该id
				closeJedis(jedis);
				return builder.toString();
			}
		}
		closeJedis(jedis);
		// 在count次重试id生成，生成的id都已经存在
		throw new RuntimeException("id生成失败，增加id长度或增加重试次数也许可以解决此问题！");
	}

	/**
	 * 添加远程主机到redis，限制其频繁访问
	 * 
	 * @param remoteHost
	 *            远程主机
	 * @return 操作的状态码
	 */
	public String addRemoteHost(String remoteHost) {
		Jedis jedis = getJedis();
		String result = jedis.setex(remoteHost, ShortURLContext.INTERVAL, remoteHost);
		closeJedis(jedis);
		return result;
	}

	/**
	 * remoteHost是否在redis中
	 * 
	 * @param remoteHost
	 *            远程主机
	 * @return 在redis中返回true，不在返回false
	 */
	public Boolean existRemoteHost(String remoteHost) {
		Jedis jedis = getJedis();
		Boolean result = equals(jedis.get(remoteHost), remoteHost);
		closeJedis(jedis);
		return result;
	}

	/**
	 * 添加url（带时限的），在s秒后自动消失
	 * 
	 * @param url
	 *            url字符串（执行前务必先判断是否为正确的url地址）
	 * @param s
	 *            时限，单位秒，不能为null，为0指一添加就消失
	 * @return id-url映射对象
	 */
	private static Map addUrl(String url, Integer s) {
		Jedis jedis = getJedis();
		String id = idGenerater(ShortURLContext.IDLEN, ShortURLContext.IDCOUNT);
		jedis.setex(id, s, url);
		jedis.setex(url, s, id);
		closeJedis(jedis);
		return new Map(id, url);
	}

	/**
	 * 根据ID或URL查询这个ID-URL映射对象
	 * 
	 * @param key
	 *            ID或URL的值
	 * @return ID-URL映射对象，查不到则为null
	 */
	public static Map queryMapperByIdOrUrl(String key) {
		Jedis jedis = getJedis();
		if (isUrl(key)) {
			// 说明key为url
			String value = jedis.get(key);
			closeJedis(jedis);
			return isNotBlank(value) ? new Map(value, key) : null;
		} else {
			// 说明key为id
			key = key.toLowerCase();
			String value = jedis.get(key);
			closeJedis(jedis);
			return isNotBlank(value) ? new Map(key, value) : null;
		}
	}
	
	/**
	 * 获取URL的短链接Key.(如果不存在,则创建)
	 * 
	 * @param url
	 *            ID或URL的值
	 * @return ID-URL映射对象，查不到则为null
	 */
	public static String createShortUrlIfNotExsit(String url) {
		Map map = queryMapperByIdOrUrl(url);
		if(null == map) {
			map = addUrl(url, ShortURLContext.IDLIFE);
		}
		return map.getId().toString();
	}

	/**
	 * 添加一条记录（这条记录的寿命为配置文件中的时间）
	 * 
	 * @param url
	 *            要添加的url地址，恶意用户也许会输入id，但是还是会被过滤掉的
	 * @param remoteHost
	 *            请求进行这个操作的那台电脑的IP
	 * @return 操作成功，返回id-url映射对象，操作失败，返回null
	 */
	public Map addMapperByUrlAndRHost(String url, String remoteHost) {
		// 判断是否是一个正确的URL地址 || 判断是否为频繁操作
		if (!isUrl(url) || existRemoteHost(remoteHost)) {
			return null;
		}
		Map map = queryMapperByIdOrUrl(url);
		if (map != null) {
			// 说明这个url对应的id已经存在
			return map;
		} else {
			addRemoteHost(remoteHost);
			return addUrl(url, ShortURLContext.IDLIFE);
		}
	}

}