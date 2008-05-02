package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Dialect extends Dialect {

    @Override
    public Connection openConnection() throws Exception {
        Class.forName("org.h2.Driver");
        return openAdditionalConnection();
    }

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:SqlTestCase");
    }

    @Override
    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("SHUTDOWN", connection); // So next test gets a new database.
    }
    
    @Override
    public Class typeOfTinyint() {
        return Byte.class;
    }
    
    @Override
    public Class typeOfSmallint() {
        return Short.class;
    }
    
    @Override
    public boolean expressionsAreTypeLong() {
        return false;
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
    public boolean trailingSpacesConsultedInComparisons() {
        return true;
    }
    
    @Override
    public boolean fromIsOptional() {
        return true;
    }
    
    @Override
    public boolean quotedIdentifiersAreCaseSensitive() {
        return true;
    }

    @Override
    public boolean callJavaMethodAsStoredProcedure() {
        return false;
    }
    
    @Override
    public boolean haveCreateAlias() {
        return true;
    }
    
    @Override
    public boolean complainAboutStoredProcedureOverloadingOnArgumentTypeOrCount() {
        return false;
    }
    
    @Override
    public boolean canDropPrimaryKeyColumn() {
        return false;
    }
    
    @Override
    public boolean canDropColumnWithForeignKey() {
        return false;
    }
    
    @Override
    public boolean defaultValueCanBeExpression() {
        return true;
    }
    
    @Override
    public boolean haveDropIndexOn() {
        return false;
    }
    
    @Override
    public boolean allowTimestampInDateColumn() {
        return true;
    }
    
    @Override
    public boolean allowDuplicateTableWithDifferingColumnNames() {
        return true;
    }
    
    @Override
    public boolean innerJoinRequiresOn() {
        return false;
    }
    
    @Override
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    @Override
    public boolean considerTablesMentionedAfterJoin() {
        return true;
    }
    
    @Override
    public boolean canDropTargetOfForeignKey() {
        return true;
    }
    
    @Override
    public boolean foreignKeyMustReferToPrimaryKeyOrUnique() {
        return false;
    }
    
    @Override
    public boolean foreignKeyJustNeedsIndex() {
        return true;
    }
    
    @Override
    public boolean autoCommitMustBeOffToCallRollback() {
        return false;
    }
    
    @Override
    public boolean willWaitForWriterToCommit() {
        /* Although H2 does have a fairly short timeout, and we could look
           for the timeout exception, it seems better
           to not slow down the tests with timeouts. */
        return true;
    }
    
    @Override
    public boolean willWaitForWriterToCommitOnTwoRowInserts() {
        return true;
    }
    
    @Override
    public boolean authorizationAllowedInCreateSchema() {
        /* Actually, H2 allows the syntax, but it seems to 
           expect the user to exist. */
        return false;
    }
    
    @Override
    public boolean canCreateSchemaAndTablesInSameStatement() {
        return false;
    }
    
    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return true;
    }
    
    @Override
    public boolean detectsAmbiguousColumnsInOrderBy() {
        return false;
    }
    
    @Override
    public boolean metaDataExpectsUppercase() {
        return true;
    }
    
    @Override
    public String productName() {
        return "H2";
    }
    
    @Override
    public boolean disallowNullOnRightHandSideOfIn() {
        return false;
    }
    
    @Override
    public boolean notRequiresBoolean() {
        return false;
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
    public boolean maySpecifyTableDotColumnToJdbc() {
        return true;
    }
    
    @Override
    public boolean canHaveLimitWithoutOrderBy() {
        return true;
    }
    
    @Override
    public boolean haveDropForeignKey() {
        return false;
    }
    
    @Override
    public boolean haveModifyColumn() {
        return false;
    }
    
    @Override
    public boolean haveConcatBuiltIn() {
        return true;
    }
    
    @Override
    public boolean disallowNullsInExpressions() {
        return false;
    }
    
    @Override
    public boolean haveAutoUnderbarIncrement() {
        return true;
    }
    
    @Override
    public boolean haveAutoIncrementSerial() {
        return true;
    }
    
    @Override
    public boolean haveIdentity() {
        return true;
    }
    
    @Override
    public boolean haveSql2003AutoIncrement() {
        return true;
    }
    
    @Override
    public boolean sql2003RelativeToLastValue() {
        return true;
    }
    
    @Override
    public boolean allowOrderByOnDelete() {
        return false;
    }
    
    @Override
    public boolean canGroupByColumnAlias() {
        return false;
    }
    
    @Override
    public boolean groupByExpressionSimpleComparator() {
        return true;
    }
    
    @Override
    public boolean errorIfNotAggregateOrGrouped(boolean rowsPresent) {
        if (rowsPresent) {
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public boolean disallowHavingOnUnaggregated() {
        return false;
    }
    
    @Override
    public boolean allowExplicitAllInAggregate() {
        return false;
    }
    
    @Override
    public boolean canTurnNullableColumnIntoPrimaryKey() {
        return false;
    }
    
}
