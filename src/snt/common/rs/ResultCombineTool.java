package snt.common.rs;

import java.util.*;
import java.sql.*;

/**
 * 提供连接的方式
 * 创建日期：(2001-10-17 19:54:35)
 * @author ：yx
 */
public class ResultCombineTool extends MrsToolBase implements IResultSetConst{
	/** 原始的RESULT SET */
	private MemoryResultSet m_SourceResultSet = null;
	/** 需要合并得 RESULT SET  */
	private ArrayList m_vctCombine = new ArrayList();
	/** 需要合并 关键值 */
	private java.lang.String[] m_CombineKey;
	/** 是否排序 */
	private boolean m_beSortIt = false;
	/** 合并方向 */
	private int m_nSortOrder = SORT_ASCEND;
/**
 * ResultSumTool 构造子注解。
 */
public ResultCombineTool() {
	super();
}
public void addResultSet(ResultSet rs) throws SQLException {
	m_vctCombine.add(ComTool.convertResultSet(rs));
}
public MemoryResultSet combine() throws java.sql.SQLException {
	MemoryResultSetMetaData mrsmd =
		(MemoryResultSetMetaData) m_SourceResultSet.getMetaData();
	List vctResult = new ArrayList();
	if (getSortOrder() != SIMPLE_COMBINE) {
		int keys[] = fetchIndex(mrsmd, getCombineKey());
		List allRowKey = fetchComKey(m_SourceResultSet.getResultList(), keys);
		for (int j = 0; j < m_vctCombine.size(); j++) {
			MemoryResultSet mrs = (MemoryResultSet) m_vctCombine.get(j);
			List appendRowKey = fetchComKey(mrs.getResultList(), keys);
			allRowKey = combine(allRowKey, appendRowKey);
		}
		for (int i = 0; i < allRowKey.size(); i++) {
			vctResult.add(((CombRowKey) allRowKey.get(i)).getUserObject());
		}
	} else {
		vctResult.addAll(m_SourceResultSet.getResultList());
		for (int j = 0; j < m_vctCombine.size(); j++) {
			MemoryResultSet mrs = (MemoryResultSet) m_vctCombine.get(j);
			vctResult.addAll(mrs.getResultList());
		}
	}
	return new MemoryResultSet(vctResult, mrsmd);
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-18 16:26:24)
 * @return java.util.List
 * @param vctRowKey1 java.util.List
 * @param vctRowKey2 java.util.List
 */
private List combine(List vctRowKey1, List vctRowKey2)
	throws SQLException {
	List newAllRowKey = new ArrayList();
	int loc1 = 0;
	int loc2 = 0;
	//---------合并
	while (loc1 < vctRowKey1.size() && loc2 < vctRowKey2.size()) {
		CombRowKey crk1 = (CombRowKey) vctRowKey1.get(loc1);
		CombRowKey crk2 = (CombRowKey) vctRowKey2.get(loc2);
		boolean chooseKey =
			CombRowKey.compareRowKey(crk1, crk2) <= 0 && getSortOrder() == SORT_ASCEND;
		if (chooseKey) {
			newAllRowKey.add(crk1);
			loc1++;
		} else {
			newAllRowKey.add(crk2);
			loc2++;
		}
	}
	//--------扫尾
	if (loc1 < vctRowKey1.size()) {
		while (loc1 < vctRowKey1.size()) {
			newAllRowKey.add(vctRowKey1.get(loc1++));
		}
	}
	if (loc1 < vctRowKey2.size()) {
		while (loc2 < vctRowKey2.size()) {
			newAllRowKey.add(vctRowKey2.get(loc2++));
		}
	}
	//
	return newAllRowKey;
}
/**
 * 执行计算
 * 创建日期：(2001-10-17 20:02:13)
 * @return snt.common.rs.MemoryResultSet
 */
public MemoryResultSet execute() throws java.sql.SQLException {
	if (isSortIt()) {
		return quickSort();
	} else {
		return combine();
	}
}
/**
 * 执行计算
 * 创建日期：(2001-10-17 20:02:13)
 * @return snt.common.rs.MemoryResultSet
 */
public MemoryResultSet executeCombine() throws java.sql.SQLException {
	if (isSortIt()) {
		return quickSort();
	} else {
		return combine();
	}
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 22:12:16)
 * @return java.lang.String[]
 */
public java.lang.String[] getCombineKey() {
	return m_CombineKey;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-18 16:19:59)
 * @return int
 */
public int getSortOrder() {
	return m_nSortOrder;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 19:55:51)
 * @return java.sql.ResultSet
 */
public java.sql.ResultSet getSourceResultSet() {
	return m_SourceResultSet;
}

public boolean isSortIt() {
	return m_beSortIt;
}

private MemoryResultSet quickSort() throws java.sql.SQLException {
	List allRow = new ArrayList();
	MemoryResultSetMetaData mrsmd =
		(MemoryResultSetMetaData) m_SourceResultSet.getMetaData();
	int keys[] = fetchIndex(mrsmd, getCombineKey());
	allRow.addAll(fetchComKey(m_SourceResultSet.getResultList(), keys));
	for (int j = 0; j < m_vctCombine.size(); j++) {
		MemoryResultSet mrs = (MemoryResultSet) m_vctCombine.get(j);
		allRow.addAll(fetchComKey(mrs.getResultList(), keys));
	}
	Object o[] =allRow.toArray();
	RowKeyComparator c = new RowKeyComparator();
	c.setAscend(getSortOrder() == SORT_ASCEND);
	QuickSort.quicksort(o, c);
	allRow.clear();
	for (int i = 0; i < o.length; i++) {
		allRow.add(((CombRowKey) o[i]).getUserObject());
	}
	return new MemoryResultSet(allRow, mrsmd);

}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 22:12:16)
 * @param newCombineKey java.lang.String[]
 */
public void setCombineKey(java.lang.String[] newCombineKey) {
	m_CombineKey = newCombineKey;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-18 15:45:12)
 * @param newSortIt boolean
 */
public void setSortIt(boolean newSortIt) {
	m_beSortIt = newSortIt;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-18 16:19:59)
 * @param newSortOrder int
 */
public void setSortOrder(int newSortOrder) {
	m_nSortOrder = newSortOrder;
}
/**
 * 设置初始的RESULT SET
 * 创建日期：(2001-10-17 19:55:51)
 * @param newSourceResultSet java.sql.ResultSet
 */
public void setSourceResultSet(java.sql.ResultSet newSourceResultSet)
	throws SQLException {
	m_SourceResultSet = ComTool.convertResultSet(newSourceResultSet);
}
}
