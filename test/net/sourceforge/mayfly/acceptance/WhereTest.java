package net.sourceforge.mayfly.acceptance;

import java.sql.*;

public class WhereTest extends SqlTestCase {

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
        execute("create table foo (a varchar(80))");
        execute("insert into foo (a) values ('Foo')");
        ResultSet wrongCase = query("select a from foo where a = 'FOO'");
        if (dialect.stringComparisonsAreCaseInsensitive()) {
            assertTrue(wrongCase.next());
            assertEquals("Foo", wrongCase.getString("a"));
        }
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
            query("select b from foo where not (foo.a in (1, 3))")
        );

        assertResultSet(
            new String[] {
                "   4 ",
            },
            query("select b from foo where foo.a not in (1, 3)")
        );

    }
    
    public void testInPrecedence() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (1, 1)");
        execute("insert into foo (a, b) values (2, 4)");
        execute("insert into foo (a, b) values (3, 9)");

        String negateTheIn = "select b from foo where not foo.a in (1, 3)";
        if (dialect.notBindsMoreTightlyThanIn()) {
            assertResultSet(
                new String[] { },
                query(negateTheIn)
            );
        } else {
            assertResultSet(
                new String[] {
                    "   4 "
                },
                query(negateTheIn)
            );
        }
        
        String booleanAsLeftSideOfIn = "select b from foo where (not foo.a) in (1, 3)";
        if (dialect.notRequiresBoolean()) {
            // Mayfly and Postgres are pickier than some databases about boolean vs non-boolean
            // If some writes SQL like that they are either making a mistake, or they are
            // being too clever for our tastes.
            expectQueryFailure(booleanAsLeftSideOfIn, "expected boolean expression but got non-boolean expression");
            
            // The message should identify what part of the expression is the problem.
            // For example, "expected boolean expression but got foo.a"
            // And/or by context, for example:
//          expectQueryFailure(booleanAsLeftSideOfIn, "operand of NOT must be a boolean expression");
        } else {
            assertResultSet(new String[] { }, query(booleanAsLeftSideOfIn));
        }
    }

    public void testInWithSubselect() throws Exception {
        if (!mayflyMissing()) {
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
