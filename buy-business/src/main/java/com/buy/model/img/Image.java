package com.buy.model.img;  

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;


public class Image extends Model<Image> {
	
	
	private static final long serialVersionUID = 1L;
	/**
	 * 类型：头像
	 */
	public static final int TYPE_AVATAR = 1;
	/**
	 * 类型：广告
	 */
	public static final int TYPE_AD = 2;
	/**
	 * 类型：分类
	 */
	public static final int TYPE_SORT = 3;

	/**
	 * 类型：品牌
	 */
	public static final int TYPE_BRAND = 4;
	/**
	 * 类型：证件
	 */
	public static final int TYPE_CREDENTIALS = 5;
	
	/**
	 * 类型：商品属性
	 */
	public static final int TYPE_PRO_PROPERTY = 6;
	/**
	 * 类型：评论
	 */
	public static final int TYPE_COMMOND = 7;
	
	/**
	 * 类型：e趣社区
	 */
	public static final int TYPE_BBS = 8;
	/**
	 * 类型：e趣学院
	 */
	public static final int TYPE_COLLEGE = 9;
	/**
	 * 类型：e趣云店信息
	 */
	public static final int TYPE_O2O_STORE = 10;
	/**
	 * 类型：嗮单
	 */
	public static final int TYPE_SHOW_ORDER = 11;
	/**
	 * 类型：LOGO
	 */
	public static final int TYPE_LOGO = 12;
	/**
	 * 类型：店铺模板
	 */
	public static final int TYPE_SHOP_TEMPLATE = 13;
	
	/**
	 * 状态：启用
	 */
	public static int STATUS_ENABLE = 1;
	/**
	 * 状态：禁用
	 */
	public static int STATUS_DISABLE= 0;
	
	public static final Image dao = new Image();
	/**
	 * 图片有效
	 * @param paths
	 * @author huangzq
	 */
	public void enable(String... paths){
		
		String sql = "update  t_image set status = ? where path in (";
		for(String path : paths){
			sql+= "'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,STATUS_ENABLE);
	}
	/**
	 * 图片无效
	 * @param paths
	 * @author huangzq
	 */
	public void disable(String... paths){
		
		String sql = "update  t_image set status = ? where path in (";
		for(String path : paths){
			sql+= "'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,STATUS_DISABLE);
	}
	
	
	/**
	 * 通过id更新图片所属
	 * @param imageId
	 * @param targetId
	 * @author huangzq
	 *//*
	public void update(Integer type,String targetId,Integer... imageId){
	
		String sql = "update  t_image set target_id= ? ,type = ? where id in (";
		for(Integer path : imageId){
			sql+=path+",";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,targetId,type);
	}
	*//**
	 * 通过路径更新图片所属
	 * @param type
	 * @param targetId
	 * @param imgPath
	 * @author huangzq
	 *//*
	public void update(Integer type,String targetId,String... imgPath){
	
		String sql = "update  t_image  set target_id=? , type = ? where path in (";
		for(String path : imgPath){
			sql+="'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,targetId,type);
	}
	*//**
	 * 逻辑删除图片
	 * @param type
	 * @param targetId
	 * @author huangzq
	 *//*
	public void delete(Integer type  ,String... imgPath){
		String sql = "update  t_image t set target_id='' where type = ? and t.path in (";
		for(String path : imgPath){
			sql+="'"+path+"',";
		}
		sql = sql.substring(0,sql.length()-1 );
		sql+=")";
		Db.update(sql,type);
		
	}
	*//**
	 * 逻辑删除图片
	 * @param type
	 * @param targetId
	 * @author huangzq
	 *//*
	public void delete(Integer type  ,String targetId){
		
		Db.update("update  t_image t set target_id='' where type = ? and target_id = ?" ,type,targetId);
		
	}
	*//**
	 * 通过类型和路径查找图片
	 * @param type
	 * @param path
	 * @return
	 * @author huangzq
	 *//*
	public Image findByTypeAndPath(Integer type ,String path){
		return  Image.dao.findFirst("select * from t_image t where t.type = ? and t.path = ? ",type,path);
	}*/
	
	
	
	

}
      