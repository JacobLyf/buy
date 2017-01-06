package com.buy.numOprate;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MathUtil {
	
	/**
	 * 生成指定长度的数字、字母组合的字符串
	 * @param length
	 * @return
	 */
	public static String getRandomString(int length) { //length表示生成字符串的长度
	    String base = "abcdefghijklmnopqrstuvwxyz0123456789";   
	    Random random = new Random();   
	    StringBuffer sb = new StringBuffer();   
	    for (int i = 0; i < length; i++) {   
	        int number = random.nextInt(base.length());   
	        sb.append(base.charAt(number));   
	    }   
	    return sb.toString();   
	 }
	
	/**
	 * 生成不重复随机数 根据给定的最小数字和最大数字，以及随机数的个数，产生指定的不重复的数组
	 * 
	 * @param begin
	 *            最小数字（包含该数）
	 * @param end
	 *            最大数字（不包含该数）
	 * @param size
	 *            指定产生随机数的个数
	 */
	public int[] generateRandomNumber(int begin, int end, int size) {
		// 加入逻辑判断，确保begin<end并且size不能大于该表示范围
		if (begin >= end || (end - begin) < size) {
			return null;
		}
		// 种子你可以随意生成，但不能重复
		int[] seed = new int[end - begin];

		for (int i = begin; i < end; i++) {
			seed[i - begin] = i;
		}
		int[] ranArr = new int[size];
		Random ran = new Random();
		// 数量你可以自己定义。
		for (int i = 0; i < size; i++) {
			// 得到一个位置
			int j = ran.nextInt(seed.length - i);
			// 得到那个位置的数值
			ranArr[i] = seed[j];
			// 将最后一个未用的数字放到这里
			seed[j] = seed[seed.length - 1 - i];
		}
		return ranArr;
	}

	/**
	 * 生成不重复随机数 根据给定的最小数字和最大数字，以及随机数的个数，产生指定的不重复的数组
	 * 
	 * @param begin
	 *            最小数字（包含该数）
	 * @param end
	 *            最大数字（不包含该数）
	 * @param size
	 *            指定产生随机数的个数
	 */
	public static Integer[] generateBySet(int begin, int end, int size) {
		// 加入逻辑判断，确保begin<end并且size不能大于该表示范围
		if (begin >= end || (end - begin) < size) {
			return null;
		}

		Random ran = new Random();
		Set<Integer> set = new HashSet<Integer>();
		while (set.size() < size) {
			set.add(begin + ran.nextInt(end - begin));
		}

		Integer[] ranArr = new Integer[size];
		ranArr = set.toArray(new Integer[size]);
		// ranArr = (Integer[]) set.toArray();

		return ranArr;
	}

	/**
	 * 生成随机数
	 * @param begin
	 * @param end
	 * @param size
	 * @return
	 * @author chenhg
	 * 2016年8月3日 上午11:00:44
	 */
	public static int generateRandomLength(int begin, int end) {
		int[] seed = new int[end - begin];

		for (int i = begin; i < end; i++) {
			seed[i - begin] = i;
		}
		Random ran = new Random();
		int j = ran.nextInt(seed.length - 0);
		return seed[j];
	}

	// 排序方法
	public static void sort(int[] array) {// 小到大的排序
		int temp = 0;
		for (int i = 0; i < array.length; i++) {
			for (int j = i; j < array.length; j++) {
				if (array[i] > array[j]) {
					temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
	}

	
	

	/**
	 * 阶乘
	 * 
	 * @param n
	 * @return
	 */
	public static int factorial(int n) {
		if (1 == n) {
			return 1;
		}
		return n * factorial(n - 1);
	}

	/**
	 * 平方根算法
	 * 
	 * @param x
	 * @return
	 */
	public static long sqrt(long x) {
		long y = 0;
		long b = (~Long.MAX_VALUE) >>> 1;
		while (b > 0) {
			if (x >= y + b) {
				x -= y + b;
				y >>= 1;
				y += b;
			} else {
				y >>= 1;
			}
			b >>= 2;
		}
		return y;
	}

	private int math_subnode(int selectNum, int minNum) {
		if (selectNum == minNum) {
			return 1;
		} else {
			return selectNum * math_subnode(selectNum - 1, minNum);
		}
	}

	private int math_node(int selectNum) {
		if (0 == selectNum) {
			return 1;
		} else {
			return selectNum * math_node(selectNum - 1);
		}
	}

	/**
	 * 可以用于计算双色球、大乐透注数的方法 selectNum：选中了的小球个数 minNum：至少要选中多少个小球
	 * 比如大乐透35选5可以这样调用processMultiple(7,5); 就是数学中的：C75=7*6/2*1
	 */
	public int processMultiple(int selectNum, int minNum) {
		int result;
		result = math_subnode(selectNum, minNum)
				/ math_node(selectNum - minNum);
		return result;
	}

	/**
	 * 求m和n的最大公约数
	 */
	public static int gongyue(int m, int n) {
		while (0 != m % n) {
			int temp = m % n;
			m = n;
			n = temp;
		}
		return n;
	}

	/**
	 * 求两数的最小公倍数
	 */
	public static int gongbei(int m, int n) {
		return m * n / gongyue(m, n);
	}

	/**
	 * 递归求两数的最大公约数
	 */
	public static int divisor(int m, int n) {
		if (0 == m % n) {
			return n;
		} else {
			return divisor(n, m % n);
		}
	}
	
	/**
	 * 保留scale位小数，并从scale+1位小数向上加1
	 * @param momeny
	 * @param scale
	 * @return
	 * @author Jacob
	 * 2016年1月22日下午1:53:10
	 */
	public static BigDecimal ceilByScale(BigDecimal momeny,Integer scale){
		String scaleSize = "1";
		for(int i=0;i<scale;i++){
			scaleSize += "0";
		}
		momeny = momeny.multiply(new BigDecimal(scaleSize));
		double momenyDouble = Math.ceil(momeny.doubleValue());
		momeny = new BigDecimal(String.valueOf(momenyDouble)).divide(new BigDecimal(scaleSize));
		return momeny;
	}

}
