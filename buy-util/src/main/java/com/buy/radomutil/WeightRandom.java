package com.buy.radomutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 根据权重随机获取结果
 * @author Eriol
 *
 */
public class WeightRandom {  
	private static Random random = new Random();
	
	/**
	 * 根据权限数据随机获取并返回相应结果
	 * @param weightDatas
	 * @return
	 * @throws
	 * @author Eriol
	 * @date 2016年6月20日下午6:27:16
	 */
    public static String getWeigthData(List<WeightData> weightDatas) { 
          Integer weightSum = 0; 
          //计算权重总和
          for (WeightData wd : weightDatas) {  
              weightSum += wd.getWeight();  
          }  
  
          if (weightSum <= 0) {  
            System.err.println("权重总和不能小于0");  
           	return null;  
          }  
          //在权限总和区间内获取随机数
          Integer n = random.nextInt(weightSum); // n in [0, weightSum)  
          Integer m = 0;  
          for (WeightData wd : weightDatas) {  
        	   //随机数在范围内则返回该范围的所属的数据
               if (m <= n && n < m + wd.getWeight()) {  
                 return wd.getData();  
               }  
               m += wd.getWeight();  
          }
          
		 return null;  
  
    }  
  
    public static void main(String[] args) {
    	List<WeightData>  categorys = new ArrayList<WeightData>();  
    	WeightData wc1 = new WeightData("A",50);  
    	WeightData wc2 = new WeightData("B",35);  
    	WeightData wc3 = new WeightData("C",15);  
    	//按这个顺序加入，则c=0-14，b=15-49,c=50-99
    	categorys.add(wc3); 
        categorys.add(wc2);  
        categorys.add(wc1);  
        
        //按这个顺序加入，则a=0-49，b=50-84,c=85-99
    	/*categorys.add(wc1); 
        categorys.add(wc2);  
        categorys.add(wc3); */ 
        String s = WeightRandom.getWeigthData(categorys);
        System.out.println(s);
	}
}  