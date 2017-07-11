package snt.common.dao.base;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import snt.common.business.BaseBusinessException;
import snt.common.rs.MemoryResultSet;

/**
 * 通用数据访问接口
 * 事务支持
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
@Transactional(
		propagation = Propagation.SUPPORTS,
		readOnly = true,
		rollbackFor={BaseBusinessException.class}
)
public interface ICommonDAOService {
	/**
	 * 批量执行多个更新操作
	 * @param sqlList
	 * @param argsList
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int[] batchUpdate(List<String> sqlList, List<Object[]> argsList) throws DataAccessException;
	
	/**
	 * 批量执行多个更新操作
	 * @param sql
	 * @param argsList
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int[] batchUpdate(String sql, List<Object[]> argsList) throws DataAccessException;
	
	/**
	 * 批量执行多个更新操作
	 * @param sql
	 * @param argsList
	 * @param argTypes
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int[] batchUpdate(String sql, List<Object[]> argsList, int[] argTypes) throws DataAccessException;
	
	/**
	 * 批量执行多个更新操作
	 * @param sqls
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int[] batchUpdate(String[] sqls) throws DataAccessException;
	
	/**
	 * 执行存储过程，返回结果
	 * @param callString
	 * @param declaredParameters
	 * @param inParams
	 * @return List
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public List executeCallableStatement(String callString, List<SqlParameter> declaredParameters, Map inParams) throws DataAccessException;
	
	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。
	 * @param sql
	 * @return 查询得到的整型数据
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql) throws DataAccessException;

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数在sql中以命名参数的形式出现，即以":"开头，其值在参数-值Map中取得。
	 * @param sql
	 * @param argMap
	 * @return 查询得到的整型数据
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Map<String, ?> argMap)
			throws DataAccessException;

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数由args数组列出，其顺序必须和查询sql一一对应。
	 * @param sql
	 * @param args
	 * @return 查询得到的整型数据
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Object[] args)
			throws DataAccessException;

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数由args数组列出，参数类型由argTypes数组列出，其顺序必须和查询sql一一对应。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 查询得到的整型数据
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Object[] args, int[] argTypes)
			throws DataAccessException;

	/**
	 * 将查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。
	 * @param sql
	 * @return Map列表
	 * @throws DataAccessException
	 */
	public List<Map<String, Object>> queryForMapList(String sql) throws DataAccessException;

	/**
	 * 执行带命名参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 命名参数-值由argMap给出。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @return Map列表
	 * @throws DataAccessException
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Map<String, ?> argMap)
			throws DataAccessException;

	/**
	 * 执行带参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 参数列表由args给出。
	 * @param sql
	 * @param args
	 * @return Map列表
	 * @throws DataAccessException
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args)
			throws DataAccessException;

	/**
	 * 执行带参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 参数列表和参数类型由args和argTypes给出。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return Map列表
	 * @throws DataAccessException
	 */
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args, int[] argTypes)
			throws DataAccessException;

	/**
	 * gxc +
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Map列表。
	 * key = 字段名（小写），
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMapList(String sql,
			Object[] args, int[] argTypes,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException;
	
	/**
	 * 执行带命名参数的查询，根据分页设置返回结果集
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql,
			Map<String, ?> argMap, RowSelection rowSelection, String orderByPart)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，根据分页设置返回结果集。
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args,
			int[] argTypes, RowSelection rowSelection, String orderByPart)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，根据分页设置返回结果集。
	 * 参数列表在args里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param args
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException;

	/**
	 * 执行带命名参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Map<String, ?> argMap,
			PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Object[] args, int[] argTypes,
			PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException;
	
	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。
	 * 参数列表在args里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param args
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Object[] args, PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则(resultSetExtractor)转换为对象返回。
	 * 参数列表和参数类型列表在argList和argTypeList里列出，必须和sql中参数的位置一一对应。
	 * @param sql
	 * @param argList
	 * @param argTypeList
	 * @param resultSetExtractor
	 * @param rowSelection
	 * @param orderbyPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPagination(String sql,
			List<Object> argList, List<Integer> argTypeList,
			ResultSetExtractor resultSetExtractor, RowSelection rowSelection,
			String orderbyPart) throws DataAccessException;

	/**
	 * 执行查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记录转换
	 * 而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名的多个
	 * 属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * @param <T>
	 * @param sql
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForPojoList(String sql, Class<T> pojoType)
			throws DataAccessException;

	/**
	 * 执行带命名参数的查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集
	 * 中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能
	 * 有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForPojoList(String sql, Map<String, ?> argMap,
			Class<T> pojoType) throws DataAccessException;

	/**
	 * 执行带命名参数的查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个
	 * pojo，从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小
	 * 写，所以pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集
	 * 和pojo的映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public List queryForPojoList(String sql, Map<String, ?> argMap,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException;

	/**
	 * 执行带参数查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记
	 * 录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名
	 * 的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForPojoList(String sql, Object[] args,
			Class<T> pojoType) throws DataAccessException;

	/**
	 * 执行带参数查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记
	 * 录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名
	 * 的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForPojoList(String sql, Object[] args,
			int[] argTypes, Class<T> pojoType) throws DataAccessException;

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public List queryForPojoList(String sql, Object[] args, int[] argTypes,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException;

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * @param sql
	 * @param args
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public List queryForPojoList(String sql, Object[] args,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException;

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * @param sql
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	public List queryForPojoList(String sql,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException;

	/**
	 * 将查询得到的结果集转换为离线结果集（内存结果集）返回
	 * @param sql
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql)
			throws DataAccessException;

	/**
	 * 执行带命名参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回。 带命名参数的sql中的命名参数以冒号":"开头，例： "select *
	 * from mytable where fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Map<String, ?> argMap)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回
	 * @param sql
	 * @param args
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args)
			throws DataAccessException;

	/**
	 * 执行带参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args,
			int[] argTypes) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * @param <T>
	 * @param sql
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	public <T> T queryForSimpObject(String sql, Class<T> requiredType)
			throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * 查询sql为带命名参数的sql，查询参数在参数Map中列出。 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from
	 * mytable where fld1 = :fld1"
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	public <T> T queryForSimpObject(String sql, Map<String, ?> argMap,
			Class<T> requiredType) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。 查询参数在args中列出。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	public <T> T queryForSimpObject(String sql, Object[] args,
			Class<T> requiredType) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * 查询参数和类型在args和argTypes中列出。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	public <T> T queryForSimpObject(String sql, Object[] args, int[] argTypes,
			Class<T> requiredType) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * @param <T>
	 * @param sql
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForSimpObjList(String sql, Class<T> elementType)
			throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * 查询sql为带命名参数的sql，查询参数在参数Map中列出。 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from
	 * mytable where fld1 = :fld1"
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForSimpObjList(String sql, Map<String, ?> argMap,
			Class<T> elementType) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。 查询参数在args中列出。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForSimpObjList(String sql, Object[] args,
			Class<T> elementType) throws DataAccessException;

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * 查询参数和类型在args和argTypes中列出。
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	public <T> List<T> queryForSimpObjList(String sql, Object[] args,
			int[] argTypes, Class<T> elementType) throws DataAccessException;

	/**
	 * 由用户自定义的结果集抽取器取得自定义结果对象
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rse
	 * @return Object
	 * @throws DataAccessException
	 */
	public Object queryForUserDefineObj(String sql, Object[] args, int[] argTypes, ResultSetExtractor rse) throws DataAccessException;
	
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
	public Object queryForUserDefineObj(String sql, Map<String, ?> argMap, ResultSetExtractor rse) throws DataAccessException;
	
	/**
	 * 保存或更新对象到数据库
	 * @param pojo
	 * @return int
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int saveOrUpdate(Object pojo) throws DataAccessException, AutoAssembleException;
	
	/**
	 * 保存或更新对象到数据库
	 * 这个方法允许数组中的POJO对象不同类型，也允许有些POJO对象属于新建而另外一些属于更新。<br>
	 * 本方法使用比较安全，但是效率较低。
	 * @param pojos
	 * @return int
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int saveOrUpdate(Object[] pojos) throws DataAccessException, AutoAssembleException;
	
	/**
	 * 保存或更新对象到数据库<br>
	 * 这个方法允许列表中的POJO对象不同类型，也允许有些POJO对象属于新建而另外一些属于更新。<br>
	 * 本方法使用比较安全，但是效率较低。
	 * @param pojoList
	 * @return int
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int saveOrUpdate(List<?> pojoList) throws DataAccessException, AutoAssembleException;
	
	/**
	 * 保存或更新对象到数据库<br>
	 * 本方法既不允许列表中的POJO对象有多种类型（即只允许一种类型），也不允许这些对象的持久化状态有不同（即要么都是<br>
	 * 新建，要么都是更新）。而类型和持久化状态均由列表中的第一个POJO对象判断得来。本方法在效率至先的原则下丝毫不考<br>
	 * 虑安全性，实乃居家旅行，谋财害命之必备良品。
	 * @param pojoList
	 * @return int
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int crazySaveOrUpdate(List<?> pojoList) throws DataAccessException, AutoAssembleException;
	
	/**
	 * 执行更新语句（无参数），返回结果为更新记录数
	 * @param sql
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int update(String sql) throws DataAccessException;

	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，返回结果为 更新的记录数。 带命名参数的sql中的命名参数以冒号":"开头，例：
	 * "update mytable set fld1 = :fld1"
	 * @param sql
	 * @param argMap
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int update(String sql, Map<String, ?> argMap)
			throws DataAccessException;

	/**
	 * 执行带参数的sql，参数列表必须按照参数在sql语句中出现的顺序组织。 参数在sql中用"?"表示
	 * @param sql
	 * @param args
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int update(String sql, Object[] args) throws DataAccessException;

	/**
	 * 执行带参数的sql，参数列表和参数类型必须按照参数在sql语句中出现的顺序组织。 参数在sql中用"?"表示
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int update(String sql, Object[] args, int[] argTypes)
			throws DataAccessException;

	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，此更新操作需要通过乐观锁的校验才能成功。
	 * 返回的结果为更新的记录数目，正常的情况下只能为1 如果为0，说明在读取数据之后，更新操作发生之前，已经有其它事务更新了数据。
	 * 如果数目大于1，只能说明调用该方法的程序员是个白痴——带乐观锁的更新不能支持批量更新。
	 * 注，本方法不能支持太复杂的sql语句，只支持简单的update语句，如下样式： 
	 * update tableXXX 
	 * set 	fld1 = :fld1,
	 * 		fld2 = :fld2 
	 * 		... 
	 * where 
	 * 		keycol1 = :keycol1 
	 * 	and keycol2 = :keycol2 
	 * 		...
	 * 经过乐观锁的转换之后，实际上的更新语句变成了：
	 * update tableXXX 
	 * set 	fld1 = :fld1, 
	 * 		fld2 = :fld2
	 * 		... , 
	 * 		version = :newver 
	 * where 
	 * 		version = :oldver and 
	 * 		keycol1 = :keycol1
	 * 	and keycol2 = :keycol2 
	 * 		... 
	 * @param sql
	 * @param argMap
	 * @param version
	 * @return 更新记录数
	 * @throws DataAccessException
	 * @throws OptimisticLockingFailureException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int updateWithOptimisticLock(String sql, Map<String, ?> argMap,
			int version) throws DataAccessException,
			OptimisticLockingFailureException;
	
	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，此更新操作需要通过乐观锁的校验才能成功。
	 * 返回的结果为更新的记录数目，正常的情况下只能为1 如果为0，说明在读取数据之后，更新操作发生之前，已经有其它事务更新了数据。
	 * 如果数目大于1，只能说明调用该方法的程序员是个白痴——带乐观锁的更新不能支持批量更新。
	 * 注，本方法不能支持太复杂的sql语句，只支持简单的update语句，如下样式： 
	 * update tableXXX 
	 * set 	fld1 = ?,
	 * 		fld2 = ? 
	 * 		... 
	 * where 
	 * 		keycol1 = ?
	 * 	and keycol2 = ? 
	 * 		...
	 * 经过乐观锁的转换之后，实际上的更新语句变成了：
	 * update tableXXX 
	 * set 	fld1 = ?, 
	 * 		fld2 = ?
	 * 		... , 
	 * 		version = ? 
	 * where 
	 * 		version = ? and 
	 * 		keycol1 = ?
	 * 	and keycol2 = ? 
	 * 		... 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param version
	 * @return 更新记录数
	 * @throws DataAccessException
	 * @throws OptimisticLockingFailureException
	 */
	@Transactional(
			propagation = Propagation.REQUIRED,
			rollbackFor={BaseBusinessException.class}
	)
	public int updateWithOptimisticLock(String sql, Object[] args, int[] argTypes, int version) throws DataAccessException, OptimisticLockingFailureException;
}