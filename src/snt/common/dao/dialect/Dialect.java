//$Id: Dialect.java,v 1.65 2006/01/16 19:00:29 steveebersole Exp $
package snt.common.dao.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import snt.common.string.StringUtil; 

/**
 * Represents a dialect of SQL implemented by a particular RDBMS.
 * Subclasses implement Hibernate compatibility with different systems.<br>
 * <br>
 * Subclasses should provide a public default constructor that <tt>register()</tt>
 * a set of type mappings and default Hibernate properties.<br>
 * <br>
 * Subclasses should be immutable.
 *
 * @author Gavin King, David Channon
 */
public abstract class Dialect {
	static final String DEFAULT_BATCH_SIZE = "15";
	static final String NO_BATCH = "0";

	private final TypeNames typeNames = new TypeNames();

	private final Properties properties = new Properties();
	private final Set sqlKeywords = new HashSet();
	
	//是否用Statement代替PrepareStatement sealinglip added on 2006-08-25
	protected boolean useStmtInsteadOfPstmt = false;
	
	protected Dialect() {		
	}

	public String toString() {
		return getClass().getName();
	}

	/**
	 * Characters used for quoting SQL identifiers
	 */
	public static final String QUOTE = "`\"[";
	public static final String CLOSED_QUOTE = "`\"]";


	/**
	 * Get the name of the database type associated with the given
	 * <tt>java.sql.Types</tt> typecode.
	 *
	 * @param code <tt>java.sql.Types</tt> typecode
	 * @return the database type name
	 * @throws HibernateException
	 */
	public String getTypeName(int code) throws DialectException {
		String result = typeNames.get( code );
		if ( result == null ) {
			throw new DialectException( "No default type mapping for (java.sql.Types) " + code );
		}
		return result;
	}

	/**
	 * Get the name of the database type associated with the given
	 * <tt>java.sql.Types</tt> typecode.
	 * @param code      <tt>java.sql.Types</tt> typecode
	 * @param length    the length or precision of the column
	 * @param precision the precision of the column
	 * @param scale the scale of the column
	 *
	 * @return the database type name
	 * @throws HibernateException
	 */
	public String getTypeName(int code, int length, int precision, int scale) throws DialectException {
		String result = typeNames.get( code, length, precision, scale );
		if ( result == null ) {
			throw new DialectException( 
					"No type mapping for java.sql.Types code: " +
					code +
					", length: " +
					length 
				);
		}
		return result;
	}

	public String getCastTypeName(int code) throws DialectException{
		return getTypeName(
				code, 
				255, 
				19, 
				2 
			);
	}
	
	@SuppressWarnings("unchecked")
	protected void registerKeyword(String word) {
		sqlKeywords.add(word);
	}
	
	public Set getKeywords() {
		return sqlKeywords;
	}

	/**
	 * Subclasses register a typename for the given type code and maximum
	 * column length. <tt>$l</tt> in the type name with be replaced by the
	 * column length (if appropriate).
	 *
	 * @param code     <tt>java.sql.Types</tt> typecode
	 * @param capacity maximum length of database type
	 * @param name     the database type name
	 */
	protected void registerColumnType(int code, int capacity, String name) {
		typeNames.put( code, capacity, name );
	}

	/**
	 * Subclasses register a typename for the given type code. <tt>$l</tt> in
	 * the type name with be replaced by the column length (if appropriate).
	 *
	 * @param code <tt>java.sql.Types</tt> typecode
	 * @param name the database type name
	 */
	protected void registerColumnType(int code, String name) {
		typeNames.put( code, name );
	}
	
	/**
	 * Does this dialect support the <tt>ALTER TABLE</tt> syntax?
	 *
	 * @return boolean
	 */
	public boolean hasAlterTable() {
		return true;
	}

	/**
	 * Do we need to drop constraints before dropping tables in this dialect?
	 *
	 * @return boolean
	 */
	public boolean dropConstraints() {
		return true;
	}

	/**
	 * Do we need to qualify index names with the schema name?
	 *
	 * @return boolean
	 */
	public boolean qualifyIndexName() {
		return true;
	}

	/**
	 * Does the <tt>FOR UPDATE OF</tt> syntax specify particular
	 * columns?
	 */
	public boolean forUpdateOfColumns() {
		return false;
	}

