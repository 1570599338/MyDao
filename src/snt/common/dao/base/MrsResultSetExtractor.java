/**
 * 
 */
package snt.common.dao.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import snt.common.rs.ComTool;

/**
 * 把结果集转换为内存结果集的工具
 * @author <a href="mailto:sealinglip@gmail.com">Sealinglip</a>
 */
public class MrsResultSetExtractor implements ResultSetExtractor {

	/**
	 * 
	 */
	public MrsResultSetExtractor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
	 */
	public Object extractData(ResultSet rs) throws SQLException,
			DataAccessException {
		return ComTool.convertResultSet(rs);
	}

}
