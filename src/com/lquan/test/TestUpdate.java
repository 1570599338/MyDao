package com.lquan.test;

import java.util.List;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lquan.Bean.Account;
import com.lquan.Bean.User;

import snt.common.dao.base.AutoAssembleConfig;
import snt.common.dao.base.CommonDAO;

/**
 * 此方法主要测试update方法
 * @author lquan
 *
 */
@SuppressWarnings("unused")
public class TestUpdate {
	
	static CommonDAO commonDao;
	static{
		AutoAssembleConfig acon = new AutoAssembleConfig();
		ApplicationContext context = new ClassPathXmlApplicationContext("ctxconf/applicationContext4Test.xml");
		 commonDao = (CommonDAO)context.getBean("commonDAO");
	}
	
	@Test
	public  void testQueryNoargs() {
		
		String sql = "select * from t_accounts where pk_id=1";
		List<Account> list = commonDao.queryForPojoList(sql, Account.class);
		for(Account u:list){
			System.out.println("修改前的数据："+ u.getDealer_name());
		}
		
		String sqlupdate ="update t_accounts set dealer_name='adminxx' where pk_id=1";
		
		int a = commonDao.update(sqlupdate);
		System.out.println("修改： "+a);
		
		List<Account> listx = commonDao.queryForPojoList(sql, Account.class);
		for(Account u:listx){
			System.out.println("修改后的数据："+ u.getDealer_name());
		}
		
		
		
		
	}
	
	

}