	/**
	 * Does this dialect support the <tt>FOR UPDATE OF</tt> syntax?
	 *
	 * @return boolean
	 */
	public String getForUpdateString(String aliases) {
		return getForUpdateString();
	}

	/**
	 * Does this dialect support the Oracle-style <tt>FOR UPDATE OF ... NOWAIT</tt>
	 * syntax?
	 *
	 * @return boolean
	 */
	public String getForUpdateNowaitString(String aliases) {
		return getForUpdateString( aliases );
	}

	/**
	 * Does this dialect support the <tt>FOR UPDATE</tt> syntax?
	 *
	 * @return boolean
	 */
	public String getForUpdateString() {
		return " for update";
	}

	/**
	 * Does this dialect support the Oracle-style <tt>FOR UPDATE NOWAIT</tt> syntax?
	 *
	 * @return boolean
	 */
	public String getForUpdateNowaitString() {
		return getForUpdateString();
	}

	/**
	 * Does this dialect support the <tt>UNIQUE</tt> column syntax?
	 *
	 * @return boolean
	 */
	public boolean supportsUnique() {
		return true;
	}
	

    /**
     * Does this dialect support adding Unique constraints via create and alter table ?
     * @return boolean
     */
	public boolean supportsUniqueConstraintInCreateAlterTable() {
	    return true;
	}


	/**
	 * The syntax used to add a column to a table (optional).
	 */
	public String getAddColumnString() {
		throw new UnsupportedOperationException( "No add column syntax supported by Dialect" );
	}

	public String getDropForeignKeyString() {
		return " drop constraint ";
	}

	public String getTableTypeString() {
		return "";
	}

	/**
	 * The syntax used to add a foreign key constraint to a table.
	 * 
	 * @param referencesPrimaryKey if false, constraint should be 
	 * explicit about which column names the constraint refers to
	 *
	 * @return String
	 */
	public String getAddForeignKeyConstraintString(
			String constraintName,
			String[] foreignKey,
			String referencedTable,
			String[] primaryKey, 
			boolean referencesPrimaryKey
	) {
		StringBuffer res = new StringBuffer( 30 );
		
		res.append( " add constraint " )
		   .append( constraintName )
		   .append( " foreign key (" )
		   .append( StringUtil.join( ", ", foreignKey ) )
		   .append( ") references " )
		   .append( referencedTable );
		
		if(!referencesPrimaryKey) {
			res.append(" (")
			   .append( StringUtil.join(", ", primaryKey) )
			   .append(')');
		}

		return res.toString();
	}

	/**
	 * The syntax used to add a primary key constraint to a table.
	 *
	 * @return String
	 */
	public String getAddPrimaryKeyConstraintString(String constraintName) {
		return " add constraint " + constraintName + " primary key ";
	}

	/**
	 * The keyword used to specify a nullable column.
	 *
	 * @return String
	 */
	public String getNullColumnString() {
		return "";
	}

	/**
	 * Does this dialect support identity column key generation?
	 *
	 * @return boolean
	 */
	public boolean supportsIdentityColumns() {
		return false;
	}

	/**
	 * Does this dialect support sequences?
	 *
	 * @return boolean
	 */
	public boolean supportsSequences() {
		return false;
	}

	public boolean supportsInsertSelectIdentity() {
		return false;
	}

	/**
	 * Append a clause to retrieve the generated identity value for the
	 * given <tt>INSERT</tt> statement.
	 */
	public String appendIdentitySelectToInsert(String insertString) {
		return insertString;
	}

	protected String getIdentitySelectString() throws DialectException {
		throw new DialectException( "Dialect does not support identity key generation" );
	}

	/**
	 * The syntax that returns the identity value of the last insert, if
	 * identity column key generation is supported.
	 *
	 * @param type TODO
	 * @throws MappingException if no native key generation
	 */
	public String getIdentitySelectString(String table, String column, int type)
			throws DialectException {
		return getIdentitySelectString();
	}

	protected String getIdentityColumnString() throws DialectException {
		throw new DialectException( "Dialect does not support identity key generation" );
	}

	/**
	 * The keyword used to specify an identity column, if identity
	 * column key generation is supported.
	 *
	 * @param type the SQL column type, as defined by <tt>java.sql.Types</tt>
	 * @throws MappingException if no native key generation
	 */
	public String getIdentityColumnString(int type) throws DialectException {
		return getIdentityColumnString();
	}

