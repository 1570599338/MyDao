/**
 * 
 */
package snt.common.dao.base;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import snt.common.dao.dialect.Dialect;
import snt.common.rs.MemoryResultSet;

/**
 * 通用数据访问类
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class CommonDAO extends JdbcDaoSupport {

	private static Log log = LogFactory.getLog(CommonDAO.class);
	private static Pattern paramPlaceHolderRegex = Pattern.compile("\\?",Pattern.CASE_INSENSITIVE);
	// private static Logg
	private static Pattern whereRegex = Pattern.compile("\\s*where\\s*",Pattern.CASE_INSENSITIVE);
	private Dialect dialect;
	private Boolean isScrollableResultSetsEnabled;
	private SqlRepairer sqlRepairer;

	private static void advance(final ResultSet rs,
			final RowSelection selection, boolean isScrollableResultSetsEnabled)
			throws SQLException {
		final int firstRow = selection.getFirstRow();
		if (firstRow != 0) {
			if (isScrollableResultSetsEnabled) {
				// we can go straight to the first required row
				rs.absolute(firstRow);
			} else {
				// we need to step through the rows one row at a time (slow)
				for (int m = 0; m < firstRow; m++)
					rs.next();
			}
		}
	}

	private static int bindLimitParameters(final Dialect dialect,
			final int index, final List<Object> argList,
			final List<Integer> argType, final RowSelection selection)
			throws DataAccessException {
		if (!dialect.supportsVariableLimit())
			return 0;
		int firstRow = selection.getFirstRow();
		int lastRow = getMaxOrLimit(selection, dialect);
		boolean hasFirstRow = firstRow > 0 && dialect.supportsLimitOffset();
		boolean reverse = dialect.bindLimitParametersInReverseOrder();
		if (hasFirstRow) {
			argList.add(index, firstRow);
			if (argType != null) {
				argType.add(index, Types.INTEGER);
			}
		}
		argList.add(index + (reverse || !hasFirstRow ? 0 : 1), lastRow);
		if (argType != null) {
			argType.add(index + (reverse || !hasFirstRow ? 0 : 1),
					Types.INTEGER);
		}
		return hasFirstRow ? 2 : 1;
	}

	private static Object convertParam(int sqlType, Object param) {
		switch (sqlType) {
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BLOB: {
			if (param == null) {
				return param;
			} else if (!(param instanceof Serializable)) {
				throw new IllegalArgumentException("对应Binary 类型字段的参数必须可序列化");
			}
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream objOut = new ObjectOutputStream(
						new BufferedOutputStream(out));
				objOut.writeObject(param);
				objOut.flush();
				byte[] bs = out.toByteArray();
				objOut.close();
				out.close();
				return bs;
			} catch (IOException e) {
				log.error("序列化对象出错", e);
				return param;
			}
		}
		default:
			return param;
		}
	}

	private static int getMaxOrLimit(final RowSelection selection,
			final Dialect dialect) {
		final int lastRow = selection.getLastRow();
		if (dialect.useMaxForLimit()) {
			return lastRow;
		} else {
			return selection.getPageSize();
		}
	}

	private static boolean useLimit(final Dialect dialect) {
		return dialect.supportsLimit();
	}

	/**
	 * 批量执行多个更新操作
	 * 
	 * @param sqlList
	 * @param argsList
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	public int[] batchUpdate(List<String> sqlList, List<Object[]> argsList)
			throws DataAccessException {
		if (sqlList == null) {
			// 懒得自己再定义一个异常了，就这样吧
			throw new IllegalArgumentException("sqlList参数不能为空！");
		}
		if (argsList == null) {
			return batchUpdate(sqlList.toArray(new String[sqlList.size()]));
		} else if (sqlList.size() != argsList.size()) {
			throw new IllegalArgumentException("sqlList参数和argsList参数长度必须一致！");
		}
		int[] updateCounts = new int[sqlList.size()];
		for (int i = 0; i < updateCounts.length; i++) {
			updateCounts[i] = update(sqlList.get(i), argsList.get(i));
		}
		return updateCounts;
	}

	/**
	 * 批量执行多个更新操作
	 * 
	 * @param sql
	 * @param argsList
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	public int[] batchUpdate(String sql, List<Object[]> argsList)
			throws DataAccessException {
		if (argsList == null) {
			int updateCount = update(sql);
			return new int[] { updateCount };
		}
		return getJdbcTemplate().batchUpdate(sql,
				new ArgTypeBatchPreparedStatementSetter(argsList, null));
	}

	/**
	 * 批量执行多个更新操作
	 * 
	 * @param sql
	 * @param argsList
	 * @param argTypes
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	public int[] batchUpdate(String sql, List<Object[]> argsList, int[] argTypes)
			throws DataAccessException {
		if (argsList == null) {
			int updateCount = update(sql);
			return new int[] { updateCount };
		}
		return getJdbcTemplate().batchUpdate(sql,
				new ArgTypeBatchPreparedStatementSetter(argsList, argTypes));
	}

	/**
	 * 批量执行多个更新操作
	 * 
	 * @param sqls
	 * @return 各个更新操作对应的更新记录数数组
	 * @throws DataAccessException
	 */
	public int[] batchUpdate(String[] sqls) throws DataAccessException {
		return getJdbcTemplate().batchUpdate(sqls);
	}

	/**
	 * 保存或更新对象到数据库<br>
	 * 本方法既不允许列表中的POJO对象有多种类型（即只允许一种类型），也不允许这些对象的持久化状态有不同（即要么都是<br>
	 * 新建，要么都是更新）。而类型和持久化状态均由列表中的第一个POJO对象判断得来。本方法在效率至先的原则下丝毫不考<br>
	 * 虑安全性，实乃居家旅行，谋财害命之必备良品。
	 * 
	 * @param pojoList
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	public int crazySaveOrUpdate(List<?> pojoList) throws DataAccessException,
			AutoAssembleException {
		int updateCount = 0;
		Object[] sqlAndArgs = AutoAssembleConfig.prepare4Persistence(pojoList);
		int[] updateCounts = batchUpdate((String) sqlAndArgs[0],
				(List<Object[]>) sqlAndArgs[1]);
		boolean bInsert = ((String) sqlAndArgs[0]).startsWith("insert");// 判断是否插入操作
		for (int i : updateCounts) {
			updateCount += i;
		}
		if (bInsert) {
			AutoAssembleConfig.updateVersion(pojoList);
		}
		return updateCount;
	}

	/**
	 * 执行存储过程，返回结果（未考虑输出类型的参数）
	 * 
	 * @param callString
	 * @param declaredParameters
	 * @param inParams
	 * @return List
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List executeCallableStatement(String callString,
			List<SqlParameter> declaredParameters, Map inParams)
			throws DataAccessException {
		CallableStatementCreatorFactory cscf = new CallableStatementCreatorFactory(
				callString, declaredParameters);
		return (List) getJdbcTemplate().execute(
				cscf.newCallableStatementCreator(inParams),
				new CallableStatementCallback() {
					@SuppressWarnings("unchecked")
					public Object doInCallableStatement(CallableStatement cs)
							throws SQLException {
						boolean retVal = cs.execute();
						int updateCount = cs.getUpdateCount();
						if (logger.isDebugEnabled()) {
							logger.debug("CallableStatement.execute() returned '"
									+ retVal + "'");
							logger.debug("CallableStatement.getUpdateCount() returned "
									+ updateCount);
						}
						List returnedResults = new ArrayList();
						if (retVal || updateCount != -1) {
							do {
								if (updateCount != -1) {
									returnedResults.add(updateCount);
								} else {
									returnedResults.add(new MemoryResultSet(cs
											.getResultSet()));
								}
								retVal = cs.getMoreResults();
								updateCount = cs.getUpdateCount();
							} while (retVal || updateCount != -1);
						}
						return returnedResults;
					}
				});
	}

	public Object executeConnectionCallback(ConnectionCallback conCallback)
			throws DataAccessException {
		if (conCallback == null) {
			return null;
		}
		return getJdbcTemplate().execute(conCallback);
	}

	public Object executePreparedStatementCallback(String sql,
			PreparedStatementCallback pscCallback) throws DataAccessException {
		if (pscCallback == null) {
			return null;
		}
		return getJdbcTemplate().execute(sql, pscCallback);
	}

	/**
	 * 得到当前对应的数据库方言
	 * 
	 * @return 当前对应的数据库方言
	 */
	public Dialect getDialect() {
		return dialect;
	}

	public int getFetchSize() {
		return getJdbcTemplate().getFetchSize();
	}

	public int getMaxRows() {
		return getJdbcTemplate().getMaxRows();
	}

	private final ResultSet getResultSet(final ResultSet rs,
			final RowSelection selection) throws SQLException,
			DataAccessException {
		if (!dialect.supportsLimitOffset() || !useLimit(dialect)) {
			advance(rs, selection, isScrollableResultSetsEnabled());
		}
		return rs;
	}

	private SqlRepairer getSqlRepairer() {
		if (sqlRepairer == null) {
			sqlRepairer = new SqlRepairer(getDialect());
		}

		return sqlRepairer;
	}

	private boolean isScrollableResultSetsEnabled() {
		if (isScrollableResultSetsEnabled == null) {
			Connection con = DataSourceUtils.getConnection(getDataSource());
			try {
				isScrollableResultSetsEnabled = con.getMetaData()
						.supportsResultSetType(
								ResultSet.TYPE_SCROLL_INSENSITIVE)
						|| con.getMetaData().supportsResultSetType(
								ResultSet.TYPE_SCROLL_SENSITIVE);
			} catch (SQLException ex) {
				// Release Connection early, to avoid potential connection pool
				// deadlock
				// in the case when the exception translator hasn't been
				// initialized yet.
				DataSourceUtils.releaseConnection(con, getDataSource());
				con = null;
				isScrollableResultSetsEnabled = false;
			} finally {
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		}
		return isScrollableResultSetsEnabled;
	}

	private Object query(PreparedStatementCreator psc,
			final PreparedStatementSetter pss, final ResultSetExtractor rse)
			throws DataAccessException {
		return getJdbcTemplate().execute(psc, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException {
				ResultSet rs = null;
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					rs = ps.executeQuery();
					ResultSet rsToUse = rs;
					if (getJdbcTemplate().getNativeJdbcExtractor() != null) {
						rsToUse = getJdbcTemplate().getNativeJdbcExtractor()
								.getNativeResultSet(rs);
					}
					return rse.extractData(rsToUse);
				} finally {
					JdbcUtils.closeResultSet(rs);
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。
	 * 
	 * @param sql
	 * @return 查询得到的整型结果
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql) throws DataAccessException {
		return getJdbcTemplate().queryForInt(sql);
	}

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数在sql中以命名参数的形式出现，即以":"开头，其值在参数-值Map中取得。
	 * 
	 * @param sql
	 * @param argMap
	 * @return 查询得到的整型结果
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Map<String, ?> argMap)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return getJdbcTemplate().queryForInt((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1]);
	}

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数由args数组列出，其顺序必须和查询sql一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @return 查询得到的整型结果
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Object[] args)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().queryForInt((String) sqlAndArgs[0]);
		} else {
			return getJdbcTemplate().queryForInt((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1]);
		}
	}

	/**
	 * 如果查询返回的结果集是一个只有一行一列的整型结果，可以用这个方法快捷的 获得该结果，如执行查询"select count(*) from
	 * ..."。 查询参数由args数组列出，参数类型由argTypes数组列出，其顺序必须和查询sql一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 查询得到的整型结果
	 * @throws DataAccessException
	 */
	public int queryForInt(String sql, Object[] args, int[] argTypes)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().queryForInt((String) sqlAndArgs[0]);
		} else {
			return getJdbcTemplate().queryForInt((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2]);
		}
	}

	/**
	 * 将查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。
	 * 
	 * @param sql
	 * @return Map列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForMapList(String sql)
			throws DataAccessException {
		return getJdbcTemplate().query(sql, new ColumnMapRowMapper());
	}

	/**
	 * 执行带命名参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 命名参数-值由argMap给出。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @return Map列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForMapList(String sql,
			Map<String, ?> argMap) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return getJdbcTemplate().query((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1], new ColumnMapRowMapper());
	}

	/**
	 * 执行带参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 参数列表由args给出。
	 * 
	 * @param sql
	 * @param args
	 * @return Map列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().query((String) sqlAndArgs[0],
					new ColumnMapRowMapper());
		} else {
			return getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], new ColumnMapRowMapper());
		}
	}

	/**
	 * 执行带参数的查询得到的结果集转换成一个列表，列表中的每个元素为一个Map，对应原结果集的每条记录。
	 * 结果集的列名对应Map中的key，结果集中的值对应Map中的value。 参数列表和参数类型由args和argTypes给出。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return Map列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> queryForMapList(String sql, Object[] args,
			int[] argTypes) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().query((String) sqlAndArgs[0],
					new ColumnMapRowMapper());
		} else {
			return getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2],
					new ColumnMapRowMapper());
		}
	}

	/**
	 * gxc + 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Map列表。 key = 字段名（小写），
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param argMap
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMapList(String sql,
			Map<String, ?> argMap, RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = new ArrayList<Object>();
		String newSql = getSqlRepairer().changeNamedParamSql2CommonSql(sql,argMap, argList);
		return queryForPagination(newSql, argList, null,new MapResultSetExtractor(), rowSelection, orderByPart);
	}

	/**
	 * gxc + 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Map列表。 key = 字段名（小写），
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页数据
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMapList(String sql,
			Object[] args, int[] argTypes, RowSelection rowSelection,
			String orderByPart) throws DataAccessException {
		List<Object> argList = args == null ? new ArrayList<Object>() : Arrays
				.asList(args);
		List<Integer> argTypeList = new ArrayList<Integer>(argTypes == null ? 2
				: argTypes.length);
		if (argTypes != null) {
			for (int argType : argTypes) {
				argTypeList.add(argType);
			}
		}
		return queryForPagination(sql, argList, argTypeList, new MapResultSetExtractor(), rowSelection, orderByPart);
	}

	/**
	 * 执行带命名参数的查询，根据分页设置返回结果集 带命名参数的sql中的命名参数以冒号":"开头，例：
	 * "select * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql,
			Map<String, ?> argMap, RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = new ArrayList<Object>();
		String newSql = getSqlRepairer().changeNamedParamSql2CommonSql(sql,
				argMap, argList);
		return queryForPagination(newSql, argList, null,
				new MrsResultSetExtractor(), rowSelection, orderByPart);
	}

	/**
	 * 执行带参数的查询，根据分页设置返回结果集。 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args,
			int[] argTypes, RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = args == null ? new ArrayList<Object>() : Arrays
				.asList(args);
		List<Integer> argTypeList = new ArrayList<Integer>(argTypes == null ? 2
				: argTypes.length);
		if (argTypes != null) {
			for (int argType : argTypes) {
				argTypeList.add(argType);
			}
		}
		return queryForPagination(sql, argList, argTypeList,
				new MrsResultSetExtractor(), rowSelection, orderByPart);
	}

	/**
	 * 执行带参数的查询，根据分页设置返回结果集。 参数列表在args里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedMrs(String sql, Object[] args,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = args == null ? new ArrayList<Object>() : Arrays
				.asList(args);
		return queryForPagination(sql, argList, null,
				new MrsResultSetExtractor(), rowSelection, orderByPart);
	}

	/**
	 * 执行带命名参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。 带命名参数的sql中的命名参数以冒号":"开头，例：
	 * "select * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Map<String, ?> argMap,
			PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = new ArrayList<Object>();
		String newSql = getSqlRepairer().changeNamedParamSql2CommonSql(sql,
				argMap, argList);
		return queryForPagination(newSql, argList, null,
				pojoResultSetExtractor, rowSelection, orderByPart);
	}

	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。
	 * 参数列表和参数类型列表在args和argTypes里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Object[] args, int[] argTypes,
			PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = args == null ? new ArrayList<Object>() : Arrays
				.asList(args);
		List<Integer> argTypeList = new ArrayList<Integer>(argTypes == null ? 2
				: argTypes.length);
		if (argTypes != null) {
			for (int argType : argTypes) {
				argTypeList.add(argType);
			}
		}
		return queryForPagination(sql, argList, argTypeList,
				pojoResultSetExtractor, rowSelection, orderByPart);
	}

	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则转换为Pojo列表。
	 * 参数列表在args里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param args
	 * @param pojoResultSetExtractor
	 * @param rowSelection
	 * @param orderByPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	public PaginationSupport queryForPaginatedPojoList(String sql,
			Object[] args, PojoResultSetExtractor pojoResultSetExtractor,
			RowSelection rowSelection, String orderByPart)
			throws DataAccessException {
		List<Object> argList = args == null ? new ArrayList<Object>() : Arrays
				.asList(args);
		return queryForPagination(sql, argList, null, pojoResultSetExtractor,
				rowSelection, orderByPart);
	}

	/**
	 * 执行带参数的查询，根据分页设置返回结果集，将结果集按照设定的转换规则(resultSetExtractor)转换为对象返回。
	 * 参数列表和参数类型列表在argList和argTypeList里列出，必须和sql中参数的位置一一对应。
	 * 
	 * @param sql
	 * @param argList
	 * @param argTypeList
	 * @param resultSetExtractor
	 * @param rowSelection
	 * @param orderbyPart
	 * @return 分页结果
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public PaginationSupport queryForPagination(String sql,
			List<Object> argList, List<Integer> argTypeList,
			ResultSetExtractor resultSetExtractor, RowSelection rowSelection,
			String orderbyPart) throws DataAccessException {
		if (argList != null && argList.size() > 0) {
			Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql,
					argList, argTypeList);
			sql = (String) sqlAndArgs[0];
			argList = (List<Object>) sqlAndArgs[1];
			argTypeList = (List<Integer>) sqlAndArgs[2];
		}
		// 求总行数
		StringBuffer totalCountSqlBuf = new StringBuffer(
				"select count(1) from (");
		int orderByIndex = -1;
		String upperCaseSql = sql.toUpperCase();
		if ((orderByIndex = upperCaseSql.lastIndexOf("ORDER BY")) >= 0) {
			totalCountSqlBuf.append(sql.substring(0, orderByIndex));
		} else {
			totalCountSqlBuf.append(sql);
			if (orderbyPart != null) {
				sql += " " + orderbyPart;
			}
		}
		totalCountSqlBuf.append(") as subquery");
		int totalCount = queryForInt(totalCountSqlBuf.toString(),
				argList.toArray());

		boolean useLimit = useLimit(dialect);
		boolean hasFirstRow = rowSelection.getFirstRow() > 0;
		boolean useOffset = hasFirstRow && useLimit
				&& dialect.supportsLimitOffset();

		boolean useScrollableResultSetToSkip = hasFirstRow && !useOffset
				&& isScrollableResultSetsEnabled();

		if (useLimit) {
			sql = dialect.getLimitString(
					sql.trim(), // use of trim()
					// here is ugly?
					useOffset ? rowSelection.getFirstRow() : 0,
					getMaxOrLimit(rowSelection, dialect));
		}
		int col = 0;
		if (useLimit && dialect.bindLimitParametersFirst()) {
			bindLimitParameters(dialect, col, argList, argTypeList,
					rowSelection);
		}
		col = argList.size();
		if (useLimit && !dialect.bindLimitParametersFirst()) {
			bindLimitParameters(dialect, col, argList, argTypeList,
					rowSelection);
		}

		SimplePreparedStatementCreator simplePreparedStatementCreator = new SimplePreparedStatementCreator(
				sql, useScrollableResultSetToSkip);
		if (!useLimit) {
			simplePreparedStatementCreator
					.setMaxRows(rowSelection.getLastRow());
		}
		int[] argTypes = null;
		if (argTypeList != null) {
			argTypes = new int[argTypeList.size()];
			int i = 0;
			for (Integer argType : argTypeList) {
				argTypes[i++] = argType;
			}
		}
		PreparedStatementSetter preparedStatementSetter = new ArgTypePreparedStatementSetter(
				argList.toArray(), argTypes);
		Object result = query(simplePreparedStatementCreator,
				preparedStatementSetter, new PaginationResultSetExtractorProxy(
						resultSetExtractor, rowSelection));

		PaginationSupport paginationSupport = new PaginationSupport(result,
				totalCount, rowSelection.getPageSize(),
				rowSelection.getStartPage());
		return paginationSupport;
	}

	/**
	 * lquan - test-OK
	 * 执行查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记录转换
	 * 而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名的多个
	 * 属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * 
	 * @param <T>
	 * @param sql
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForPojoList(String sql, Class<T> pojoType)throws DataAccessException {
		return (List<T>) getJdbcTemplate().query(sql,new PojoResultSetExtractor(pojoType));
	}

	/**
	 * 执行带命名参数的查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集
	 * 中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能
	 * 有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * 
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForPojoList(String sql, Map<String, ?> argMap,Class<T> pojoType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1], new PojoResultSetExtractor(pojoType));

	}

	/**
	 * 执行带命名参数的查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个
	 * pojo，从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小
	 * 写，所以pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集
	 * 和pojo的映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List queryForPojoList(String sql, Map<String, ?> argMap,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1], pojoResultSetExtractor);
	}

	/**
	 * 执行带参数查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记
	 * 录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名
	 * 的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForPojoList(String sql, Object[] args,
			Class<T> pojoType) throws DataAccessException {
		return (List<T>) queryForPojoList(sql, args,
				new PojoResultSetExtractor(pojoType));
	}

	/**
	 * 执行带参数查询，将得到的结果集拼装成一个pojo列表，列表中的每一个元素为一个pojo，从原结果集中的记
	 * 录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以pojo中不能有同名
	 * 的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或 者InputStream类型。
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoType
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForPojoList(String sql, Object[] args,
			int[] argTypes, Class<T> pojoType) throws DataAccessException {
		return (List<T>) queryForPojoList(sql, args, argTypes,
				new PojoResultSetExtractor(pojoType));
	}

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List queryForPojoList(String sql, Object[] args, int[] argTypes,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
					pojoResultSetExtractor);
		} else {
			return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2],
					pojoResultSetExtractor);
		}
	}

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * 
	 * @param sql
	 * @param args
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List queryForPojoList(String sql, Object[] args,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
					pojoResultSetExtractor);
		} else {
			return (List) getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], pojoResultSetExtractor);
		}
	}

	/**
	 * 执行带参数查询，将得到的结果集按照指定的转换器拼装成一个pojo列表，列表中的每一个元素为一个pojo，
	 * 从原结果集中的记录转换而来。转换的规则是将结果集的列名和pojo的属性名进行匹配（不区分大小写，所以
	 * pojo中不能有同名的多个属性），匹配上则将结果集中的这列的值赋给pojo的对应的属性。结果集和pojo的
	 * 映射关系可以在PojoResultSetExtractor中设置。
	 * 默认情况下，clob字段会转换为String类型，而不是保持Clob类型，但是blob字段还是保持为Blob类型或
	 * 者InputStream类型。是否转换lob类型的字段也可以在PojoResultSetExtractor中设置。
	 * 
	 * @param sql
	 * @param pojoResultSetExtractor
	 * @return Pojo对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List queryForPojoList(String sql,
			PojoResultSetExtractor pojoResultSetExtractor)
			throws DataAccessException {
		return (List) getJdbcTemplate().query(sql, pojoResultSetExtractor);
	}

	/**
	 * 将查询得到的结果集转换为离线结果集（内存结果集）返回
	 * 
	 * @param sql
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql)
			throws DataAccessException {
		return (MemoryResultSet) getJdbcTemplate().query(sql,
				new MrsResultSetExtractor());
	}

	/**
	 * 执行带命名参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回。 带命名参数的sql中的命名参数以冒号":"开头，例： "select *
	 * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Map<String, ?> argMap)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return (MemoryResultSet) getJdbcTemplate().query(
				(String) sqlAndArgs[0], (Object[]) sqlAndArgs[1],
				new MrsResultSetExtractor());
	}

	/**
	 * 执行带参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回
	 * 
	 * @param sql
	 * @param args
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return (MemoryResultSet) getJdbcTemplate().query(
					(String) sqlAndArgs[0], new MrsResultSetExtractor());
		} else {
			return (MemoryResultSet) getJdbcTemplate().query(
					(String) sqlAndArgs[0], (Object[]) sqlAndArgs[1],
					new MrsResultSetExtractor());
		}
	}

	/**
	 * 执行带参数的查询，将得到的结果集转换成离线结果集（内存结果集）返回
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 内存结果集
	 * @throws DataAccessException
	 */
	public MemoryResultSet queryForResultSet(String sql, Object[] args,
			int[] argTypes) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return (MemoryResultSet) getJdbcTemplate().query(
					(String) sqlAndArgs[0], new MrsResultSetExtractor());
		} else {
			return (MemoryResultSet) getJdbcTemplate().query(
					(String) sqlAndArgs[0], (Object[]) sqlAndArgs[1],
					(int[]) sqlAndArgs[2], new MrsResultSetExtractor());
		}
	}

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * 
	 * @param <T>
	 * @param sql
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryForSimpObject(String sql, Class<T> requiredType)
			throws DataAccessException {
		return (T) getJdbcTemplate().queryForObject(sql, requiredType);
	}

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * 查询sql为带命名参数的sql，查询参数在参数Map中列出。 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from
	 * mytable where fld1 = :fld1"
	 * 
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryForSimpObject(String sql, Map<String, ?> argMap,
			Class<T> requiredType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return (T) getJdbcTemplate().queryForObject((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1], requiredType);
	}

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。 查询参数在args中列出。
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryForSimpObject(String sql, Object[] args,
			Class<T> requiredType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return (T) getJdbcTemplate().queryForObject((String) sqlAndArgs[0],
					requiredType);
		} else {
			return (T) getJdbcTemplate().queryForObject((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], requiredType);
		}
	}

	/**
	 * 如果查询返回的结果集只有一列一行，可以用这个方法快捷的得到结果集中的对象，对象类型 与传入的类型必须匹配。
	 * 查询参数和类型在args和argTypes中列出。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param requiredType
	 * @return 简单对象
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> T queryForSimpObject(String sql, Object[] args, int[] argTypes,
			Class<T> requiredType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return (T) getJdbcTemplate().queryForObject((String) sqlAndArgs[0],
					requiredType);
		} else {
			return (T) getJdbcTemplate().queryForObject((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2],
					requiredType);
		}
	}

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * 
	 * @param <T>
	 * @param sql
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForSimpObjList(String sql, Class<T> elementType)
			throws DataAccessException {
		return (List<T>) getJdbcTemplate().queryForList(sql, elementType);
	}

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * 查询sql为带命名参数的sql，查询参数在参数Map中列出。 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from
	 * mytable where fld1 = :fld1"
	 * 
	 * @param <T>
	 * @param sql
	 * @param argMap
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForSimpObjList(String sql, Map<String, ?> argMap,
			Class<T> elementType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return (List<T>) getJdbcTemplate().query((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1],
				new SingleColumnRowMapper(elementType));
	}

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。 查询参数在args中列出。
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForSimpObjList(String sql, Object[] args,
			Class<T> elementType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return (List<T>) getJdbcTemplate().query((String) sqlAndArgs[0],
					new SingleColumnRowMapper(elementType));
		} else {
			return (List<T>) getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1],
					new SingleColumnRowMapper(elementType));
		}
	}

	/**
	 * 如果查询返回的结果集只有一列，可以用这个方法快捷的得到结果集中的对象列表，对象类型
	 * 与传入的类型必须匹配。返回列表的长度和结果集中的记录数一致，列表中的每个元素即为结 果集中该列对应的数据。
	 * 查询参数和类型在args和argTypes中列出。
	 * 
	 * @param <T>
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param elementType
	 * @return 简单对象列表
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> queryForSimpObjList(String sql, Object[] args,
			int[] argTypes, Class<T> elementType) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return (List<T>) getJdbcTemplate().query((String) sqlAndArgs[0],
					new SingleColumnRowMapper(elementType));
		} else {
			return (List<T>) getJdbcTemplate().query((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2],
					new SingleColumnRowMapper(elementType));
		}
	}

	/**
	 * 由用户自定义的结果集抽取器取得自定义结果对象 查询sql为带命名参数的sql，查询参数在参数Map中列出。
	 * 带命名参数的sql中的命名参数以冒号":"开头，例： "select * from mytable where fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap
	 * @param rse
	 * @return Object
	 * @throws DataAccessException
	 */
	public Object queryForUserDefineObj(String sql, Map<String, ?> argMap,
			ResultSetExtractor rse) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return getJdbcTemplate().query((String) sqlAndArgs[0],
				(Object[]) sqlAndArgs[1], rse);
	}

	/**
	 * 由用户自定义的结果集抽取器取得自定义结果对象
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param rse
	 * @return Object
	 * @throws DataAccessException
	 */
	public Object queryForUserDefineObj(String sql, Object[] args,
			int[] argTypes, ResultSetExtractor rse) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().query((String) sqlAndArgs[0], rse);
		} else {
			return getJdbcTemplate().query(
					(String) sqlAndArgs[0],
					new ArgTypePreparedStatementSetter(
							(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2]),
					rse);
		}
	}

	/**
	 * 保存或更新对象到数据库<br>
	 * 这个方法允许列表中的POJO对象不同类型，也允许有些POJO对象属于新建而另外一些属于更新。<br>
	 * 本方法使用比较安全，但是效率较低。
	 * 
	 * @param pojoList
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	public int saveOrUpdate(List<?> pojoList) throws DataAccessException,
			AutoAssembleException {
		int updateCount = 0;
		for (Object object : pojoList) {
			updateCount += saveOrUpdate(object);
		}
		return updateCount;
	}

	/**
	 * 保存或更新对象到数据库
	 * 
	 * @param pojo
	 * @return int
	 */
	public int saveOrUpdate(Object pojo) throws DataAccessException,
			AutoAssembleException {
		int updateCount = 0;
		if (pojo != null) {
			Object[] sqlAndArgs = AutoAssembleConfig.prepare4Persistence(pojo);
			updateCount = update((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1]);
			if (updateCount > 0) {
				AutoAssembleConfig.updateVersion(pojo);
			}
		}
		return updateCount;
	}

	/**
	 * 保存或更新对象到数据库
	 * 
	 * @param pojos
	 * @return int
	 */
	@SuppressWarnings("unchecked")
	public int saveOrUpdate(Object[] pojos) throws DataAccessException,
			AutoAssembleException {
		if (pojos == null) {
			return 0;
		}
		return saveOrUpdate(Arrays.asList(pojos));
	}

	/**
	 * 设置数据库连接对应的方言
	 * 
	 * @param dialect
	 */
	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public void setFetchSize(int fetchSize) {
		getJdbcTemplate().setFetchSize(fetchSize);
	}

	public void setMaxRows(int maxRows) {
		getJdbcTemplate().setMaxRows(maxRows);
	}

	/**
	 * 执行更新语句（无参数），返回结果为更新记录数
	 * 
	 * @param sql
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	public int update(String sql) throws DataAccessException {
		return getJdbcTemplate().update(sql);
	}

	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，返回结果为 更新的记录数。 带命名参数的sql中的命名参数以冒号":"开头，例：
	 * "update mytable set fld1 = :fld1"
	 * 
	 * @param sql
	 * @param argMap 参数
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	public int update(String sql, Map<String, ?> argMap)throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, argMap);
		return getJdbcTemplate().update((String) sqlAndArgs[0],(Object[]) sqlAndArgs[1]);
	}

	/**
	 * 执行带参数的sql，参数列表必须按照参数在sql语句中出现的顺序组织。 参数在sql中用"?"表示
	 * 
	 * @param sql
	 * @param args 参数 
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	public int update(String sql, Object[] args) throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer()
				.repairSqlAndArgs(sql, args, null);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().update((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1]);
		} else {
			return getJdbcTemplate().update((String) sqlAndArgs[0],
					(Object[]) sqlAndArgs[1]);
		}
	}

	/**
	 * 执行带参数的sql，参数列表和参数类型必须按照参数在sql语句中出现的顺序组织。 参数在sql中用"?"表示
	 * 
	 * @param sql	SQL语句
	 * @param args  参数
	 * @param argTypes 参数的类型
	 * @return 更新记录数
	 * @throws DataAccessException
	 */
	public int update(String sql, Object[] args, int[] argTypes)
			throws DataAccessException {
		Object[] sqlAndArgs = getSqlRepairer().repairSqlAndArgs(sql, args,
				argTypes);
		if (sqlAndArgs[1] == null) {
			return getJdbcTemplate().update((String) sqlAndArgs[0]);
		} else {
			return getJdbcTemplate().update(
					(String) sqlAndArgs[0],
					new ArgTypePreparedStatementSetter(
							(Object[]) sqlAndArgs[1], (int[]) sqlAndArgs[2]));
		}
	}

	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，此更新操作需要通过乐观锁的校验才能成功。
	 * 返回的结果为更新的记录数目，正常的情况下只能为1 如果为0，说明在读取数据之后，更新操作发生之前，已经有其它事务更新了数据。
	 * 如果数目大于1，只能说明调用该方法的程序员是个白痴——带乐观锁的更新不能支持批量更新。
	 * 注，本方法不能支持太复杂的sql语句，只支持简单的update语句，如下样式： update tableXXX set fld1 = :fld1,
	 * fld2 = :fld2 ... where keycol1 = :keycol1 and keycol2 = :keycol2 ...
	 * 经过乐观锁的转换之后，实际上的更新语句变成了： update tableXXX set fld1 = :fld1, fld2 = :fld2
	 * ... , version = :newver where version = :oldver and keycol1 = :keycol1
	 * and keycol2 = :keycol2 ...
	 * 
	 * @param sql
	 * @param argMap
	 * @param version
	 * @return 更新记录数
	 * @throws DataAccessException
	 * @throws OptimisticLockingFailureException
	 */
	public int updateWithOptimisticLock(String sql, Map<String, ?> argMap,
			int version) throws DataAccessException,
			OptimisticLockingFailureException {
		sql = whereRegex.matcher(sql).replaceFirst(
				" , version=:newver where version=:oldver and ");
		if (sql.indexOf("where") < 0) {
			throw new OptimisticLockingFailureException(0,
					"Update statement with optimistic lock must have where clause!");
		}
		Map<String, Object> newArgMap = new HashMap<String, Object>();
		newArgMap.putAll(argMap);
		newArgMap.put("newver", version + 1);
		newArgMap.put("oldver", version);
		int updatedCount = update(sql, newArgMap);
		if (updatedCount == 0) {
			throw new OptimisticLockingFailureException(
					updatedCount,
					"The data had been modified by others! Please refresh your data from the database before update it!");
		} else if (updatedCount > 1) {
			throw new OptimisticLockingFailureException(
					updatedCount,
					"You guys are extremely an idiot! Update with optimistic lock can only update one record per execution! Please check your code first!");
		}
		return updatedCount;
	}

	/**
	 * 根据带命名参数的sql语句和参数-值Map构造更新语句更新数据库，此更新操作需要通过乐观锁的校验才能成功。
	 * 返回的结果为更新的记录数目，正常的情况下只能为1 如果为0，说明在读取数据之后，更新操作发生之前，已经有其它事务更新了数据。
	 * 如果数目大于1，只能说明调用该方法的程序员是个白痴——带乐观锁的更新不能支持批量更新。
	 * 注，本方法不能支持太复杂的sql语句，只支持简单的update语句，如下样式： update tableXXX set fld1 = ?,
	 * fld2 = ? ... where keycol1 = ? and keycol2 = ? ...
	 * 经过乐观锁的转换之后，实际上的更新语句变成了： update tableXXX set fld1 = ?, fld2 = ? ... ,
	 * version = ? where version = ? and keycol1 = ? and keycol2 = ? ...
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @param version
	 * @return 更新记录数
	 * @throws DataAccessException
	 * @throws OptimisticLockingFailureException
	 */
	public int updateWithOptimisticLock(String sql, Object[] args,
			int[] argTypes, int version) throws DataAccessException,
			OptimisticLockingFailureException {
		sql = whereRegex.matcher(sql).replaceFirst(
				" , version=? where version=? and ");
		int whereIndex = sql.indexOf("where");
		if (whereIndex < 0) {
			throw new OptimisticLockingFailureException(0,
					"Update statement with optimistic lock must have where clause!");
		}
		int origParamCountBeforewhere = -1;// 之所以设为-1是因为替换where之后where前的参数个数比原来多了一个，
											// 所以这里设为-1刚好抵消替换的影响
		Matcher regexMatcher = paramPlaceHolderRegex.matcher(sql.substring(0,
				whereIndex));
		while (regexMatcher.find()) {
			origParamCountBeforewhere++;
		}
		Object[] newArgs = new Object[args == null ? 2 : args.length + 2];
		int[] newArgTypes = argTypes == null ? (args == null ? new int[2]
				: null) : new int[argTypes.length + 2];
		if (args != null) {
			System.arraycopy(args, 0, newArgs, 0, origParamCountBeforewhere);
			System.arraycopy(args, origParamCountBeforewhere, newArgs,
					origParamCountBeforewhere + 2, args.length
							- origParamCountBeforewhere);
		}
		newArgs[origParamCountBeforewhere] = version + 1;
		newArgs[origParamCountBeforewhere + 1] = version;
		if (newArgTypes != null) {
			if (argTypes != null) {
				System.arraycopy(argTypes, 0, newArgTypes, 0,
						origParamCountBeforewhere);
				System.arraycopy(argTypes, origParamCountBeforewhere,
						newArgTypes, origParamCountBeforewhere + 2,
						argTypes.length - origParamCountBeforewhere);
			}
			newArgTypes[origParamCountBeforewhere] = Types.INTEGER;
			newArgTypes[origParamCountBeforewhere + 1] = Types.INTEGER;
		}
		int updatedCount = newArgTypes == null ? update(sql, newArgs) : update(
				sql, newArgs, newArgTypes);
		if (updatedCount == 0) {
			throw new OptimisticLockingFailureException(
					updatedCount,
					"The data had been modified by others! Please refresh your data from the database before update it!");
		} else if (updatedCount > 1) {
			throw new OptimisticLockingFailureException(
					updatedCount,
					"You guys are extremely an idiot! Update with optimistic lock can only update one record per execution! Please check your code first!");
		}
		return updatedCount;
	}

	/**
	 * 在更新的同时，校验更新记录数。 只有在更新记录数为1的情况下才能通过校验。 通常用于扣费处理等要求并发性能和数据一致性要求较高的场合。
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return 更新记录数
	 * @throws DataAccessException
	 * @throws NoAccordedRecordException
	 */
	public int updateWithUpdateCountCheck(String sql, Object[] args,
			int[] argTypes) throws DataAccessException,
			NoAccordedRecordException {
		int updatedCount = update(sql, args, argTypes);
		if (updatedCount == 0) {
			throw new NoAccordedRecordException(updatedCount,
					"The corresponding record has not been found!");
		} else if (updatedCount > 1) {
			throw new NoAccordedRecordException(
					updatedCount,
					"You guys are extremely an idiot! Update with updated record count check can only update one record per execution! Please check your code first!");
		}
		return updatedCount;
	}


