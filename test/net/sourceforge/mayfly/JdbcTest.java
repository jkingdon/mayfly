package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public class JdbcTest extends TestCase {

    public void testPrepareNoParameters() throws Exception {
        Class.forName("net.sourceforge.mayfly.JdbcDriver");
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
    
    public void testBadJdbcUrl() throws Exception {
        Class.forName("net.sourceforge.mayfly.JdbcDriver");
        try {
            DriverManager.getConnection("jdbc:mayfly:x");
            fail();
        } catch (SQLException expected) {
            assertEquals("Mayfly only allows jdbc:mayfly: for the JDBC URL", expected.getMessage());
        }
    }
    
    public void testReturnValueFromExecuteUpdate() throws Exception {
        Connection connection = new Database().openConnection();
        Statement statement = connection.createStatement();
        assertEquals(0, statement.executeUpdate("CREATE Table Foo (b number)"));
        assertEquals(1, statement.executeUpdate("inSERT into foo (b) values (77)"));
        statement.close();
        
        PreparedStatement prepared = connection.prepareStatement("insert into foo (b) values (88)");
        assertEquals(1, prepared.executeUpdate());
        prepared.close();

        connection.close();
    }

}
