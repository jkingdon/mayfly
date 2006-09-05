package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is for MySQL 5.x.  For 4.x, see {@link net.sourceforge.mayfly.acceptance.MySql4Dialect}.
 * 
 * To make this work, install MySQL (the server), start it up on localhost,
 * and that might be all you need...
 */
public class MySqlDialect extends Dialect {
    
    // For the moment, we keep the default SQL MODE setting.
    // We probably want SET sql_mode = 'ANSI'

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection bootstrapConnection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "");
        SqlTestCase.execute("CREATE DATABASE mayflytest", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost/mayflytest", "root", "");
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE mayflytest", connection);
        connection.close();
    }
    
    public boolean backslashInAStringIsAnEscape() {
        return true;
    }
    
    public boolean canQuoteIdentifiers() {
        return false;
    }
    
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if");
    }

    public boolean verticalBarsMeanConcatenation() {
        return false;
    }
    
    public boolean tableNamesMightBeCaseSensitive() {
        // Whether table names are case sensitive in MySQL depends on whether
        // file names are.
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
    
    public boolean canSumStrings() {
        return true;
    }
    
    public boolean errorIfNotAggregateOrGrouped() {
        return false;
    }
    
    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
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

    public boolean allowMultipleNullsInUniqueColumn() {
        return true;
    }
    
    public boolean haveTransactions() {
        // If we could make sure we were using InnoDB, this
        // perhaps could be true.  At least for now, don't
        // worry about trying to make sure we have & use InnoDB.
        return false;
    }

    public String databaseTypeForForeignKeys() {
        return " type=innodb";
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

    public boolean datesAreOff() {
        return true;
    }

}
