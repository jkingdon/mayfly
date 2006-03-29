package net.sourceforge.mayfly.acceptance;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyDialect extends Dialect {

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

    public Connection openAdditionalConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:derby:test");
    }

    public void shutdown(Connection connection) throws Exception {
        connection.close();
        try {
            DriverManager.getConnection("jdbc:derby:test;shutdown=true");
        } catch (SQLException derbyThrowsThisToMeanItShutDown) {
        }
    }
    
    public boolean crossJoinCanHaveOn() {
        return true;
    }

    public boolean crossJoinRequiresOn() {
        return true;
    }
    
    public boolean onIsRestrictedToJoinsTables() {
        return false;
    }
    
    boolean onCanMentionOutsideTable() {
        return false;
    }
    
    public boolean uniqueColumnMayBeNullable() {
        return false;
    }
    
    public boolean canCreateSchemaAndTablesInSameStatement() {
        return false;
    }
    
    public boolean authorizationAllowedInCreateSchema() {
        return false;
    }
    
    public boolean isReservedWord(String word) {
        return "first".equals(word)
            || "last".equals(word);
    }
    
    public boolean quotedIdentifiersAreCaseSensitive() {
        return true;
    }
    
    public boolean canConcatenateStringAndInteger() {
        return false;
    }
    
    public boolean canGroupByExpression() {
        return false;
    }
    
    public boolean canGroupByColumnAlias() {
        return false;
    }

    public boolean canOrderByExpression() {
        return true;
    }

    public boolean nullSortsLower() {
        return false;
    }    

    public boolean haveLimit() {
        return false;
    }

}
