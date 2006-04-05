package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * To make this work:
 <pre>
  - Install postgres server and start it running
  - As a database superuser (perhaps "postgres"), run
    createuser --createdb --no-adduser -P mayflytest
      and supply a password of mayflytest.
    createdb mayflytest
  - Also edit pg_hba.conf to have a line such as:
    "host    all         all         127.0.0.1/32          trust"
    (which basically means that connections from localhost don't need to
     authenticate themselves; I'm not sure how this relates to passwords).
 </pre>
 */
public class PostgresDialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("org.postgresql.Driver");

        Connection bootstrapConnection = DriverManager.getConnection("jdbc:postgresql:", "mayflytest", "mayflytest");
        try {
            SqlTestCase.execute("DROP DATABASE test", bootstrapConnection);
        } catch (SQLException databaseDoesNotExist) {
        }
        SqlTestCase.execute("CREATE DATABASE test", bootstrapConnection);
        bootstrapConnection.close();

        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql:test", "mayflytest", "mayflytest");
    }

    public void shutdown(Connection connection) throws Exception {
        connection.close();

        Connection teardownConnection = DriverManager.getConnection("jdbc:postgresql:", "mayflytest", "mayflytest");
        // The connection.close() above is needed for this to work, but
        // doesn't complete immediately.  So we need the retries.
        executeWithRetries("DROP DATABASE test", teardownConnection);
        teardownConnection.close();
    }
    
    private void executeWithRetries(String sql, Connection connection) throws Exception {
        int tries = 0;
        while (true) {
            try {
                SqlTestCase.execute(sql, connection);
                break;
            } catch (SQLException e) {
                if (tries == 10) {
                    throw e;
                }
                ++tries;
                Thread.sleep(100);
            }
        }
    }
    
    public boolean fromIsOptional() {
        return true;
    }
    
    public boolean canHaveLimitWithoutOrderBy() {
        // The postgres manual warns that the results may not be
        // meaningful, but postgres doesn't throw an error.
        return true;
    }
    
    public boolean isReservedWord(String word) {
        return "offset".equalsIgnoreCase(word);
    }
    
    public boolean canOrderByExpression() {
        return true;
    }
    
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }
    
    public boolean aggregateAsteriskIsForCountOnly() {
        // I didn't really look into just what postgres
        // does for this case.
        return false;
    }
    
    public boolean nullSortsLower() {
        return false;
    }
    
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    public boolean backslashInAStringIsAnEscape() {
        return true;
    }
    
    public boolean schemasMissing() {
        // Haven't really looked too much at what postgres has
        // for schemas.  "create schema authorization mayflytest"
        // seemed to get somewhere but "set schema" didn't work was
        // about as far as I got.
        return true;
    }
    
    public boolean numberOfValuesMustMatchNumberOfColumns() {
        return false;
    }

    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    public boolean allowMultipleNullsInUniqueColumn() {
        return true;
    }
    
    public boolean haveTinyint() {
        return false;
    }
    
    public boolean haveDropTableFooIfExists() {
        return false;
    }

    public boolean haveDropTableIfExistsFoo() {
        return false;
    }

}
