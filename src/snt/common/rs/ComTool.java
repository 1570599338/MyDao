package snt.common.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 数据提取小工具，直接提取数据，打印数据等功能 创建日期：(2001-10-18 8:34:55)
 * 
 * @author ：阳雄
 */
public class ComTool implements IResultSetConst {

	public final static boolean beDebug = false;

	public final static int N_JW = 10000;

	private static Random r = new Random();

	/**
	 * ComTool 构造子注解。
	 */
	public ComTool() {
		super();
	}

	/**
	 * 
	 * 将一个结果集添加到另一个结果集后面。 Keys[] 是进行匹配的字段
	 * 
	 */
	public static MemoryResultSet appendDistinctResult(MemoryResultSet source,
			MemoryResultSet append, String keys[]) throws SQLException {
		/** 删除相同记录 */
		List oa = MrsToolBase.fetchComKey(source, keys);
		List newAl = new ArrayList();
		Set hasht = new HashSet();
		for (int i = 0; i < oa.size(); i++) {
			CombRowKey crk = (CombRowKey) oa.get(i);
			if (!hasht.contains(crk)) {
				newAl.add(crk.getUserObject());
				hasht.add(crk);
			}
		}
		oa = MrsToolBase.fetchComKey(append, keys);
		int[] appendkeyIndex = MrsToolBase.fetchIndex(append.getMetaData0(),
				keys);
		int[] srckeyIndex = MrsToolBase.fetchIndex(source.getMetaData0(), keys);
		int columnCount = source.getMetaData().getColumnCount();
		for (int i = 0; i < oa.size(); i++) {
			CombRowKey crk = (CombRowKey) oa.get(i);
			if (!hasht.contains(crk)) {
				// newAl.add(crk.getUserObject());
				Object[] objs = MrsToolBase.fetchObject((List) crk
						.getUserObject(), appendkeyIndex);
				List newLine = MrsToolBase.getNullLine(columnCount);
				for (int j = 0; j < objs.length; j++) {
					Object object = objs[j];
					newLine.set(srckeyIndex[j], object);
				}
				newAl.add(newLine);
				hasht.add(crk);
			}
		}
		return new MemoryResultSet(newAl, (MemoryResultSetMetaData) source
				.getMetaData());

	}

	/**
	 * 将一个结果集添加到另一个结果集后面。 这两个结果集的元数据必须同构
	 * 
	 * @param source
	 * @param append
	 * @param keys
	 *            进行匹配的字段
	 * @param delSame
	 *            是否删除相同的记录
	 * @return MemoryResultSet
	 * @throws SQLException
	 */
	public static MemoryResultSet appendResult(MemoryResultSet source,
			MemoryResultSet append, String keys[], boolean delSame)
			throws SQLException {
		if (!delSame)
			return combineResultSet(source, append, keys, false,
					IResultSetConst.SIMPLE_COMBINE);
		/** 删除相同记录 */
		List newAl = new ArrayList();
		Set hasht = new HashSet();
		MemoryResultSet mrsA[] = { source, append };
		for (int k = 0; k < mrsA.length; k++) {
			List oa = MrsToolBase.fetchComKey(mrsA[k], keys);
			for (int i = 0; i < oa.size(); i++) {
				CombRowKey crk = (CombRowKey) oa.get(i);
				if (!hasht.contains(crk)) {
					newAl.add(crk.getUserObject());
					hasht.add(crk);
				}
			}
		}
		return new MemoryResultSet(newAl, (MemoryResultSetMetaData) source
				.getMetaData());
	}

	/**
	 * 合并结果集
	 * 
	 * @param rs1
	 * @param rs2
	 * @param keys
	 * @param sortIt
	 * @param type
	 *            见IResultSetConst
	 * @return MemoryResultSet
	 * @throws SQLException
	 */
	public static MemoryResultSet combineResultSet(ResultSet rs1,
			ResultSet rs2, String keys[], boolean sortIt, int type)
			throws SQLException {
		ResultCombineTool rct = new ResultCombineTool();
		rct.setSortIt(sortIt);
		rct.setSortOrder(type);
		rct.setSourceResultSet(rs1);
		rct.addResultSet(rs2);
		rct.setCombineKey(keys);
		return rct.execute();
	}

	/**
	 * 得到唯一的数据 创建日期：(2001-10-19 13:07:34)
	 * 
	 * @return snt.common.rs.MemoryResultSet
	 * @param mrs
	 *            snt.common.rs.MemoryResultSet
	 * @param keys
	 *            java.lang.String[]
	 */
	public static MemoryResultSet getDistinctResultSet(MemoryResultSet mrs,
			String[] keys) throws SQLException {
		Object[] allKey = MrsToolBase.fetchComKey(mrs, keys).toArray();
		Set keySet = new HashSet();
		for (int i = 0; i < allKey.length; i++) {
			if (!keySet.contains(allKey[i]))
				keySet.add(allKey[i]);
		}
		Iterator it = keySet.iterator();
		List al = new ArrayList();
		int[] indexs = MrsToolBase.fetchIndex(mrs.getMetaData0(), keys);
		while (it.hasNext()) {
			CombRowKey crk = (CombRowKey) it.next();
			al.add(MrsToolBase.copyRow((List) crk.getUserObject(), indexs));
		}
		MemoryResultSet mrsNew = new MemoryResultSet(al,
				(MemoryResultSetMetaData) mrs.getMetaData());
		return mrsNew;
	}

