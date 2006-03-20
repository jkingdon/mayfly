package net.sourceforge.mayfly.acceptance;

import junit.framework.Assert;

import net.sourceforge.mayfly.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class MayflyDialect extends Dialect {

    private Database database;

    public Connection openConnection() throws Exception {
        database = new Database();
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
    
    public boolean updateMissing() {
        return true;
    }

}
