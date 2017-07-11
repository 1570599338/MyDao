//$Id: DB2Dialect.java,v 1.38 2005/12/08 02:41:15 oneovthafew Exp $
package snt.common.dao.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * An SQL dialect for DB2.
 * @author Gavin King
 */
public class DB2Dialect extends Dialect {

	public DB2Dialect() {
		super();
		registerColumnType( Types.BIT, "smallint" );
		registerColumnType( Types.BIGINT, "bigint" );
		registerColumnType( Types.SMALLINT, "smallint" );
		registerColumnType( Types.TINYINT, "smallint" );
		registerColumnType( Types.INTEGER, "integer" );
		registerColumnType( Types.CHAR, "char(1)" );
		registerColumnType( Types.VARCHAR, "varchar($l)" );
		registerColumnType( Types.FLOAT, "float" );
		registerColumnType( Types.DOUBLE, "double" );
		registerColumnType( Types.DATE, "date" );
		registerColumnType( Types.TIME, "time" );
		registerColumnType( Types.TIMESTAMP, "timestamp" );
		registerColumnType( Types.VARBINARY, "varchar($l) for bit data" );
		registerColumnType( Types.NUMERIC, "numeric($p,$s)" );
		registerColumnType( Types.BLOB, "blob($l)" );
		registerColumnType( Types.CLOB, "clob($l)" );

		registerKeyword("current");
		registerKeyword("date");
		registerKeyword("time");
		registerKeyword("timestamp");
		registerKeyword("fetch");
		registerKeyword("first");
		registerKeyword("rows");
		registerKeyword("only");
		
		getDefaultProperties().setProperty("STATEMENT_BATCH_SIZE", NO_BATCH);
	}

	public String getLowercaseFunction() {
		return "lcase";
	}
	
	public String getAddColumnString() {
		return "add column";
	}
	public boolean dropConstraints() {
		return false;
	}
	public boolean supportsIdentityColumns() {
		return true;
	}
	public String getIdentitySelectString() {
		return "values identity_val_local()";
	}
	public String getIdentityColumnString() {
		return "generated by default as identity"; //not null ... (start with 1) is implicit
	}
	public String getIdentityInsertString() {
		return "default";
	}

	public String getSequenceNextValString(String sequenceName) {
		return "values nextval for " + sequenceName;
	}
	public String getCreateSequenceString(String sequenceName) {
		return "create sequence " + sequenceName;
	}
	public String getDropSequenceString(String sequenceName) {
		return "drop sequence " + sequenceName + " restrict";
	}

	public boolean supportsSequences() {
		return true;
	}

	public String getQuerySequencesString() {
		return "select seqname from sysibm.syssequences";
	}

	public boolean supportsLimit() {
		return true;
	}

	/*public String getLimitString(String sql, boolean hasOffset) {
		StringBuffer rownumber = new StringBuffer(50)
			.append(" rownumber() over(");
		int orderByIndex = sql.toLowerCase().indexOf("order by");
		if (orderByIndex>0) rownumber.append( sql.substring(orderByIndex) );
		rownumber.append(") as row_,");
		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 )
			.append("select * from ( ")
			.append(sql)
			.insert( getAfterSelectInsertPoint(sql)+16, rownumber.toString() )
			.append(" ) as temp_ where row_ ");
		if (hasOffset) {
			pagingSelect.append("between ?+1 and ?");
		}
		else {
			pagingSelect.append("<= ?");
		}
		return pagingSelect.toString();
	}*/
	
	/**
	 * Render the <tt>rownumber() over ( .... ) as rownumber_,</tt> 
	 * bit, that goes in the select list
	 */
	private String getRowNumber(String sql) {
		StringBuffer rownumber = new StringBuffer(50)
			.append("rownumber() over(");

		int orderByIndex = sql.toLowerCase().indexOf("order by");
		
		if ( orderByIndex>0 && !hasDistinct(sql) ) {
			rownumber.append( sql.substring(orderByIndex) );
		}
			 
		rownumber.append(") as rownumber_,");
		
		return rownumber.toString();
	}

