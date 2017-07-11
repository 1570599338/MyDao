//$Id: SybaseDialect.java,v 1.29 2005/11/27 22:42:03 oneovthafew Exp $
package snt.common.dao.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * An SQL dialect compatible with Sybase and MS SQL Server.
 * @author Gavin King
 */

public class SybaseDialect extends Dialect {
	public SybaseDialect() {
		super();
		registerColumnType( Types.BIT, "tinyint" ); //Sybase BIT type does not support null values
		registerColumnType( Types.BIGINT, "numeric(19,0)" );
		registerColumnType( Types.SMALLINT, "smallint" );
		registerColumnType( Types.TINYINT, "tinyint" );
		registerColumnType( Types.INTEGER, "int" );
		registerColumnType( Types.CHAR, "char(1)" );
		registerColumnType( Types.VARCHAR, "varchar($l)" );
		registerColumnType( Types.FLOAT, "float" );
		registerColumnType( Types.DOUBLE, "double precision" );
		registerColumnType( Types.DATE, "datetime" );
		registerColumnType( Types.TIME, "datetime" );
		registerColumnType( Types.TIMESTAMP, "datetime" );
		registerColumnType( Types.VARBINARY, "varbinary($l)" );
		registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
		registerColumnType( Types.BLOB, "image" );
		registerColumnType( Types.CLOB, "text" );
		
		getDefaultProperties().setProperty("STATEMENT_BATCH_SIZE", NO_BATCH);
	}

	public String getAddColumnString() {
		return "add";
	}
	public String getNullColumnString() {
		return " null";
	}
	public boolean qualifyIndexName() {
		return false;
	}

	public String getForUpdateString() {
		return "";
	}

	public boolean supportsIdentityColumns() {
		return true;
	}
	public String getIdentitySelectString() {
		return "select @@identity";
	}
	public String getIdentityColumnString() {
		return "identity not null"; //starts with 1, implicitly
	}

	public boolean supportsInsertSelectIdentity() {
		return true;
	}

	public String appendIdentitySelectToInsert(String insertSQL) {
		return insertSQL + "\nselect @@identity";
	}
	
	public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
		return col; // sql server just returns automatically
	}
	
	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		boolean isResultSet = ps.execute(); 
//		 This assumes you will want to ignore any update counts 
		while (!isResultSet && ps.getUpdateCount() != -1) { 
		    isResultSet = ps.getMoreResults(); 
		} 
		ResultSet rs = ps.getResultSet(); 
//		 You may still have other ResultSets or update counts left to process here 
//		 but you can't do it now or the ResultSet you just got will be closed 
		return rs;
	}

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	public String getCurrentTimestampSelectString() {
		return "select getdate()";
	}

	public boolean supportsTemporaryTables() {
		return true;
	}

	public String generateTemporaryTableName(String baseTableName) {
		return "#" + baseTableName;
	}

	public boolean dropTemporaryTableAfterUse() {
		return true;  // sql-server, at least needed this dropped after use; strange!
	}
}
