/**
 * 
 */
package snt.common.dao.base;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;

import snt.common.rs.MemoryResultSet;

/**
 * 通用数据访问服务 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class CommonDAOServiceImpl implements ICommonDAOService {
	private CommonDAO commonDAO;
	
	public int[] batchUpdate(List<String> sqlList, List<Object[]> argsList) throws DataAccessException {
		return getCommonDAO().batchUpdate(sqlList, argsList);
	}
	
	public int[] batchUpdate(String sql, List<Object[]> argsList) throws DataAccessException {
		return getCommonDAO().batchUpdate(sql, argsList);
	}	

	public int[] batchUpdate(String sql, List<Object[]> argsList, int[] argTypes) throws DataAccessException {
		return getCommonDAO().batchUpdate(sql, argsList, argTypes);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#batchUpdate(java.lang.String[])
	 */
	public int[] batchUpdate(String[] sqls) throws DataAccessException {
		return getCommonDAO().batchUpdate(sqls);
	}

	public List executeCallableStatement(String callString, List<SqlParameter> declaredParameters, Map inParams) throws DataAccessException{
		return getCommonDAO().executeCallableStatement(callString, declaredParameters, inParams);
	}
	
	//属性的get/set
	public CommonDAO getCommonDAO() {
		return commonDAO;
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForInt(java.lang.String)
	 */
	public int queryForInt(String sql) throws DataAccessException {
		return getCommonDAO().queryForInt(sql);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForInt(java.lang.String, java.util.Map)
	 */
	public int queryForInt(String sql, Map<String, ?> argMap) throws DataAccessException {
		return getCommonDAO().queryForInt(sql, argMap);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForInt(java.lang.String, java.lang.Object[])
	 */
	public int queryForInt(String sql, Object[] args) throws DataAccessException {
		return getCommonDAO().queryForInt(sql, args);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForInt(java.lang.String, java.lang.Object[], int[])
	 */
	public int queryForInt(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return getCommonDAO().queryForInt(sql, args, argTypes);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForMapList(java.lang.String)
	 */
	public List<Map<String, Object>> queryForMapList(String sql) throws DataAccessException {
		return getCommonDAO().queryForMapList(sql);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForMapList(java.lang.String, java.util.Map)
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Map<String, ?> argMap) throws DataAccessException {
		return getCommonDAO().queryForMapList(sql, argMap);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForMapList(java.lang.String, java.lang.Object[])
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args) throws DataAccessException {
		return getCommonDAO().queryForMapList(sql, args);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForMapList(java.lang.String, java.lang.Object[], int[])
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return getCommonDAO().queryForMapList(sql, args, argTypes);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedMrs(java.lang.String, java.util.Map, snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Map<String, ?> argMap, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedMrs(sql, argMap, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedMrs(java.lang.String, java.lang.Object[], int[], snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args, int[] argTypes, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedMrs(sql, args, argTypes, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedMrs(java.lang.String, java.lang.Object[], snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedMrs(sql, args, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedPojoList(java.lang.String, java.util.Map, snt.bizteller.dao.base.PojoResultSetExtractor, snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql, Map<String, ?> argMap, PojoResultSetExtractor pojoResultSetExtractor, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedPojoList(sql, argMap, pojoResultSetExtractor, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedPojoList(java.lang.String, java.lang.Object[], int[], snt.bizteller.dao.base.PojoResultSetExtractor, snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql, Object[] args, int[] argTypes, PojoResultSetExtractor pojoResultSetExtractor, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedPojoList(sql, args, argTypes, pojoResultSetExtractor, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedMapList(java.lang.String, java.lang.Object[], int[], snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedMapList(String sql, Object[] args, int[] argTypes, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedMapList(sql, args, argTypes, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPaginatedPojoList(java.lang.String, java.lang.Object[], snt.bizteller.dao.base.PojoResultSetExtractor, snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql, Object[] args, PojoResultSetExtractor pojoResultSetExtractor, RowSelection rowSelection, String orderByPart) throws DataAccessException {
		return getCommonDAO().queryForPaginatedPojoList(sql, args, pojoResultSetExtractor, rowSelection, orderByPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPagination(java.lang.String, java.util.List, java.util.List, org.springframework.jdbc.core.ResultSetExtractor, snt.bizteller.dao.base.RowSelection, java.lang.String)
	 */
	public PaginationSupport queryForPagination(String sql, List<Object> argList, List<Integer> argTypeList, ResultSetExtractor resultSetExtractor, RowSelection rowSelection, String orderbyPart) throws DataAccessException {
		return getCommonDAO().queryForPagination(sql, argList, argTypeList, resultSetExtractor, rowSelection, orderbyPart);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.lang.Class)
	 */
	public <T> List<T> queryForPojoList(String sql, Class<T> pojoType) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, pojoType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.util.Map, java.lang.Class)
	 */
	public <T> List<T> queryForPojoList(String sql, Map<String, ?> argMap, Class<T> pojoType) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, argMap, pojoType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.util.Map, snt.bizteller.dao.base.PojoResultSetExtractor)
	 */
	public List queryForPojoList(String sql, Map<String, ?> argMap, PojoResultSetExtractor pojoResultSetExtractor) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, argMap, pojoResultSetExtractor);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.lang.Object[], java.lang.Class)
	 */
	public <T> List<T> queryForPojoList(String sql, Object[] args, Class<T> pojoType) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, args, pojoType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.lang.Object[], int[], java.lang.Class)
	 */
	public <T> List<T> queryForPojoList(String sql, Object[] args, int[] argTypes, Class<T> pojoType) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, args, argTypes, pojoType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.lang.Object[], int[], snt.bizteller.dao.base.PojoResultSetExtractor)
	 */
	public List queryForPojoList(String sql, Object[] args, int[] argTypes, PojoResultSetExtractor pojoResultSetExtractor) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, args, argTypes, pojoResultSetExtractor);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, java.lang.Object[], snt.bizteller.dao.base.PojoResultSetExtractor)
	 */
	public List queryForPojoList(String sql, Object[] args, PojoResultSetExtractor pojoResultSetExtractor) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, args, pojoResultSetExtractor);
	}
	
	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForPojoList(java.lang.String, snt.bizteller.dao.base.PojoResultSetExtractor)
	 */
	public List queryForPojoList(String sql, PojoResultSetExtractor pojoResultSetExtractor) throws DataAccessException {
		return getCommonDAO().queryForPojoList(sql, pojoResultSetExtractor);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForResultSet(java.lang.String)
	 */
	public MemoryResultSet queryForResultSet(String sql) throws DataAccessException {
		return getCommonDAO().queryForResultSet(sql);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForResultSet(java.lang.String, java.util.Map)
	 */
	public MemoryResultSet queryForResultSet(String sql, Map<String, ?> argMap) throws DataAccessException {
		return getCommonDAO().queryForResultSet(sql, argMap);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForResultSet(java.lang.String, java.lang.Object[])
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args) throws DataAccessException {
		return getCommonDAO().queryForResultSet(sql, args);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForResultSet(java.lang.String, java.lang.Object[], int[])
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return getCommonDAO().queryForResultSet(sql, args, argTypes);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObject(java.lang.String, java.lang.Class)
	 */
	public <T> T queryForSimpObject(String sql, Class<T> requiredType) throws DataAccessException {
		return getCommonDAO().queryForSimpObject(sql, requiredType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObject(java.lang.String, java.util.Map, java.lang.Class)
	 */
	public <T> T queryForSimpObject(String sql, Map<String, ?> argMap, Class<T> requiredType) throws DataAccessException {
		return getCommonDAO().queryForSimpObject(sql, argMap, requiredType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObject(java.lang.String, java.lang.Object[], java.lang.Class)
	 */
	public <T> T queryForSimpObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
		return getCommonDAO().queryForSimpObject(sql, args, requiredType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObject(java.lang.String, java.lang.Object[], int[], java.lang.Class)
	 */
	public <T> T queryForSimpObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException {
		return getCommonDAO().queryForSimpObject(sql, args, argTypes, requiredType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObjList(java.lang.String, java.lang.Class)
	 */
	public <T> List<T> queryForSimpObjList(String sql, Class<T> elementType) throws DataAccessException {
		return getCommonDAO().queryForSimpObjList(sql, elementType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObjList(java.lang.String, java.util.Map, java.lang.Class)
	 */
	public <T> List<T> queryForSimpObjList(String sql, Map<String, ?> argMap, Class<T> elementType) throws DataAccessException {
		return getCommonDAO().queryForSimpObjList(sql, argMap, elementType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObjList(java.lang.String, java.lang.Object[], java.lang.Class)
	 */
	public <T> List<T> queryForSimpObjList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
		return getCommonDAO().queryForSimpObjList(sql, args, elementType);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#queryForSimpObjList(java.lang.String, java.lang.Object[], int[], java.lang.Class)
	 */
	public <T> List<T> queryForSimpObjList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException {
		return getCommonDAO().queryForSimpObjList(sql, args, argTypes, elementType);
	}
	
	/**
	 * 由用户自定义的结果集抽取器取得自定义结果对象
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rse
	 * @return Object
	 * @throws DataAccessException
	 */
	public Object queryForUserDefineObj(String sql, Object[] args, int[] argTypes, ResultSetExtractor rse) throws DataAccessException{
		return getCommonDAO().queryForUserDefineObj(sql, args, argTypes, rse);
	}
	
	/**
	 * 由用户自定义的结果集抽取器取得自定义结果对象
	 * 查询sql为带命名参数的sql，查询参数在参数Map中列出。 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from
	 * mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @param rse
	 * @return Object
	 * @throws DataAccessException
	 */
	public Object queryForUserDefineObj(String sql, Map<String, ?> argMap, ResultSetExtractor rse) throws DataAccessException{
		return getCommonDAO().queryForUserDefineObj(sql, argMap, rse);
	}
	
	/**
	 * 保存或更新对象到数据库
	 * @param pojo
	 * @return int
	 */
	public int saveOrUpdate(Object pojo) throws DataAccessException, AutoAssembleException{
		return getCommonDAO().saveOrUpdate(pojo);
	}
	
	/**
	 * 保存或更新对象到数据库
	 * @param pojos
	 * @return int
	 */
	public int saveOrUpdate(Object[] pojos) throws DataAccessException, AutoAssembleException {
		return getCommonDAO().saveOrUpdate(pojos);
	}
	
	/**
	 * 保存或更新对象到数据库<br>
	 * 这个方法允许列表中的POJO对象不同类型，也允许有些POJO对象属于新建而另外一些属于更新。<br>
	 * 本方法使用比较安全，但是效率较低。
	 * @param pojoList
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	public int saveOrUpdate(List<?> pojoList) throws DataAccessException, AutoAssembleException {
		return getCommonDAO().saveOrUpdate(pojoList);
	}
	
	/**
	 * 保存或更新对象到数据库<br>
	 * 本方法既不允许列表中的POJO对象有多种类型（即只允许一种类型），也不允许这些对象的持久化状态有不同（即要么都是<br>
	 * 新建，要么都是更新）。而类型和持久化状态均由列表中的第一个POJO对象判断得来。本方法在效率至先的原则下丝毫不考<br>
	 * 虑安全性，实乃居家旅行，谋财害命之必备良品。
	 * @param pojoList
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	public int crazySaveOrUpdate(List<?> pojoList) throws DataAccessException, AutoAssembleException {
		return getCommonDAO().crazySaveOrUpdate(pojoList);
	}
	
	public void setCommonDAO(CommonDAO commonDAO) {
		this.commonDAO = commonDAO;
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#update(java.lang.String)
	 */
	public int update(String sql) throws DataAccessException {
		return getCommonDAO().update(sql);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#update(java.lang.String, java.util.Map)
	 */
	public int update(String sql, Map<String, ?> argMap) throws DataAccessException {
		return getCommonDAO().update(sql, argMap);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#update(java.lang.String, java.lang.Object[])
	 */
	public int update(String sql, Object[] args) throws DataAccessException {
		return getCommonDAO().update(sql, args);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#update(java.lang.String, java.lang.Object[], int[])
	 */
	public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return getCommonDAO().update(sql, args, argTypes);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#updateWithOptimisticLock(java.lang.String, java.lang.Object[], int[], int)
	 */
	public int updateWithOptimisticLock(String sql, Map<String, ?> argMap, int version) throws DataAccessException, OptimisticLockingFailureException {
		return getCommonDAO().updateWithOptimisticLock(sql, argMap, version);
	}

	/* (non-Javadoc)
	 * @see snt.bizteller.dao.base.ICommonDAOService#updateWithOptimisticLock(java.lang.String, java.util.Map, int)
	 */
	public int updateWithOptimisticLock(String sql, Object[] args, int[] argTypes, int version) throws DataAccessException, OptimisticLockingFailureException {
		return getCommonDAO().updateWithOptimisticLock(sql, args, argTypes, version);
	}
}
