package snt.common.rs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供连接的方式 创建日期：(2001-10-17 19:54:35)
 * 
 * @author ：yx
 */
public class ResultJoinTool extends MrsToolBase implements IResultSetConst {
	/** 原始结果集 */
	private MemoryResultSet m_SourceResultSet = null;

	/** 需要JOIN 的结果集 */
	private List m_vctJoinResultSet = new ArrayList();

	private int m_nJoinMode = LEFT_OUT_JOIN;

	/**
	 * ResultSumTool 构造子注解。
	 */
	public ResultJoinTool() {
		super();
	}

	public void addJoinResultSet(java.sql.ResultSet rs, String[] joinKey,
			String joinValue[]) throws SQLException {
		JoinStruct js = new JoinStruct();
		js.setJoinResultSet(ComTool.convertResultSet(rs));
		js.setJoinKey(joinKey);
		js.setJoinFields(joinValue);
		m_vctJoinResultSet.add(js);
	}

	public void addJoinResultSet(java.sql.ResultSet rs, String joinKey,
			String joinValue[]) throws SQLException {
		String[] joinKeys = { joinKey };
		addJoinResultSet(rs, joinKeys, joinValue);
	}

	/**
	 * 执行计算 创建日期：(2001-10-17 20:02:13)
	 * 
	 * @return snt.common.rs.MemoryResultSet
	 */
	public MemoryResultSet execute() throws java.sql.SQLException {
		return join();
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:01:32)
	 * 
	 * @return int
	 */
	public int getJoinMode() {
		return m_nJoinMode;
	}

	private MemoryResultSet join() throws SQLException {
		/** 拼接 */
		MemoryResultSet mrsResult = m_SourceResultSet;

		for (int i = 0; i < m_vctJoinResultSet.size(); i++) {
			/** 1.添加 METER DATA */
			JoinStruct js = (JoinStruct) m_vctJoinResultSet.get(i);
			MemoryResultSet jrs = (MemoryResultSet) js.getJoinResultSet();
			MemoryResultSetMetaData mrsmdJoin = (MemoryResultSetMetaData) jrs
					.getMetaData();

			mrsResult.appendColumn(mrsmdJoin, js.getJoinFields());
			int newLength = mrsResult.getMetaData().getColumnCount();
			/** 读出需要做连接的值 */
			int keyIndexSource[] = fetchIndex(mrsResult.getMetaData0(), js
					.getJoinKey());
			int keyIndexJoin[] = fetchIndex(mrsmdJoin, js.getJoinKey());
			int joinFieldsLocation[] = fetchIndex(mrsmdJoin, js.getJoinFields());
			int targetLocation[] = fetchIndex(mrsResult.getMetaData0(), js
					.getJoinFields());

			boolean bAppendWhenNoMatch = getJoinMode() == LEFT_OUT_JOIN
					|| getJoinMode() == JOIN_ALL;
			/** JOIN 是一个 双重循环 */
			/** 提高比较的速度,进行HASH */
			Map hashJoinMatch = new HashMap();
			jrs.beforeFirst();
			while (jrs.next()) {
				List vctOneLineJoin = jrs.getRowList();
				Object[] oneKeyJoin = fetchObject(vctOneLineJoin, keyIndexJoin);
				RowKey rkJoin = new RowKey(oneKeyJoin);
				List alListRecord = (List) hashJoinMatch.get(rkJoin);
				if (alListRecord == null) {
					alListRecord = new ArrayList();
					hashJoinMatch.put(rkJoin, alListRecord);
				}
				alListRecord.add(new Integer(jrs.getRow() - 1));
			}
			/** 进行比较 */
			boolean[] secondHasJoined = new boolean[jrs.getResultList()
					.size()];
			Arrays.fill(secondHasJoined, false);
			mrsResult.beforeFirst();
			List alJoinResult = new ArrayList();
			while (mrsResult.next()) {
				List vctOneLineSource = mrsResult.getRowList();
				Object[] oneKeySource = fetchObject(vctOneLineSource,
						keyIndexSource);
				RowKey rkSource = new RowKey(oneKeySource);
				boolean matched = false;
				List alMatchRow = (List) hashJoinMatch.get(rkSource);
				if (alMatchRow != null) {
					for (int k = 0; k < alMatchRow.size(); k++) {
						Integer iLoc = (Integer) alMatchRow.get(k);
						int nLoc = iLoc.intValue();
						List vctOneLineJoin = (List) jrs
								.getResultList().get(iLoc.intValue());
						secondHasJoined[nLoc] = true;
						/** 产生一个新的记录 */
						List al = new ArrayList(vctOneLineSource.size());
						al.addAll(vctOneLineSource);
						for (int j = 0; j < joinFieldsLocation.length; j++) {
							al.set(targetLocation[j], vctOneLineJoin
									.get(joinFieldsLocation[j]));
						}
						alJoinResult.add(al);
					}
				} else if (!matched && bAppendWhenNoMatch) {
					List al = new ArrayList(vctOneLineSource.size());
					al.addAll(vctOneLineSource);
					alJoinResult.add(al);
				}
			}
			if (getJoinMode() == JOIN_ALL || getJoinMode() == RIGHT_OUT_JOIN) {
				/** 增加没有被处理过的行 */
				for (int k = 0; k < secondHasJoined.length; k++) {
					if (!secondHasJoined[k]) {
						List vctOneLineJoin = (List) jrs
								.getResultList().get(k);
						List al = getNullLine(newLength);
						for (int j = 0; j < joinFieldsLocation.length; j++) {
							al.set(targetLocation[j], vctOneLineJoin
									.get(joinFieldsLocation[j]));
						}
						/* 添加连接参照列信息 */
						for (int m = 0; m < keyIndexJoin.length; m++) {
							al.set(keyIndexSource[m], vctOneLineJoin
									.get(keyIndexJoin[m]));
						}
						/* 添加完毕 */
						alJoinResult.add(al);
					}
				}
			}
			mrsResult = new MemoryResultSet(alJoinResult, mrsResult
					.getMetaData0());
		}
		return mrsResult;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:01:32)
	 * 
	 * @param newJoinMode
	 *            int
	 */
	public void setJoinMode(int newJoinMode) {
		m_nJoinMode = newJoinMode;
	}

	/**
	 * 设置初始的RESULT SET 创建日期：(2001-10-17 19:55:51)
	 * 
	 * @param newSourceResultSet
	 *            java.sql.ResultSet
	 */
	public void setSourceResultSet(java.sql.ResultSet newSourceResultSet)
			throws SQLException {
		m_SourceResultSet = ComTool.convertResultSet(newSourceResultSet);
	}
}