package snt.common.rs;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class MemoryResultSetMetaData implements java.io.Serializable,
		java.sql.ResultSetMetaData, Cloneable{
	private static final long serialVersionUID = 1111L;
	public static final int T_UFDOUBLE = 1000;
	
	private MetaDataVO[] metaDatas = null;
	
	/**
	 * ARESULT 构造子注解。
	 */
	public MemoryResultSetMetaData() {
		super();
		metaDatas = new MetaDataVO[0];
	}

	public MemoryResultSetMetaData(ResultSetMetaData r) {
		try {
			int columnCount = r.getColumnCount();
			metaDatas = new MetaDataVO[columnCount];
			for (int i = 0; i < columnCount; i++) {
				metaDatas[i] = new MetaDataVO();
				metaDatas[i].setNullable(r.isNullable(i + 1));
				metaDatas[i].setColumnType(r.getColumnType(i + 1));
				metaDatas[i].setScale(r.getScale(i + 1));
				metaDatas[i].setPrecision(r.getPrecision(i + 1));
				metaDatas[i].setFieldName(r.getColumnName(i + 1));
				metaDatas[i].setDisplayName(r.getColumnLabel(i+1));
			}
		} catch (Throwable e) {
			metaDatas = new MetaDataVO[0];
		}
	}

	public MetaDataVO getMetaDataVO(String colName) throws SQLException{
		int colIndex = getNameIndex(colName);
		return metaDatas[colIndex];
	}
	
	public MetaDataVO[] getMetaDataVO(String[] colNames) throws SQLException{
		return getMetaDataVO(MemoryResultSetUtils.fetchIndex(this, colNames));
	}
	
	public MetaDataVO getMetaDataVO(int colIndex) throws SQLException{
//		if (colIndex < 0 || colIndex >= metaDatas.length) {
//			throw new SQLException("结果集元数据中不存在该列！");
//		}
		return metaDatas[colIndex];
	}
	
	public MetaDataVO[] getMetaDataVO(int[] colIndices) throws SQLException{
		MetaDataVO[] subMetaDatas = new MetaDataVO[colIndices.length];
		for (int i = 0; i < colIndices.length; i++) {
			subMetaDatas[i] = getMetaDataVO(colIndices[i]);
		}
		return subMetaDatas;
	}
	
	public void appendColumn(MetaDataVO fieldVO) throws java.sql.SQLException {
		appendColumn(new MetaDataVO[] { fieldVO });
	}

	public void appendColumn(MetaDataVO[] fieldVOs)
			throws java.sql.SQLException {
		if (fieldVOs == null || fieldVOs.length == 0) {
			return;
		}
		int newLength = metaDatas.length + fieldVOs.length;

		MetaDataVO[] newMetaDatas = new MetaDataVO[newLength];
		System.arraycopy(metaDatas, 0, newMetaDatas, 0, metaDatas.length);		
		System.arraycopy(fieldVOs, 0, newMetaDatas, metaDatas.length, fieldVOs.length);
		
		metaDatas = newMetaDatas;
	}
	
	public void appendColumn(MemoryResultSetMetaData mrsmd, String fields[])
			throws java.sql.SQLException {
		if (fields == null || fields.length == 0) {
			return;
		}
		int newLength = metaDatas.length + fields.length;

		MetaDataVO[] newMetaDatas = new MetaDataVO[newLength];
		System.arraycopy(metaDatas, 0, newMetaDatas, 0, metaDatas.length);
		
		for (int i = 0; i < fields.length; i++) {
			MetaDataVO metaData = mrsmd.getMetaDataVO(fields[i]);
			try {
				newMetaDatas[metaDatas.length+i] = (MetaDataVO)metaData.clone();
			} catch (CloneNotSupportedException e) {
				throw new SQLException("复制元数据出错，添加列失败!");
			}
		}
		metaDatas = newMetaDatas;
	}
	
	public void appendColumn(String fields[]) throws java.sql.SQLException {
		if (fields == null || fields.length == 0) {
			return;
		}
		int newLength = metaDatas.length + fields.length;

		MetaDataVO[] newMetaDatas = new MetaDataVO[newLength];
		System.arraycopy(metaDatas, 0, newMetaDatas, 0, metaDatas.length);
		for (int i = 0; i < fields.length; i++) {
			MetaDataVO metaData = new MetaDataVO();
			newMetaDatas[metaDatas.length+i] = metaData;
			metaData.setColumnType(Types.VARCHAR);
			metaData.setNullable(1);
			metaData.setScale(0);
			metaData.setPrecision(0);
			metaData.setFieldName(fields[i]);
			metaData.setDisplayName(fields[i]);
		}
		metaDatas = newMetaDatas;
	}

	public void appendColumn(String field) throws java.sql.SQLException {
		appendColumn(new String[]{field});
	}

	/**
	 * getCatalogName method comment.
	 */
	public String getCatalogName(int column) throws java.sql.SQLException {
		return null;
	}

	/**
	 * JDBC 2.0
	 * 
	 * <p>
	 * Returns the fully-qualified name of the Java class whose instances are
	 * manufactured if the method <code>ResultSet.getObject</code> is called
	 * to retrieve a value from the column. <code>ResultSet.getObject</code>
	 * may return a subclass of the class returned by this method.
	 * 
	 * @return the fully-qualified name of the class in the Java programming
	 *         language that would be used by the method
	 *         <code>ResultSet.getObject</code> to retrieve the value in the
	 *         specified column. This is the class name used for custom mapping.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String getColumnClassName(int column) throws java.sql.SQLException {
		return null;
	}

	/**
	 * getColumnCount method comment.
	 */
	public int getColumnCount() throws java.sql.SQLException {
//		if (metaDatas == null)
//			throw new java.sql.SQLException("没有列数");
		return metaDatas.length;
	}

	/**
	 * getColumnDisplaySize method comment.
	 */
	public int getColumnDisplaySize(int column) throws java.sql.SQLException {
		return 0;
	}

	/**
	 * getColumnLabel method comment.
	 */
	public String getColumnLabel(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getDisplayName();
	}

	/**
	 * getColumnName method comment.
	 */
	public String getColumnName(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getFieldName();
	}

	/**
	 * getColumnType method comment.
	 */
	public int getColumnType(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getColumnType();
	}

	void setColumnType(int column, int type) {
		column--;
		metaDatas[column].setColumnType(type);
	}

	/**
	 * getColumnTypeName method comment.
	 */
	public String getColumnTypeName(int column) throws java.sql.SQLException {
		return null;
	}

	/**
	 * This method was created by a SmartGuide.
	 * 如果是第一列，则返回0
	 * @param str
	 *            java.lang.String
	 */
	public int getNameIndex(String str) throws java.sql.SQLException {
		int i = getNameIndex0(str);
		if (i == -1) {
			throw new java.sql.SQLException("Can't find column[" + str + "] from metadata");
		}
		return i;
	}
	
	/**
	 * This method was created by a SmartGuide.
	 * 和getNameIndex方法类似，区别是如果列不存在，返回-1而不是抛异常
	 * @param str
	 *            java.lang.String
	 */
	protected int getNameIndex0(String str) throws java.sql.SQLException {
		if (str == null)
			return -1;
		for (int i = 0; i < metaDatas.length; i++) {
			if (str.equalsIgnoreCase(metaDatas[i].getFieldName()))
				return i;
		}
		return -1;
	}

	/**
	 * getPrecision method comment.
	 */
	public int getPrecision(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getPrecision();
	}

	/**
	 * getScale method comment.
	 */
	public int getScale(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getScale();
	}

	/**
	 * getSchemaName method comment.
	 */
	public String getSchemaName(int column) throws java.sql.SQLException {
		return null;
	}

	/**
	 * getTableName method comment.
	 */
	public String getTableName(int column) throws java.sql.SQLException {
		return null;
	}

	/**
	 * isAutoIncrement method comment.
	 */
	public boolean isAutoIncrement(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isCaseSensitive method comment.
	 */
	public boolean isCaseSensitive(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isCurrency method comment.
	 */
	public boolean isCurrency(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isDefinitelyWritable method comment.
	 */
	public boolean isDefinitelyWritable(int column)
			throws java.sql.SQLException {
		return false;
	}

	/**
	 * isNullable method comment.
	 */
	public int isNullable(int column) throws java.sql.SQLException {
		column--;
//		if (metaDatas == null || column < 0 || metaDatas.length <= column)
//			throw new java.sql.SQLException(
//					"column < 1 or column >= column length" + (column + 1));
		return metaDatas[column].getNullable();
	}

	/**
	 * isReadOnly method comment.
	 */
	public boolean isReadOnly(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isSearchable method comment.
	 */
	public boolean isSearchable(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isSigned method comment.
	 */
	public boolean isSigned(int column) throws java.sql.SQLException {
		return false;
	}

	/**
	 * isWritable method comment.
	 */
	public boolean isWritable(int column) throws java.sql.SQLException {
		return false;
	}

	public MemoryResultSetMetaData(int[] colTypes,
			List colNameList) {
		int iLen = colTypes.length;
		metaDatas = new MetaDataVO[iLen];
		for (int i = 0; i < iLen; i++) {
			metaDatas[i] = new MetaDataVO();
			metaDatas[i].setColumnType(colTypes[i]);
			metaDatas[i].setFieldName((String)colNameList.get(i));
			metaDatas[i].setNullable(1);
			metaDatas[i].setDisplayName(metaDatas[i].getFieldName());
		}
	}
	
	/**
	 * getColumnType method comment.
	 */
	public int getColumnType4ST(int column) throws java.sql.SQLException {
		int iType = getColumnType(column);
		if (iType == java.sql.Types.NUMERIC) {
			if (getScale(column) == 0)
				iType = java.sql.Types.INTEGER;
			else
				iType = java.sql.Types.DOUBLE;
		}
		return iType;
	}

	/**
	 * getScale method comment.
	 */
	public int getNullable(int column) throws java.sql.SQLException {
		return isNullable(column);
	}

	/** zjb+ */
	public void insertColumn(MetaDataVO fieldVO, int iIndex)
			throws java.sql.SQLException {
		int newLength = metaDatas.length + 1;

		MetaDataVO[] newMetaDatas = new MetaDataVO[newLength];
		System.arraycopy(metaDatas, 0, newMetaDatas, 0, iIndex);		
		System.arraycopy(metaDatas, iIndex, newMetaDatas, iIndex+1, metaDatas.length-iIndex);
		
		newMetaDatas[iIndex] = fieldVO;
		
		metaDatas = newMetaDatas;
	}

	public int removeColumn(String field) throws java.sql.SQLException {
		int newLength = metaDatas.length - 1;

		int iIndex = getNameIndex(field);
		
		MetaDataVO[] newMetaDatas = new MetaDataVO[newLength];
		System.arraycopy(metaDatas, 0, newMetaDatas, 0, iIndex);		
		System.arraycopy(metaDatas, iIndex+1, newMetaDatas, iIndex, newMetaDatas.length-iIndex);
		
		metaDatas = newMetaDatas;
		
		return iIndex;
	}

	public void setColumnStyle(int index, Class c) {
		if (BigDecimal.class.isAssignableFrom(c))
			metaDatas[index].setColumnType(Types.DECIMAL);
		else if (c == Double.class)
			metaDatas[index].setColumnType(Types.DECIMAL);
		else if (Date.class.isAssignableFrom(c))
			metaDatas[index].setColumnType(Types.DATE);
		else if (c == String.class)
			metaDatas[index].setColumnType(Types.VARCHAR);
		else if (c == Integer.class)
			metaDatas[index].setColumnType(Types.INTEGER);
	}

	public void setColumnStyle(int index, Object o) {
		setColumnStyle(index, o.getClass());
	}

	public void setColumnLabels(String[] colLabels) {		
		int columnCount = Math.min(colLabels.length, metaDatas.length);
		for (int i = 0; i < columnCount; i++) {
			metaDatas[i].setDisplayName(colLabels[i]);
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return (T) this;
	}
}
