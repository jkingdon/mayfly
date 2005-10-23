package net.sourceforge.mayfly;

import junit.framework.*;

import java.sql.*;

public class DataTypeTest extends TestCase {

    public void testTypes() throws Exception {
        Connection connection = MetaDataTest.openConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate("create table foo (a integer)");
        statement.close();
        connection.close();
    }

}
