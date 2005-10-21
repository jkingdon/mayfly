package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public class JdbcTest extends TestCase {

    public void testPrepareNoParameters() throws Exception {
        Class.forName("net.sourceforge.mayfly.JdbcDriver"); // how are these usually named?
        // can we distinguish between multiple databases via the url?
        // if no, is there only one legal value?
        Connection connection = DriverManager.getConnection("jdbc:mayfly:");
        PreparedStatement createTable = connection.prepareStatement("CREATE TABLE FOO (a NUMBER)");
        assertEquals(0, createTable.executeUpdate());
        createTable.close();
        
        PreparedStatement select = connection.prepareStatement("SELECT A FROM FOO");
        ResultSet results = select.executeQuery();
        assertFalse(results.next());
        results.close();
        select.close();
        
        connection.close();
    }

}
