package net.sourceforge.mayfly.acceptance;

import junit.framework.Assert;

import net.sourceforge.mayfly.Database;

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
    
    public void shutdown(Connection connection) {
    }
    
    public void assertMessage(String expectedMessage, SQLException exception) {
        Assert.assertEquals(expectedMessage, exception.getMessage());
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
        return word.equalsIgnoreCase("if");
    }
    
    public boolean canGroupByExpression() {
        return wishThisWereTrue();
    }
    
    public boolean canGroupByColumnAlias() {
        /** We don't implement column aliases yet.  {@see ResultTest#testAs()}. */
        return wishThisWereTrue();
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
    boolean wishThisWereTrue() {
        return false;
    }

    public boolean haveSql200xAutoIncrement() {
        /* 
         * Not comletely sure how to parse this if
         * GENERATED is non-reserved.
         */
        return wishThisWereTrue();
    }
    
    public boolean haveAutoUnderbarIncrement() {
        /* 
         * Not comletely sure how to parse this if
         * AUTO_INCREMENT is non-reserved.
         */
        return wishThisWereTrue();
    }
    
    public boolean haveIdentity() {
        return true;
    }

    public boolean haveSerial() {
        return true;
    }
    
    public boolean decimalScaleIsFromType() {
        return wishThisWereTrue();
    }

}