/**
 * Simple adapter for PreparedStatementCreator, allowing to use a plain SQL
 * statement.
 */
private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {

	private int maxRows = -1;

	private final boolean scrollable;

	private final String sql;

	public SimplePreparedStatementCreator(String sql, boolean scrollable) {
		this.sql = sql;
		this.scrollable = scrollable;
	}

	public PreparedStatement createPreparedStatement(Connection con)
			throws SQLException {
		PreparedStatement pstmt = null;
		if (scrollable) {
			pstmt = con.prepareStatement(this.sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} else {
			pstmt = con.prepareStatement(this.sql);
		}
		if (maxRows >= 0)
			pstmt.setMaxRows(maxRows);
		return pstmt;
	}

	public String getSql() {
		return sql;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
}

/**
 * 结果集抽取器的分页包装
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
private class PaginationResultSetExtractorProxy implements ResultSetExtractor {
	private ResultSetExtractor delegate;

	private RowSelection rowSelection;

	public PaginationResultSetExtractorProxy(ResultSetExtractor delegate,
			RowSelection rowSelection) {
		this.delegate = delegate;
		this.rowSelection = rowSelection;
	}

	public Object extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		rs = getResultSet(rs, rowSelection);
		return delegate.extractData(rs);
	}
}

/**
 * Simple adapter for PreparedStatementSetter that applies given arrays of
 * arguments and JDBC argument types.
 */
private static class ArgTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {
	private final Object[] args;
	private final int[] argTypes;

	public ArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		this.args = args;
		this.argTypes = argTypes;
		validArgSet();
	}

	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

	public void setValues(PreparedStatement ps) throws SQLException {
		if (this.args != null) {
			if (this.argTypes != null) {// 减少判断次数，所以代码长了些，其实两可
				for (int i = 0; i < this.args.length; i++) {
					StatementCreatorUtils.setParameterValue(ps, i + 1,
							this.argTypes[i], null,
							convertParam(this.argTypes[i], this.args[i]));
				}
			} else {
				for (int i = 0; i < this.args.length; i++) {
					StatementCreatorUtils.setParameterValue(ps, i + 1,
							SqlTypeValue.TYPE_UNKNOWN, null, this.args[i]);
				}
			}
		}
	}

	private void validArgSet() {
		if ((args == null && argTypes != null)
				|| (args != null && argTypes != null && args.length != argTypes.length)) {
			throw new InvalidDataAccessApiUsageException(
					"args and argTypes parameters must match");
		}
	}
}

/**
 * Simple adapter for BatchPreparedStatementSetter that applies given list
 * consists of arrays of arguments and JDBC argument types.
 */
	private static class ArgTypeBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
		private final List<Object[]> argsList;
		private final int[] argTypes;
	
		public ArgTypeBatchPreparedStatementSetter(List<Object[]> argsList,
				int[] argTypes) {
			this.argsList = argsList;
			this.argTypes = argTypes;
		}
	
		public int getBatchSize() {
			return argsList == null ? 0 : argsList.size();
		}
	
		public void setValues(PreparedStatement ps, int j) throws SQLException {
			Object[] args = argsList.get(j);
			if (args != null) {
				if (this.argTypes != null) {// 减少判断次数，所以代码长了些，其实两可
					for (int i = 0; i < args.length; i++) {
						StatementCreatorUtils.setParameterValue(ps, i + 1,
								this.argTypes[i], null,
								convertParam(this.argTypes[i], args[i]));
					}
				} else {
					for (int i = 0; i < args.length; i++) {
						StatementCreatorUtils.setParameterValue(ps, i + 1,
								SqlTypeValue.TYPE_UNKNOWN, null, args[i]);
					}
				}
			}
		}
}
}
