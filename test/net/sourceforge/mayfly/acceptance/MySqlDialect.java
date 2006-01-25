package net.sourceforge.mayfly.acceptance;

import java.sql.*;

/**
 * To make this work, install MySQL (the server), start it up on localhost,
 * and that might be all you need...
 */
public class MySqlDialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        Connection bootstrapConnection = DriverManager.getConnection("jdbc:mysql://localhost/");
        SqlTestCase.execute("CREATE DATABASE test", bootstrapConnection);
        bootstrapConnection.close();

        return DriverManager.getConnection("jdbc:mysql://localhost/test");
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE test", connection);
    }
    
    public boolean backslashMeansSomethingInAString() {
        // Ugh.  MySQL is incompatible with the rest of the world on this one.
        return true;
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

    public boolean considerTablesMentionedAfterJoin() {
        return true;
    }
    
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    public boolean notBindsMoreTightlyThanIn() {
        return true;
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
    
    public boolean verticalBarsMeanConcatenation() {
        return false;
    }
    
    public boolean maySpecifyTableDotColumnToJdbc() {
        return true;
    }
    
    public boolean schemasMissing() {
        // Not something missing in MySQL so much as something we haven't figured
        // out how to test.
        return true;
    }
    
    public boolean aggregateDistinctIsForCountOnly() {
        return true;
    }
    
    public boolean errorIfNotAggregateOrGrouped() {
        return false;
    }
    
    public boolean columnInHavingMustAlsoBeInSelect() {
        return false;
    }
    
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }

    public boolean canGetValueViaExpressionName() {
        return true;
    }
    
    public boolean canQuoteIdentifiers() {
        return false;
    }

}
