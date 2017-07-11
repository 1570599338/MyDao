/**
 * 
 */
package snt.common.dao.base;

import java.io.StringWriter;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import snt.common.dao.dialect.Dialect;

/**
 * 万般无奈，需提供这样的一个方法<br>
 * 以修补sql语句和参数，主要是将where条件中的表达式，如where a =?<br/> 而相应参数为空的情况，修补为where a is null
 * 
 * 修改于2006-08-25 为了适配某些烂到体无完肤的数据库驱动程序对于PrepareStmt的不友好，<br/>
 * 增加了把PrepareStmt转换成Stmt的功能（根据Dialect的设定）
 * 
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
class SqlRepairer {
	private static Log log = LogFactory.getLog(SqlRepairer.class);

	private static DateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private static Pattern paramSqlRegex = Pattern.compile(":(\\w*)",
			Pattern.CASE_INSENSITIVE);

	/** 数据库方言 */
	private Dialect dialect;

	SqlRepairer(Dialect dialect) {
		if (dialect == null) {
			throw new IllegalArgumentException(
					"构造SqlRepairer的Dialect参数不能为null！");
		}
		this.dialect = dialect;
	}

	/**
	 * 修补Sql和参数
	 * 
	 * @param sql
	 * @param args
	 * @param argTypes
	 * @return Object[] 数组元素顺序为 sql语句，参数列表[，参数类型列表]
	 */
	@SuppressWarnings("unchecked")
	Object[] repairSqlAndArgs(String sql, Object[] args, int[] argTypes) {
		if (args == null || args.length == 0) {
			// 如果无参数则直接返回
			return new Object[] { sql, args, argTypes };
		}
		Object[] sqlAndArgs = repairSqlAndArgs(sql, Arrays.asList(args),
				convertArray(argTypes));
		sqlAndArgs[1] = convertObjectList((List<Object>) sqlAndArgs[1]);
		sqlAndArgs[2] = convertList((List<Integer>) sqlAndArgs[2]);
		return sqlAndArgs;
	}

	/**
	 * 修补Sql和参数
	 * 
	 * @param sql
	 * @param argMap
	 * @return Object[] 数组元素顺序为 sql语句，参数列表[，参数类型列表]
	 */
	@SuppressWarnings("unchecked")
	Object[] repairSqlAndArgs(String sql, Map<String, ?> argMap) {
		if (argMap == null || argMap.size() == 0) {
			// 如果无参数则直接返回
			return new Object[] { sql, new Object[0], new int[0] };
		}
		List<Object> argList = new ArrayList<Object>();
		sql = changeNamedParamSql2CommonSql(sql, argMap, argList);
		Object[] sqlAndArgs = repairSqlAndArgs(sql, argList, null);
		sqlAndArgs[1] = convertObjectList((List<Object>) sqlAndArgs[1]);
		sqlAndArgs[2] = convertList((List<Integer>) sqlAndArgs[2]);
		return sqlAndArgs;
	}

	/**
	 * 
	 * @param sql
	 * @param argList
	 * @param argTypeList
	 * @return Object[] 数组元素顺序为 sql语句，参数列表[，参数类型列表]
	 */
	Object[] repairSqlAndArgs(String sql, List<Object> argList,
			List<Integer> argTypeList) {
		if (log.isDebugEnabled()) {
			log.debug("修补前sql：" + sql);
		}
		boolean useStmtInsteadOfPstmt = dialect.isUseStmtInsteadOfPstmt();
		if (argList == null || argList.size() == 0) {
			// 如果无参数则直接返回
			return new Object[] { sql, argList, argTypeList };
		}
		String upperSql = sql.toUpperCase();
		// 期望小的们不要在sql语句里带有回车符、换行符
		int unionIndex = upperSql.indexOf(" UNION ");
		int whereIndex = -1;
		if (unionIndex < 0) {
			whereIndex = upperSql.indexOf(" WHERE ");
			if (whereIndex < 0 && !useStmtInsteadOfPstmt) {// 对于根本没有where的sql，又不需要转换为stmt的，我不必多此一举。
				return new Object[] { sql, argList, argTypeList };
			}
		}
		//判断sql中参数个数是否和传入参数列表个数一致
		int paramCount = countParamPlaceHolder(sql, 0, sql.length());
		if(paramCount != argList.size()){
			throw new IllegalArgumentException("Sql语句中参数个数和传入参数列表参数个数不一致");
		}
		// 注，下面的参数序号以0为基
		int startParamIndex = unionIndex > 0 ? 0 : whereIndex < 0 ? argList
				.size() : countParamPlaceHolder(sql, 0, whereIndex);
		TreeSet<Integer> nullParamList = new TreeSet<Integer>();// 为null的参数位置记录
		for (int i = startParamIndex, in = argList.size(); i < in; i++) {
			if (argList.get(i) == null) {
				nullParamList.add(i);
			}
		}
		if (nullParamList.isEmpty() && !useStmtInsteadOfPstmt) {
			// 没有null参数又不需要转换为stmt的，也不需要管了
			return new Object[] { sql, argList, argTypeList };
		}
		// 下面正式开始修补
		startParamIndex = useStmtInsteadOfPstmt ? 0 : nullParamList.iterator()
				.next();// 开始处理的参数号，以0为基（包含）
		int endParamIndex = useStmtInsteadOfPstmt ? argList.size()
				: nullParamList.last() + 1;// 处理结束的参数号，以0为基（不含）
		int formerParamIndex = startParamIndex - 1;
		int beginIndex = findParamPos(sql, startParamIndex, 0);// 这里找的是第formerParamIndex个参数在sql中的位置
		// 如果formerIParamIndex比0小，这里返回的是位置是0

		StringBuffer newSqlBuf = new StringBuffer(sql.length());
		newSqlBuf.append(sql.subSequence(0, beginIndex));

		List<Integer> replaceParamIndicesList = new ArrayList<Integer>();
		for (int paramIndex = startParamIndex; paramIndex < endParamIndex; paramIndex++) {
			int endIndex = 0;
			if (paramIndex > formerParamIndex + 1) {
				endIndex = findParamPos(sql, paramIndex - formerParamIndex - 1,
						beginIndex);
				newSqlBuf.append(sql.subSequence(beginIndex, endIndex));
				beginIndex = endIndex;
				formerParamIndex = paramIndex-1;
			}
			endIndex = findParamPos(sql, 1, beginIndex);

			if (nullParamList.contains(paramIndex)) {// 空参数
				int substitutePos = findSubstitutionBeginIndex(sql, beginIndex,
						endIndex);
				if (substitutePos >= 0) {// 是需要被替换成 is null的
					newSqlBuf.append(sql, beginIndex, substitutePos);
					newSqlBuf.append(" is null");
					replaceParamIndicesList.add(paramIndex);
				} else {// 不能被替换的
					if (useStmtInsteadOfPstmt) {
						substitutePos = endIndex - 1;
						newSqlBuf.append(sql, beginIndex, substitutePos);
						newSqlBuf.append("null");
						replaceParamIndicesList.add(paramIndex);
					} else {
						newSqlBuf.append(sql, beginIndex, endIndex);
					}
					nullParamList.remove(paramIndex);// 注意，这里删除不是按索引来删除，而是按对象来删除（虽然参数是一个整数）
				}
				beginIndex = endIndex;
				formerParamIndex = paramIndex;
			} else if (useStmtInsteadOfPstmt) {// 替换可能的参数
				String replaceStr = null;
				boolean canbeReplaced = false;
				try {
					replaceStr = convertParam(argList.get(paramIndex), argTypeList==null?null:argTypeList.get(paramIndex));// 获得转换后的值
					canbeReplaced = true;
				} catch (Throwable th) {
				}
				if (canbeReplaced) {
					int substitutePos = endIndex - 1;
					newSqlBuf.append(sql, beginIndex, substitutePos);
					newSqlBuf.append(replaceStr);
					replaceParamIndicesList.add(paramIndex);
					beginIndex = endIndex;
					formerParamIndex = paramIndex;
				}
			}
		}

		if (nullParamList.isEmpty() && !useStmtInsteadOfPstmt) {
			// 没有null参数也不需要管了
			return new Object[] { sql, argList, argTypeList };
		}
		newSqlBuf.append(sql.substring(beginIndex));// 加上尾巴
		argList = new ArrayList<Object>(argList);
		if (argTypeList != null)
			argTypeList = new ArrayList<Integer>(argTypeList);
		for (int i = replaceParamIndicesList.size() - 1; i >= 0; i--) {
			int index = replaceParamIndicesList.get(i);
			argList.remove(index);
			if (argTypeList != null)
				argTypeList.remove(index);
		}
		if (log.isDebugEnabled()) {
			log.debug("修补后sql：" + newSqlBuf.toString());
		}
		return new Object[] { newSqlBuf.toString(), argList, argTypeList };
	}

	/**
	 * 通俗的说，数Sql语句在某个区间中问号的个数 注意，被包含在单引号中的问号需要剔除
	 * 
	 * @param sql
	 *            sql字符串
	 * @param beginIndex
	 *            查找的字符开始位置（千万不要位于引号的字符串中间，否则我会玩完），以0为基
	 * @param endIndex
	 *            查找的字符结束位置，以0为基（不含）
	 * @return int 参数占位符个数
	 */
	static int countParamPlaceHolder(String sql, int beginIndex, int endIndex) {
		int count = 0;
		boolean inQuote = false;// 被引用在字符串中
		boolean maybeEndQuote = false;// 可能是关闭字符串的引号，它为真时inQuote肯定为真，inQuote为假时两者都为假
		for (int i = beginIndex; i < endIndex; i++) {
			char c = sql.charAt(i);
			if (c == '\'') {
				if (!inQuote) {// 不在引用字符串中
					inQuote = true;
				} else if (maybeEndQuote) {// 在引用字符串中且前一个字符为单引号（此时可判断前一个单引号为转义单引号）
					maybeEndQuote = false;
				} else {// 在引用字符串中且为单引号
					maybeEndQuote = true;
				}
			} else {
				if (maybeEndQuote) {// 因为前一个字符为“关”单引号
					maybeEndQuote = false;
					inQuote = false;
				}
				if (!inQuote && c == '?') {// 不在单引号中的问号可计数
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 返回某个参数占位符在sql语句中之后一个字符的位置
	 * 
	 * @param sql
	 *            sql字符串
	 * @param paramIndex
	 *            参数位置，以1为基，1代表第一个参数，……
	 * @param beginIndex
	 *            查找的字符开始位置（千万不要位于引号的字符串中间，否则我会玩完），以0为基
	 * @return int 参数占位符(?)的后一个字符的位置，如果没有找到，则返回-1
	 */
	static int findParamPos(String sql, int paramIndex, int beginIndex) {
		if (paramIndex == 0) {
			return beginIndex;
		}
		int count = 0;
		boolean inQuote = false;// 被引用在字符串中
		boolean maybeEndQuote = false;// 可能是关闭字符串的引号
		for (int i = beginIndex, in = sql.length(); i < in; i++) {
			char c = sql.charAt(i);
			if (c == '\'') {
				if (!inQuote) {// 不在引用字符串中
					inQuote = true;
				} else if (maybeEndQuote) {// 在引用字符串中且前一个字符为单引号（此时可判断前一个单引号为转义单引号）
					maybeEndQuote = false;
				} else {// 在引用字符串中且为单引号
					maybeEndQuote = true;
				}
			} else {
				if (maybeEndQuote) {// 因为前一个字符为“关”单引号
					maybeEndQuote = false;
					inQuote = false;
				}
				if (!inQuote && c == '?') {// 不在单引号中的问号可计数
					count++;
				}
			}
			if (count == paramIndex)
				return i + 1;
		}
		return -1;
	}

	/**
	 * 找到需要替换情况下，替换的起始位置，比如where a = ?，如果对应参数值为null应该被替换为where a is null<br>
	 * 那么替换的起始位置就是=的位置。<br>
	 * 如果是不需要替换的情况，则返回-1。
	 * 
	 * @param sql
	 * @param beginIndex
	 * @param endIndex
	 * @return int
	 */
	private static int findSubstitutionBeginIndex(String sql, int beginIndex,
			int endIndex) {
		boolean findEqualsFirst = false;
		for (int i = endIndex - 2; i >= beginIndex; i--) {
			switch (sql.charAt(i)) {
			case ' ':// \t\n\f\r
			case '\t':
			case '\n':
			case '\f':
			case '\r': {
				break;
			}
			case '=': {
				if (findEqualsFirst) {
					return -1;
				} else {
					findEqualsFirst = true;
				}
				break;
			}
			case '>':// 如果还有其它字符影响能否替换，一并加在这里即可
			case '<': {
				return -1;
			}
			default:
				if (!findEqualsFirst) {
					return -1;
				} else {
					return i + 1;
				}
			}
		}
		return -1;
	}

	private static int[] convertList(List<Integer> list) {
		if (list == null || list.size() == 0) {
			return null;
		} else {
			int[] a = new int[list.size()];
			int index = 0;
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				Integer element = (Integer) iter.next();
				a[index++] = element;
			}
			return a;
		}
	}

	private static Object[] convertObjectList(List<Object> list) {
		if (list == null || list.size() == 0) {
			return null;
		} else {
			return list.toArray();
		}
	}

	/**
	 * 将数组转换成整型数据列表
	 * 
	 * @param a
	 * @return
	 */
	private static List<Integer> convertArray(int[] a) {
		if (a == null || a.length == 0) {
			return null;
		} else {
			List<Integer> list = new ArrayList<Integer>();
			for (int i : a) {
				list.add(i);
			}
			return list;
		}
	}

	/**
	 * 将带命名参数的sql语句和命名参数-值Map转换为普通的带参数sql（参数用"?"表示）和参数列表。 参数名不区分大小写。
	 * 
	 * @param sql
	 *            带命名参数的sql语句
	 * @param argMap
	 *            命名参数-值Map
	 * @param argList
	 *            参数列表。argList为一个[out]型参数，需要在调用本方法之前构造好，调用本方法后<br>
	 *            该列表中的值即为参数列表，其元素和转换后的sql语句中参数一一对应。
	 * @return 转换后的普通带参数sql
	 */
	String changeNamedParamSql2CommonSql(String sql, Map<String, ?> argMap,
			List<Object> argList) {
		StringBuffer resultSql = new StringBuffer();
		if (argMap != null) {
			Map<String, Object> newArgMap = new HashMap<String, Object>();
			for (String key : argMap.keySet()) {
				newArgMap.put(key == null ? null : key.toUpperCase(), argMap
						.get(key));
			}
			argMap = newArgMap;
		}
		argList.clear();
		Matcher regexMatcher = paramSqlRegex.matcher(sql);
		while (regexMatcher.find()) {
			String groupValue = regexMatcher.group(1).toUpperCase();
			if (argMap != null && argMap.containsKey(groupValue)) {
				regexMatcher.appendReplacement(resultSql, "?");
				argList.add(argMap.get(groupValue));
			}
		}
		regexMatcher.appendTail(resultSql);
		return resultSql.toString();
	}

	static String convertParam(Object value, Integer sqlType) {
		if (value == null) {
			return "null";
		}
		if(sqlType != null){
			switch (sqlType) {
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:{
				throw new IllegalArgumentException("不能转换");
			}
			}
		}
		if (value instanceof Number) {
			Number num = (Number) value;
			if (num.doubleValue() == num.longValue()) {
				return "" + num.longValue();
			} else {
				return "" + num.doubleValue();
			}
		} else if (value instanceof String || value instanceof StringBuffer
				|| value instanceof StringWriter) {
			return escapeString(value.toString());
		} else if (value instanceof Date) {
			return formatDate((Date) value);
		} else if (value instanceof Calendar) {
			return formatDate(((Calendar) value).getTime());
		} else if (value instanceof Boolean) {
			return (Boolean) value ? "1" : "0";
		} else {
			throw new IllegalArgumentException("不能转换");
		}
	}

	static String escapeString(String origStr) {
		int index = origStr.indexOf('\'');
		if (index < 0) {
			return "\'" + origStr + "\'";
		} else {
			StringBuffer newStrBuf = new StringBuffer("\'");
			for (int i = 0, in = origStr.length(); i < in; i++) {
				char c = origStr.charAt(i);
				if (c == '\'') {
					newStrBuf.append("''");
				} else {
					newStrBuf.append(c);
				}
			}
			newStrBuf.append("\'");
			return newStrBuf.toString();
		}
	}

	/**
	 * 转换日期（这个方法可能需要移入Dialect，因为不同数据库不知道对字符串表示的日期是否处理一致——阳某按）
	 * @param date
	 * @return String
	 */
	static String formatDate(Date date) {
		return "\'" + df.format(date) + "\'";
	}
}
