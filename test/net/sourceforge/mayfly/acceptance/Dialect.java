package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.util.StringBuilder;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Dialect {

    abstract public Connection openConnection() throws Exception;
    abstract public Connection openAdditionalConnection() throws Exception;

    public void assertTableCount(int expected) {
        // Could probably do this with JDBC metadata or database-specific tricks.
        // Not clear we should bother.
    }

    abstract public void shutdown(Connection connection) throws Exception;

    public void assertMessage(String expectedMessage, SQLException exception) {
        // To assert on this we'd need to keep lists of messages for many
        // databases in many versions.  That seems hard.
        // But we would like to see that databases fail for the same
        // reasons.  So we provide the ability to manually inspect
        // the messages side by side.

        if (SqlTestCase.SHOW_MESSAGES) {
            System.out.print("Mayfly message would be " + expectedMessage + "\n");
            System.out.print("Actual message was " + exception.getMessage() + "\n\n");
        }
        else if (SqlTestCase.SHOW_STACK_TRACES) {
            System.out.print("Mayfly message would be " + expectedMessage + "\n");
            System.out.print("Actual exception was:\n");
            printException(exception);
            System.out.print("\n\n");
        }
    }

    private void printException(SQLException exception) {
        exception.printStackTrace(System.out);
        System.out.print("SQL state was: " + exception.getSQLState() + "\n");
        System.out.print("Vendor code was: " + exception.getErrorCode() + "\n");
        if (exception.getNextException() != null) {
            // Is it really true that next exceptions are not related to
            // causes?  Or is that just an artifact of libgcj 4.0.2?
            printException(exception.getNextException());
        }
    }

    public boolean backslashInAStringIsAnEscape() {
        // For most SQL dialects (including SQL92 I believe), '\' is just a string
        // with one character in it.
        return false;
    }

    public boolean isReservedWord(String word) {
        // War on reserved words: they can make it quite a pain to
        // port SQL from one implementation to another.  Mayfly's
        // rule of thumb: Don't put big kluges in the parser to
        // avoid a reserved word, but make words non-reserved
        // where feasible.
        
        // A few specific cases:

        // LIMIT needs/wants to be a reserved word, but there is no
        // particular need for OFFSET to be (unless just for symmetry
        // with LIMIT or something).
        return false;
    }

    public boolean tableNamesMightBeCaseSensitive() {
        return false;
    }

    public boolean detectsAmbiguousColumns() {
        return true;
    }

    /** 
     * @internal
     * Should a test look for behavior in which Mayfly intentionally diverges
     * from what other databases do.
     * 
     * (In most case it makes more sense to have an individual test for a specific questions
     * like detectsAmbiguousColumns or whatever).  */
    public boolean expectMayflyBehavior() {
        return false;
    }

    /**
     * @internal
     * This is how we mark things where Mayfly doesn't yet implement the behavior
     * which is desired for Mayfly.  So for non-Mayfly databases, there is no 
     * "wish" to be marked.
     */
    boolean wishThisWereTrue() {
        return true;
    }
    
    public boolean crossJoinRequiresOn() {
        return false;
    }

    public boolean crossJoinCanHaveOn() {
        return false;
    }

    public boolean innerJoinRequiresOn() {
        return true;
    }
    
    public boolean rightHandArgumentToJoinCanBeJoin() {
        return true;
    }

    public boolean onIsRestrictedToJoinsTables() {
        return true;
    }

    public boolean considerTablesMentionedAfterJoin() {
        return false;
    }
    
    boolean onCanMentionOutsideTable() {
        return !onIsRestrictedToJoinsTables();
    }

    public boolean detectsSyntaxErrorsInPrepareStatement() {
        return true;
    }

    public boolean requiresAllParameters() {
        return true;
    }

    public boolean stringComparisonsAreCaseInsensitive() {
        return false;
    }

    public boolean notBindsMoreTightlyThanIn() {
        return false;
    }

    public boolean orderByCountsAsWhat() {
        return false;
    }

    public boolean haveLimit() {
        // Interestingly enough, the majority of my test databases
        // do have this, in a more or less compatible fashion.
        return true;
    }

    public boolean canHaveLimitWithoutOrderBy() {
        return false;
    }

    public boolean fromIsOptional() {
        return false;
    }

    public boolean verticalBarsMeanConcatenation() {
        return true;
    }

    public boolean maySpecifyTableDotColumnToJdbc() {
        return false;
    }

    public boolean schemasMissing() {
        return false;
    }

    public boolean canCreateSchemaAndTablesInSameStatement() {
        // Not sure how clean/useful/popular this is.
        return true;
    }
    
    public boolean authorizationAllowedInCreateSchema() {
        return true;
    }

    public boolean authorizationRequiredInCreateSchema() {
        return false;
    }

    public String createEmptySchemaCommand(String name) {
        StringBuilder sql = new StringBuilder();
        sql.append("create schema ");
        sql.append(name);
        if (authorizationRequiredInCreateSchema()) {
            sql.append(" authorization dba");
        }
        return sql.toString();
    }

    public boolean aggregateDistinctIsForCountOnly() {
        return false;
    }

    public boolean aggregateAsteriskIsForCountOnly() {
        return true;
    }

    public boolean errorIfNotAggregateOrGrouped() {
        return true;
    }

    public boolean errorIfNotAggregateOrGroupedWhenGroupByExpression() {
        return errorIfNotAggregateOrGrouped();
    }

    public boolean canGroupByExpression() {
        return true;
    }

    public boolean canGroupByColumnAlias() {
        return true;
    }

    public boolean canOrderByExpression() {
        return false;
    }

    public boolean canGetValueViaExpressionName() {
        return false;
    }

    public boolean allowCountDistinctStar() {
        return false;
    }

    public boolean canQuoteIdentifiers() {
        return true;
    }

    public boolean columnInHavingMustAlsoBeInSelect() {
        return true;
    }

    public boolean canHaveHavingWithoutGroupBy() {
        return false;
    }

    public boolean nullSortsLower() {
        // I don't know whether there are arguments pro or con on this.
        // Different databases seem to disagree, and several make it
        // configurable somehow.
        return true;
    }

    public boolean disallowColumnAndAggregateInExpression() {
        return true;
    }

    public boolean notRequiresBoolean() {
        return true;
    }

    public boolean numberOfValuesMustMatchNumberOfColumns() {
        return true;
    }

    public boolean canConcatenateStringAndInteger() {
        // Most databases seem to allow this.  I'm sure there
        // are larger issues/tradeoffs here (like "do what I
        // mean" versus avoiding surprises).
        return true;
    }

    public boolean disallowNullsInExpressions() {
        // If this is true, insist people say "null" rather than
        // "5 + null".  This may reduce confusion over the
        // "null propagates up" semantics (or might just delay
        // the time when people discover them :-)).
        return true;
    }

    public boolean canGetValueViaExpression() {
        return false;
    }

    protected boolean constraintCanHaveForwardReference() {
        return true;
    }

    public boolean uniqueColumnMayBeNullable() {
        return true;
    }
    
    public boolean allowMultipleNullsInUniqueColumn() {
        // False is analogous to the GROUP BY model in which all rows
        // with null go in a single group.  It isn't clear whether this
        // is the best way to treat null or not.
        return false;
    }

    public boolean allowUniqueAsPartOfColumnDeclaration() {
        return true;
    }

    public boolean haveUpdateDefault() {
        return true;
    }

    public boolean quotedIdentifiersAreCaseSensitive() {
        // I guess SQL92 says this should be true.
        // Perhaps a bit tricky to get this true, and still have
        // messages case-preserving in general.
        return false;
    }

    public boolean haveTransactions() {
        /**
         * @internal
         * Should we be testing {@link Connection#getTransactionIsolation()}
         * and {@link Connection#setTransactionIsolation(int)} ?
         * Need to do more testing, but the issue is whether most/any
         * databases will indicate what they can do, or change their
         * behavior based on this setting.  In a case like MySQL,
         * transaction capability is per-table, for one thing. 
         * 
         * Anyway, for now we set the isolation level appropriate for
         * each test, but in terms of figuring out what the database
         * can do, we have methods here.
         */
        
        return true;
    }

    public boolean willReadUncommitted() {
        return false;
    }

    public boolean willWaitForWriterToCommit() {
        return false;
    }

    public boolean haveTinyint() {
        return true;
    }

    public boolean haveTextType() {
        return true;
    }

    /** Is there a command DROP TABLE name IF EXISTS (with IF EXISTS after the name)? */
    public boolean haveDropTableFooIfExists() {
        return true;
    }

    /** Is there a command DROP TABLE IF EXISTS name (with IF EXISTS before the name)? */
    public boolean haveDropTableIfExistsFoo() {
        return true;
    }

    public boolean defaultValueCanBeExpression() {
        // Who does this besides postgres?

        // The postgres manual gives two examples: setting a date to now() (the
        // SQL92 way seems to be CURRENT_DATE/CURRENT_TIME/CURRENT_TIMESTAMP,
        // which is specifically allowed as a default value),
        
        // and auto-increment (which we
        // will support, but maybe not with all the many syntaxes out there).
        return false;
    }

    public boolean canUpdateToDefault() {
        return true;
    }

    public boolean allowJdbcParameterAsDefault() {
        return false;
    }

}
