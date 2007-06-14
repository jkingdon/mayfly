package net.sourceforge.mayfly.acceptance;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.MayflySqlException;
import net.sourceforge.mayfly.dump.SqlDumper;

import java.sql.Connection;
import java.sql.SQLException;

public class MayflyDialect extends Dialect {

    private Database database;

    public Connection openConnection() throws Exception {
        database = new Database();
        return openAdditionalConnection();
    }

    public Connection openAdditionalConnection() throws SQLException {
        return database.openConnection();
    }

    public void assertTableCount(int expected) {
        Assert.assertEquals(expected, database.tables().size());
    }
    
    public void checkDump(String expected) {
        String dump = new SqlDumper().dump(database.dataStore());
        Assert.assertEquals(expected, dump);
    }
    
    public void shutdown(Connection connection) {
    }
    
    public void assertMessage(String expectedMessage, SQLException exception) {
        Assert.assertEquals(expectedMessage, exception.getMessage());
    }
    
    public void assertMessage(String expectedMessage, SQLException exception, 
        int expectedStartLine, int expectedStartColumn, int expectedEndLine, int expectedEndColumn) {
        super.assertMessage(expectedMessage, exception, expectedStartLine,
                expectedStartColumn, expectedEndLine, expectedEndColumn);
        MayflySqlException mayflyException = (MayflySqlException) exception;
        assertLocation(expectedStartLine, 
            mayflyException.startLineNumber(), mayflyException);
        assertLocation(expectedStartColumn, 
            mayflyException.startColumn(), mayflyException);
        assertLocation(expectedEndLine, 
            mayflyException.endLineNumber(), mayflyException);
        assertLocation(expectedEndColumn, 
            mayflyException.endColumn(), mayflyException);
    }

    private void assertLocation(int expected, int actual, 
        Exception exception) throws AssertionFailedError {
        /* The point is we want to see exception so we can proceed
           directly to debugging why it doesn't know about the
           location.  The cause seems like an expedient (if
           perhaps klugy) way to get that. */
        if (expected != actual) {
            throw (AssertionFailedError)new AssertionFailedError(
                "location wrong: expected " + expected + 
                " but got " + actual)
            .initCause(exception);
        }
    }
    
    public boolean expectMayflyBehavior() {
        return true;
    }
    
    public boolean isReservedWord(String word) {
        // Although some databases don't reserve IF, all that have IF EXISTS
        // in their DROP TABLE do.  The parsing of DROP TABLE seems pretty
        // problematic if IF is not reserved, and furthermore it is the
        // kind of word that people probably aren't in the habit of trying
        // to use in identifiers, just because it is reserved in so many
        // other languages.
        return word.equalsIgnoreCase("if")
        
        /**
         * Don't see how I can do the MySQL index syntax without INDEX keyword.
         */
            || word.equalsIgnoreCase("index")
        ;
    }
        
    public boolean willReadUncommitted() {
        /** It isn't clear that there is any way to just
           dip our toes into this water.  It seems like
           we need to maintain a log of changes to the database
           in the various connections, and then be able
           to apply those logs at commit time.
           {@link TransactionTest#testTwoWriters()}
         */
        return !wishThisWereTrue();
    }
    
    public boolean haveTransactions() {
        /**
         * {@link net.sourceforge.mayfly.MayflyConnection#rollback()}
         * is not implemented.
         */
        return wishThisWereTrue();
    }

    /**
     * @internal
     * Stuff to work on.
     */
    public boolean wishThisWereTrue() {
        return false;
    }

    public boolean haveSql2003AutoIncrement() {
        return true;
    }
    
    public boolean haveAutoUnderbarIncrement() {
        return true;
    }
    
    public boolean haveIdentity() {
        return true;
    }

    public boolean haveSerial() {
        return true;
    }
    
    public String autoIncrementType() {
        return "integer auto_increment";
    }
    
    public boolean haveOnUpdateValue() {
        return true;
    }

    public boolean trailingSpacesConsultedInComparisons() {
        // I guess we should go with the standard instead of hypersonic...
        // I don't know, are there any strong arguments one way or the other?
        return !wishThisWereTrue();
    }
    
    public boolean onUpdateSetNullAndCascadeMissing() {
        return !wishThisWereTrue();
    }
    
    public boolean foreignKeyJustNeedsIndex() {
        /* Here we adopt the most permissive rule of our
           tested databases.  That seems expedient in
           terms of porting applications which were written
           for MySQL, but not so good in terms of wanting
           to catch problems in the SQL with Mayfly,
           rather than just having them show up with
           the production database (say, Postgres).
           We might end up reversing this decision,
           or introducing the concept of options
           (say, Database#options() and JdbcDriver#options(String url)
           methods which return an Options object in which
           one can set options.  Or perhaps we only allow
           the options to be passed in when the Database is
           created).
         */
        return true;
    }
    
    /**
       Although I have my doubts about whether it is really
       desirable to be loose with types like this, at least
       for now, compatibility with other databases is
       winning out.
     */
    public boolean allowDateInTimestampColumn() {
        return true;
    }
    
    /**
       Although I have my doubts about whether it is really
       desirable to have someone give us a date&time, and throw
       out the time part of it, at least
       for now, compatibility with other databases is
       winning out.
     */
    public boolean allowTimestampInDateColumn() {
        return true;
    }
    
    public boolean allowOrderByOnDelete() {
        /* I don't know how I'd implement ORDER BY on DELETE.
           It also doesn't seem very elegant.
           Maybe for now just tell people to work around it? */
        return false;
    }
    
    public boolean canSumStrings(boolean rowsPresent) {
        if (rowsPresent) {
            return super.canSumStrings(rowsPresent);
        }
        else {
            /* Interesting.  We don't carry the type forward to where we
               actually compute the sum.  But maybe it would work fine
               to have a checking phase ahead of time.  */
            return true;
        }
    }
    
    public boolean createTableCanContainIndex() {
        // MySQL compatibility.
        return true;
    }
    
    public boolean canIndexPartOfColumn() {
        // MySQL compatibility.
        return true;
    }
    
    public boolean duplicateIndexNamesOk() {
        return !wishThisWereTrue();
    }
    
    public boolean haveAddColumnAfter() {
        return true;
    }

    public boolean haveInsertSetSyntax() {
        /* Only supported by MySQL as far as I know.  But it really
           is a better syntax, seems like... */
        return true;
    }
    
    public Class typeOfInteger() {
        /* Is it important to be compatible with other databases (which
           have Integer.class here)?  There's also a potential speed/space
           issue, but I'm guessing we're a while before the rest of Mayfly
           is efficient enough for that to matter.
         */
        return Long.class;
    }
    
    public String productName() {
        return "Mayfly";
    }
    
}
