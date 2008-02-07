package net.sourceforge.mayfly.acceptance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class StatementTest extends SqlTestCase {

    public void testReturnValueFromExecuteUpdate() throws Exception {
        Statement statement = connection.createStatement();
        assertEquals(0, statement.executeUpdate("CREATE Table foo (b integer)"));
        assertEquals(1, statement.executeUpdate("inSERT into foo (b) values (77)"));
        statement.close();
        
        PreparedStatement prepared = connection.prepareStatement("insert into foo (b) values (88)");
        assertEquals(1, prepared.executeUpdate());
        prepared.close();
    }
    
    public void testSyntaxErrorDetectedEarly() throws Exception {
        String sql = "insert into some place or another";
        if (dialect.detectsSyntaxErrorsInPrepareStatement()) {
            try {
                connection.prepareStatement(sql);
                fail();
            } catch (SQLException e) {
                assertMessage("expected VALUES or SELECT but got place", e);
            }
        }
        else {
            PreparedStatement prepared = connection.prepareStatement(sql);

            try {
                prepared.executeUpdate();
                fail();
            }
            catch (SQLException e) {
                assertMessage("expected VALUES but got place", e);
            }
            prepared.close();
        }
    }
    
    public void testQuestionMarkInPreparedStatement() throws Exception {
        execute("create table foo (B Integer, a integer)");

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
    
    public void testStringInSelect() throws Exception {
        execute("create table foo (s VARCHAR(80))");
        execute("insert into foo (s) values ('can''t')");
        
        PreparedStatement prepared = connection.prepareStatement("select s from foo where s = ?");
        prepared.setString(1, "can't");
        ResultSet results = prepared.executeQuery();

        assertTrue(results.next());
        assertEquals("can't", results.getString(1));
        assertFalse(results.next());
        results.close();

        prepared.close();
    }
    
    public void testParameterInNonPreparedStatement() throws Exception {
        execute("create table foo (x integer)");
        expectExecuteFailure("insert into foo(x) values (?)", "Attempt to specify '?' outside a prepared statement");
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
    
    public void testSetToNull() throws Exception {
        execute("create table foo (a varchar(80))");

        PreparedStatement prepared = connection.prepareStatement("insert into foo (a) values (?)");
        // That passing null should mean the same as setNull sems to be the consensus
        // of databases tested.
        prepared.setString(1, null);
        prepared.executeUpdate();
        prepared.close();
        
        assertResultSet(new String[] { " null " }, query("select a from foo"));
    }
    
    public void testSetNull() throws Exception {
        execute("create table foo (x integer)");
        
        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (x) values (?)");
        prepared.setNull(1, Types.INTEGER);
        prepared.executeUpdate();
        prepared.close();
        
        assertResultSet(new String[] { " null " }, query("select x from foo"));
    }
    
    public void testSetObjectNull() throws Exception {
        execute("create table foo (x integer)");
        
        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (x) values (?)");
        if (dialect.canSetObjectNull()) {
            prepared.setObject(1, null);
            prepared.executeUpdate();
            
            prepared.close();
            
            assertResultSet(
                new String[] { " null " }, 
                query("select x from foo"));
        }
        else {
            try {
                prepared.setObject(1, null);
                fail();
            }
            catch (SQLException expected) {
                assertMessage("expected data type integer but got null",
                    expected);
            }
        }
    }
    
    public void testSetObjectNullWithType() throws Exception {
        execute("create table foo (x integer)");
        
        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (x) values (?)");
        prepared.setObject(1, null, Types.INTEGER);
        prepared.executeUpdate();
        
        prepared.close();
        
        assertResultSet(
            new String[] { " null " }, 
            query("select x from foo"));
    }
    
    public void testSetObjectInteger() throws Exception {
        execute("create table foo (x integer)");
        
        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (x) values (?)");
        
        prepared.setObject(1, new Integer(55));
        prepared.executeUpdate();

        assertResultSet(
            new String[] { " 55 " }, 
            query("select x from foo"));
    }
    
    public void testSetObjectMismatchedTypes() throws Exception {
        execute("create table foo (x integer)");
        
        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (x) values (?)");

        if (dialect.canSetStringOnDecimalColumn()) {
            prepared.setObject(1, "66");
            prepared.executeUpdate();

            assertResultSet(
                new String[] { " 66 " }, 
                query("select x from foo"));
        }
        else {
            try {
                prepared.setObject(1, "66");
                prepared.executeUpdate();
                fail();
            }
            catch (SQLException e) {
                assertMessage(
                    "attempt to store string '66' into integer column x", e);
            }
        }
    }

    public void testMissingSetCall() throws Exception {
        execute("create table foo (a Integer, b integer)");

        PreparedStatement prepared = connection.prepareStatement(
            "insert into foo (a, b) values (?, ?)");
        prepared.setInt(2, 90);
        if (dialect.requiresAllParameters()) {
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
