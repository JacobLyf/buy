package com.buy.common;

import java.sql.SQLException;

import com.jfinal.plugin.activerecord.IAtom;
/**
 * 事务操作
 * @author huangzq
 *
 */
public abstract class MyAtom implements IAtom {
	private Ret ret = null;
	
	public MyAtom(Ret ret){
		this.ret = ret;
	}

	public Ret getRet() {
		return ret;
	}

	public void setRet(Ret ret) {
		this.ret = ret;
	}
	
	

}
