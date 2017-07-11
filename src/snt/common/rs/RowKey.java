package snt.common.rs;

/**
 * 一行的KEY值，用来处理多列值时的判断是否相等的内容。
 * 创建日期：(2001-10-17 20:06:43)
 * @author ：阳雄
 */
public class RowKey implements Comparable{
	private java.lang.Object[] m_objRow;

	private boolean m_flag = false;

	private MemoryResultSet m_MemoryResultSet;

	private int m_orderId;

	/**
	 * RowHashValue 构造子注解。
	 */
	public RowKey() {
		super();
	}

	/**
	 * RowHashValue 构造子注解。
	 */
	public RowKey(Object oa[]) {
		super();
		setRow(oa);
	}	

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 20:08:19)
	 * @return int
	 */
	public boolean equals(Object o) {		
		if (!(o instanceof RowKey))
			return false;
		RowKey rk = (RowKey) o;
		/** 键值长度一定要相等 */
		Object[] rkKey = rk.getRow();
		if (rkKey.length != getRow().length)
			return false;
		return compareRowKey(this, rk)==0;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-22 21:37:04)
	 * @return int
	 */
	public int getId() {
		return m_orderId;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-22 15:59:11)
	 * @return snt.common.rs.MemoryResultSet
	 */
	public MemoryResultSet getMemoryResultSet() {
		return m_MemoryResultSet;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 20:08:03)
	 * @return java.lang.Object[]
	 */
	public java.lang.Object[] getRow() {
		return m_objRow;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 20:08:19)
	 * @return int
	 */
	public int hashCode() {
		int v = 0;
		for (int i = 0; i < m_objRow.length; i++) {
			if (m_objRow[i] != null) {
				v = 31 * v + m_objRow[i].hashCode();
			}
		}
		return v;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 22:02:14)
	 * @return boolean
	 */
	public boolean isFlag() {
		return m_flag;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 22:02:14)
	 * @param newM_flag boolean
	 */
	public void setFlag(boolean newM_flag) {
		m_flag = newM_flag;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-22 21:37:04)
	 * @param newId int
	 */
	public void setId(int newId) {
		m_orderId = newId;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-22 15:59:11)
	 * @param newMemoryResultSet snt.common.rs.MemoryResultSet
	 */
	public void setMemoryResultSet(MemoryResultSet newMemoryResultSet) {
		m_MemoryResultSet = newMemoryResultSet;
	}

	/**
	 * 此处插入方法描述。
	 * 创建日期：(2001-10-17 20:08:03)
	 * @param newRow java.lang.Object[]
	 */
	public void setRow(java.lang.Object[] newRow) {
		m_objRow = newRow;
	}

	public String toString() {
		String v = "";
		for (int i = 0; i < m_objRow.length; i++) {
			v += "[" + m_objRow[i] + "]";
		}
		return v;
	}

	
	/* （非 Javadoc）
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof RowKey) {
			return compareRowKey(this, (RowKey)o);
		}
		else {
			return 1;
		}
	}
	
	/**
	 * 比较函数
	 *  
	 */
	public static int compareRowKey(RowKey crk1, RowKey crk2) {		
		return compareRowKey(crk1, crk2, null);
	}
	
	/**
	 * 比较函数
	 *  
	 */
	public static int compareRowKey(RowKey crk1, RowKey crk2, KeyComparator[] comparator) {
		for (int i = 0; i < crk1.getRow().length; i++) {
			Object v1 = crk1.getRow()[i];
			Object v2 = crk2.getRow()[i];
			int c = 0;
			if (comparator != null && comparator.length > i && comparator[i] != null) {
				c = comparator[i].compare(v1, v2);
			} 
			else {
				c = MrsToolBase.compare(v1, v2);
			}
				
			if (c == 0) {
				continue;
			}
			else{
				return c;
			}
		}
		return crk1.getId() - crk2.getId();
	}
}