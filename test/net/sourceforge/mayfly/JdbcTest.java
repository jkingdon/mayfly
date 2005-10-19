package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public class JdbcTest extends TestCase {

    public void testOverall() throws Exception {
        Class.forName("net.sourceforge.mayfly.JdbcDriver"); // how are these usually named?
        // can we distinguish between multiple databases via the url?
        // if no, is there only one legal value?
        Connection connection = DriverManager.getConnection("jdbc:mayfly:");
        connection.prepareStatement("CREATE TABLE FOO (X NUMBER)");
    }
}
