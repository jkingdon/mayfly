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
       to clear that database on each test.  Of course, if we
       were to start testing schemas, we would need more than mayflytest.
       
       Oh, yeah, and we should read the password from a 
       file/property/environment-variable and all that
       jazz (sigh - see why I like embedded databases like
       Derby and Hypersonic?).
     */
    private static final String MYSQL_ROOT_PASSWORD = "";

    // For the moment, we keep the default SQL MODE setting.
    // We probably want SET sql_mode = 'ANSI'

    @Override
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

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost/mayflytest", "root", MYSQL_ROOT_PASSWORD);
    }

    @Override
    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE mayflytest", connection);
        connection.close();
    }
    
    @Override
    public boolean backslashInAStringIsAnEscape() {
        return true;
    }
    
    @Override
    public boolean canQuoteIdentifiers() {
        // We'd need to set ANSI mode, and check whether the JDBC driver
        // even supports ANSI mode.
        return false;
    }
    
    @Override
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if")
            || word.equalsIgnoreCase("index");
    }

    @Override
    public boolean verticalBarsMeanConcatenation() {
        // We'd need to set ANSI mode.
        return false;
    }
    
    @Override
    public boolean tableNamesMightBeCaseSensitive() {
        // Whether table names are case sensitive in MySQL depends on whether
        // file names are.
        return true;
    }
    
    @Override
    public boolean constraintNamesMightBeCaseSensitive() {
        // I didn't find this one in the MySQL 5.1 documentation under foreign
        // keys.  Maybe there is a separate section about constraint names.
        return true;
    }
    
    @Override
    public boolean duplicateConstraintNamesOk() {
        /* It would appear that UNIQUE are in one namespace, PRIMARY KEY
           in another, and FOREIGN KEY in a third.  But what happens
           on "alter table foo drop constraint foo_unique"?  Which
           one gets dropped? */
        return true;
    }

    @Override
    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    @Override
    public boolean innerJoinRequiresOn() {
        return false;
    }

    @Override
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        // This appears to be a case in which MySQL5 is
        // less standard (or at least, less similar to
        // the other databases in our tests) than MySQL4.
        return withParentheses;
    }

    @Override
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    @Override
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    @Override
    public boolean notBindsMoreTightlyThanIn() {
        // The default is the same as most databases: NOT has lower
        // precedence than IN.  The other (MySQL4) behavior is
        // available by setting the SQL mode HIGH_NOT_PRECEDENCE.
        return super.notBindsMoreTightlyThanIn();
    }

    @Override
    public boolean notRequiresBoolean() {
        return false;
    }

    @Override
    public boolean canHaveLimitWithoutOrderBy() {
        return true;
    }
    
    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return true;
    }
    
    @Override
    public boolean fromIsOptional() {
        return true;
    }
    
    @Override
    public boolean maySpecifyTableDotColumnToJdbc() {
        return true;
    }
    
    @Override
    public boolean schemasMissing() {
        // Not something missing in MySQL so much as something we haven't figured
        // out how to test.
        return true;
    }
    
    @Override
    public boolean canSumStrings(boolean rowsPresent) {
        return true;
    }
    
    @Override
    public boolean errorIfNotAggregateOrGrouped() {
        /* This is documented behavior.
           See the ONLY_FULL_GROUP_BY SQL mode to change it. */
        return false;
    }
    
    @Override
    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    @Override
    public boolean valuesClauseCanReferToColumn() {
        return true;
    }
    
    @Override
    public boolean haveInsertSetSyntax() {
        return true;
    }

    @Override
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }

    @Override
    public boolean whereCanReferToColumnAlias() {
        return false;
    }

    @Override
    public boolean canGetValueViaExpressionName() {
        return true;
    }
    
    @Override
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    @Override
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }

    @Override
    public void endTransaction(Connection connection) throws SQLException {
        // Neither setAutoCommit(true) nor commit() seems to suffice.
        // Is that really true about commit()?  That it only works if
        // you have made a change?
        connection.rollback();
    }

    @Override
    public String tableTypeForTransactions() {
        return " type=innodb";
    }
    
    @Override
    public String tableTypeForForeignKeys() {
        return " type=innodb";
    }
    
    @Override
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
    
    @Override
    public boolean haveCheckConstraints() {
        /* As of MySQL 5.1 CHECK is a no-op for all engines. */
        return false;
    }
    
    @Override
    public boolean onDeleteSetDefaultMissing(boolean tableCreateTime) {
        return true;
    }
    
    @Override
    public boolean haveDropTableFooIfExists() {
        return false;
    }
    
    @Override
    public boolean allowJdbcParameterAsDefault() {
        // I guess this fits along with "from foo?.tab"
        // and other looseness allowed with ?
        return true;
    }
    
    @Override
    public boolean notNullImpliesDefaults() {
        // An odd (though documented) quirk of MySQL:
        // declaring a field NOT NULL changes its
        // default value from NULL to some other
        // value (0, '', etc).
        return true;
    }
    
    @Override
    public boolean timestampDoesNotRespectNull() {
        /* Is the behavior documented?  Is there any way to get
           a null into a TIMESTAMP column?  What else is
           going on here?  */
        return true;
    }

    @Override
    public boolean haveOnUpdateValue() {
        // As far as I can tell from the MySQL documentation,
        // ON UPDATE only applies to ON UPDATE CURRENT_TIMESTAMP.
        return false;
    }
    
    @Override
    public boolean haveAutoUnderbarIncrement() {
        return true;
    }
    
    @Override
    public boolean haveSerial() {
        return true;
    }
    
    @Override
    public boolean autoIncrementIsRelativeToLastValue() {
        return true;
    }
    
    @Override
    public String identityType() {
        return "integer auto_increment primary key";
    }
    
    @Override
    public String autoIncrementType() {
        return identityType();
    }
    
    @Override
    public String lastIdentityValueQuery(String table, String column) {
        return "select last_insert_id()";
    }

    @Override
    public boolean datesAreOff() {
        return true;
    }
    
    @Override
    public boolean allowDateInTimestampColumn() {
        return true;
    }
    
    // seems to be false for 5.0.45 (MysqlDataTruncation), true for 5.0.27
//    public boolean allowTimestampInDateColumn() {
//        return true;
//    }

    @Override
    public boolean dataTypesAreEnforced() {
        return false;
    }
    
    @Override
    public boolean canGetBytesOnNumber() {
        return true;
    }
    
    @Override
    public boolean canMixStringAndInteger() {
        return true;
    }
    
    @Override
    public boolean canSetStringOnDecimalColumn() {
        return true;
    }

    @Override
    public boolean allowHexForInteger() {
        return true;
    }
    
    @Override
    public boolean addingColumnCountsAsAffectedRow() {
        return true;
    }
    
    @Override
    public boolean errorIfOrderByNotInSelectDistinct() {
        return false;
    }
    
    @Override
    public boolean createTableCanContainIndex() {
        return true;
    }
    
    @Override
    public boolean indexNamesArePerTable() {
        return true;
    }
    
    @Override
    public boolean canIndexPartOfColumn() {
        return true;
    }
    
    @Override
    public boolean haveAddColumnAfter() {
        return true;
    }
    
    @Override
    public String productName() {
        return "MySQL";
    }

    @Override
    public boolean callJavaMethodAsStoredProcedure() {
        return false;
    }

}
