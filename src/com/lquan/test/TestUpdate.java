package com.lquan.test;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lquan.Bean.Account;
import com.lquan.Bean.User;

import snt.common.dao.base.AutoAssembleConfig;
import snt.common.dao.base.CommonDAO;
import snt.common.dao.base.PrimaryKeyGenerator;

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
	public  void testQueryNoargsInsertData() {
		
		String sql = "select pk_id,login_name,dealer_name,role,emailname,login_pwd from t_accounts";
		List<Account> list = commonDao.queryForPojoList(sql, Account.class);
		for(Account ac:list){
			System.out.println("修改前的数据："+ ac.getDealer_name());
			String sqlinsert ="insert  into t_user(pk_id,log_name,USER_NAME,role,email,password) values(?,?,?,?,?,?) ";
		
			int a = commonDao.update(sqlinsert,new Object[]{ac.getPk_id(),ac.getLogin_name(),ac.getDealer_name(),ac.getRole(),ac.getEmailname(),ac.getLogin_pwd()});
		}
		
		List<Account> listx = commonDao.queryForPojoList(sql, Account.class);
		for(Account u:listx){
			System.out.println("修改后的数据："+ u.getDealer_name());
		}
		
	}
	
	/**
	 * 将参数拼接到SQL语句
	 * 或不带参数的update的执行
	 */
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
	
	
	
	/**
	 * 测试带有冒号的是update的语句
	 */
	@Test
	public void testUpdateColonStyle()throws Exception{
		String sql = "select * from t_user where pk_id=2";
		List<User> list = commonDao.queryForPojoList(sql, User.class);
		for(User u:list){
			System.out.println("修改前的数据："+ u.getUser_name());
		}
		String sqludpate ="update t_user set user_name=:name where pk_id=:id";
		Map<String,Object> argMap = new HashMap<String,Object>();
		argMap.put("name", "张三s");
		argMap.put("id", 2);
		int xx = commonDao.update(sqludpate, argMap);
		String sqlinsert ="insert into t_user(pk_id,log_name,user_name,company,email,password,role)"
				+ " values(:pk_id,:log_name,:user_name,:company,:email,:password,:role)";
		
		Map<String,Object> argMapInsert = new HashMap<String,Object>();
		argMapInsert.put("pk_id", PrimaryKeyGenerator.getLongKey());
		argMapInsert.put("log_name", "insertDemo");
		argMapInsert.put("user_name", "user_name");
		argMapInsert.put("company", "公司");
		argMapInsert.put("email", "110@qq.com");
		argMapInsert.put("password", "123456");
		argMapInsert.put("role", "A");
		int xinsert = commonDao.update(sqlinsert, argMapInsert);
		
		List<User> listx = commonDao.queryForPojoList(sql, User.class);
		for(User u:list){
			System.out.println("修改后的数据："+ u.getUser_name());
		}
	}
	
	
	/**
	 * 测试update(String sql, Object[] args)方法
	 * args	参数
	 */
	@Test
	public void testUpdateArgForObjects(){
		String sql ="update t_user set user_name=? where pk_id=?";
		int a = commonDao.update(sql, new Object[]{"测试data",2});
		System.out.println(a);
		
		String sqlInsert = "insert into t_user(pk_id,log_name,user_name,company,email,password,role) values(?,?,?,?,?,?,?) ";
		int b = commonDao.update(sqlInsert,new Object[]{PrimaryKeyGenerator.getLongKey(),"登录名","用户名X","公司X","111@qq.com","12312","F"});
	}
	
	/**
	 * 测试update(String sql, Object[] args, int[] argTypes)方法
	 * args	参数
	 * argTypes	参数类型
	 */
	@Test
	public void test5(){
		String sql ="update t_user set user_name=? where pk_id=?";
		int a = commonDao.update(sql, new Object[]{"测试dataargTypes",2},new int[]{Types.VARCHAR,Types.INTEGER});
		System.out.println(a);
		
		String sqlInsert = "insert into t_user(pk_id,log_name,user_name,company,email,password,role) values(?,?,?,?,?,?,?) ";
		int b = commonDao.update(sqlInsert,new Object[]{PrimaryKeyGenerator.getLongKey(),"登录名argTypes","用户名XargTypes","公司XargTypes","112@qq.com","12312","F"},
				new int[]{Types.LONGVARBINARY,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR});
		System.out.println(b);
		
	}
	
	
	
	
	
	
	
	

}