	/**
	 * The keyword used to insert a generated value into an identity column (or null).
	 * Need if the dialect does not support inserts that specify no column values.
	 *
	 * @return String
	 */
	public String getIdentityInsertString() {
		return null;
	}

	/**
	 * The keyword used to insert a row without specifying any column values.
	 * This is not possible on some databases.
	 */
	public String getNoColumnsInsertString() {
		return "values ( )";
	}

	/**
	 * Generate the appropriate select statement to to retreive the next value
	 * of a sequence, if sequences are supported.
	 * <p/>
	 * This should be a "stand alone" select statement.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String The "nextval" select string.
	 * @throws MappingException if no sequences
	 */
	public String getSequenceNextValString(String sequenceName) throws DialectException {
		throw new DialectException( "Dialect does not support sequences" );
	}

	/**
	 * Generate the select expression fragment that will retreive the next
	 * value of a sequence, if sequences are supported.
	 * <p/>
	 * This differs from {@link #getSequenceNextValString(String)} in that this
	 * should return an expression usable within another select statement.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String
	 * @throws MappingException if no sequences
	 */
	public String getSelectSequenceNextValString(String sequenceName) throws DialectException {
		throw new DialectException( "Dialect does not support sequences" );
	}

	/**
	 * The syntax used to create a sequence, if sequences are supported.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String
	 * @throws MappingException if no sequences
	 */
	protected String getCreateSequenceString(String sequenceName) throws DialectException {
		throw new DialectException( "Dialect does not support sequences" );
	}

	/**
	 * The multiline script used to create a sequence, if sequences are supported.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String[]
	 * @throws MappingException if no sequences
	 */
	public String[] getCreateSequenceStrings(String sequenceName) throws DialectException {
		return new String[]{getCreateSequenceString( sequenceName )};
	}

	/**
	 * The syntax used to drop a sequence, if sequences are supported.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String
	 * @throws MappingException if no sequences
	 */
	protected String getDropSequenceString(String sequenceName) throws DialectException {
		throw new DialectException( "Dialect does not support sequences" );
	}

	/**
	 * The multiline script used to drop a sequence, if sequences are supported.
	 *
	 * @param sequenceName the name of the sequence
	 * @return String[]
	 * @throws MappingException if no sequences
	 */
	public String[] getDropSequenceStrings(String sequenceName) throws DialectException {
		return new String[]{getDropSequenceString( sequenceName )};
	}

	/**
	 * A query used to find all sequences
	 *
	 */
	public String getQuerySequencesString() {
		return null;
	}

	/**
	 * Retrieve a set of default Hibernate properties for this database.
	 *
	 * @return a set of Hibernate properties
	 */
	public final Properties getDefaultProperties() {
		return properties;
	}

	/**
	 * Completely optional cascading drop clause
	 *
	 * @return String
	 */
	public String getCascadeConstraintsString() {
		return "";
	}

	/**
	 * The name of the SQL function that transforms a string to
	 * lowercase
	 *
	 * @return String
	 */
	public String getLowercaseFunction() {
		return "lower";
	}

	/**
	 * Does this <tt>Dialect</tt> have some kind of <tt>LIMIT</tt> syntax?
	 */
	public boolean supportsLimit() {
		return false;
	}

	/**
	 * Does this dialect support an offset?
	 */
	public boolean supportsLimitOffset() {
		return supportsLimit();
	}

	/**
	 * Add a <tt>LIMIT</tt> clause to the given SQL <tt>SELECT</tt>
	 *
	 * @return the modified SQL
	 */
	public String getLimitString(String querySelect, boolean hasOffset) {
		throw new UnsupportedOperationException( "paged queries not supported" );
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		return getLimitString( querySelect, offset>0 );
	}

	public boolean supportsVariableLimit() {
		return supportsLimit();
	}

	/**
	 * Does the <tt>LIMIT</tt> clause specify arguments in the "reverse" order
	 * limit, offset instead of offset, limit?
	 *
	 * @return true if the correct order is limit, offset
	 */
	public boolean bindLimitParametersInReverseOrder() {
		return false;
	}

	/**
	 * Does the <tt>LIMIT</tt> clause come at the start of the
	 * <tt>SELECT</tt> statement, rather than at the end?
	 *
	 * @return true if limit parameters should come before other parameters
	 */
	public boolean bindLimitParametersFirst() {
		return false;
	}

