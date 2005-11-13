package net.sourceforge.mayfly;

import java.sql.*;

public class ValueTest extends SqlTestCase {

    public void testDropNonexisting() throws Exception {
        try {
            execute("DROP TABLE FOO");
            fail();
        } catch (SQLException expected) {
            assertMessage("no such table FOO", expected);
        }
    }
    
    public void testInsertWithBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A integer)");
        try {
            execute("INSERT INTO FOO (b) values (5)");
            fail();
        }
        catch (SQLException e) {
            assertMessage("no column b", e);
        }
    }
    
    public void testInsertIntoNonexistentTable() throws Exception {
        try {
            execute("INSERT INTO FOO (b) values (5)");
            fail();
        }
        catch (SQLException e) {
            assertMessage("no such table FOO", e);
        }
    }
    
    public void testNull() throws Exception {
        if (CONNECT_TO_MAYFLY) {
            // little/none of this is implemented yet
            return;
        }

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

        {
            // Wrong way to look for null.  Should this be an error?
            // (apparently SQL specifies that "a = null" evaluates to null, but
            // we need not implement this unless it is actually useful.  Is it?)
            ResultSet results = query("select a from foo where a = null");
            assertFalse(results.next());
            results.close();
        }

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
        if (CONNECT_TO_MAYFLY) {
            // little/none of this is implemented yet
            return;
        }

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

        {
            // Wrong way to look for null.
            ResultSet results = query("select a from foo where a = null");
            assertFalse(results.next());
            results.close();
        }
        
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
    
    public void testSelectExpression() throws Exception {
        if (CONNECT_TO_MAYFLY) {
            /** This turns out to be hard.  The fact that the {@link MyResultSet} takes
             * a {@link net.sourceforge.mayfly.ldbc.Columns}, which is a collection of
             * {@link net.sourceforge.mayfly.ldbc.what.Column} (rather than
             * {@link net.sourceforge.mayfly.ldbc.what.WhatElement} or some such), would
             * need to be changed to make this work.
             */ 
            return;
        }

        execute("create table foo (dummy integer)");
        execute("insert into foo(dummy) values(5)");
        assertResultSet(new String[] {"5"}, query("select 5 from foo"));
    }

}
