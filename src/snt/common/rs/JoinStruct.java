package snt.common.rs;

/**
 * 合并结构，描述合并得方式
 * 创建日期：(2001-10-17 21:02:14)
 * @author ：阳雄
 */
public class JoinStruct {
	private MemoryResultSet m_JoinResultSet;
	private java.lang.String[] m_JoinFields;
	private java.lang.String[] m_JoinKey;
/**
 * JoinStruct 构造子注解。
 */
public JoinStruct() {
	super();
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:03:01)
 * @return java.lang.String[]
 */
public java.lang.String[] getJoinFields() {
	return m_JoinFields;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:03:31)
 * @return java.lang.String[]
 */
public java.lang.String[] getJoinKey() {
	return m_JoinKey;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:02:39)
 * @return snt.common.rs.MemoryResultSet
 */
public MemoryResultSet getJoinResultSet() {
	return m_JoinResultSet;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:03:01)
 * @param newJoinFields java.lang.String[]
 */
public void setJoinFields(java.lang.String[] newJoinFields) {
	m_JoinFields = newJoinFields;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:03:31)
 * @param newJoinKey java.lang.String[]
 */
public void setJoinKey(java.lang.String[] newJoinKey) {
	m_JoinKey = newJoinKey;
}
/**
 * 此处插入方法描述。
 * 创建日期：(2001-10-17 21:02:39)
 * @param newJoinResultSet snt.common.rs.MemoryResultSet
 */
public void setJoinResultSet(MemoryResultSet newJoinResultSet) {
	m_JoinResultSet = newJoinResultSet;
}
}
