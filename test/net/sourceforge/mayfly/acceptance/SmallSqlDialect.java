package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SmallSQL is a pure Java, file-based database for which you just need
 * to drop in a jar (it is like Derby in those ways).
 * 
 * See http://www.smallsql.de/ or http://sourceforge.net/projects/smallsql
 * 
 * This Dialect subclass is able to connect and pass some tests, but
 * many tests are still failing.  Given the large number of failures,
 * I'm not sure whether we want to add a whole bunch of methods to
 * Dialect until SmallSQL is a little closer to a complete SQL
 * implementation (where "complete" means those features tested
 * in the mayfly acceptance tests....).
 */
public class SmallSqlDialect extends Dialect {

    @Override
    public Connection openConnection() throws Exception {
        Class.forName("smallsql.database.SSDriver");
        Connection bootstrapConnection = 
            DriverManager.getConnection("jdbc:smallsql");
        SqlTestCase.execute("DROP DATABASE mayflytest", bootstrapConnection);
        SqlTestCase.execute("CREATE DATABASE mayflytest", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:smallsql:mayflytest");
    }

    @Override
    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("DROP DATABASE mayflytest", connection);
        
        // Not sure how this is supposed to work.  However, it isn't
        // clear whether leaking connections is a problem for this driver.
/*        try {
            connection.close();
        }
        catch (SQLException e) {
            if (!"[SmallSQL]Connection was close".equals(e.getMessage())) {
                // We're called from teardown, so might be better not to throw.
                e.printStackTrace();
            }
        }
*/    }
    
    @Override
    public boolean detectsAmbiguousColumns() {
        return false;
    }
    
    @Override
    public boolean allowDuplicateTableInQuery() {
        return true;
    }
    
    @Override
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    @Override
    public boolean notRequiresBoolean() {
        return false;
    }
    
    @Override
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }
    
    @Override
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    @Override
    public boolean whereCanReferToColumnAlias() {
        return false;
    }
    
    @Override
    public boolean haveModifyColumn() {
        return false;
    }
    
    @Override
    public boolean haveUpdateDefault() {
        return false;
    }
    
    @Override
    public boolean haveLimit() {
        return false;
    }
    
    @Override
    public boolean schemasMissing() {
        return true;
    }
    
}
