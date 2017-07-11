//$Id: MySQLDialect.java,v 1.43 2006/01/20 17:02:04 steveebersole Exp $
package snt.common.dao.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import snt.common.string.StringUtil;

/**
 * An SQL dialect for MySQL (prior to 5.x).
 *
 * @author Gavin King
 */
public class MySQLDialect extends Dialect {

	public MySQLDialect() {
		super();
		registerColumnType( Types.BIT, "bit" );
		registerColumnType( Types.BIGINT, "bigint" );
		registerColumnType( Types.SMALLINT, "smallint" );
		registerColumnType( Types.TINYINT, "tinyint" );
		registerColumnType( Types.INTEGER, "integer" );
		registerColumnType( Types.CHAR, "char(1)" );
		registerColumnType( Types.FLOAT, "float" );
		registerColumnType( Types.DOUBLE, "double precision" );
		registerColumnType( Types.DATE, "date" );
		registerColumnType( Types.TIME, "time" );
		registerColumnType( Types.TIMESTAMP, "datetime" );
		registerColumnType( Types.VARBINARY, "longblob" );
		registerColumnType( Types.VARBINARY, 16777215, "mediumblob" );
		registerColumnType( Types.VARBINARY, 65535, "blob" );
		registerColumnType( Types.VARBINARY, 255, "tinyblob" );
		registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
		registerColumnType( Types.BLOB, "longblob" );
		registerColumnType( Types.BLOB, 16777215, "mediumblob" );
		registerColumnType( Types.BLOB, 65535, "blob" );
		registerColumnType( Types.CLOB, "longtext" );
		registerColumnType( Types.CLOB, 16777215, "mediumtext" );
		registerColumnType( Types.CLOB, 65535, "text" );
		registerVarcharTypes();

		getDefaultProperties().setProperty("MAX_FETCH_DEPTH", "2");
		getDefaultProperties().setProperty("STATEMENT_BATCH_SIZE", DEFAULT_BATCH_SIZE);
	}

	protected void registerVarcharTypes() {
		registerColumnType( Types.VARCHAR, "longtext" );
		registerColumnType( Types.VARCHAR, 16777215, "mediumtext" );
		registerColumnType( Types.VARCHAR, 65535, "text" );
		registerColumnType( Types.VARCHAR, 255, "varchar($l)" );
	}

	public String getAddColumnString() {
		return "add column";
	}
	
	public boolean qualifyIndexName() {
		return false;
	}

	public boolean supportsIdentityColumns() {
		return true;
	}
	
	public String getIdentitySelectString() {
		return "select last_insert_id()";
	}

	public String getIdentityColumnString() {
		return "not null auto_increment"; //starts with 1, implicitly
	}

	public String getAddForeignKeyConstraintString(
			String constraintName, 
			String[] foreignKey, 
			String referencedTable, 
			String[] primaryKey, boolean referencesPrimaryKey
	) {
		String cols = StringUtil.join(", ", foreignKey);
		return new StringBuffer(30)
			.append(" add index ")
			.append(constraintName)
			.append(" (")
			.append(cols)
			.append("), add constraint ")
			.append(constraintName)
			.append(" foreign key (")
			.append(cols)
			.append(") references ")
			.append(referencedTable)
			.append(" (")
			.append( StringUtil.join(", ", primaryKey) )
			.append(')')
			.toString();
	}

	public boolean supportsLimit() {
		return true;
	}
	
	public String getDropForeignKeyString() {
		return " drop foreign key ";
	}

	public String getLimitString(String sql, boolean hasOffset) {
		return new StringBuffer( sql.length()+20 )
			.append(sql)
			.append( hasOffset ? " limit ?, ?" : " limit ?")
			.toString();
	}
	
	/*
	 * Temporary, until MySQL fix Connector/J bug
	 */
	/*public String getLimitString(String sql, int offset, int limit) {
		StringBuffer buf = new StringBuffer( sql.length()+20 )
			.append(sql);
		if (offset>0) {
			buf.append(" limit ")
				.append(offset)
				.append(", ")
				.append(limit);
		}
		else {
			buf.append(" limit ")
				.append(limit);
		}
		return buf.toString();
	}*/

	/*
	 * Temporary, until MySQL fix Connector/J bug
	 */
	/*public boolean supportsVariableLimit() {
		return false;
	}*/

	public char closeQuote() {
		return '`';
	}

	public char openQuote() {
		return '`';
	}

	public boolean supportsIfExistsBeforeTableName() {
		return true;
	}

	public String getSelectGUIDString() {
		return "select uuid()";
	}

	public boolean supportsCascadeDelete() {
		return false;
	}
	
	public String getTableComment(String comment) {
		return " comment='" + comment + "'";
	}

	public String getColumnComment(String comment) {
		return " comment '" + comment + "'";
	}

	public boolean supportsTemporaryTables() {
		return true;
	}

	public String getCreateTemporaryTableString() {
		return "create temporary table if not exists";
	}

	public String getCastTypeName(int code) throws DialectException{
		if ( code==Types.INTEGER ) {
			return "signed";
		}
		else if ( code==Types.VARCHAR ) {
			return "char";
		}
		else if ( code==Types.VARBINARY ) {
			return "binary";
		}
		else {
			return super.getCastTypeName( code );
		}
	}

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	public String getCurrentTimestampSelectString() {
		return "select now()";
	}

	public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
		return col;
	} 
	
	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		boolean isResultSet = ps.execute(); 
		while (!isResultSet && ps.getUpdateCount() != -1) { 
			isResultSet = ps.getMoreResults(); 
		} 
		ResultSet rs = ps.getResultSet(); 
		return rs;
	}
	
}