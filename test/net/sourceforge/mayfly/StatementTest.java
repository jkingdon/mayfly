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
    
    public void testSyntaxErrorDetectedEarly() throws Exception {
        try {
            connection.prepareStatement("insert into some place or another");
            fail();
        } catch (SQLException e) {
            assertMessage("unexpected token: place", e);
        }
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
    
    public void testStringJdbcParameter() throws Exception {
        execute("create table foo (s VARCHAR(80))");
        PreparedStatement prepared = connection.prepareStatement("insert into foo (s) values (?)");
        prepared.setString(1, "can't");
        assertEquals(1, prepared.executeUpdate());
        prepared.close();
        
        ResultSet results = query("select s from foo");
        assertTrue(results.next());
        assertEquals("can't", results.getString(1));
        assertFalse(results.next());
        results.close();
    }
    
    public void testBadSetIntCalls() throws Exception {
        execute("create table Foo (B Integer, a integer)");

        PreparedStatement prepared = connection.prepareStatement("insert into foo (a, b) values (?, ?)");
        try {
            prepared.setInt(0, 70);
            fail();
        } catch (SQLException e) {
            assertMessage("Parameter index 0 is out of bounds", e);
        }

        try {
            prepared.setInt(3, 70);
            fail();
        } catch (SQLException e) {
            assertMessage("Parameter index 3 is out of bounds", e);
        }
        prepared.close();
    }
    
    public void xtestSetToNull() throws Exception {
        execute("create table Foo (a varchar(80))");

        PreparedStatement prepared = connection.prepareStatement("insert into foo (a) values (?)");
        try {
            prepared.setString(1, null); // Or maybe this should mean the same as setNull?
            fail();
        } catch (SQLException e) {
            assertMessage("no null", e);
        }
        prepared.close();
    }
    
    public void testMissingSetCall() throws Exception {
        execute("create table Foo (a Integer, b integer)");

        PreparedStatement prepared = connection.prepareStatement("insert into foo (a, b) values (?, ?)");
        prepared.setInt(2, 90);
        if (EXPECT_MAYFLY_BEHAVIOR) {
            try {
                prepared.executeUpdate();
                fail();
            } catch (SQLException e) {
                assertMessage("Parameter 1 missing", e);
            }
            prepared.close();
        } else {
            // Hypersonic behavior.  Defaulting to null seems too forgiving, especially given
            // the way that JDBC, used straightforwardly, tends to turn null into 0.
            assertEquals(1, prepared.executeUpdate());
            prepared.close();
            
            assertResultSet(new String[] { " null , 90 " }, query("select a, b from foo"));
        }
    }
    
    public void testSelect() throws Exception {
        if (!MAYFLY_MISSING) {
            /** Still haven't implemented {@link net.sourceforge.mayfly.ldbc.Select#substitute(Collection)} */
            return;
        }

        execute("create table foo (a integer, b integer, c integer)");
        execute("insert into foo (a, b, c) values (4, 5, 6)");
        execute("insert into foo (a, b, c) values (7, 8, 9)");
        PreparedStatement prepared = connection.prepareStatement(
            "select a from foo where (? = c and b = ?) or a = ?");
        prepared.setInt(1, 9);
        prepared.setInt(2, 8);
        prepared.setInt(3, 3);
        ResultSet results = prepared.executeQuery();
        assertResultSet(new String[] { " 7 " }, results);
        prepared.close();
    }
    
}
