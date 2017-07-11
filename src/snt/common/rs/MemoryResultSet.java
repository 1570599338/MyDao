package snt.common.rs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;

//import snt.common.expparser.ExpressionParser;
import snt.common.expparser.ExpressionParserException;

/**
 * 内存当中的结果集 创建日期：(2001-10-17 19:46:11)
 * 
 * @author ：zhw<br>
 *         修改 ：阳雄
 */
public class MemoryResultSet implements ResultSet, Serializable {
	private static final long serialVersionUID = 1111L;
	private int current = -1;
	private String cursorName;
	private SQLException eStore;
	private SQLWarning eWarning;
	/** List of listeners */
	protected EventListenerList listenerList = new EventListenerList();
	private List m_RowKey;
	private MemoryResultSetMetaData metaData = null;
	private String name;
	private List resultList = new ArrayList();

	private boolean wasnull;
	 private boolean closed = false;

	/**
	 * 由一个列表和内存结果集元数据描述构造一个内存结果集
	 * 
	 * @param infor
	 * @param metaSet
	 */
	public MemoryResultSet(List infor, MemoryResultSetMetaData metaSet) {
		resultList = infor;
		metaData = metaSet;
	}

	public MemoryResultSet(ResultSet result) throws SQLException {
		this(result, false, false);
	}