	public String getLimitString(String sql, boolean hasOffset) {
		
		int startOfSelect = sql.toLowerCase().indexOf("select");
		
		StringBuffer pagingSelect = new StringBuffer( sql.length()+100 )
					.append( sql.substring(0, startOfSelect) ) //add the comment
					.append("select * from ( select ") //nest the main query in an outer select
					.append( getRowNumber(sql) ); //add the rownnumber bit into the outer query select list
		
		if ( hasDistinct(sql) ) {
			pagingSelect.append(" row_.* from ( ") //add another (inner) nested select
				.append( sql.substring(startOfSelect) ) //add the main query
				.append(" ) as row_"); //close off the inner nested select
		}
		else {
			pagingSelect.append( sql.substring( startOfSelect + 6 ) ); //add the main query
		}
				
		pagingSelect.append(" ) as temp_ where rownumber_ ");
		
		//add the restriction to the outer select
		if (hasOffset) {
			pagingSelect.append("between ?+1 and ?");
		}
		else {
			pagingSelect.append("<= ?");
		}
		
		return pagingSelect.toString();
	}

	private static boolean hasDistinct(String sql) {
		return sql.toLowerCase().indexOf("select distinct")>=0;
	}
	
	public String getForUpdateString() {
		return " for read only with rs";
	}

	public boolean useMaxForLimit() {
		return true;
	}
	
	public boolean supportsOuterJoinForUpdate() {
		return false;
	}
	
	public boolean supportsNotNullUnique() {
		return false;
	}

	public String getSelectClauseNullString(int sqlType) {
		String literal;
		switch(sqlType) {
			case Types.VARCHAR:
			case Types.CHAR:
				literal = "'x'";
				break;
			case Types.DATE:
				literal = "'2000-1-1'";
				break;
			case Types.TIMESTAMP:
				literal = "'2000-1-1 00:00:00'";
				break;
			case Types.TIME:
				literal = "'00:00:00'";
				break;
			default:
				literal = "0";
		}
		return "nullif(" + literal + ',' + literal + ')';
	}
	
	public static void main(String[] args) {
		System.out.println( new DB2Dialect().getLimitString("/*foo*/ select * from foos", true) );
		System.out.println( new DB2Dialect().getLimitString("/*foo*/ select distinct * from foos", true) );
		System.out.println( new DB2Dialect().getLimitString("/*foo*/ select * from foos foo order by foo.bar, foo.baz", true) );
		System.out.println( new DB2Dialect().getLimitString("/*foo*/ select distinct * from foos foo order by foo.bar, foo.baz", true) );
	}

	public boolean supportsUnionAll() {
		return true;
	}
	
	public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
		return col;
	}
	
	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		boolean isResultSet = ps.execute(); 
		// This assumes you will want to ignore any update counts 
		while (!isResultSet && ps.getUpdateCount() != -1) { 
		    isResultSet = ps.getMoreResults(); 
		} 
		ResultSet rs = ps.getResultSet(); 
		// You may still have other ResultSets or update counts left to process here 
		// but you can't do it now or the ResultSet you just got will be closed 
		return rs;
	}

	public boolean supportsCommentOn() {
		return true;
	}
	
	public boolean supportsTemporaryTables() {
		return true;
	}

	public String getCreateTemporaryTableString() {
		return "declare global temporary table";
	}

	public String getCreateTemporaryTablePostfix() {
		return "not logged";
	}

	public String generateTemporaryTableName(String baseTableName) {
		return "session." + super.generateTemporaryTableName(baseTableName);
	}

	public boolean supportsCurrentTimestampSelection() {
		return true;
	}

	public String getCurrentTimestampSelectString() {
		return "values current timestamp";
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		return false;
	}

	public boolean supportsParametersInInsertSelect() {
		// DB2 known to not support parameters within the select
		// clause of an SQL INSERT ... SELECT ... statement
		return false;
	}

	public String getCurrentTimestampSQLFunctionName() {
		return "sysdate";
	}
}