	/**
	 * 连接两个结果集
	 * @param rs1
	 * @param rs2
	 * @param keys
	 * @param joinValue
	 * @param type {@link IResultSetConst}
	 * @return MemoryResultSet
	 * @throws SQLException
	 */
	public static MemoryResultSet joinResultSet(ResultSet rs1, ResultSet rs2,
			String keys[], String joinValue[], int type) throws SQLException {
		ResultJoinTool rjt = new ResultJoinTool();
		rjt.setSourceResultSet(rs1);
		rjt.addJoinResultSet(rs2, keys, joinValue);
		rjt.setJoinMode(type);
		return rjt.execute();
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-19 13:08:55)
	 * 
	 * @return snt.common.rs.MemoryResultSet
	 * @param rs1
	 *            java.sql.ResultSet
	 * @param rs2
	 *            java.sql.ResultSet
	 * @param type
	 *            int
	 */
	public static MemoryResultSet joinResultSet(ResultSet rs1, ResultSet rs2,
			String key, String joinValue[], int type) throws SQLException {
		ResultJoinTool rjt = new ResultJoinTool();
		rjt.setSourceResultSet(rs1);
		rjt.addJoinResultSet(rs2, key, joinValue);
		rjt.setJoinMode(type);
		return rjt.execute();
	}

	/**
	 * 相加结果集
	 * @param rs1
	 * @param rs2
	 * @param keys
	 * @param sumValue
	 * @param type
	 * @return MemoryResultSet
	 * @throws SQLException
	 */
	public static MemoryResultSet sumResultSet(ResultSet rs1, ResultSet rs2,
			String keys[], String sumValue[], int type) throws SQLException {
		ResultSumTool rst = new ResultSumTool();
		rst.setSourceResultSet(rs1);
		rst.addSumResultSet(rs2);
		rst.setSumKey(keys);
		rst.setSumValueKey(sumValue);
		rst.setSumMode(type);
		return rst.execute();
	}

	/**
	 * 此处插入方法描述。 创建日期：(2002-11-5 10:21:59)
	 * 
	 * @return java.lang.String
	 * @param mrs
	 *            snt.common.rs.MemoryResultSet
	 * @param columnName
	 *            java.lang.String
	 */
	public static String[] fetchStringArray(MemoryResultSet mrs,
			String columnName) throws SQLException {
		int loc = mrs.getColumnIndex(columnName);
		List al = mrs.getResultList();
		String strArray[] = new String[al.size()];
		for (int i = 0; i < al.size(); i++) {
			List alRow = (List) al.get(i);
			Object oStr = alRow.get(loc - 1);
			if (oStr == null || oStr instanceof String)
				strArray[i] = (String) oStr;
			else
				strArray[i] = oStr.toString();
		}
		return strArray;
	}

	public static String getTempName() {
		long l = System.currentTimeMillis();
		/** 到秒级，＋随机数 */
		l = (l / 1000) % (N_JW) * N_JW + r.nextInt() % N_JW;
		return "" + l;
	}

	public static MemoryResultSet convertResultSet(ResultSet rs)
			throws SQLException {
		if (rs instanceof MemoryResultSet) {
			return (MemoryResultSet) rs;
		} else {
			return new MemoryResultSet(rs);
		}
	}

	/**
	 * 通过数据库连接和查询语句获取结果集
	 * 创建日期：(2001-10-18 8:35:33)
	 * @return snt.common.rs.MemoryResultSet
	 * @param con java.sql.Connection
	 * @param sql java.lang.String
	 */
	public static MemoryResultSet getMemoryResultSet(java.sql.Connection con,
			String sql) throws SQLException {
		return getMemoryResultSet(con, sql, false, false);
	}

	/**
	 * 通过数据库连接和查询语句获取结果集
	 * 创建日期：(2001-10-18 8:35:33)
	 * @return snt.common.rs.MemoryResultSet
	 * @param con java.sql.Connection
	 * @param sql java.lang.String
	 */
	public static MemoryResultSet getMemoryResultSet(java.sql.Connection con,
			String sql, boolean isStringTrim) throws SQLException {
		return getMemoryResultSet(con, sql, isStringTrim, true);
	}

	/**
	 * 通过数据库连接和查询语句获取结果集
	 * @param con
	 * @param sql
	 * @param isStringTrim
	 * @param tamperData
	 * @return MemoryResultSet
	 * @throws SQLException
	 */
	public static MemoryResultSet getMemoryResultSet(java.sql.Connection con,
			String sql, boolean isStringTrim, boolean tamperData)
			throws SQLException {
		SQLException es = null;
		Statement state = null;
		ResultSet rs = null;
		MemoryResultSet mrs = null;
		try {
			state = con.createStatement();
			rs = state.executeQuery(sql);
			mrs = new MemoryResultSet(rs, isStringTrim, tamperData);
		} catch (SQLException e) {
			es = e;
		}
		if (rs != null)
			try {
				rs.close();
			} catch (Exception e) {
			}
		if (state != null)
			try {

				state.close();
			} catch (Exception e) {
			}
		if (es != null)
			throw es;
		return mrs;
	}
}
