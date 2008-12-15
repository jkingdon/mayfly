package net.sourceforge.mayfly.acceptance;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDialect extends Dialect {

    @Override
    public Connection openConnection() throws Exception {
        System.setProperty("derby.system.home", "derby");
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        File testDirectory = new File("derby", "test");

        // This works, but Derby is extremely slow on create=true
        // So we'd like a better way...
        FileUtils.deleteDirectory(testDirectory);

        if (!testDirectory.exists()) {
            // If you need to clean out all state for sure, just delete the derby/test
            // directory and all its contents
            return DriverManager.getConnection("jdbc:derby:test;create=true");
        }
        else {
            return openAdditionalConnection();
        }
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
        } catch (SQLException derbyThrowsThisToMeanItShutDown) {
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
    
    @Override
    public boolean uniqueColumnMayBeNullable() {
        return false;
    }
    
    @Override
    public boolean canTurnNullableColumnIntoPrimaryKey() {
        return false;
    }
    
    @Override
    public boolean canSetObjectNull() {
        return false;
    }
    
    @Override
    public boolean canCreateSchemaAndTablesInSameStatement() {
        return false;
    }
    
    @Override
    public boolean authorizationAllowedInCreateSchema() {
        return false;
    }
    
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

    @Override
    public boolean haveSlashStarComments() {
        return false;
    }

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
    
    @Override
    public boolean canGroupByExpression() {
        // Seems to be allowed as of Derby 10.2.1.6
        return false;
    }
    
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
    
    @Override
    public boolean errorIfOrderByNotInSelectDistinct() {
        return false;
    }

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
