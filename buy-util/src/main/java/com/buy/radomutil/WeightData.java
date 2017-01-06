package com.buy.radomutil;

/**
 * 权重数据
 * @author Eriol
 *
 */
public class WeightData {  
    private String data;  //数据表示
    private Integer weight;  //权重数
      
  
    public WeightData() {  
        super();  
    }  
    /**
     * 初始化数据表示及权重
     * @param data
     * @param weight
     */
    public WeightData(String data, Integer weight) {  
        super();  
        this.setData(data);  
        this.setWeight(weight);  
    }  
  
  
    public Integer getWeight() {  
        return weight;  
    }  
  
    public void setWeight(Integer weight) {  
        this.weight = weight;  
    }  
  
    public String getData() {  
        return data;  
    }  
  
    public void setData(String data) {  
        this.data = data;  
    }  
} 