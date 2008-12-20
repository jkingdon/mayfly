package net.sourceforge.mayfly.acceptance;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DerbyDialect extends Dialect {

    @Override
    public Connection openConnection() throws Exception {
        System.setProperty("derby.system.home", "derby");
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        File testDirectory = new File("derby", "test");
        
        if (!testDirectory.exists()) {
            // If you need to clean out all state for sure, just delete the derby/test
            // directory and all its contents
            return DriverManager.getConnection("jdbc:derby:test;create=true");
        }
        else {
            Connection connection = openAdditionalConnection();
            dropAllTables(connection);
            return connection;
        }
    }

    private void dropAllTables(Connection connection) throws Exception {
        // This is much faster than rm -rf derby/test and re-creating
        List<String> tables = listTables(connection);
        while (tables.size() > 0) {
            int startingSize = tables.size();
            Iterator<String> i = tables.iterator();
            while (i.hasNext()) {
                String table = i.next();
                boolean success = true;
                try {
                    SqlTestCase.execute("drop table \"" + table + "\"", connection);
                }
                catch (SQLException e) {
                    if (e.getSQLState().equals("X0Y25")) {
                        // There is a foreign key pointing to this table.
                        success = false;
                    }
                    else {
                        throw e;
                    }
                }
                if (success) {
                    i.remove();
                }
            }
            if (startingSize == tables.size()) {
                throw new Exception("Cannot delete tables " + join(tables, ", "));
            }
        }
    }

    private String join(List<String> tables, String separator) {
        StringBuilder result = new StringBuilder();
        Iterator<String> i = tables.iterator();
        if (i.hasNext()) {
            String first = i.next();
            result.append(first);
        }
        while (i.hasNext()) {
            String nonFirst = i.next();
            result.append(separator);
            result.append(nonFirst);
        }
        return result.toString();
    }

    private List<String> listTables(Connection connection) throws Exception {
        List<String> result = new ArrayList<String>();
        ResultSet tables =
            connection.getMetaData().getTables(null, "APP", "%", null);
        while (tables.next()) {
            result.add(tables.getString("TABLE_NAME"));
        }
        tables.close();
        return result;
    }

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:derby:test");
    }

    @Override
    public void shutdown(Connection connection) throws Exception {
        connection.close();
        try {
            DriverManager.getConnection("jdbc:derby:test;shutdown=true");
        } catch (SQLException shutdownException) {
            // These two indicate success, others failure (see Derby docs).
            if (!(shutdownException.getSQLState().equals("XJ015")
                || shutdownException.getSQLState().equals("08006")
                )) {
                throw shutdownException;
            }
        }
    }
    
    @Override
    public boolean crossJoinCanHaveOn() {
        return true;
    }

    @Override
    public boolean crossJoinRequiresOn() {
        return true;
    }
    
    @Override
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    @Override
    boolean onCanMentionOutsideTable() {
        return false;
    }
    
    /* False for Derby 10.1.2.1, true for 10.4.2.0 */
//    @Override
//    public boolean uniqueColumnMayBeNullable() {
//        return false;
//    }
    
    @Override
    public boolean canTurnNullableColumnIntoPrimaryKey() {
        return false;
    }
    
    @Override
    public boolean canSetObjectNull() {
        return false;
    }
    
    @Override
    public boolean schemasMissing() {
        /** Interacts poorly with the table deletion code in 
            {@link #openConnection()}. So disabled for now. */
        return true;
    }

    @Override
    public boolean canCreateSchemaAndTablesInSameStatement() {
        return false;
    }
    
    /* False for Derby 10.1.2.1, true for 10.4.2.0 */
//    @Override
//    public boolean authorizationAllowedInCreateSchema() {
//        return false;
//    }
    
    @Override
    public boolean isReservedWord(String word) {
        return "first".equalsIgnoreCase(word)
            || "last".equalsIgnoreCase(word)
            || "identity".equalsIgnoreCase(word)
        ;
    }
    
    @Override
    public boolean quotedIdentifiersAreCaseSensitive() {
        return true;
    }

    /* False for Derby 10.1.2.1, true for 10.4.2.0 */
