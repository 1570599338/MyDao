package snt.common.rs;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将多个RESULT SET按照规定的方式加起来 创建日期：(2001-10-17 19:54:35)
 * 
 * @author ：yx
 */
public class ResultSumTool extends MrsToolBase implements IResultSetConst {
	/** 原始结果集 */
	private MemoryResultSet m_SourceResultSet = null;

	/** 填加的KEY值 */
	private java.lang.String[] m_SumKey;

	/** 作合计的KEY值 */
	private java.lang.String[] m_SumValueKey;

	/** 需要做合计的Vector值 */
	private List m_vctAddupResultSet = new ArrayList();

	private java.lang.String[] m_strOrderRule;

	private int m_nSumMode = JOIN_MODE;
	
	/** 合计项符号标志(zjb+) */
	private List m_vecSign = new ArrayList();

	/**
	 * ResultSumTool 构造子注解。
	 */
	public ResultSumTool() {
		super();
	}

	public void addSumResultSet(java.sql.ResultSet rs) throws SQLException {
		m_vctAddupResultSet.add(ComTool.convertResultSet(rs));
		m_vecSign.add(new Boolean(true));
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:43:22)
	 * 
	 * @param source
	 *            java.util.List
	 * @param add
	 *            java.util.List
	 * @param loc
	 *            int[]
	 */
	private void addUpValue(List source, List add, int[] loc)
			throws SQLException {
		for (int i = 0; i < loc.length; i++) {
			Object o = source.get(loc[i]);
			Object oa = add.get(loc[i]);
			if (oa == null)
				continue;
			if (o == null) {
				// ////
				o = new Double(0);
			}
			if (o instanceof java.math.BigDecimal) {
				java.math.BigDecimal bd = (java.math.BigDecimal) o;
				if (oa instanceof java.math.BigDecimal) {
					bd = bd.add((java.math.BigDecimal) oa);
				} else {
					bd = bd.add(new java.math.BigDecimal("" + oa));
				}
				source.set(loc[i], bd);
			} else if (o instanceof Double) {
				Double d = (Double) o;
				if (oa instanceof Double) {
					d = new Double(d.doubleValue()
							+ ((Double) oa).doubleValue());
				} else {
					d = new Double(d.doubleValue()
							+ Double.parseDouble("" + oa));
				}
				source.set(loc[i], d);
			} else if (o instanceof Integer) {
				Integer d = (Integer) o;
				if (oa instanceof Integer) {
					d = new Integer(d.intValue() + ((Integer) oa).intValue());
				} else {
					d = new Integer(d.intValue() + Integer.parseInt("" + oa));
				}
				source.set(loc[i], d);
			} else if (o instanceof Long) {
				Long d = (Long) o;
				if (oa instanceof Long) {
					d = new Long(d.longValue() + ((Long) oa).longValue());
				} else {
					d = new Long(d.longValue() + Long.parseLong("" + oa));
				}
				source.set(loc[i], d);
			} else
				throw new SQLException("Invalidate Num type" + o.getClass());
		}
	}

