package net.sourceforge.mayfly.acceptance;

import java.sql.*;

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
    }

    public boolean backslashMeansSomethingInAString() {
        // For most SQL dialects (including SQL92 I believe), '\' is just a string
        // with one character in it.
        return false;
    }

    public boolean tableNamesMightBeCaseSensitive() {
        return false;
    }

    public boolean detectsAmbiguousColumns() {
        return true;
    }

    /** Should a test look for behavior in which Mayfly intentionally diverges
     * from what hypersonic does?
     * 
     * (This doesn't make as much sense as the individual tests for specific questions
     * like detectsAmbiguousColumns or whatever).  */
    public boolean expectMayflyBehavior() {
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

}
