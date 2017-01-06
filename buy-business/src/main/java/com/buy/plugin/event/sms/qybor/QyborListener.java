package com.buy.plugin.event.sms.qybor;

import java.util.List;

import com.buy.common.BaseConstants;
import com.buy.model.SysParam;
import com.buy.model.shop.Shop;
import com.buy.model.sms.SMS;
import com.buy.model.user.User;
import com.buy.service.sms.qybor.QyborService;
import com.buy.string.StringUtil;
import com.jfinal.aop.Duang;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import net.dreamlu.event.core.ApplicationListener;
import net.dreamlu.event.core.Listener;

@Listener (enableAsync = true)
public class QyborListener implements ApplicationListener<QyborEvent>
{

	@Override
	public void onApplicationEvent(QyborEvent event)
	{
		QyborService service = Duang.duang(QyborService.class);
		
		/*
		 * 验证 - 群发短信资料
		 */
		Record source = (Record) event.getSource();
		if (null == source)
			return;
		
		/*
		 * 验证 - 短信是否存在
		 */
		SMS sms = null;
		Integer smsId = source.getInt("smsId");
		if (null == smsId)
			return;
		else
			sms = SMS.dao.findFirst("SELECT * FROM t_sms_record WHERE id = ?", smsId);
		if (null == sms)
			return;
		
		/*
		 * 验证 - 用户身份 I
		 */
		Integer userType = source.getInt("userType");
		if (null == userType)
		{
			service.updateBySmsFail(sms, "不存在用户类型");
			return;
		}
		
		/*
		 * 验证 - 用户身份 II
		 */
		int status = BaseConstants.YES;
		StringBuffer sql = new StringBuffer(" SELECT DISTINCT mobile FROM ");
		String templateCode = "";
		boolean flag = true;
		switch (userType)
		{
			// 验证 - 会员身份 - 会员
			case User.FRONT_USER:
				sql.append(" t_user ");
				templateCode = "user_all_sms";
				break;
				
			// 验证 - 会员身份 - 店铺
			case User.FRONT_USER_SHOP:
				sql.append(" t_shop ");
				status = Shop.STATUS_ACTIVATED;
				templateCode = "shop_all_sms";
				break;
				
			// 验证 - 会员身份 - 代理商
			case User.FRONT_USER_AGENT:
				sql.append(" t_agent ");
				templateCode = "agent_all_sms";
				break;
				
			// 验证 - 会员身份 - 供货商
			case User.FRONT_USER_SUPPLIER:
				sql.append(" t_supplier ");
				templateCode = "supplier_all_sms";
				break;
			
			// 验证 - 会员身份 - 没有用户身份
			default:
				service.updateBySmsFail(sms, "不存在用户类型");
				flag = false;
				break;
		}
		
		/*
		 * 发短信
		 */
		if (flag)
		{
			// 查询用户电话
			sql.append(" WHERE 1 = 1 ");
			sql.append(" AND mobile IS NOT NULL ");
			sql.append(" AND LENGTH(mobile) = 11 ");
			sql.append(" AND status = ? ");
			
			Integer isDev = SysParam.dao.getIntByCode("is_dev");
			if (isDev == 0)
			{
				String testMobileStr = SysParam.dao.getStrByCode("test_mobile");
				if (testMobileStr.indexOf(",") < 0)
					testMobileStr = "'" + testMobileStr + "'";
				else
					testMobileStr = StringUtil.arrayToStringForSql(",", testMobileStr.split(","));
				sql.append(" AND mobile IN (" + testMobileStr +  ") ");
			}
			List<String> phones = Db.query(sql.toString(), status);
			if (StringUtil.isNull(phones) || (StringUtil.notNull(phones)) && phones.size() == 0)
			{
				service.updateBySmsFail(sms, "该用户类型没有用户数据");
				return;
			}
			
			service.updateBySms(sms, templateCode, phones);
		}
	}
	
}
