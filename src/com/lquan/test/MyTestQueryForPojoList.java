package com.lquan.test;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.lquan.Bean.User;

import snt.common.dao.base.AutoAssembleConfig;
import snt.common.dao.base.CommonDAO;
import snt.common.dao.base.PojoResultSetExtractor;
@SuppressWarnings("unchecked")
public class MyTestQueryForPojoList {
	static CommonDAO commonDao;
	static{
		AutoAssembleConfig acon = new AutoAssembleConfig();
		ApplicationContext context = new ClassPathXmlApplicationContext("ctxconf/applicationContext4Test.xml");
		 commonDao = (CommonDAO)context.getBean("commonDAO");
	}

	/**
	 * 无参情况下直接
	 */
	@Test
	public  void testqueryForPojoList() {
		String sql = "select * from t_user ";
		List<User> list = commonDao.queryForPojoList(sql, User.class);
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	@Test
	public  void testqueryForPojoListargMap() {
		String sql = "select * from t_user where pk_id=:pk_id ";
		Map<String , Object> map = new HashMap<String , Object>();
		map.put("pk_id", 1);
		List<User> list = commonDao.queryForPojoList(sql, map,  User.class);
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}

	@Test
	public  void testqueryForPojoListargMapPojoResultSetExtractor() {
		String sql = "select * from t_user where pk_id=:pk_id ";
		Map<String , Object> map = new HashMap<String , Object>();
		map.put("pk_id", "1419592516093");
		List<User> list = commonDao.queryForPojoList(sql, map, new PojoResultSetExtractor( User.class));
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	@Test
	public  void testqueryForPojoListObject_args() {
		String sql = "select * from t_user where pk_id=? ";
		List<User> list = commonDao.queryForPojoList(sql, new Object[]{1}, new PojoResultSetExtractor( User.class));
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	
	@Test
	public  void testqueryForPojoListObject_argTypes() {
		String sql = "select * from t_user where pk_id=? ";
		List<User> list = commonDao.queryForPojoList(sql, new Object[]{2}, new int[] {Types.INTEGER}, User.class);
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	
	@Test
	public  void testqueryForPojoListObject_argTypes_pojoResultSetExtractor() {
		String sql = "select * from t_user where pk_id=? ";
		List<User> list = commonDao.queryForPojoList(sql, new Object[]{2}, new int[] {Types.INTEGER}, new PojoResultSetExtractor(User.class));
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	@Test
	public  void testqueryForPojoListObject_pojoResultSetExtractor() {
		String sql = "select * from t_user where pk_id=? ";
		List<User> list = commonDao.queryForPojoList(sql, new Object[]{2},  new PojoResultSetExtractor(User.class));
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	
	@Test
	public  void testqueryForPojoList_pojoResultSetExtractor() {
		String sql = "select * from t_user  ";
		List<User> list = commonDao.queryForPojoList(sql,   new PojoResultSetExtractor(User.class));
		for(User u:list){
			System.out.println( u.getUser_name());
		}
	}
	
	
}
