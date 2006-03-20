package net.sourceforge.mayfly.acceptance;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Dialect {

    abstract public Connection openConnection() throws Exception;

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

    public boolean offsetIsReservedWord() {
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

    /** Should a test look for behavior in which Mayfly intentionally diverges
     * from what other databases do.
     * 
     * (In most case it makes more sense to have an individual test for a specific questions
     * like detectsAmbiguousColumns or whatever).  */
    public boolean expectMayflyBehavior() {
        return false;
    }

    public boolean updateMissing() {
        return false;
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

    public boolean considerTablesMentionedAfterJoin() {
        return false;
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

    public boolean onIsRestrictedToJoinsTables() {
        return true;
    }

    public boolean notRequiresBoolean() {
        return true;
    }

    public boolean numberOfValuesMustMatchNumberOfColumns() {
        return true;
    }

    public boolean disallowNullsInExpressions() {
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
        // This corresponds to the GROUP BY model in which all rows
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

}
