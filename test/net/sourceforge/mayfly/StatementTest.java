package net.sourceforge.mayfly;

import java.sql.*;

public class StatementTest extends SqlTestCase {

    public void testReturnValueFromExecuteUpdate() throws Exception {
        Statement statement = connection.createStatement();
        assertEquals(0, statement.executeUpdate("CREATE Table Foo (b integer)"));
        assertEquals(1, statement.executeUpdate("inSERT into foo (b) values (77)"));
        statement.close();
        
        PreparedStatement prepared = connection.prepareStatement("insert into foo (b) values (88)");
        assertEquals(1, prepared.executeUpdate());
        prepared.close();
    }
    
    public void testQuestionMarkInPreparedStatement() throws Exception {
        execute("create table Foo (B Integer, a integer)");

        PreparedStatement prepared = connection.prepareStatement("insert into foo (a, b) values (?, ?)");
        prepared.setInt(1, 70);
        prepared.setInt(2, 90);
        assertEquals(1, prepared.executeUpdate());
        prepared.close();
        
        assertResultSet(new String[] { "90, 70" }, query("select b, a from foo"));
    }
    
    // Strings, including something like setString(1, "don't")
    // order of ?'s (depth first, breadth first, what? Does short circuiting affect anything?)
    // one of the setInt calls is missing
    // setInt call with index <= 0 or index > size
    // ? works in select statements too

}
