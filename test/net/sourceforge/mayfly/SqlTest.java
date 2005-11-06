package net.sourceforge.mayfly;

import java.sql.*;
import java.util.*;

public class SqlTest extends SqlTestCase {
    
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
    
    public void testSelectEmpty() throws Exception {
        execute("CREATE TABLE FOO (a INTEGER)");
        ResultSet results = query("select a from foo");
        assertFalse(results.next());
    }

    public void testSelectFromBadTable() throws Exception {
        expectQueryFailure("select a from foo", "no such table foo");
    }

    public void testBadColumnName() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        expectQueryFailure("select b from foo", "no column b");
    }

    public void testSelect() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertFalse(results.next());
    }
    
    public void testAskResultSetForUnqueriedColumn() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        try {
            results.getInt("b");
            fail();
        } catch (SQLException e) {
            assertMessage("no column b", e);
        }
    }
    
    public void testTryToGetResultsBeforeCallingNext() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertMessage("no current result row", e);
        }
    }
    
    public void testTryToGetResultsAfterNextReturnsFalse() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        assertFalse(results.next());
        try {
            results.getInt("a");
            fail();
        } catch (SQLException e) {
            assertMessage("already read last result row", e);
        }
    }
    
    public void testTwoRows() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        execute("INSERT INTO FOO (a) values (7)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());
        int firstResult = results.getInt("a");
        assertTrue(results.next());
        int secondResult = results.getInt("a");
        assertFalse(results.next());
        
        Set expected = new HashSet(Arrays.asList(new Integer[] {new Integer(5), new Integer(7)}));
        Set actual = new HashSet();
        actual.add(new Integer(firstResult));
        actual.add(new Integer(secondResult));
        assertEquals(expected, actual);
    }
    
    public void testMultipleColumns() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER, b INTEGER)");
        execute("INSERT INTO FOO (a, B) values (5, 25)");
        ResultSet results = query("select A, b from foo");
        assertTrue(results.next());
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("B"));
        assertFalse(results.next());
    }
    
    public void testColumnNumbers() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("INSERT INTO FOO (A) values (5)");
        ResultSet results = query("select a from foo");
        assertTrue(results.next());

        try {
            results.getInt(0);
            fail();
        } catch (SQLException e) {
            assertMessage("no column 0", e);
        }

        assertEquals(5, results.getInt(1));

        try {
            results.getInt(2);
            fail();
        } catch (SQLException e) {
            assertMessage("no column 2", e);
        }

        assertFalse(results.next());
    }
    
    public void testWhere() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (4, 16)");
        execute("insert into foo (a, b) values (5, 25)");
        ResultSet results = query("select a, b from foo where b = 25");
        assertTrue(results.next());
        
        assertEquals(5, results.getInt("a"));
        assertEquals(25, results.getInt("b"));
        
        assertFalse(results.next());
    }
    
    public void testWhereIsCaseSensitive() throws Exception {
        execute("create table foo (a varchar)");
        execute("insert into foo (a) values ('Foo')");
        ResultSet wrongCase = query("select a from foo where a = 'FOO'");
        assertFalse(wrongCase.next());

        ResultSet correctCase = query("select a from foo where a = 'Foo'");
        assertTrue(correctCase.next());
        assertEquals("Foo", correctCase.getString("a"));
        assertFalse(correctCase.next());
    }
    


    public void testWhereAnd() throws Exception {
        execute("create table foo (a integer, b integer, c integer)");
        execute("insert into foo (a, b, c) values (1, 1, 1)");
        execute("insert into foo (a, b, c) values (1, 1, 2)");
        execute("insert into foo (a, b, c) values (1, 2, 1)");
        execute("insert into foo (a, b, c) values (1, 2, 2)");
        execute("insert into foo (a, b, c) values (2, 2, 2)");

        assertResultSet(
            new String[] {
                "   1,  1,  1 ",
                "   1,  2,  1 "
            },
            query("select a, b, c from foo where a=1 and c=1")
        );
    }

    public void testWhatOr() throws Exception {
        execute("create table foo (a integer, b integer, c integer)");
        execute("insert into foo (a, b, c) values (1, 1, 1)");
        execute("insert into foo (a, b, c) values (1, 1, 2)");
        execute("insert into foo (a, b, c) values (1, 2, 1)");
        execute("insert into foo (a, b, c) values (1, 2, 2)");
        execute("insert into foo (a, b, c) values (2, 2, 2)");

        assertResultSet(
            new String[] {
                "   1,  1,  1 ",
                "   1,  1,  2 ",
                "   2,  2,  2 ",
            },
            query("select a, b, c from foo where a=2 or b=1")
        );
    }

    public void testNotEqual() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (6)");

        assertResultSet(
            new String[] {
                "   4 ",
                "   6 ",
            },
            query("select a from foo where a != 5")
        );

        assertResultSet(
            new String[] {
                "   4 ",
                "   6 ",
            },
            query("select a from foo where 5 != a")
        );

        assertResultSet(
            new String[] {
                "   4 ",
                "   6 ",
            },
            query("select a from foo where a <> 5")
        );

        assertResultSet(
            new String[] {
                "   4 ",
                "   6 ",
            },
            query("select a from foo where 5 <> a")
        );
    }

    public void testGreaterThan() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (6)");

        assertResultSet(
            new String[] {
                "   5 ",
                "   6 ",
            },
            query("select a from foo where a > 4")
        );

        assertResultSet(
            new String[] {
                "   4 ",
                "   5 ",
            },
            query("select a from foo where 6 > a ")
        );

        assertResultSet(
                new String[] {
                    "   4 ",
                    "   5 ",
                },
                query("select a from foo where a < 6 ")
            );
    }


    public void testSimpleIn() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (1, 1)");
        execute("insert into foo (a, b) values (2, 4)");
        execute("insert into foo (a, b) values (3, 9)");

        assertResultSet(
            new String[] {
                "   1 ",
                "   9 ",
            },
            query("select b from foo where foo.a in (1, 3)")
        );

        assertResultSet(
            new String[] {
                "   4 ",
            },
            query("select b from foo where not foo.a in (1, 3)")
        );

        if (!CONNECT_TO_MAYFLY) {
            // Needs fixing in LDBC grammar.
            assertResultSet(
                new String[] {
                    "   4 ",
                },
                query("select b from foo where foo.a not in (1, 3)")
            );
        }

    }

    public void testInWithSubselect() throws Exception {
        if (CONNECT_TO_MAYFLY) {
            // Needs fixing in LDBC grammar.
            return;
        }

        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (1, 1)");
        execute("insert into foo (a, b) values (2, 4)");
        execute("insert into foo (a, b) values (3, 9)");

        execute("create table bar (c integer)");
        execute("insert into bar (c) values (2)");
        execute("insert into bar (c) values (3)");

        assertResultSet(
            new String[] {
                "   4 ",
                "   9 ",
            },
            query("select b from foo where foo.a in (select c from bar)")
        );

    }

}
