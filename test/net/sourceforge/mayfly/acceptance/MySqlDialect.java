package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is for MySQL 5.x.  For 4.x, see 
 * {@link net.sourceforge.mayfly.acceptance.MySql4Dialect}.
 * 
 * To make this work, install MySQL (the server), start it up on localhost,
 * and that might be all you need...
 * If you have a root password, see {@link #MYSQL_ROOT_PASSWORD}.
 */
public class MySqlDialect extends Dialect {
    
    /* Wouldn't really have to be the root password, I don't
       think, although I don't know whether there is a way to
       give selective access to CREATE/DROP DATABASE.  We don't
       need access to more than mayflytest, but we do need a way
       to clear that database on each test (unless we start
       testing schemas).
       
       Oh, yeah, and we should read the password from a 
       file/property/environment-variable and all that
       jazz (sigh - see why I like embedded databases like
       Derby and Hypersonic?).
     */
    private static final String MYSQL_ROOT_PASSWORD = "";

    // For the moment, we keep the default SQL MODE setting.
    // We probably want SET sql_mode = 'ANSI'

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection bootstrapConnection = DriverManager.getConnection(
            "jdbc:mysql://localhost/", "root", MYSQL_ROOT_PASSWORD);
        SqlTestCase.execute("DROP DATABASE IF EXISTS mayflytest", 
            bootstrapConnection);
        SqlTestCase.execute("CREATE DATABASE mayflytest", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost/mayflytest", "root", MYSQL_ROOT_PASSWORD);
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE mayflytest", connection);
        connection.close();
    }
    
    public boolean backslashInAStringIsAnEscape() {
        return true;
    }
    
    public boolean canQuoteIdentifiers() {
        // We'd need to set ANSI mode, and check whether the JDBC driver
        // even supports ANSI mode.
        return false;
    }
    
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if");
    }

    public boolean verticalBarsMeanConcatenation() {
        // We'd need to set ANSI mode.
        return false;
    }
    
    public boolean tableNamesMightBeCaseSensitive() {
        // Whether table names are case sensitive in MySQL depends on whether
        // file names are.
        return true;
    }
    
    public boolean constraintNamesMightBeCaseSensitive() {
        // I didn't find this one in the MySQL 5.1 documentation under foreign
        // keys.  Maybe there is a separate section about constraint names.
        return true;
    }
    
    public boolean duplicateConstraintNamesOk() {
        /* It would appear that UNIQUE are in one namespace, PRIMARY KEY
           in another, and FOREIGN KEY in a third.  But what happens
           on "alter table foo drop constraint foo_unique"?  Which
           one gets dropped? */
        return true;
    }

    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    public boolean innerJoinRequiresOn() {
        return false;
    }

    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        // This appears to be a case in which MySQL5 is
        // less standard (or at least, less similar to
        // the other databases in our tests) than MySQL4.
        return withParentheses;
    }

    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    public boolean notBindsMoreTightlyThanIn() {
        // The default is the same as most databases: NOT has lower
        // precedence than IN.  The other (MySQL4) behavior is
        // available by setting the SQL mode HIGH_NOT_PRECEDENCE.
        return super.notBindsMoreTightlyThanIn();
    }

    public boolean notRequiresBoolean() {
        return false;
    }

    public boolean canHaveLimitWithoutOrderBy() {
        return true;
    }
    
    public boolean canOrderByExpression() {
        return true;
    }
    
    public boolean fromIsOptional() {
        return true;
    }
    
    public boolean maySpecifyTableDotColumnToJdbc() {
        return true;
    }
    
    public boolean schemasMissing() {
        // Not something missing in MySQL so much as something we haven't figured
        // out how to test.
        return true;
    }
    
    public boolean canSumStrings(boolean rowsPresent) {
        return true;
    }
    
    public boolean errorIfNotAggregateOrGrouped() {
        return false;
    }
    
    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    public boolean valuesClauseCanReferToColumn() {
        return true;
    }

    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }

    public boolean whereCanReferToColumnAlias() {
        return false;
    }

    public boolean canGetValueViaExpressionName() {
        return true;
    }
    
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }

    public void endTransaction(Connection connection) throws SQLException {
        // Neither setAutoCommit(true) nor commit() seems to suffice.
        // Is that really true about commit()?  That it only works if
        // you have made a change?
        connection.rollback();
    }

    public String tableTypeForTransactions() {
        return " type=innodb";
    }
    
    public String tableTypeForForeignKeys() {
        return " type=innodb";
    }
    
    public boolean foreignKeyJustNeedsIndex() {
        /*
           The MySQL rule is somewhat complicated, and is harder to figure
           out because the error message is just "errno 150" with the
           real error message buried in SHOW ENGINE INNODB STATUS.
           But the behavior is basically documented.
           
           The rule is that a foreign key needs an index on both
           the referenced column, and the referring column.
           The index on the the referring column will be automatically
           created if need be.  The index on the referenced one will
           not.  Various bits of SQL cause an index to be created
           (most obviously PRIMARY KEY, but also things like another
           foreign key, per the rule given above).
         */
        return true;
    }
    
    public boolean onDeleteSetDefaultMissing(boolean tableCreateTime) {
        return true;
    }
    
    public boolean haveDropTableFooIfExists() {
        return false;
    }
    
    public boolean allowJdbcParameterAsDefault() {
        // I guess this fits along with "from foo?.tab"
        // and other looseness allowed with ?
        return true;
    }
    
    public boolean notNullImpliesDefaults() {
        // An odd (though documented) quirk of MySQL:
        // declaring a field NOT NULL changes its
        // default value from NULL to some other
        // value (0, '', etc).
        return true;
    }
    
    public boolean timestampDoesNotRespectNull() {
        /* Is the behavior documented?  Is there any way to get
           a null into a TIMESTAMP column?  What else is
           going on here?  */
        return true;
    }

    public boolean haveOnUpdateValue() {
        // As far as I can tell from the MySQL documentation,
        // ON UPDATE only applies to ON UPDATE CURRENT_TIMESTAMP.
        return false;
    }
    
    public boolean haveAutoUnderbarIncrement() {
        return true;
    }
    
    public boolean haveSerial() {
        return true;
    }
    
    public boolean autoIncrementIsRelativeToLastValue() {
        return true;
    }
    
    public String identityType() {
        return "integer auto_increment primary key";
    }
    
    public String lastIdentityValueQuery(String table, String column) {
        return "select last_insert_id()";
    }

    public boolean datesAreOff() {
        return true;
    }
    
    public boolean allowDateInTimestampColumn() {
        return true;
    }
    
    public boolean allowTimestampInDateColumn() {
        return true;
    }

    public boolean dataTypesAreEnforced() {
        return false;
    }
    
    public boolean canGetBytesOnNumber() {
        return true;
    }
    
    public boolean canCompareStringColumnToIntegerLiteral() {
        return true;
    }
    
    public boolean allowHexForInteger() {
        return true;
    }
    
    public boolean addingColumnCountsAsAffectedRow() {
        return true;
    }
    
    public boolean errorIfOrderByNotInSelectDistinct() {
        return false;
    }

}
