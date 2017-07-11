package snt.common.rs;

import java.io.Serializable;
import java.sql.Types;

/**
 * 结果集元数据VO
 * 
 */
public class MetaDataVO implements Serializable, Cloneable, Comparable {
	private static final long serialVersionUID = 1111L;
	//数值类型，精度从低到高
	private static final int[] numericalTypes = new int[]{
			Types.TINYINT,
			Types.SMALLINT,
			Types.INTEGER,
			Types.BIGINT,
			Types.FLOAT,
			Types.DOUBLE,
			Types.NUMERIC,
			Types.DECIMAL
	};
	private String m_strFieldName;

	private String m_strDisplayName;

	private int m_iColumnType = Types.VARCHAR;

	private int m_iNullable = 1;

	private int m_iScale = 0;

	private int m_iPrecision = 0;

	/**
	 * MetaDataVO 构造子注解。
	 */
	public MetaDataVO() {
		super();
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof MetaDataVO) {
			if(obj == this)
				return true;
			MetaDataVO metaData = (MetaDataVO)obj;
			return (MemoryResultSetUtils.equals(getFieldName(), metaData.getFieldName())
					&& (getColumnType() == metaData.getColumnType())
					&& (getNullable() == metaData.getNullable())
					&& (getPrecision() == metaData.getPrecision())
					&& (getScale() == metaData.getScale())
					&& MemoryResultSetUtils.equals(getDisplayName(), metaData.getDisplayName()));
		}
		return false;
	}

	/*
	 * （非 Javadoc）
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * 获得数据类型 创建日期：(02-6-5 15:15:33)
	 * 
	 * @return java.lang.String
	 */
	public int getColumnType() {
		return m_iColumnType;
	}

	/**
	 * 返回数值对象的显示名称。
	 * 
	 * 创建日期：(2001-2-15 14:18:08)
	 * 
	 * @return java.lang.String 返回数值对象的显示名称。
	 */
	public String getDisplayName() {
		return m_strDisplayName;
	}

	/**
	 * 设置数值对象的显示名称
	 * 
	 * @param strDisplayName
	 */
	public void setDisplayName(String strDisplayName) {
		m_strDisplayName = strDisplayName;
	}

	/**
	 * 获得字段名 创建日期：(02-6-5 15:15:33)
	 * 
	 * @return java.lang.String
	 */
	public String getFieldName() {
		return m_strFieldName;
	}

	/**
	 * 获得可空属性 创建日期：(02-6-5 15:15:33)
	 * 
	 * @return java.lang.String
	 */
	public int getNullable() {
		return m_iNullable;
	}

	/**
	 * 获得数据精度 创建日期：(02-6-5 15:15:33)
	 * 
	 * @return java.lang.String
	 */
	public int getPrecision() {
		return m_iPrecision;
	}

	/**
	 * 获得数值范围 创建日期：(02-6-5 15:15:33)
	 * 
	 * @return java.lang.String
	 */
	public int getScale() {
		return m_iScale;
	}

	/**
	 * 设置数据类型 创建日期：(02-6-5 15:15:33)
	 */
	public void setColumnType(int iColumnType) {
		m_iColumnType = iColumnType;
	}

	/**
	 * 设置字段名 创建日期：(02-6-5 15:15:33)
	 */
	public void setFieldName(String strFieldName) {
		m_strFieldName = strFieldName;
	}

	/**
	 * 设置可空属性 创建日期：(02-6-5 15:15:33)
	 */
	public void setNullable(int iNullable) {
		m_iNullable = iNullable;
	}

	/**
	 * 设置数据精度 创建日期：(02-6-5 15:15:33)
	 */
	public void setPrecision(int iPrecision) {
		m_iPrecision = iPrecision;
	}

	/**
	 * 设置数值范围 创建日期：(02-6-5 15:15:33)
	 */
	public void setScale(int iScale) {
		m_iScale = iScale;
	}

	public int compareTo(Object o) {
		if (o instanceof MetaDataVO) {
			if(o == this || equals(o))
				return 0;
			MetaDataVO metaData = (MetaDataVO)o;
			try {
				int c = MemoryResultSetUtils.compare(getFieldName(), metaData.getFieldName());
				if (c != 0) {
					return c;
				}
				c = compareTypes(getColumnType(), metaData.getColumnType());
				if (c != 0) {
					return c;
				}
				c = getNullable()-metaData.getNullable();
				if (c != 0) {
					return c;
				}
				c = getPrecision()-metaData.getPrecision();
				if (c != 0) {
					return c;
				}
				c = getScale()-metaData.getScale();
				if (c != 0) {
					return c;
				}
				c = MemoryResultSetUtils.compare(getDisplayName(), metaData.getDisplayName());
				return c;
			} catch (Exception e) {
				return 0;
			}
		}
		return 1;
	}
	
	private static int compareTypes(int sqlType1, int sqlType2){
		if(sqlType1 == sqlType2){
			return 0;
		}
		int index1 = -1;
		int index2 = -1;
		for (int i = 0; i < numericalTypes.length; i++) {
			if (sqlType1 == numericalTypes[i]) {
				index1 = i;
			}
			if (sqlType2 == numericalTypes[i]) {
				index2 = i;
			}
		}
		if (index1 >=0 || index2>=0) {
			return index1-index2;
		} 
		else {
			return sqlType1-sqlType2;
		}
	}

	/* （非 Javadoc）
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return m_strDisplayName==null?m_strFieldName:m_strDisplayName;
	}
}