	/**
	 * 执行计算 创建日期：(2001-10-17 20:02:13)
	 * 
	 * @return snt.common.rs.MemoryResultSet
	 */
	public MemoryResultSet execute() throws java.sql.SQLException {
		Map hashSum = new HashMap();
		List vctRows = new ArrayList();
		/** 列的位置 */
		MemoryResultSetMetaData rsmd = (MemoryResultSetMetaData) m_SourceResultSet
				.getMetaData();
		int sumKeyLoc[] = fetchIndex(rsmd, getSumKey());
		int valueKeyLoc[] = fetchIndex(rsmd, getSumValueKey());
		/** 填写数值 */
		MemoryResultSet aryResultSet[] = new MemoryResultSet[m_vctAddupResultSet
				.size() + 1];
		aryResultSet[0] = m_SourceResultSet;
		for (int i = 0; i < m_vctAddupResultSet.size(); i++) {
			aryResultSet[i + 1] = (MemoryResultSet) m_vctAddupResultSet.get(i);
		}
		/** 处理符号(zjb+) */
		boolean bSigns[] = new boolean[m_vecSign.size() + 1];
		bSigns[0] = true;
		for (int i = 0; i < m_vecSign.size(); i++)
			bSigns[i + 1] = ((Boolean) m_vecSign.get(i)).booleanValue();
		//
		for (int i = 0; i < aryResultSet.length; i++) {
			MemoryResultSet mrs = aryResultSet[i];
			mrs.beforeFirst();
			while (mrs.next()) {
				List vctOneLine = mrs.getRowList();
				Object[] oneKey = fetchObject(vctOneLine, sumKeyLoc);
				RowKey rk = new RowKey(oneKey);
				/** 添加到Map中以便查找 */
				/** 添加到List中以便进行结果集返回 */
				Object oRow = hashSum.get(rk);
				if (oRow == null) {
					/** 找到一个新的记录 */
					if (i == 0 || getSumMode() == JOIN_MODE) {
						hashSum.put(rk, vctOneLine);
						vctRows.add(vctOneLine);
					}
				} else {
					List oneRow = (List) oRow;
					if (bSigns[i])
						addUpValue(oneRow, vctOneLine, valueKeyLoc);
					else
						subtractValue(oneRow, vctOneLine, valueKeyLoc);
				}
			}			
		}
		/** 由于数据比较复杂，需要清理掉不需要的内容 */
		MemoryResultSet mrsResultSet = new MemoryResultSet(vctRows, rsmd);
		return mrsResultSet;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:01:32)
	 * 
	 * @return int
	 */
	public int getSumMode() {
		return m_nSumMode;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:00:28)
	 * 
	 * @return java.lang.String[]
	 */
	public java.lang.String[] getOrderRule() {
		return m_strOrderRule;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 19:55:51)
	 * 
	 * @return java.sql.ResultSet
	 */
	public java.sql.ResultSet getSourceResultSet() {
		return m_SourceResultSet;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 19:57:15)
	 * 
	 * @return java.lang.String[]
	 */
	public java.lang.String[] getSumKey() {
		return m_SumKey;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 19:57:30)
	 * 
	 * @return java.lang.String[]
	 */
	public java.lang.String[] getSumValueKey() {
		return m_SumValueKey;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:01:32)
	 * 
	 * @param newSumMode
	 *            int
	 */
	public void setSumMode(int newSumMode) {
		m_nSumMode = newSumMode;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 20:00:28)
	 * 
	 * @param newOrderRule
	 *            java.lang.String[]
	 */
	public void setOrderRule(java.lang.String[] newOrderRule) {
		m_strOrderRule = newOrderRule;
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

	/**
	 * 设置计算的的主KEY 创建日期：(2001-10-17 19:57:15)
	 * 
	 * @param newSumKey
	 *            java.lang.String[]
	 */
	public void setSumKey(java.lang.String[] newSumKey) {
		m_SumKey = newSumKey;
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-17 19:57:30)
	 * 
	 * @param newSumValueKey
	 *            java.lang.String[]
	 */
	public void setSumValueKey(java.lang.String[] newSumValueKey) {
		m_SumValueKey = newSumValueKey;
	}	

	/**
	 * 增加合计项（bPlus为符号标志，true代表+，false代表-） zjb+
	 */
	public void addSumResultSet(java.sql.ResultSet rs, boolean bPlus)
			throws SQLException {
		m_vctAddupResultSet.add(ComTool.convertResultSet(rs));
		m_vecSign.add(new Boolean(bPlus));
	}

	/**
	 * 执行相减（zjb+） 创建日期：(2001-10-17 20:43:22)
	 * 
	 * @param source
	 *            java.util.List
	 * @param add
	 *            java.util.List
	 * @param loc
	 *            int[]
	 */
	private void subtractValue(List source, List add, int[] loc)
			throws SQLException {
		for (int i = 0; i < loc.length; i++) {
			Object o = source.get(loc[i]);
			Object oa = add.get(loc[i]);
			if (oa == null)
				continue;
			if (o == null) {
				// ////
				o = new Double(0);
			}
			if (o instanceof java.math.BigDecimal) {
				java.math.BigDecimal bd = (java.math.BigDecimal) o;
				if (oa instanceof java.math.BigDecimal) {
					bd = bd.subtract((java.math.BigDecimal) oa);
				} else {
					bd = bd.subtract(new java.math.BigDecimal("" + oa));
				}
				source.set(loc[i], bd);
			} else if (o instanceof Double) {
				Double d = (Double) o;
				if (oa instanceof Double) {
					d = new Double(d.doubleValue()
							- ((Double) oa).doubleValue());
				} else {
					d = new Double(d.doubleValue()
							- Double.parseDouble("" + oa));
				}
				source.set(loc[i], d);
			} else if (o instanceof Integer) {
				Integer d = (Integer) o;
				if (oa instanceof Integer) {
					d = new Integer(d.intValue() - ((Integer) oa).intValue());
				} else {
					d = new Integer(d.intValue() - Integer.parseInt("" + oa));
				}
				source.set(loc[i], d);
			} else if (o instanceof Long) {
				Long d = (Long) o;
				if (oa instanceof Long) {
					d = new Long(d.longValue() - ((Long) oa).longValue());
				} else {
					d = new Long(d.longValue() - Long.parseLong("" + oa));
				}
				source.set(loc[i], d);
			} else
				throw new SQLException("Invalidate Num type" + o.getClass());
		}
	}
}
