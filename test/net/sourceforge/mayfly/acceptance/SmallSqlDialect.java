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
 * the various expectations are not yet set up for a clean test run.
 */
public class SmallSqlDialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("smallsql.database.SSDriver");
        Connection bootstrapConnection = 
            DriverManager.getConnection("jdbc:smallsql");
        SqlTestCase.execute("CREATE DATABASE mayflytest", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:smallsql:mayflytest");
    }

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
    
    public boolean detectsAmbiguousColumns() {
        return false;
    }
    
    public boolean allowDuplicateTableInQuery() {
        return true;
    }
    
    public boolean stringComparisonsAreCaseInsensitive() {
        return true;
    }
    
    public boolean notRequiresBoolean() {
        return false;
    }
    
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }
    
    public boolean whereCanReferToColumnAlias() {
        return false;
    }
    
    public boolean haveModifyColumn() {
        return false;
    }
    
    public boolean haveUpdateDefault() {
        return false;
    }
    
    public boolean haveLimit() {
        return false;
    }
    
    public boolean schemasMissing() {
        return true;
    }
    
}
