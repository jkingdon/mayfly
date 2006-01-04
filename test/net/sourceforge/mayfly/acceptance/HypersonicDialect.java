package net.sourceforge.mayfly.acceptance;

import java.sql.*;

public class HypersonicDialect extends Dialect {

    public Connection openConnection() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        return DriverManager.getConnection("jdbc:hsqldb:mem:SqlTestCase");
    }

    public void shutdown(Connection connection) throws Exception {
        SqlTestCase.execute("SHUTDOWN", connection); // So next test gets a new database.
    }
    
    public boolean detectsAmbiguousColumns() {
        return false;
    }
    
    public boolean crossJoinRequiresOn() {
        // In hypersonic, CROSS JOIN seems to be a synonym for INNER JOIN
        return true;
    }
    
    public boolean crossJoinCanHaveOn() {
        return true;
    }
    
    public boolean rightHandArgumentToJoinCanBeJoin() {
        return false;
    }
    
    public boolean requiresAllParameters() {
        return false;
    }
    
    public boolean orderByCountsAsWhat() {
        return true;
    }

}
