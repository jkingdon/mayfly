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
    
    public void testInsert() throws Exception {
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
