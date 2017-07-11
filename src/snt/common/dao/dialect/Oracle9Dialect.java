//$Id: Oracle9Dialect.java,v 1.49 2006/01/18 03:09:10 steveebersole Exp $
package snt.common.dao.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * An SQL dialect for Oracle 9 (uses ANSI-style syntax where possible).
 * @author Gavin King, David Channon
 */
public class Oracle9Dialect extends Dialect {

	public Oracle9Dialect() {
		super();
		registerColumnType( Types.BIT, "number(1,0)" );
		registerColumnType( Types.BIGINT, "number(19,0)" );
		registerColumnType( Types.SMALLINT, "number(5,0)" );
		registerColumnType( Types.TINYINT, "number(3,0)" );
		registerColumnType( Types.INTEGER, "number(10,0)" );
		registerColumnType( Types.CHAR, "char(1 char)" );
		registerColumnType( Types.VARCHAR, 4000, "varchar2($l char)" );
		registerColumnType( Types.VARCHAR, "long" );
		registerColumnType( Types.FLOAT, "float" );
		registerColumnType( Types.DOUBLE, "double precision" );
		registerColumnType( Types.DATE, "date" );
		registerColumnType( Types.TIME, "date" );
		registerColumnType( Types.TIMESTAMP, "timestamp" );
		registerColumnType( Types.VARBINARY, 2000, "raw($l)" );
		registerColumnType( Types.VARBINARY, "long raw" );
		registerColumnType( Types.NUMERIC, "number($p,$s)" );
		registerColumnType( Types.BLOB, "blob" );
		registerColumnType( Types.CLOB, "clob" );

		getDefaultProperties().setProperty("USE_STREAMS_FOR_BINARY", "true");
		getDefaultProperties().setProperty("STATEMENT_BATCH_SIZE", DEFAULT_BATCH_SIZE);
	}

	public String getAddColumnString() {
		return "add";
	}

	public String getSequenceNextValString(String sequenceName) {
		return "select " + getSelectSequenceNextValString( sequenceName ) + " from dual";
	}

	public String getSelectSequenceNextValString(String sequenceName) {
		return sequenceName + ".nextval";
	}

	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName; //starts with 1, implicitly
	}

	public String getDropSequenceString(String sequenceName) {
		return "drop sequence " + sequenceName;
	}

	public String getCascadeConstraintsString() {
		return " cascade constraints";
	}

	public boolean dropConstraints() {
		return false;
	}

	public String getForUpdateNowaitString() {
		return " for update nowait";
	}

	public boolean supportsSequences() {
		return true;
	}

	public boolean supportsLimit() {
		return true;
	}

	public String getLimitString(String sql, boolean hasOffset) {
		
		sql = sql.trim();
		boolean isForUpdate = false;
		if ( sql.toLowerCase().endsWith(" for update") ) {
			sql = sql.substring( 0, sql.length()-11 );
			isForUpdate = true;
		}
		
		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 );
		if (hasOffset) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		}
		else {
			pagingSelect.append("select * from ( ");
		}
		pagingSelect.append(sql);
		if (hasOffset) {
			pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
		}
		else {
			pagingSelect.append(" ) where rownum <= ?");
		}

		if ( isForUpdate ) {
			pagingSelect.append( " for update" );
		}
		
		return pagingSelect.toString();
	}

	public String getForUpdateString(String aliases) {
		return getForUpdateString() + " of " + aliases;
	}

	public String getForUpdateNowaitString(String aliases) {
		return getForUpdateString() + " of " + aliases + " nowait";
	}

	public boolean bindLimitParametersInReverseOrder() {
		return true;
	}

	public boolean useMaxForLimit() {
		return true;
	}
	
	public boolean forUpdateOfColumns() {
		return true;
	}

	public String getQuerySequencesString() {
		return "select sequence_name from user_sequences";
	}

	public String getSelectGUIDString() {
		return "select rawtohex(sys_guid()) from dual";
	}

	// not final-static to avoid possible classcast exceptions if using different oracle drivers.
	int oracletypes_cursor_value = 0; 
	public int registerResultSetOutParameter(java.sql.CallableStatement statement,int col) throws SQLException {
		if(oracletypes_cursor_value==0) {
			try {
				Class types = Class.forName("oracle.jdbc.driver.OracleTypes");
				oracletypes_cursor_value = types.getField("CURSOR").getInt(types.newInstance());
			} catch (Exception se) {
				throw new DialectException("Problem while trying to load or access OracleTypes.CURSOR value",se);
			} 
		}
		//	register the type of the out param - an Oracle specific type
		statement.registerOutParameter(col, oracletypes_cursor_value);
		col++;
		return col;
	}
	
	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		ps.execute();
		ResultSet rs = (ResultSet) ps.getObject(1);			
		return rs;		
	}

	public boolean supportsUnionAll() {
		return true;
	}
	
	public boolean supportsCommentOn() {
		return true;
	}

	public boolean supportsTemporaryTables() {
		return true;
	}

	public String generateTemporaryTableName(String baseTableName) {
		String name = super.generateTemporaryTableName(baseTableName);
		return name.length() > 30 ? name.substring( 1, 30 ) : name;
	}

	public String getCreateTemporaryTableString() {
		return "create global temporary table";
	}

	public String getCreateTemporaryTablePostfix() {
		return "on commit delete rows";
	}

	public boolean dropTemporaryTableAfterUse() {
		return false;
	}

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public String getCurrentTimestampSelectString() {
		return "select systimestamp from dual";
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}
}
