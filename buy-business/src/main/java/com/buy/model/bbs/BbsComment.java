package com.buy.model.bbs;

import com.jfinal.plugin.activerecord.Model;

/**
 * Model - BBS - 帖子回复
 */
public class BbsComment extends Model<BbsComment> {

	private static final long serialVersionUID = 1L;
	
	public static final BbsComment dao = new BbsComment();

}