	public MemoryResultSet(ResultSet result, boolean isTrim,
			boolean tamperDataAsDouble) throws SQLException {
		eStore = null;
		try {
			cursorName = result.getCursorName();
		} catch (Throwable e) {
			cursorName = null;
		}

		try {
			metaData = new MemoryResultSetMetaData(result.getMetaData());
			long descCount = metaData.getColumnCount();
			while (result.next()) {
				List tmpArrayList = new ArrayList();
				for (int i = 1; i <= descCount; i++) {
					int tp = metaData.getColumnType(i);
					switch (tp) {
					/** 字符串处理 */
					case (Types.CHAR):
					case (Types.VARCHAR):
					case (Types.LONGVARCHAR): {
						String str = result.getString(i);
						if (isTrim && str != null)
							tmpArrayList.add(str.trim());
						else
							tmpArrayList.add(str);
						break;
					}
					case (Types.NUMERIC): /* && metaData.getScale(i) != 0 */
					case (Types.DECIMAL):
					case (Types.FLOAT):
					case (Types.REAL):
					case (Types.DOUBLE): {
						Object o = result.getObject(i);
						if (tamperDataAsDouble) {
							if (o == null)
								tmpArrayList.add(o);
							else if (o instanceof BigDecimal)
								tmpArrayList.add(new Double(((BigDecimal) o)
										.doubleValue()));
							else if (o instanceof Double)
								tmpArrayList.add((Double) o);
							else
								tmpArrayList.add(new Double(o.toString()));
							// 既然篡改数据，就要篡改得彻底一点(yx+)
							metaData.setColumnType(i, Types.DOUBLE);
						} else {
							if (o == null)
								tmpArrayList.add(o);
							else if (o instanceof BigDecimal)
								tmpArrayList.add((BigDecimal) o);
							else if (o instanceof Double)
								tmpArrayList.add(new BigDecimal(((Double) o)
										.doubleValue()));
							else
								tmpArrayList.add(new BigDecimal(o.toString()));
							// 既然篡改数据，就要篡改得彻底一点(yx+)
							metaData.setColumnType(i, Types.DECIMAL);
						}
						break;
					}
					case (Types.BLOB): {
						Blob blob = result.getBlob(i);
						if (blob != null) {// Oracle
							tmpArrayList.add(new ByteBlob(blob));
						} else {// DB2
							try {
								InputStream in = result.getBinaryStream(i);
								tmpArrayList.add(in == null ? null
										: new ByteBlob(in));
							} catch (Exception e) {
							}
						}
						break;
					}
					case (Types.CLOB): {
						Clob clob = result.getClob(i);
						if (clob != null) {// Oracle
							tmpArrayList.add(new CharClob(clob));
						} else {// DB2
							try {
								String str = result.getString(i);
								tmpArrayList.add(str == null ? null
										: new CharClob(str.toCharArray()));
							} catch (Exception e) {
							}
						}
						break;
					}
					default: {
						tmpArrayList.add(result.getObject(i));
					}
					}
				}
				resultList.add(tmpArrayList);
			}

		} catch (SQLWarning eWarning) {
			this.eWarning = eWarning;
			throw eWarning;
		} catch (SQLException e) {
			eStore = e;
			throw e;
		}
		try {
			result.close();
		} catch (SQLException e) {
			throw e;
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the given row number in the result set.
	 *
	 * <p>
	 * If the row number is positive, the cursor moves to the given row number
	 * with respect to the beginning of the result set. The first row is row 1,
	 * the second is row 2, and so on.
	 *
	 * <p>
	 * If the given row number is negative, the cursor moves to an absolute row
	 * position with respect to the end of the result set. For example, calling
	 * <code>absolute(-1)</code> positions the cursor on the last row,
	 * <code>absolute(-2)</code> indicates the next-to-last row, and so on.
	 *
	 * <p>
	 * An attempt to position the cursor beyond the first/last row in the result
	 * set leaves the cursor before/after the first/last row, respectively.
	 *
	 * <p>
	 * Note: Calling <code>absolute(1)</code> is the same as calling
	 * <code>first()</code>. Calling <code>absolute(-1)</code> is the same as
	 * calling <code>last()</code>.
	 *
	 * @return true if the cursor is on the result set; false otherwise
	 * @exception SQLException
	 *                if a database access error occurs or row is 0, or result
	 *                set type is TYPE_FORWARD_ONLY.
	 */
	public boolean absolute(int row) throws java.sql.SQLException {
		int size = resultList.size();
		if (row < 0) {
			current = row > -size ? size + row : -1;
			return row > -size ? true : false;
		} else if (row > 0) {
			current = row > size ? size : row - 1;
			return row > size ? false : true;
		} else {
			current = -1;
			return false;
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the end of the result set, just after the last row.
	 * Has no effect if the result set contains no rows.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY
	 */
	public void afterLast() throws java.sql.SQLException {
		current = resultList.size();
	}

	/**
	 * 添加结果集元数据结构中指定的那些列
	 * 
	 * @param mrsmd
	 * @param fields
	 * @throws SQLException
	 */
	public void appendColumn(MemoryResultSetMetaData mrsmd, String fields[])
			throws SQLException {
		metaData.appendColumn(mrsmd, fields);
		for (int i = 0; i < resultList.size(); i++) {
			List v = (List) resultList.get(i);
			/** 在每一行后面填加空列 */
			for (int j = 0; j < fields.length; j++) {
				v.add(null);
			}
		}
	}

	/**
	 * 用给定的默认值在结果集中增加列
	 * 
	 * @param field
	 * @param type
	 * @param value
	 * @throws SQLException
	 */
	public void appendColumnByDefaultValue(String field, int type, Object value)
			throws SQLException {
		appendColumnByDefaultValue(new String[] { field }, new int[] { type },
				new Object[] { value });
	}

	/**
	 * 用给定的默认值在结果集中增加列
	 * 
	 * @param fields
	 * @param types
	 * @param values
	 * @throws SQLException
	 */
	public void appendColumnByDefaultValue(String fields[], int[] types,
			Object values[]) throws SQLException {
		MetaDataVO vo = null;
		for (int i = 0; i < fields.length; i++) {
			vo = new MetaDataVO();
			vo.setFieldName(fields[i]);
			vo.setColumnType(types[i]);
			metaData.appendColumn(vo);
		}
		for (int i = 0; i < resultList.size(); i++) {
			List v = (List) resultList.get(i);
			/** 在每一行后面填加空列 */
			for (int j = 0; j < fields.length; j++) {
				v.add(values[j]);
			}
		}
	}

	/**
	 * 按给定的默认值增加列
	 * 
	 * @param fields
	 * @param values
	 * @throws SQLException
	 */
	public void appendColumnByDefaultValue(String fields[], Object values[])
			throws SQLException {
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			appendColumnByDefaultValue(field, values[i]);
		}
	}

	/**
	 * 用给定的默认值增加列
	 * 
	 * @param field
	 * @param value
	 * @throws SQLException
	 */
	public void appendColumnByDefaultValue(String field, Object value)
			throws SQLException {
		MetaDataVO vo = new MetaDataVO();
		vo.setFieldName(field);
		if (value != null) {
			vo.setColumnType(MrsToolBase.class2SqlType(value.getClass()));
		} else {
			vo.setColumnType(java.sql.Types.VARCHAR);
		}
		metaData.appendColumn(vo);
		for (int i = 0; i < resultList.size(); i++) {
			List v = (List) resultList.get(i);
			/** 在每一行后面填加空列 */
			v.add(value);
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the front of the result set, just before the first
	 * row. Has no effect if the result set contains no rows.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY
	 */
	public void beforeFirst() throws java.sql.SQLException {
		current = -1;
	}

	public List buildRowKey(String fields[]) throws SQLException {
		m_RowKey = MrsToolBase.fetchComKey(this, fields);
		this.beforeFirst();
		return m_RowKey;
	}

	/**
	 * JDBC 2.0
	 *
	 * Cancels the updates made to a row. This method may be called after
	 * calling an <code>updateXXX</code> method(s) and before calling
	 * <code>updateRow</code> to rollback the updates made to a row. If no
	 * updates have been made or <code>updateRow</code> has already been called,
	 * then this method has no effect.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or if called when on the
	 *                insert row
	 *
	 */
	public void cancelRowUpdates() throws java.sql.SQLException {
	}

	/**
	 * 此处插入方法描述。 创建日期：(2001-10-19 13:37:03)
	 * 
	 * @param keys
	 *            int[]
	 */
	protected void clearValueExcludeKey(int[] keys) {
		Set keySet = new HashSet();
		for (int j = 0; j < keys.length; j++) {
			keySet.add(new Integer(keys[j]));
		}
		for (int i = 0; i < resultList.size(); i++) {
			List al = (List) resultList.get(i);

			for (int j = 0; j < al.size(); j++) {
				if (!keySet.contains(new Integer(j))) {
					al.set(j, null);
				}
			}
		}
	}

	protected void clearValueExcludeKey(String[] names) throws SQLException {
		int keys[] = MrsToolBase.fetchIndex(metaData, names);
		clearValueExcludeKey(keys);
	}

	/**
	 * After this call getWarnings returns null until a new warning is reported
	 * for this ResultSet.
	 *
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public void clearWarnings() throws SQLException {
		eWarning = new SQLWarning();
	}

	/**
	 * This method was created by a SmartGuide.
	 */
	public void close() {
		resultList = null;
		return;
	}

	/**
	 * JDBC 2.0
	 *
	 * Deletes the current row from the result set and the underlying database.
	 * Cannot be called when on the insert row.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or if called when on the
	 *                insert row.
	 */
	public void deleteRow() throws java.sql.SQLException {
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return int
	 */
	public int findColumn(String ColumnName) throws SQLException {
		return getColumnIndex(ColumnName);

	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the first row in the result set.
	 *
	 * @return true if the cursor is on a valid row; false if there are no rows
	 *         in the result set
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY
	 */
	public boolean first() throws java.sql.SQLException {
		current = 0;
		return resultList.size() > 0;
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets an SQL ARRAY value from the current row of ths
	 * <code>ResultSet</code> object.
	 *
	 * @param i
	 *            the first column is 1, the second is 2, ...
	 * @return an <code>Array</code> object representing the SQL ARRAY value in
	 *         the specified column
	 */
	public java.sql.Array getArray(int i) throws java.sql.SQLException {
		throw new SQLException("MemoryResultSet.getArray()not simulated!");
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets an SQL ARRAY value in the current row of this <code>ResultSet</code>
	 * object.
	 *
	 * @param colName
	 *            the name of the column from which to retrieve the value
	 * @return an <code>Array</code> object representing the SQL ARRAY value in
	 *         the specified column
	 */
	public java.sql.Array getArray(String colName) throws java.sql.SQLException {
		return getArray(getColumnIndex(colName));
	}

	/**
	 * A column value can be retrieved as a stream of ASCII characters and then
	 * read in chunks from the stream. This method is particularly suitable for
	 * retrieving large LONGVARCHAR values. The JDBC driver will do any
	 * necessary conversion from the database format into ASCII.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the returned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream. . Also, a stream may return 0 for
	 * available() whether there is data available or not.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of one byte ASCII characters. If the value is SQL NULL
	 *         then the result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getAsciiStream(int columnIndex)
			throws SQLException {
		throw new SQLException("MemoryResultSet.getAsciiStream()not simulated!");
	}

	/**
	 * A column value can be retrieved as a stream of ASCII characters and then
	 * read in chunks from the stream. This method is particularly suitable for
	 * retrieving large LONGVARCHAR values. The JDBC driver will do any
	 * necessary conversion from the database format into ASCII.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the returned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of one byte ASCII characters. If the value is SQL NULL
	 *         then the result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getAsciiStream(String columnName)
			throws SQLException {
		return getAsciiStream(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets the value of a column in the current row as a java.math.BigDecimal
	 * object with full precision.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value (full precision); if the value is SQL NULL, the
	 *         result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.math.BigDecimal getBigDecimal(int columnIndex)
			throws java.sql.SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj instanceof BigDecimal) {
				return (BigDecimal) obj;
			} else {
				return obj == null ? null : new BigDecimal(obj.toString());
			}
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getBigDecimal() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a java.lang.BigDecimal
	 * object.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param scale
	 *            the number of digits to the right of the decimal
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 * @deprecated
	 */
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		try {
			if (getObject(columnIndex) != null)
				return java.math.BigDecimal
						.valueOf(getLong(columnIndex), scale);
			else
				return null;
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getBigDecimal(,) incorrect type!");
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets the value of a column in the current row as a java.math.BigDecimal
	 * object with full precision.
	 * 
	 * @param columnName
	 *            the column name
	 * @return the column value (full precision); if the value is SQL NULL, the
	 *         result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 *
	 */
	public java.math.BigDecimal getBigDecimal(String columnName)
			throws java.sql.SQLException {
		return getBigDecimal(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a java.lang.BigDecimal
	 * object.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @param scale
	 *            the number of digits to the right of the decimal
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public BigDecimal getBigDecimal(String columnName, int scale)
			throws SQLException {
		return getBigDecimal(getColumnIndex(columnName), scale);
	}

	/**
	 * A column value can be retrieved as a stream of uninterpreted bytes and
	 * then read in chunks from the stream. This method is particularly suitable
	 * for retrieving large LONGVARBINARY values.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the returned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream. Also, a stream may return 0 for available()
	 * whether there is data available or not.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of uninterpreted bytes. If the value is SQL NULL then the
	 *         result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getBinaryStream(int columnIndex)
			throws SQLException {
		try {
			Object val = getObject(columnIndex);
			if (val instanceof Blob) {
				Blob blob = (Blob) val;
				return blob.getBinaryStream();
			} else if (val instanceof InputStream) {
				return (InputStream) val;
			} else if (val instanceof byte[]) {
				return new ByteArrayInputStream((byte[]) val);
			} else if (val == null) {
				return null;
			} else {
				throw new SQLException(
						"MemoryResultSet.getBinaryStream() incorrect type!");
			}
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getBinaryStream() incorrect type!");
		}
	}

	/**
	 * A column value can be retrieved as a stream of uninterpreted bytes and
	 * then read in chunks from the stream. This method is particularly suitable
	 * for retrieving large LONGVARBINARY values.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the returned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of uninterpreted bytes. If the value is SQL NULL then the
	 *         result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getBinaryStream(String columnName)
			throws SQLException {
		return getBinaryStream(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a BLOB value in the current row of this <code>ResultSet</code>
	 * object.
	 *
	 * @param i
	 *            the first column is 1, the second is 2, ...
	 * @return a <code>Blob</code> object representing the SQL BLOB value in the
	 *         specified column
	 */
	public java.sql.Blob getBlob(int i) throws java.sql.SQLException {
		try {
			return (Blob) getObject(i);
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getBlob() incorrect type!");
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a BLOB value in the current row of this <code>ResultSet</code>
	 * object.
	 *
	 * @param colName
	 *            the name of the column from which to retrieve the value
	 * @return a <code>Blob</code> object representing the SQL BLOB value in the
	 *         specified column
	 */
	public java.sql.Blob getBlob(String colName) throws java.sql.SQLException {
		return getBlob(getColumnIndex(colName));
	}

	/**
	 * Get the value of a column in the current row as a Java boolean.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is false
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null) {
				return ((Boolean) obj).booleanValue();
			} else {
				return false;
			}
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getBoolean() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java boolean.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is false
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public boolean getBoolean(String columnName) throws SQLException {
		return getBoolean(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a Java byte.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public byte getByte(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null)
				return ((Byte) obj).byteValue();
			else
				return 0;
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getByte() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java byte.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public byte getByte(String columnName) throws SQLException {
		return getByte(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a Java byte array. The
	 * bytes represent the raw values returned by the driver.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		try {
			if (metaData.getColumnType(columnIndex) == Types.BLOB) {
				Blob blob = (Blob) getObject(columnIndex);
				if (blob != null) {
					return blob.getBytes(1L, (int) blob.length());
				} else {
					return null;
				}
			} else {
				return (byte[]) getObject(columnIndex);
			}
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getBytes() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java byte array. The
	 * bytes represent the raw values returned by the driver.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public byte[] getBytes(String columnName) throws SQLException {
		return getBytes(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Gets the value of a column in the current row as a java.io.Reader.
	 * 
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 */
	public java.io.Reader getCharacterStream(int columnIndex)
			throws java.sql.SQLException {
		try {
			Object val = getObject(columnIndex);
			if (val instanceof Clob) {
				Clob clob = (Clob) val;
				return clob.getCharacterStream();
			} else if (val instanceof Reader) {
				return (Reader) val;
			} else if (val instanceof String) {
				return new StringReader((String) val);
			} else if (val == null) {
				return null;
			} else {
				throw new SQLException(
						"MemoryResultSet.getCharacterStream() incorrect type!");
			}
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getCharacterStream() incorrect type!");
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Gets the value of a column in the current row as a java.io.Reader.
	 * 
	 * @param columnName
	 *            the name of the column
	 * @return the value in the specified column as a
	 *         <code>java.io.Reader</code>
	 */
	public java.io.Reader getCharacterStream(String columnName)
			throws java.sql.SQLException {
		return getCharacterStream(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a CLOB value in the current row of this <code>ResultSet</code>
	 * object.
	 *
	 * @param i
	 *            the first column is 1, the second is 2, ...
	 * @return a <code>Clob</code> object representing the SQL CLOB value in the
	 *         specified column
	 */
	public java.sql.Clob getClob(int i) throws java.sql.SQLException {
		try {
			return (Clob) getObject(i);

		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getClob() incorrect type!");
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a CLOB value in the current row of this <code>ResultSet</code>
	 * object.
	 *
	 * @param colName
	 *            the name of the column from which to retrieve the value
	 * @return a <code>Clob</code> object representing the SQL CLOB value in the
	 *         specified column
	 */
	public java.sql.Clob getClob(String colName) throws java.sql.SQLException {
		return getClob(getColumnIndex(colName));
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return int
	 */
	public int getColumnIndex(String ColumnName) throws SQLException {
		return metaData.getNameIndex(ColumnName.toUpperCase()) + 1;
	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the concurrency mode of this result set. The concurrency used is
	 * deterined by the statement that created the result set.
	 *
	 * @return the concurrency type, CONCUR_READ_ONLY or CONCUR_UPDATABLE
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getConcurrency() throws java.sql.SQLException {
		return CONCUR_READ_ONLY;
	}

	/**
	 * Get the name of the SQL cursor used by this ResultSet.
	 *
	 * <P>
	 * In SQL, a result table is retrieved through a cursor that is named. The
	 * current row of a result can be updated or deleted using a positioned
	 * update/delete statement that references the cursor name.
	 *
	 * <P>
	 * JDBC supports this SQL feature by providing the name of the SQL cursor
	 * used by a ResultSet. The current row of a ResultSet is also the current
	 * row of this SQL cursor.
	 *
	 * <P>
	 * <B>Note:</B> If positioned update is not supported a SQLException is
	 * thrown
	 *
	 * @return the ResultSet's SQL cursor name
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public String getCursorName() throws SQLException {
		return cursorName;
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Date object.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Date getDate(int columnIndex) throws SQLException {
		try {
			return (Date) getObject(columnIndex);
		}

		catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getDate() incorrect type!");
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets the value of a column in the current row as a java.sql.Date object.
	 * This method uses the given calendar to construct an appropriate
	 * millisecond value for the Date if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param cal
	 *            the calendar to use in constructing the date
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Date getDate(int columnIndex, java.util.Calendar cal)
			throws java.sql.SQLException {
		return null;
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Date object.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Date getDate(String columnName) throws SQLException {
		return getDate(getColumnIndex(columnName));
	}

	/**
	 * Gets the value of a column in the current row as a java.sql.Date object.
	 * This method uses the given calendar to construct an appropriate
	 * millisecond value for the Date, if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnName
	 *            the SQL name of the column from which to retrieve the value
	 * @param cal
	 *            the calendar to use in constructing the date
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Date getDate(String columnName, java.util.Calendar cal)
			throws java.sql.SQLException {
		return getDate(getColumnIndex(columnName), cal);
	}

	/**
	 * Get the value of a column in the current row as a Java double.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public double getDouble(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null) {
				if (obj instanceof Number) {
					return ((Number) obj).doubleValue();
				} else {
					return Double.valueOf(obj.toString()).doubleValue();
				}
			} else
				return 0;
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getDouble() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java double.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public double getDouble(String columnName) throws SQLException {
		return getDouble(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the fetch direction for this result set.
	 *
	 * @return the current fetch direction for this result set
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getFetchDirection() throws java.sql.SQLException {
		return 0;
	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the fetch size for this result set.
	 *
	 * @return the current fetch size for this result set
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getFetchSize() throws java.sql.SQLException {
		return 0;
	}

	/**
	 * Get the value of a column in the current row as a Java float.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public float getFloat(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null)
				if (obj instanceof Number) {
					return ((Number) obj).floatValue();
				} else {
					return Float.valueOf(obj.toString()).floatValue();
				}
			else
				return 0;
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getFloat() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java float.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public float getFloat(String columnName) throws SQLException {
		return getFloat(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a Java int.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public int getInt(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null) {
				if (obj instanceof Number) {
					return ((Number) obj).intValue();
				} else {
					return Integer.valueOf(obj.toString()).intValue();
				}
			} else
				return 0;
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getInt() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java int.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public int getInt(String columnName) throws SQLException {
		return getInt(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a Java long.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public long getLong(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null) {
				if (obj instanceof Number) {
					return ((Number) obj).longValue();
				} else {
					return Long.valueOf(obj.toString()).longValue();
				}
			} else
				return 0;
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getLong() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java long.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public long getLong(String columnName) throws SQLException {
		return getLong(getColumnIndex(columnName));
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return java.sql.ResultSetMetaData
	 */
	public java.sql.ResultSetMetaData getMetaData() {
		return metaData;
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return java.sql.ResultSetMetaData
	 */
	public MemoryResultSetMetaData getMetaData0() {
		return metaData;
	}

	public String getName() {
		return name;
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return java.lang.Object
	 * @param columnIndex
	 *            int
	 */
	public Object getObject(int columnIndex) throws SQLException,
			ArrayIndexOutOfBoundsException {
		columnIndex--;
		try {
			List listRow = (List) (resultList.get(current));
			if (listRow.get(columnIndex) != null) {
				wasnull = false;
				return listRow.get(columnIndex);
			} else {
				wasnull = true;
				return null;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("out of index");
		} catch (Throwable e) {
			e.printStackTrace();
			throw new SQLException(
					"MemoryResultSet.getObject() incorrect type!");
		}

	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the value of a column in the current row as a Java object. This
	 * method uses the given <code>Map</code> object for the custom mapping of
	 * the SQL structured or distinct type that is being retrieved.
	 *
	 * @param i
	 *            the first column is 1, the second is 2, ...
	 * @param map
	 *            the mapping from SQL type names to Java classes
	 * @return an object representing the SQL value
	 */
	public Object getObject(int i, java.util.Map map)
			throws java.sql.SQLException {
		return null;
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return java.lang.Object
	 * @param ColumnName
	 *            java.lang.String
	 */
	public Object getObject(String ColumnName) throws SQLException {
		return getObject(getColumnIndex(ColumnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the value in the specified column as a Java object. This method
	 * uses the specified <code>Map</code> object for custom mapping if
	 * appropriate.
	 *
	 * @param colName
	 *            the name of the column from which to retrieve the value
	 * @param map
	 *            the mapping from SQL type names to Java classes
	 * @return an object representing the SQL value in the specified column
	 */
	public Object getObject(String colName, java.util.Map map)
			throws java.sql.SQLException {
		return getObject(getColumnIndex(colName), map);
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a REF(&lt;structured-type&gt;) column value from the current row.
	 *
	 * @param i
	 *            the first column is 1, the second is 2, ...
	 * @return a <code>Ref</code> object representing an SQL REF value
	 */
	public java.sql.Ref getRef(int i) throws java.sql.SQLException {
		throw new SQLException("MemoryResultSet.getRef()not simulated!");
	}

	/**
	 * JDBC 2.0
	 *
	 * Gets a REF(&lt;structured-type&gt;) column value from the current row.
	 *
	 * @param colName
	 *            the column name
	 * @return a <code>Ref</code> object representing the SQL REF value in the
	 *         specified column
	 */
	public java.sql.Ref getRef(String colName) throws java.sql.SQLException {
		return getRef(getColumnIndex(colName));
	}

	public List getResultList() {
		return resultList;
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Retrieves the current row number. The first row is number 1, the second
	 * number 2, and so on.
	 *
	 * @return the current row number; 0 if there is no current row
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getRow() throws java.sql.SQLException {
		return current + 1;
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Retrieves the current row number. The first row is number 1, the second
	 * number 2, and so on.
	 *
	 * @return the current row number; 0 if there is no current row
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	protected CombRowKey getRowKey() throws java.sql.SQLException {
		try {
			return (CombRowKey) (m_RowKey.get(current));
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("out of index");
		} catch (Throwable e) {
			throw new SQLException("incorrect type!");
		}
	}

	/**
	 * 返回结果集当前行的数据，数据组织成一个列表，结果集的第一列对应列表的第一个元素，依次类推
	 * 
	 * @return List
	 * @throws SQLException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public List getRowList() throws SQLException,
			ArrayIndexOutOfBoundsException {
		try {
			List listRow = (List) (resultList.get(current));
			return listRow;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e;
		} catch (Throwable e) {
			throw new SQLException("incorrect type!");
		}

	}

	// ======================================================================
	// Methods for accessing results by column index
	// ======================================================================

	/**
	 * Get the value of a column in the current row as a Java short.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public short getShort(int columnIndex) throws SQLException {
		try {
			Short s = (Short) getObject(columnIndex);
			return s == null ? 0 : s.shortValue();
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getShort() incorrect type!");
		}
	}

	// ======================================================================
	// Methods for accessing results by column name
	// ======================================================================

	/**
	 * Get the value of a column in the current row as a Java short.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is 0
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public short getShort(String columnName) throws SQLException {
		return getShort(getColumnIndex(columnName));
	}

	/**
	 * JDBC 2.0
	 *
	 * Returns the Statement that produced this <code>ResultSet</code> object.
	 * If the result set was generated some other way, such as by a
	 * <code>DatabaseMetaData</code> method, this method returns
	 * <code>null</code>.
	 *
	 * @return the Statment that produced the result set or null if the result
	 *         set was produced some other way
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Statement getStatement() throws java.sql.SQLException {
		return null;
	}

	/**
	 * Get the value of a column in the current row as a Java String.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public String getString(int columnIndex) throws SQLException {
		try {
			Object obj = getObject(columnIndex);
			if (obj != null) {
				if (obj instanceof Clob) {
					return ((Clob) obj).getSubString(1,
							(int) ((Clob) obj).length());
				} else {
					return obj.toString();
				}
			} else {
				return null;
			}
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getString() incorrect type!");
		}
	}

	/**
	 * Get the value of a column in the current row as a Java String.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public String getString(String columnName) throws SQLException {
		return getString(getColumnIndex(columnName));
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Time object.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Time getTime(int columnIndex) throws SQLException {
		try {
			return (Time) getObject(columnIndex);
		} catch (Throwable e) {
			throw new SQLException("MemoryResultSet.getTime() incorrect type!");
		}
	}

	/**
	 * Gets the value of a column in the current row as a java.sql.Time object.
	 * This method uses the given calendar to construct an appropriate
	 * millisecond value for the Time if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param cal
	 *            the calendar to use in constructing the time
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Time getTime(int columnIndex, java.util.Calendar cal)
			throws java.sql.SQLException {
		return null;
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Time object.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Time getTime(String columnName) throws SQLException {
		return getTime(getColumnIndex(columnName));

	}

	/**
	 * Gets the value of a column in the current row as a java.sql.Time object.
	 * This method uses the given calendar to construct an appropriate
	 * millisecond value for the Time if the underlying database does not store
	 * timezone information.
	 *
	 * @param columnName
	 *            the SQL name of the column
	 * @param cal
	 *            the calendar to use in constructing the time
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Time getTime(String columnName, java.util.Calendar cal)
			throws java.sql.SQLException {
		return getTime(getColumnIndex(columnName), cal);
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Timestamp
	 * object.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
		try {
			return (java.sql.Timestamp) getObject(columnIndex);
		} catch (Throwable e) {
			throw new SQLException(
					"MemoryResultSet.getTimestamp() incorrect type!");
		}
	}

	/**
	 * Gets the value of a column in the current row as a java.sql.Timestamp
	 * object. This method uses the given calendar to construct an appropriate
	 * millisecond value for the Timestamp if the underlying database does not
	 * store timezone information.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param cal
	 *            the calendar to use in constructing the timestamp
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Timestamp getTimestamp(int columnIndex,
			java.util.Calendar cal) throws java.sql.SQLException {
		return null;
	}

	/**
	 * Get the value of a column in the current row as a java.sql.Timestamp
	 * object.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.sql.Timestamp getTimestamp(String columnName)
			throws SQLException {
		return getTimestamp(getColumnIndex(columnName));
	}

	/**
	 * Gets the value of a column in the current row as a java.sql.Timestamp
	 * object. This method uses the given calendar to construct an appropriate
	 * millisecond value for the Timestamp if the underlying database does not
	 * store timezone information.
	 *
	 * @param columnName
	 *            the SQL name of the column
	 * @param cal
	 *            the calendar to use in constructing the timestamp
	 * @return the column value; if the value is SQL NULL, the result is null
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public java.sql.Timestamp getTimestamp(String columnName,
			java.util.Calendar cal) throws java.sql.SQLException {
		return getTimestamp(getColumnIndex(columnName), cal);
	}

	// =====================================================================
	// Advanced features:
	// =====================================================================

	/**
	 * JDBC 2.0
	 *
	 * Returns the type of this result set. The type is determined by the
	 * statement that created the result set.
	 *
	 * @return TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE, or
	 *         TYPE_SCROLL_SENSITIVE
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public int getType() throws java.sql.SQLException {
		return TYPE_SCROLL_INSENSITIVE;
	}

	/**
	 * A column value can be retrieved as a stream of Unicode characters and
	 * then read in chunks from the stream. This method is particularly suitable
	 * for retrieving large LONGVARCHAR values. The JDBC driver will do any
	 * necessary conversion from the database format into Unicode.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the eturned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream. . Also, a stream may return 0 for
	 * available() whether there is data available or not.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of two byte Unicode characters. If the value is SQL NULL
	 *         then the result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getUnicodeStream(int columnIndex)
			throws SQLException {
		throw new SQLException(
				"MemoryResultSet.getUnicodeStream()not simulated!");
	}

	/**
	 * A column value can be retrieved as a stream of Unicode characters and
	 * then read in chunks from the stream. This method is particularly suitable
	 * for retrieving large LONGVARCHAR values. The JDBC driver will do any
	 * necessary conversion from the database format into Unicode.
	 *
	 * <P>
	 * <B>Note:</B> All the data in the returned stream must be read prior to
	 * getting the value of any other column. The next call to a get method
	 * implicitly closes the stream.
	 *
	 * @param columnName
	 *            is the SQL name of the column
	 * @return a Java input stream that delivers the database column value as a
	 *         stream of two byte Unicode characters. If the value is SQL NULL
	 *         then the result is null.
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public java.io.InputStream getUnicodeStream(String columnName)
			throws SQLException {
		return getUnicodeStream(getColumnIndex(columnName));
	}

	public URL getURL(int columnIndex) throws java.sql.SQLException {
		return (URL) getObject(columnIndex);
	}

	public URL getURL(String columnName) throws java.sql.SQLException {
		return getURL(getColumnIndex(columnName));
	}

	/**
	 * <p>
	 * The first warning reported by calls on this ResultSet is returned.
	 * Subsequent ResultSet warnings will be chained to this SQLWarning.
	 *
	 * <P>
	 * The warning chain is automatically cleared each time a new row is read.
	 *
	 * <P>
	 * <B>Note:</B> This warning chain only covers warnings caused by ResultSet
	 * methods. Any warning caused by statement methods (such as reading OUT
	 * parameters) will be chained on the Statement object.
	 *
	 * @return the first SQLWarning or null
	 * @exception SQLException
	 *                if a database-access error occurs.
	 */
	public SQLWarning getWarnings() throws SQLException {
		return eWarning;
	}

	/**
	 * 在指定位置按默认值插入列
	 * 
	 * @param fieldVO
	 * @param value
	 * @param iIndex
	 * @throws SQLException
	 */
	public void insertColumnByDefaultValue(MetaDataVO fieldVO, Object value,
			int iIndex) throws SQLException {
		metaData.insertColumn(fieldVO, iIndex);
		for (int i = 0; i < resultList.size(); i++) {
			List v = (List) resultList.get(i);
			/** 在指定行后面添加空行 */
			v.add(iIndex, value);
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Inserts the contents of the insert row into the result set and the
	 * database. Must be on the insert row when this method is called.
	 *
	 * @exception SQLException
	 *                if a database access error occurs, if called when not on
	 *                the insert row, or if not all of non-nullable columns in
	 *                the insert row have been given a value
	 */
	public void insertRow() throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Indicates whether the cursor is after the last row in the result set.
	 *
	 * @return true if the cursor is after the last row, false otherwise.
	 *         Returns false when the result set contains no rows.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isAfterLast() throws java.sql.SQLException {
		return current == resultList.size();
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Indicates whether the cursor is before the first row in the result set.
	 *
	 * @return true if the cursor is before the first row, false otherwise.
	 *         Returns false when the result set contains no rows.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isBeforeFirst() throws java.sql.SQLException {
		return current == -1;
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Indicates whether the cursor is on the first row of the result set.
	 *
	 * @return true if the cursor is on the first row, false otherwise.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isFirst() throws java.sql.SQLException {
		return current == 0;
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Indicates whether the cursor is on the last row of the result set. Note:
	 * Calling the method <code>isLast</code> may be expensive because the JDBC
	 * driver might need to fetch ahead one row in order to determine whether
	 * the current row is the last row in the result set.
	 *
	 * @return true if the cursor is on the last row, false otherwise.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isLast() throws java.sql.SQLException {
		return current == resultList.size() - 1;
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the last row in the result set.
	 *
	 * @return true if the cursor is on a valid row; false if there are no rows
	 *         in the result set
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY.
	 */
	public boolean last() throws java.sql.SQLException {
		current = resultList.size() - 1;
		return true;
	}

	/**
	 * JDBC 2.0
	 *
	 * Moves the cursor to the remembered cursor position, usually the current
	 * row. This method has no effect if the cursor is not on the insert row.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or the result set is not
	 *                updatable
	 */
	public void moveToCurrentRow() throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Moves the cursor to the insert row. The current cursor position is
	 * remembered while the cursor is positioned on the insert row.
	 *
	 * The insert row is a special row associated with an updatable result set.
	 * It is essentially a buffer where a new row may be constructed by calling
	 * the <code>updateXXX</code> methods prior to inserting the row into the
	 * result set.
	 *
	 * Only the <code>updateXXX</code>, <code>getXXX</code>, and
	 * <code>insertRow</code> methods may be called when the cursor is on the
	 * insert row. All of the columns in a result set must be given a value each
	 * time this method is called before calling <code>insertRow</code>. The
	 * method <code>updateXXX</code> must be called before a <code>getXXX</code>
	 * method can be called on a column value.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or the result set is not
	 *                updatable
	 */
	public void moveToInsertRow() throws java.sql.SQLException {
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return java.lang.Boolean
	 */
	public boolean next() throws SQLException {
		try {
			if (eStore != null)
				throw eStore;
			if (current >= resultList.size())
				return false;
			else {
				current += 1;
				if (current == resultList.size())
					return false;
				return true;
			}
		} catch (SQLWarning eWarning) {
			System.out.println(eWarning);
			this.eWarning = eWarning;
			return false;
		} catch (SQLException e) {
			System.out.println(e);
			eStore = e;
			return false;
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor to the previous row in the result set.
	 *
	 * <p>
	 * Note: <code>previous()</code> is not the same as
	 * <code>relative(-1)</code> because it makes sense to
	 * call</code>previous()</code> when there is no currentrow.
	 *
	 * @return true if the cursor is on a valid row; false if it is off the
	 *         result set
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY
	 */
	public boolean previous() throws java.sql.SQLException {
		if (current > 0 && current <= resultList.size()) {
			current--;
			return true;
		} else if (current <= 0) {
			current = -1;
			return false;
		} else {
			current--;
			return false;
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Refreshes the current row with its most recent value in the database.
	 * Cannot be called when on the insert row.
	 *
	 * The <code>refreshRow</code> method provides a way for an application to
	 * explicitly tell the JDBC driver to refetch a row(s) from the database. An
	 * application may want to call <code>refreshRow</code> when caching or
	 * prefetching is being done by the JDBC driver to fetch the latest value of
	 * a row from the database. The JDBC driver may actually refresh multiple
	 * rows at once if the fetch size is greater than one.
	 *
	 * All values are refetched subject to the transaction isolation level and
	 * cursor sensitivity. If <code>refreshRow</code> is called after calling
	 * <code>updateXXX</code>, but before calling <code>updateRow</code>, then
	 * the updates made to the row are lost. Calling the method
	 * <code>refreshRow</code> frequently will likely slow performance.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or if called when on the
	 *                insert row
	 */
	public void refreshRow() throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * <p>
	 * Moves the cursor a relative number of rows, either positive or negative.
	 * Attempting to move beyond the first/last row in the result set positions
	 * the cursor before/after the the first/last row. Calling
	 * <code>relative(0)</code> is valid, but does not change the cursor
	 * position.
	 *
	 * <p>
	 * Note: Calling <code>relative(1)</code> is different from calling
	 * <code>next()</code> because is makes sense to call <code>next()</code>
	 * when there is no current row, for example, when the cursor is positioned
	 * before the first row or after the last row of the result set.
	 *
	 * @return true if the cursor is on a row; false otherwise
	 * @exception SQLException
	 *                if a database access error occurs, there is no current
	 *                row, or the result set type is TYPE_FORWARD_ONLY
	 */
	public boolean relative(int rows) throws java.sql.SQLException {
		if (rows == 0) {
			return current >= 0 && current < resultList.size();
		}
		current += rows;
		if (current < 0) {
			current = -1;
			return false;
		} else if (current >= resultList.size()) {
			current = resultList.size();
			return false;
		}
		return true;
	}

	/**
	 * 删除列（zjb+） 创建日期：(2001-10-17 21:15:14)
	 * 
	 * @param field
	 *            java.lang.String
	 */
	public void removeColumn(String field) throws SQLException {
		int iIndex = metaData.removeColumn(field);
		if (iIndex != -1)
			for (int i = 0; i < resultList.size(); i++) {
				List v = (List) resultList.get(i);
				v.remove(iIndex);
			}
	}

	/**
	 * JDBC 2.0
	 *
	 * Indicates whether a row has been deleted. A deleted row may leave a
	 * visible "hole" in a result set. This method can be used to detect holes
	 * in a result set. The value returned depends on whether or not the result
	 * set can detect deletions.
	 *
	 * @return true if a row was deleted and deletions are detected
	 * @exception SQLException
	 *                if a database access error occurs
	 *
	 * @see DatabaseMetaData#deletesAreDetected
	 */
	public boolean rowDeleted() throws java.sql.SQLException {
		return false;
	}

	/**
	 * JDBC 2.0
	 *
	 * Indicates whether the current row has had an insertion. The value
	 * returned depends on whether or not the result set can detect visible
	 * inserts.
	 *
	 * @return true if a row has had an insertion and insertions are detected
	 * @exception SQLException
	 *                if a database access error occurs
	 *
	 * @see DatabaseMetaData#insertsAreDetected
	 */
	public boolean rowInserted() throws java.sql.SQLException {
		return false;
	}

	/**
	 * JDBC 2.0
	 *
	 * Indicates whether the current row has been updated. The value returned
	 * depends on whether or not the result set can detect updates.
	 *
	 * @return true if the row has been visibly updated by the owner or another,
	 *         and updates are detected
	 * @exception SQLException
	 *                if a database access error occurs
	 *
	 * @see DatabaseMetaData#updatesAreDetected
	 */
	public boolean rowUpdated() throws java.sql.SQLException {
		return false;
	}

	/**
	 * 在指定位置插入表达式列
	 * 
	 * @param fieldVO
	 * @param formula
	 * @param iIndex
	 * @throws SQLException
	 * @throws ExpressionParserException
	 */
/*	public void setColumnByExpression(MetaDataVO fieldVO, String formula,
			int iIndex) throws SQLException, ExpressionParserException {
		String field = fieldVO.getFieldName();
		if (metaData.getNameIndex0(field) == -1) {
			insertColumnByDefaultValue(fieldVO, null, iIndex);
		}
		setColumnByExpressions(new String[] { field },
				new int[] { fieldVO.getColumnType() }, new String[] { formula });
	}*/

	/**
	 * 添加表达式列
	 * 
	 * @param field
	 *            字段名
	 * @param type
	 *            字段类型
	 * @param formula
	 *            表达式
	 * @throws SQLException
	 * @throws ExpressionParserException
	 */
/*	public void setColumnByExpression(String field, int type, String formula)
			throws SQLException, ExpressionParserException {
		String[] fields = { field };
		String[] formulas = { formula };
		int[] types = new int[] { type };
		setColumnByExpressions(fields, types, formulas);
	}*/

	/**
	 * 添加表达式列
	 * 
	 * @param fields
	 * @param types
	 * @param formulas
	 * @throws SQLException
	 * @throws ExpressionParserException
	 */
/*	public void setColumnByExpressions(String fields[], int[] types,
			String[] formulas) throws SQLException, ExpressionParserException {
		setColumnByExpressions(fields, types, formulas, null);
	}*/

	/**
	 * 添加表达式列
	 * 
	 * @param fields
	 * @param types
	 * @param formulas
	 * @throws SQLException
	 * @throws ExpressionParserException
	 */
	/*public void setColumnByExpressions(String fields[], int[] types,
			String[] formulas, ExpressionParser ep) throws SQLException,
			ExpressionParserException {
		for (int i = 0; i < fields.length; i++) {
			if (metaData.getNameIndex0(fields[i]) == -1) {
				appendColumnByDefaultValue(fields[i], types[i], null);
			}
		}

		if (ep == null) {
			ep = new ExpressionParser();
		}

		int[] destColIndices = MrsToolBase.fetchIndex(getMetaData0(), fields);

		// 执行公式
		ep.setExpressionArray(formulas);
		// 获得变量名
		List variableList = ep.getVarNameList();
		String[] variables = (String[]) variableList
				.toArray(new String[variableList.size()]);
		int[] varIndices = MrsToolBase.fetchIndex(getMetaData0(), variables);
		List varValueList = MemoryResultSetUtils.fetchColValuesByColDir(this,
				varIndices);
		for (int vi = 0, vn = variables.length; vi < vn; vi++) {
			ep.setVarValue(variables[vi], varValueList.get(vi));
		}
		// 设置结果
		Object o = ep.getValueAsObject();
		if (!(o instanceof List)) {// 返回结果不是一个列表，说明表达式中不含结果集中列变量，故返回结果为一个单一值
			setColumnValue(destColIndices[0],
					MemoryResultSetUtils.convertValue(o, types[0]));
		} else {
			List result = (List) o;
			if (result != null) {
				if (fields.length == 1) {
					MemoryResultSetUtils.fillColValue(this, destColIndices[0],
							MemoryResultSetUtils.convertValueList(result,
									types[0]));
				} else {
					for (int i = 0; i < destColIndices.length; i++) {
						Object ele = result.get(i);
						if (!(ele instanceof List)) {
							setColumnValue(destColIndices[i],
									MemoryResultSetUtils.convertValue(ele,
											types[i]));
						} else {
							MemoryResultSetUtils.fillColValue(this,
									destColIndices[i], MemoryResultSetUtils
											.convertValueList((List) ele,
													types[i]));
						}
					}
				}
			}
		}
	}*/

	public void setColumnValue(String colName, Object value)
			throws SQLException {
		int colIndex = metaData.getNameIndex(colName);
		setColumnValue(colIndex, value);
	}

	public void setColumnValue(int colIndex, Object value) throws SQLException {
		for (int i = 0; i < resultList.size(); i++) {
			List al = (List) resultList.get(i);
			al.set(colIndex, value);
		}
	}

	/**
	 * JDBC 2.0
	 *
	 * Gives a hint as to the direction in which the rows in this result set
	 * will be processed. The initial value is determined by the statement that
	 * produced the result set. The fetch direction may be changed at any time.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or the result set type
	 *                is TYPE_FORWARD_ONLY and the fetch direction is nt
	 *                FETCH_FORWARD.
	 */
	public void setFetchDirection(int direction) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Gives the JDBC driver a hint as to the number of rows that should be
	 * fetched from the database when more rows are needed for this result set.
	 * If the fetch size specified is zero, the JDBC driver ignores the value
	 * and is free to make its own best guess as to what the fetch size should
	 * be. The default value is set by the statement that created the result
	 * set. The fetch size may be changed at any time.
	 *
	 * @param rows
	 *            the number of rows to fetch
	 * @exception SQLException
	 *                if a database access error occurs or the condition 0 <=
	 *                rows <= this.getMaxRows() is not satisfied.
	 */
	public void setFetchSize(int rows) throws java.sql.SQLException {
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void setResultList(List newResult) {
		resultList = newResult;
	}

	/**
	 * 此处插入方法说明。 创建日期：(2002-11-5 17:27:17)
	 * 
	 * @param loc
	 *            int
	 */
	public void skipTo(int loc) {
		current = loc;
	}

	public void updateArray(int parm1, Array parm2)
			throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateArray() not yet implemented.");
	}

	public void updateArray(String parm1, Array parm2)
			throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateArray() not yet implemented.");
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an ascii stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @param length
	 *            the length of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateAsciiStream(int columnIndex, java.io.InputStream x,
			int length) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an ascii stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @param length
	 *            of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateAsciiStream(String columnName, java.io.InputStream x,
			int length) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a BigDecimal value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBigDecimal(int columnIndex, java.math.BigDecimal x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a BigDecimal value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBigDecimal(String columnName, java.math.BigDecimal x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a binary stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @param length
	 *            the length of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBinaryStream(int columnIndex, java.io.InputStream x,
			int length) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a binary stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @param length
	 *            of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBinaryStream(String columnName, java.io.InputStream x,
			int length) throws java.sql.SQLException {
	}

	public void updateBlob(int parm1, Blob parm2) throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateBlob() not yet implemented.");
	}

	public void updateBlob(String parm1, Blob parm2)
			throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateBlob() not yet implemented.");
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a boolean value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBoolean(int columnIndex, boolean x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a boolean value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBoolean(String columnName, boolean x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a byte value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateByte(int columnIndex, byte x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a byte value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateByte(String columnName, byte x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a byte array value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBytes(int columnIndex, byte[] x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a byte array value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateBytes(String columnName, byte[] x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a character stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @param length
	 *            the length of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateCharacterStream(int columnIndex, java.io.Reader x,
			int length) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a character stream value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param reader
	 *            the new column value
	 * @param length
	 *            of the stream
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateCharacterStream(String columnName, java.io.Reader reader,
			int length) throws java.sql.SQLException {
	}

	public void updateClob(int parm1, Clob parm2) throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateClob() not yet implemented.");
	}

	public void updateClob(String parm1, Clob parm2)
			throws java.sql.SQLException {
		/** @todo Implement this java.sql.ResultSet method */
		throw new java.lang.UnsupportedOperationException(
				"Method updateClob() not yet implemented.");
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Date value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateDate(int columnIndex, java.sql.Date x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Date value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateDate(String columnName, java.sql.Date x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Double value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateDouble(int columnIndex, double x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a double value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateDouble(String columnName, double x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a float value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateFloat(int columnIndex, float x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a float value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateFloat(String columnName, float x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an integer value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateInt(int columnIndex, int x) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an integer value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; insteadthe <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateInt(String columnName, int x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a long value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateLong(int columnIndex, long x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a long value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateLong(String columnName, long x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Give a nullable column a null value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateNull(int columnIndex) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a null value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateNull(String columnName) throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an Object value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateObject(int columnIndex, Object x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an Object value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @param scale
	 *            For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
	 *            this is the number of digits after the decimal. For all other
	 *            types this value will be ignored.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateObject(int columnIndex, Object x, int scale)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an Object value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateObject(String columnName, Object x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with an Object value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @param scale
	 *            For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
	 *            this is the number of digits after the decimal. For all other
	 *            types this value will be ignored.
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateObject(String columnName, Object x, int scale)
			throws java.sql.SQLException {
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-6-11 15:28:13)
	 * 
	 * @param columnIndex
	 *            int
	 * @param x
	 *            java.sql.Ref
	 * @exception java.sql.SQLException
	 *                异常说明。
	 */
	public void updateRef(int columnIndex, Ref x) throws java.sql.SQLException {
	}

	/**
	 * 此处插入方法说明。 创建日期：(2004-6-11 15:28:13)
	 * 
	 * @param columnName
	 *            String
	 * @param x
	 *            java.sql.Ref
	 * @exception java.sql.SQLException
	 *                异常说明。
	 */
	public void updateRef(String columnName, Ref x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates the underlying database with the new contents of the current row.
	 * Cannot be called when on the insert row.
	 *
	 * @exception SQLException
	 *                if a database access error occurs or if called when on the
	 *                insert row
	 */
	public void updateRow() throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a short value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateShort(int columnIndex, short x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a short value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateShort(String columnName, short x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a String value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the inert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateString(int columnIndex, String x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a String value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateString(String columnName, String x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Time value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateTime(int columnIndex, java.sql.Time x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Time value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateTime(String columnName, java.sql.Time x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Timestamp value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnIndex
	 *            the first column is 1, the second is 2, ...
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
			throws java.sql.SQLException {
	}

	/**
	 * JDBC 2.0
	 *
	 * Updates a column with a Timestamp value.
	 *
	 * The <code>updateXXX</code> methods are used to update column values in
	 * the current row, or the insert row. The <code>updateXXX</code> methods do
	 * not update the underlying database; instead the <code>updateRow</code> or
	 * <code>insertRow</code> methods are called to update the database.
	 *
	 * @param columnName
	 *            the name of the column
	 * @param x
	 *            the new column value
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void updateTimestamp(String columnName, java.sql.Timestamp x)
			throws java.sql.SQLException {
	}

	/**
	 * This method was created by a SmartGuide.
	 * 
	 * @return boolean
	 */
	public boolean wasNull() throws SQLException {
		if (wasnull == true)
			return true;
		else
			return false;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) this;
	}
	public int getHoldability() throws SQLException {
		return 2;
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
	    return getNCharacterStream(getColumnIndex(columnLabel));
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return getNClob(getColumnIndex(columnLabel));
	}

	public String getNString(int columnIndex) throws SQLException {
		return null;
	}

	public String getNString(String columnLabel) throws SQLException {
		return getNString(getColumnIndex(columnLabel));
	}

	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return null;
	}

	public <T> T getObject(String columnLabel, Class<T> type)
			throws SQLException {
		return getObject(getColumnIndex(columnLabel), type);
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		return null;
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return getRowId(getColumnIndex(columnLabel));
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return null;
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
	    return getSQLXML(getColumnIndex(columnLabel));

	}

	public boolean isClosed() throws SQLException {
		return false;
	}

	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {

	}

	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		updateAsciiStream(getColumnIndex(columnLabel), x);

	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {

	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		updateAsciiStream(getColumnIndex(columnLabel), x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
	}

	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		updateBinaryStream(getColumnIndex(columnLabel), x);

	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {

	}

	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
	    updateBinaryStream(getColumnIndex(columnLabel), x, length);


	}

	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		updateBlob(columnIndex, new ByteBlob(inputStream));

	}

	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		updateBlob(getColumnIndex(columnLabel), inputStream);

	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		updateBlob(columnIndex, new ByteBlob(inputStream));

	}

	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		updateBlob(getColumnIndex(columnLabel), inputStream, length);

	}

	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {

	}

	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		updateCharacterStream(getColumnIndex(columnLabel), reader);

	}

	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {

	}

	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		updateCharacterStream(getColumnIndex(columnLabel), reader, length);

	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		updateClob(columnIndex, new CharClob(reader));

	}

	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		updateClob(getColumnIndex(columnLabel), reader);

	}

	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		updateClob(columnIndex, new CharClob(reader));
	}

	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		updateClob(getColumnIndex(columnLabel), reader, length);

	}

	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {

	}

	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		updateNCharacterStream(getColumnIndex(columnLabel), reader);

	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
	}

	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		updateNCharacterStream(getColumnIndex(columnLabel), reader, length);

	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
	}

	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		 updateNClob(getColumnIndex(columnLabel), nClob);

	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {

	}

	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
	    updateNClob(getColumnIndex(columnLabel), reader);

	}

	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
	}

	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		updateNClob(getColumnIndex(columnLabel), reader, length);

	}

	public void updateNString(int columnIndex, String nString)
			throws SQLException {
	}

	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		updateNString(getColumnIndex(columnLabel), nString);

	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		 updateRowId(getColumnIndex(columnLabel), x);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {

	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject)throws SQLException {
		updateSQLXML(getColumnIndex(columnLabel), xmlObject);

	}
}
