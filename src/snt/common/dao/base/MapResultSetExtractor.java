/**
 * 
 */
package snt.common.dao.base;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * @author gxc
 *
 */
public class MapResultSetExtractor implements ResultSetExtractor {
	

	public MapResultSetExtractor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	public List<Map> extractData(ResultSet rs) throws SQLException, DataAccessException {
		List<Map> objList = new ArrayList<Map>(20);
		try {
			while (rs.next()) {
				Map<String, Object> map = new HashMap<String, Object>();
				ResultSetMetaData rsMetaData = rs.getMetaData();
				int columnCount = rsMetaData.getColumnCount();
				for(int colIndex = 1; colIndex <= columnCount; colIndex ++){
					String colName = rsMetaData.getColumnName(colIndex);
					Object value = getValue(rs, colIndex, rsMetaData.getColumnType(colIndex));
					map.put(colName, value);
				}				
				objList.add(map);
			}
		} catch (Throwable e) {
			throw new ObjectRetrievalFailureException("ƴװMapהгԶխá", e);
		} 
		return objList;
	}

	private Object getValue(ResultSet rs, int columnIndex, int columnType) throws SQLException{
		Object obj = null;
		switch (columnType) {
		case Types.BLOB:{
			obj = rs.getBlob(columnIndex);
			break;
		}			
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:{
			obj = rs.getObject(columnIndex);
			break;
		}
		case Types.CLOB:{
			obj = rs.getClob(columnIndex);
			break;
		}
		default:{
			obj = rs.getObject(columnIndex);
			break;
		}
		}
		return obj;
	}
}