//    @Override
//    public boolean haveSlashStarComments() {
//        return false;
//    }

    @Override
    public boolean canConcatenateStringAndInteger() {
        return false;
    }
    
    @Override
    public boolean caseExpressionPickyAboutTypes() {
        /* Not sure exactly what is going on here.  It might have
           to do with omitting ELSE from the case expression, but
           I'm not sure of that. */
        return true;
    }
    
    /* False for Derby 10.1.2.1, true for 10.4.2.0 */
    // Seems to be allowed as of Derby 10.2.1.6
//    @Override
//    public boolean canGroupByExpression() {
//        return false;
//    }
    
    @Override
    public boolean canGroupByColumnAlias() {
        return false;
    }
    
    @Override
    public boolean whereCanReferToColumnAlias() {
        return false;
    }

    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return true;
    }

    @Override
    public boolean nullSortsLower() {
        return false;
    }    

    @Override
    public boolean haveLimit() {
        return false;
    }
    
    @Override
    public boolean willWaitForWriterToCommit() {
        /* If we are in a situation where another
           connection has written data but not committed,
           Derby will just wait.  It will eventually time
           out if we wait long enough.
         */
        return true;
    }
    
    @Override
    public boolean autoCommitMustBeOffToCallRollback() {
        return false;
    }
    
    @Override
    public void endTransaction(Connection connection) throws SQLException {
        /* setAutoCommit(true) would also suffice.
           How about commit() (don't think I've tried that one)?
         */
        connection.rollback();
    }
    
    @Override
    public boolean haveTinyint() {
        return false;
    }
    
    @Override
    public boolean expressionsAreTypeLong() {
        /* I suppose the question being what is maxint + maxint?
           (not that forcing to long solves this, with maxint * maxint * maxint
           being the obvious counterexample). */
        return false;
    }

    @Override
    public boolean allowHexForBinary() {
        /* Derby has the x'00' syntax, but not for type BLOB.  I haven't
           checked what types it is allowed for. */
        return false;
    }
    
    @Override
    public boolean haveTextType() {
        return false;
    }

    @Override
    public boolean canInsertNoValues() {
        return false;
    }
    
    @Override
    public boolean canSetStringOnDecimalColumn() {
        return true;
    }

    /**
       @internal
       Seems strange to me that one would allow this one but not
       {@link Dialect#allowDateInTimestampColumn()}, as the latter
       isn't losing any precision.  But that seems to be what Derby does.
     */
    @Override
    public boolean allowTimestampInDateColumn() {
        return true;
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
    public boolean notNullRequiresDefault() {
        return true;
    }

    @Override
    public boolean haveDropColumn() {
        return false;
    }
    
    @Override
    public boolean nameForeignKeysWithIbfk() {
        return false;
    }

    @Override
    public boolean haveModifyColumn() {
        /* Derby 10.2.1.6 claims the ability to change the nullability
        of a column (or certain other changes).  For the nullability
        case, the syntax seems to be
        ALTER TABLE foo ALTER [COLUMN] a [NOT] NULL */
        return false;
    }
    
    @Override
    public boolean haveAlterTableRenameTo() {
        return false;
    }

    @Override
    public boolean haveSql2003AutoIncrement() {
        return true;
    }
    
    @Override
    public String identityType() {
        return "INTEGER GENERATED BY DEFAULT " +
            "AS IDENTITY(START WITH 1) PRIMARY KEY";
    }
    
    @Override
    public String lastIdentityValueQuery(String table, String column) {
        return "values identity_val_local()";
    }
    
    // Derby also has the foreign key actions:
    // ON UPDATE RESTRICT
    // ON DELETE RESTRICT
    // which appear to be the same as NO ACTION except how triggers are handled.
    // We don't test these currently.

    @Override
    public boolean onDeleteSetDefaultMissing(boolean tableCreateTime) {
        /* Derby doesn't claim to have ON DELETE SET DEFAULT.
           But it doesn't complain when you create the table,
           just when you try to delete. */
        return !tableCreateTime;
    }

    @Override
    public boolean onUpdateSetNullAndCascadeMissing() {
        return true;
    }
    
    @Override
    public boolean allowOrderByOnDelete() {
        return false;
    }

    @Override
    public boolean deleteAllRowsIsSmartAboutForeignKeys() {
        return true;
    }
    
    /* False for Derby 10.1.2.1, true for 10.4.2.0 */
//    @Override
//    public boolean errorIfOrderByNotInSelectDistinct() {
//        return false;
//    }

    @Override
    public boolean metaDataExpectsUppercase() {
        return true;
    }
    
    @Override
    public String productName() {
        return "Apache Derby";
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
