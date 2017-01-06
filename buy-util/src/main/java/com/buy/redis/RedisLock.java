package com.buy.redis;

import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * Redis实现锁.
 * 
 * @author Chengyb
 */
public class RedisLock {
	
	/**
	 * 申请锁
	 * 
	 * @param jedis
	 *            redis连接
	 * @param lockName
	 *            锁名称
	 * @param acquireTimeout
	 *            申请超时时间,ms
	 * @param lockTimeout
	 *            锁超时时间,s
	 * @return
	 */
	public static String acquireLock(Jedis jedis, String lockName, long acquireTimeout, int lockTimeout) {
		String identifier = UUID.randomUUID().toString(); // 锁随机标识符
		long acquiresEnd = System.currentTimeMillis() + acquireTimeout;
		lockName = "lock:" + lockName;
		while (System.currentTimeMillis() < acquiresEnd) {
			if (jedis.setnx(lockName, identifier) == 1) { // 尝试获取锁
				jedis.expire(lockName, lockTimeout);
				return identifier;
			}
		}
		return "";
	}

	/**
	 * 释放锁
	 * 
	 * @param jedis
	 *            redis连接
	 * @param lockName
	 *            锁名称
	 * @param identifier
	 *            锁标识符
	 * @return
	 */
	public static boolean releaseLock(Jedis jedis, String lockName, String identifier) {
		Pipeline pipeline = jedis.pipelined();
		lockName = "lock:" + lockName;
		while (true) {
			try {
				pipeline.watch(lockName);
				if (pipeline.get(lockName).equals(identifier)) { // 检查线程是否仍然持有锁
					pipeline.multi(); // 释放锁
					pipeline.del(lockName);
					if (pipeline.exec() != null) {
						return true;
					}
				}
				break;
			} catch (Exception e) { // 其他客户端修改了锁
				System.out.println("release failed");
				return false;
			}
		}
		return false;
	}
	
}