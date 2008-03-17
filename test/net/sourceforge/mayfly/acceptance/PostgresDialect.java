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

    @Override
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

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql:test", "mayflytest", "mayflytest");
    }

    @Override
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
    
    @Override
    public boolean fromIsOptional() {
        return true;
    }
    
    @Override
    public boolean canHaveLimitWithoutOrderBy() {
        // The postgres manual warns that the results may not be
        // meaningful, but postgres doesn't throw an error.
        return true;
    }
    
    @Override
    public boolean isReservedWord(String word) {
        return "offset".equalsIgnoreCase(word);
    }
    
    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return true;
    }
    
    @Override
    public boolean whereCanReferToColumnAlias() {
        return false;
    }

    // True for postgres 8.0.7, false for postgres 8.1.4
//    public boolean canHaveHavingWithoutGroupBy() {
//        return true;
//    }
    
    // Seems to be false for postgres 8.1.4, true for 8.2.5
//    public boolean aggregateAsteriskIsForCountOnly() {
//        // I didn't really look into just what postgres
//        // does for this case.
//        return false;
//    }
    
    /*
     * As of Postgres 8.1.8, this is true.
     */
//    public boolean errorIfUpdateToAggregate(boolean rowsPresent) {
//        /* Some versions of Postgres apparently can crash - CVE-2006-5540 */
//
//        if (rowsPresent) {
//            // false for 8.1.4.  Probably true for some future version.
//            return false;
//        }
//        else {
//            /* This one is already true, I guess, although the message
//               is "ctid is NULL" which doesn't really make it clear to
//               me that Posgres is winning on purpose rather than by accident.
//             */
//            return true;
//        }
//    }
    
    @Override
    public boolean nullSortsLower() {
        return false;
    }
    
    @Override
    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return false;
    }
    
    @Override
    public boolean backslashInAStringIsAnEscape() {
        /*
         * "our long-term plan to transition to SQL-standard 
         * string literal rules, wherein backslash is 
         * not a special character."
         * http://www.postgresql.org/docs/techdocs.50
         */
        return true;
    }
    
    @Override
    public boolean trailingSpacesConsultedInComparisons() {
        return true;
    }

    @Override
    public boolean schemasMissing() {
        // Haven't really looked too much at what postgres has
        // for schemas.  "create schema authorization mayflytest"
        // seemed to get somewhere but "set schema" didn't work was
        // about as far as I got.
        return true;
    }
    
    @Override
    public boolean numberOfValuesMustMatchNumberOfColumns() {
        return false;
    }
    
    @Override
    public boolean canInsertNoValues() {
        /* The hibernate dialect makes it look like
           the postgres syntax is "insert into foo default values"
           which actually seems fairly sensible.  Verify this.
         */
        return false;
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
    public boolean haveTinyint() {
        return false;
    }
    
    @Override
    public boolean expressionsAreTypeLong() {
        return false;
    }
    
    @Override
    public String binaryTypeName() {
        return "bytea";
    }
    
    @Override
    public boolean blobTypeWorks() {
        /* The error I'm getting is: Bad value for type int: \001\003\377\220
           I guess this is just a postgres bug (why would the type be "int"
           when we declare it as bytea?).  This is postgres 8.1.8-1.fc6 as
           shipped in Fedora.  */
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
        return false;
    }
    
    @Override
    public boolean haveDropTableFooIfExists() {
        return false;
    }

    @Override
    public boolean haveDropTableIfExistsFoo() {
        return false;
    }
    
    @Override
    public boolean haveModifyColumn() {
        return false;
    }
    
    @Override
    public boolean canDropLastColumn() {
        return true;
    }
    
    @Override
    public boolean haveDropForeignKey() {
        return false;
    }
    
    @Override
    public boolean defaultValueCanBeExpression() {
        return true;
    }
    
    @Override
    public boolean allowDateInTimestampColumn() {
        return true;
    }
    
    @Override
    public boolean allowTimestampInDateColumn() {
        return true;
    }

    @Override
    public boolean haveSequencySerial() {
        return true;
    }
    
    @Override
    public String identityType() {
        return "serial primary key";
    }
    
    /**
     * According to discussion on postgres mailing lists, they plan on
     * adding sql200x syntax only when they can give it sql200x semantics.
     */
    @Override
    public boolean haveSql2003AutoIncrement() {
        return false;
    }
    
    @Override
    public boolean allowHexForBinary() {
        /* Postgres does have the x'00' syntax but it just seems to be
           for BIT VARYING(x) which doesn't seem to behave quite like
           BYTEA (or BLOB/BINARY in other databases).
         */
        return false;
    }
    
    @Override
    public String lastIdentityValueQuery(String table, String column) {
        return new StringBuffer().append("select currval('")
            .append(table)
            .append('_')
            .append(column)
            .append("_seq')")
            .toString();
    }
    
    @Override
    public boolean autoCommitMustBeOffToCallRollback() {
        return false;
    }
    
    @Override
    public boolean allowOrderByOnDelete() {
        return false;
    }
    
    @Override
    public boolean metaDataProblemWithUppercaseTableName() {
        return true;
    }
    
    @Override
    public String productName() {
        return "PostgreSQL";
    }
    
    @Override
    public boolean deleteAllRowsIsSmartAboutForeignKeys() {
        return true;
    }

    @Override
    public boolean callJavaMethodAsStoredProcedure() {
        return false;
    }

    @Override
    public boolean haveDropIndexOn() {
        return false;
    }

}
