package com.buy.model.agent;

import java.util.Date;

import com.buy.common.Ret;
import com.buy.model.file.EqFile;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;

public class AgentSupplierRecommend extends Model<AgentSupplierRecommend> {

	private static final long serialVersionUID = 1L;
	
	public static final AgentSupplierRecommend dao = new AgentSupplierRecommend();
	
	/**
	 * 0：已提交
	 */
	public static final int STATUS_SUBMIT = 0 ;
	
	/**
	 * 1：同意合作
	 */
	public static final int STATUS_AGREE = 1 ;
	
	/**
	 * 2：己收到资料
	 */
	public static final int STATUS_RECEIVED_DATA = 2 ;
	
	/**
	 * 3：商品资料审核
	 */
	public static final int STATUS_AUDIT_DATA = 3 ;
	
	/**
	 * 4：上架中
	 */
	public static final int STATUS_SHELVING = 4 ;
	
	/**
	 * 5：上架完成
	 */
	public static final int STATUS_SHELVES_COMPLETE = 5 ;
	
	/**
	 * 审核状态：待审核
	 */
	public static final int AUDIT_STATUS_WAIT = 0;
	
	/**
	 * 审核状态：通过
	 */
	public static final int AUDIT_STATUS_PASS = 1;
	
	/**
	 * 审核状态：未通过
	 */
	public static final int AUDIT_STATUS_UNPASS = 2;

	
	/**
	 * 添加代理商引荐厂商记录
	 * @param r
	 */
	@Before(Tx.class)
	public void addRecommendSupplier(Ret r) {
		AgentSupplierRecommend agentSupplierRecommend = new AgentSupplierRecommend();
		String path = r.get("filePath").toString();
		agentSupplierRecommend
				.set("agent_id", r.get("agentId"))
				.set("supplier_name", r.get("supplierName"))
				.set("pro_sort", r.get("productSort"))
				.set("contact", r.get("contact"))
				.set("contact_tel", r.get("contactTel"))
				.set("pro_about", r.get("productAbout"))
				.set("file_path", r.get("filePath"))
				.set("status", AgentSupplierRecommend.STATUS_SUBMIT)
				.set("audit_status", AgentSupplierRecommend.AUDIT_STATUS_WAIT)
				.set("create_time", new Date())
				.save();
		//让文件有效
		EqFile.dao.enable(path);
	}
}