	/**
	 * Does the <tt>LIMIT</tt> clause take a "maximum" row number instead
	 * of a total number of returned rows?
	 */
	public boolean useMaxForLimit() {
		return false;
	}

	/**
	 * The opening quote for a quoted identifier
	 */
	public char openQuote() {
		return '"';
	}

	/**
	 * The closing quote for a quoted identifier
	 */
	public char closeQuote() {
		return '"';
	}

	public boolean supportsIfExistsBeforeTableName() {
		return false;
	}

	public boolean supportsIfExistsAfterTableName() {
		return false;
	}
	
	/**
	 * Does this dialect support column-level check constraints?
	 */
	public boolean supportsColumnCheck() {
		return true;
	}
	
	/**
	 * Does this dialect support table-level check constraints?
	 */
	public boolean supportsTableCheck() {
		return true;
	}

	/**
	 * Whether this dialect have an Identity clause added to the data type or a
	 * completely seperate identity data type
	 *
	 * @return boolean
	 */
	public boolean hasDataTypeInIdentityColumn() {
		return true;
	}

	public boolean supportsCascadeDelete() {
		return true;
	}

	public String getSelectGUIDString() {
		throw new UnsupportedOperationException( "dialect does not support GUIDs" );
	}

	public boolean supportsOuterJoinForUpdate() {
		return true;
	}

	public String getSelectClauseNullString(int sqlType) {
		return "null";
	}
	
	public boolean supportsNotNullUnique() {
		return true;
	}

	public final String quote(String column) {
		if ( column.charAt( 0 ) == '`' ) {
			return openQuote() + column.substring( 1, column.length() - 1 ) + closeQuote();
		}
		else {
			return column;
		}
	}

	public boolean hasSelfReferentialForeignKeyBug() {
		return false;
	}
	

	public boolean useInputStreamToInsertBlob() {
		return true;
	}

	public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
		throw new UnsupportedOperationException(
				getClass().getName() + 
				" does not support resultsets via stored procedures"
			);
	}

	public ResultSet getResultSet(CallableStatement ps) throws SQLException {
		throw new UnsupportedOperationException(
				getClass().getName() + 
				" does not support resultsets via stored procedures"
			);
	}
	
	public boolean supportsUnionAll() {
		return false;
	}
	
	public boolean supportsCommentOn() {
		return false;
	}
	
	public String getTableComment(String comment) {
		return "";
	}

	public String getColumnComment(String comment) {
		return "";
	}
	
	public String transformSelectString(String select) {
		return select;
	}

	public boolean supportsTemporaryTables() {
		return false;
	}

	public String generateTemporaryTableName(String baseTableName) {
		return "HT_" + baseTableName;
	}

	public String getCreateTemporaryTableString() {
		return "create table";
	}

	public boolean performTemporaryTableDDLInIsolation() {
		return false;
	}

	public String getCreateTemporaryTablePostfix() {
		return "";
	}

	public boolean dropTemporaryTableAfterUse() {
		return true;
	}
	
	public int getMaxAliasLength() {
		return 10;
	}

	public boolean supportsCurrentTimestampSelection() {
		return false;
	}

	public String getCurrentTimestampSelectString() {
		throw new UnsupportedOperationException( "Database not known to define a current timestamp function" );
	}

	public boolean isCurrentTimestampSelectStringCallable() {
		throw new UnsupportedOperationException( "Database not known to define a current timestamp function" );
	}
	
	/**
	 * The SQL value that the JDBC driver maps boolean values to
	 */
	public String toBooleanValueString(boolean bool) {
		return bool ? "1" : "0";
	}

	/**
	 * Does this dialect support parameters within the select clause of
	 * INSERT ... SELECT ... statements?
	 *
	 * @return True if this is supported; false otherwise.
	 */
	public boolean supportsParametersInInsertSelect() {
		return true;
	}

	/**
	 * The name of the database-specific SQL function for retrieving the
	 * current timestamp.
	 *
	 * @return The function name.
	 */
	public String getCurrentTimestampSQLFunctionName() {
		// the standard SQL function name is current_timestamp...
		return "current_timestamp";
	}

	public boolean isUseStmtInsteadOfPstmt() {
		return useStmtInsteadOfPstmt;
	}

	public void setUseStmtInsteadOfPstmt(boolean useStmtInsteadOfPstmt) {
		this.useStmtInsteadOfPstmt = useStmtInsteadOfPstmt;
	}
}
