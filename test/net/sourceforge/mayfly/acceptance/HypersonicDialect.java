package net.sourceforge.mayfly.acceptance;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class HypersonicDialect extends Dialect {

    @Override
    public Connection openConnection() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        return openAdditionalConnection();
    }

    @Override
    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:mem:SqlTestCase");
    }

    @Override
    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("SHUTDOWN", connection); // So next test gets a new database.
    }
    
    @Override
    public boolean detectsAmbiguousColumns() {
        return false;
    }
    
    @Override
    public boolean detectsAmbiguousColumnsInOrderBy() {
        return false;
    }
    
    @Override
    public boolean crossJoinRequiresOn() {
        // In hypersonic, CROSS JOIN seems to be a synonym for INNER JOIN
        return true;
    }
    
    @Override
    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    @Override
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    @Override
    public boolean allowDuplicateTableInQuery() {
        return true;
    }
    
    @Override
    public boolean allowDuplicateTableWithDifferingColumnNames() {
        return true;
    }
    
    @Override
    public boolean rightHandArgumentToJoinCanBeJoin(boolean withParentheses) {
        return false;
    }
    
    @Override
    public boolean authorizationRequiredInCreateSchema() {
        return true;
    }

    @Override
    public boolean requiresAllParameters() {
        return false;
    }
    
    @Override
    public boolean trailingSpacesConsultedInComparisons() {
        return true;
    }
    
    @Override
    public boolean complainAboutDubiousStoredProcedure() {
        return false;
    }

    @Override
    public boolean orderByCountsAsWhat() {
        return true;
    }
    
    @Override
    public boolean canOrderByExpression(boolean isAggregate) {
        return true;
    }
    
    @Override
    public boolean aggregateAsteriskIsForCountOnly() {
        // Hypersonic has a variety of behaviors, depending on whether there
        // are any rows, and which function.  None of them seem very useful.
        return false;
    }
    
    @Override
    public boolean allowCountDistinctStar() {
        // What count(distinct *) means I don't really know.
        return true;
    }
    
    @Override
    public boolean canSumStrings(boolean rowsPresent) {
        if (rowsPresent) {
            return super.canSumStrings(rowsPresent);
        }
        else {
            return true;
        }
    }
    
    @Override
    public Class typeFromAddingLongs() {
        /* Not such a bad idea.  I wonder when hypersonic promotes to
           BigDecimal and when it doesn't */
        return BigDecimal.class;
    }
    
    @Override
    public boolean errorIfNotAggregateOrGroupedWhenGroupByExpression(boolean rowsPresent) {
        return false;
    }
    
    @Override
    public boolean errorIfUpdateToAggregate(boolean rowsPresent) {
        return rowsPresent;
    }
    
    @Override
    public boolean errorIfAggregateInWhere() {
        return false;
    }
    
    @Override
    public boolean errorIfBadTableAndNoRows() {
        return false;
    }

    @Override
    public boolean disallowColumnAndAggregateInExpression() {
        return false;
    }
    
    @Override
    public boolean canHaveHavingWithoutGroupBy() {
        return true;
    }
    
    @Override
    public boolean havingCanReferToEnclosingRow() {
        return false;
    }
    
    @Override
    public boolean notRequiresBoolean() {
        return false;
    }
    
    @Override
    public boolean notRequiresBooleanForLike() {
        // reports "Not a condition", a perfectly good error.
        return true;
    }
    
    @Override
    public boolean canInsertNoValues() {
        return false;
    }

    @Override
    public boolean disallowNullsInExpressions() {
        return false;
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
    public boolean quotedIdentifiersAreCaseSensitive() {
        return true;
    }
    
    @Override
    public boolean isReservedWord(String word) {
        return word.equalsIgnoreCase("if");
    }

    @Override
    protected boolean constraintCanHaveForwardReference() {
        return false;
    }
    
    @Override
    public boolean allowUniqueAsPartOfColumnDeclaration() {
        return false;
    }
    
    @Override
    public boolean allowMultipleNullsInUniqueColumn() {
        return false;
    }
    
    @Override
    public boolean haveUpdateDefault() {
        return false;
    }
    
    @Override
    public boolean willReadUncommitted() {
        return true;
    }
    
    @Override
    public boolean canProvideRepeatableRead() {
        return false;
    }
    
    @Override
    public boolean autoCommitMustBeOffToCallRollback() {
        return false;
    }
    
    @Override
    public boolean haveForUpdate() {
        return false;
    }
    
    @Override
    public boolean foreignKeyCanReferToAnotherSchema() {
        return false;
    }
    
    @Override
    public boolean haveTextType() {
        // VARCHAR or LONGVARCHAR (which are the same as
        // each other, I think) are the hypersonic equivalent.
        return false;
    }
    
    @Override
    public String binaryTypeName() {
        return "BINARY";
    }
    
    @Override
    public boolean allowHexForBinary() {
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
    public boolean decimalScaleIsFromType() {
        return false;
    }
    
    @Override
    public boolean canUpdateToDefault() {
        return false;
    }

    @Override
    public boolean canDropLastColumn() {
        return true;
    }
    
    @Override
    public boolean canDropColumnWithForeignKey() {
        return false;
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
    public boolean haveIdentity() {
        /* Sometimes this seems to start with 0.  Haven't fully tried to
           figure out what is going on there.  */
        return true;
    }
    
    @Override
    public String identityType() {
        return "INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1)";
    }
    
    @Override
    public String autoIncrementType() {
        return identityType();
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
        /* The message is 
             Integrity constraint violation SYS_FK_46 table: FOO
           which makes me wonder whether the ORDER BY is simply
           being ignored.
           */
        return false;
    }
    
    @Override
    public boolean metaDataExpectsUppercase() {
        return true;
    }
    
    @Override
    public String productName() {
        return "HSQL Database Engine";
    }
    
    @Override
    public boolean canDropIndexGivingWrongTable() {
        return true;
    }

    @Override
    public boolean complainAboutStoredProcedureOverloadingOnArgumentTypeOrCount() {
        return false;
    }
    
    @Override
    public boolean haveConcatBuiltInWithOneArgument() {
        /* What is really going on here?  Does an unrecognized function 
           have a defined behavior or something? */
        return true;
    }
    
    @Override
    public boolean haveConcatBuiltInWithZeroArguments() {
        /* What is really going on here?  Does an unrecognized function 
        have a defined behavior or something? */
        return true;
    }
    
}
