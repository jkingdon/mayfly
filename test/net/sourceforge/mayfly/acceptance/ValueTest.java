package net.sourceforge.mayfly.acceptance;

import java.sql.*;

public class ValueTest extends SqlTestCase {

    public void testDropNonexisting() throws Exception {
        expectExecuteFailure("DROP TABLE FOO", "no table FOO");
    }

    public void testInsertWithBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A integer)");
        expectExecuteFailure("INSERT INTO FOO (b) values (5)", "no column b");
    }
    
    public void testInsertIntoNonexistentTable() throws Exception {
        expectExecuteFailure("INSERT INTO FOO (b) values (5)", "no table FOO");
    }
    
    public void testNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (null)");
        assertResultSet(new String[] { }, query("select a from foo where a = 5"));

        {
            ResultSet results = query("select a from foo");
            assertTrue(results.next());
    
            assertEquals(0, results.getInt(1));
            assertTrue(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        checkWrongWayToLookForNull();

        {
            // Right way to look for null.
            ResultSet results = query("select a from foo where a is null");
            assertTrue("got null row", results.next());
    
            assertEquals(0, results.getInt(1));
            assertTrue(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select a from foo where a is not null");
            assertFalse(results.next());
            results.close();
        }

    }

    public void testNonNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (5)");
        
        {
            ResultSet results = query("select a from foo");
            assertTrue(results.next());

            assertEquals(5, results.getInt(1));
            assertFalse(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

        checkWrongWayToLookForNull();

        {
            ResultSet results = query("select a from foo where a is null");
            assertFalse(results.next());
            results.close();
        }

        {
            ResultSet results = query("select a from foo where a is not null");
            assertTrue(results.next());

            assertEquals(5, results.getInt(1));
            assertFalse(results.wasNull());
    
            assertFalse(results.next());
            results.close();
        }

    }
    
    public void testWasNullGetsClearedForNextColumn() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (null, 5)");
        ResultSet results = query("select a, b from foo");
        assertTrue(results.next());

        assertEquals(0, results.getInt("a"));
        assertTrue(results.wasNull());

        assertEquals(5, results.getInt("b"));
        assertFalse(results.wasNull());
        
        assertFalse(results.next());
    }

    public void testWasNullGetsClearedForNextRow() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (null)");
        execute("insert into foo (a) values (7)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());

        assertEquals(0, results.getInt("a"));
        assertTrue(results.wasNull());

        assertTrue(results.next());

        assertEquals(7, results.getInt("a"));
        assertFalse(results.wasNull());

        assertFalse(results.next());
    }

    private void checkWrongWayToLookForNull() throws SQLException {
        String wrongWayToLookForNull = "select a from foo where a = null";
        if (EXPECT_MAYFLY_BEHAVIOR) {
            expectQueryFailure(wrongWayToLookForNull, 
                "To check for null, use IS NULL or IS NOT NULL, not a null literal"
            );
        } else {
            // Hypersonic behavior.  I think SQL specifies that "a = null"
            // evaluates to null, which then means false, but is this
            // really useful or just a trap?  Until proven otherwise,
            // I'm going with "trap".
            ResultSet results = query(wrongWayToLookForNull);
            assertFalse(results.next());
            results.close();
        }
    }
    
    public void testAssertResultSetAndNull() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (null)");
        
        assertResultSet(
            new String[] {
                " 5 ",
                " null "
            },
            query("select a from foo")
        );
    }

    public void testEmptyStringAsNull() throws Exception {
        // TODO: empty string is treated as null by Oracle, but not by
        // Postgres.  I've seen Postgres examples like 'foo' || '' => 'foo'
        // but 'foo' || null => null.
        // So what should Mayfly do?
    }
    
    public void testInsertSomeColumns() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (b) values (5)");
        assertResultSet(
            new String[] { " null, 5 " },
            query("select a, b from foo")
        );
    }
    
    public void testInsertAllColumns() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo values (5, 7)");
        assertResultSet(
            new String[] { " 5, 7 " },
            query("select a, b from foo")
        );
    }
    
    public void testInsertBadColumnName() throws Exception {
        execute("create table foo (a integer)");
        // Do we want to know about all of them, or just the first?
        // Just the first might be better in terms of avoiding
        // information overload.
        expectExecuteFailure("insert into foo (b, c) values (5, 7)", "no column b");
    }
    
    public void testTooManyValues() throws Exception {
        execute("create table foo (a integer)");
        // Given a long list of column names, and a long list of values,
        // it might not be obvious which value/name is missing/surplus.
        // So printing out the table of both is an attempt to make this
        // easy to figure out.
        expectExecuteFailure("insert into foo (a) values (5, 7)",
            "Too many values.\n" +
            "Columns and values were:\n" +
            "a 5\n" +
            "(none) 7\n"
        );
    }
    
    public void testTooFewValues() throws Exception {
        execute("create table foo (a integer, b integer)");
        // Given a long list of column names, and a long list of values,
        // it might not be obvious which value/name is missing/surplus.
        // So printing out the table of both is an attempt to make this
        // easy to figure out.
        expectExecuteFailure("insert into foo (a, b) values (5)",
            "Too few values.\n" +
            "Columns and values were:\n" +
            "a 5\n" +
            "b (none)\n"
        );
    }
    
    public void testInsertAllColumnsChecksForNumberOfValues() throws Exception {
        execute("create table foo (a integer, b integer)");
        expectExecuteFailure("insert into foo values (5)",
            "Too few values.\n" +
            "Columns and values were:\n" +
            "a 5\n" +
            "b (none)\n"
        );
    }
    
    public void testDuplicateColumnName() throws Exception {
        execute("create table foo (Id integer)");
        expectExecuteFailure("insert into foo (Id, Id) values (5, 7)", "duplicate column Id");
    }
    
    public void testNegativeNumber() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table foo (x integer)");
        execute("insert into foo (x) values (-5)");
        execute("insert into foo (x) values (-3)");
        execute("insert into foo (x) values (7)");
        
        assertResultSet(new String[] { " -5 " }, query("select x from foo where x < -4"));
    }
    
    public void testReadInteger() throws Exception {
        execute("create table foo (x integer)");
        execute("insert into foo (x) values (5)");
        
        ResultSet results = query("select x from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("x"));
        assertEquals("5", results.getString("x"));
        if (mayflyMissing()) {
            assertEquals(5.0, results.getDouble("x"), 0.00001);
        }
        assertFalse(results.next());
    }
    
}