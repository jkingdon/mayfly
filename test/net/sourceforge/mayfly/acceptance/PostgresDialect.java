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
    
    public boolean whereCanReferToColumnAlias() {
        return false;
    }

    // True for postgres 8.0.7, false for postgres 8.1.4
//    public boolean canHaveHavingWithoutGroupBy() {
//        return true;
//    }
    
    public boolean aggregateAsteriskIsForCountOnly() {
        // I didn't really look into just what postgres
        // does for this case.
        return false;
    }
    
    public boolean errorIfUpdateToAggregate(boolean rowsPresent) {
        /* Some versions of Postgres apparently can crash - CVE-2006-5540 */

        if (rowsPresent) {
            // false for 8.1.4.  Probably true for some future version.
            return false;
        }
        else {
            /* This one is already true, I guess (although the message
               is "ctid is NULL" which doesn't really make it clear to
               me that Posgres is winning on purpose rather than by accident.
             */
            return true;
        }
    }
    
    public boolean nullSortsLower() {
        return false;
    }
    
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    public boolean backslashInAStringIsAnEscape() {
        /*
         * "our long-term plan to transition to SQL-standard 
         * string literal rules, wherein backslash is 
         * not a special character."
         * http://www.postgresql.org/docs/techdocs.50
         */
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
    
    public boolean canInsertNoValues() {
        /* The hibernate dialect makes it look like
           the postgres syntax is "insert into foo default values"
           which actually seems fairly sensible.  Verify this.
         */
        return false;
    }

    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }

    public boolean haveTinyint() {
        return false;
    }
    
    public String binaryTypeName() {
        return "bytea";
    }
    
    public boolean canGetBytesOnNumber() {
        return true;
    }
    
    public boolean canCompareStringColumnToIntegerLiteral() {
        return true;
    }
    
    public boolean haveDropTableFooIfExists() {
        return false;
    }

    public boolean haveDropTableIfExistsFoo() {
        return false;
    }
    
    public boolean haveModifyColumn() {
        return false;
    }
    
    public boolean canDropLastColumn() {
        return true;
    }
    
    public boolean haveDropForeignKey() {
        return false;
    }
    
    public boolean defaultValueCanBeExpression() {
        return true;
    }
    
    public boolean allowDateInTimestampColumn() {
        return true;
    }
    
    public boolean allowTimestampInDateColumn() {
        return true;
    }

    public boolean haveSerial() {
        return true;
    }
    
    public String identityType() {
        return "serial not null";
    }
    
    public boolean allowHexForBinary() {
        /* Postgres does have the x'00' syntax but it just seems to be
           for BIT VARYING(x) which doesn't seem to behave quite like
           BYTEA (or BLOB/BINARY in other databases).
         */
        return false;
    }
    
    public String lastIdentityValueQuery(String table, String column) {
        return new StringBuffer().append("select currval('")
            .append(table)
            .append('_')
            .append(column)
            .append("_seq')")
            .toString();
    }
    
    public boolean autoCommitMustBeOffToCallRollback() {
        return false;
    }
    
    public boolean allowOrderByOnDelete() {
        return false;
    }
    
    public boolean deleteAllRowsIsSmartAboutForeignKeys() {
        return true;
    }

}
