package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;

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
        execute("insert into foo (a, b, c) values (1, 2, null)");
        execute("insert into foo (a, b, c) values (null, 1, 1)");

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
        execute("insert into foo (a, b, c) values (2, null, 1)");
        execute("insert into foo (a, b, c) values (null, 1, 1)");
        execute("insert into foo (a, b, c) values (null, null, 1)");

        assertResultSet(
            new String[] {
                "   1,  1,  1 ",
                "   1,  1,  2 ",
                "   2,  2,  2 ",
                "   2,  null,  1 ",
                "   null,  1,  1 ",
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
        execute("insert into foo (a) values (null)");

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
    
    public void testLessThanOrEqual() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (6)");
        execute("insert into foo (a) values (null)");

        assertResultSet(
            new String[] {
                "   4 ",
                "   5 ",
            },
            query("select a from foo where a <= 5")
        );
    }

    public void testGreaterThanOrEqual() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into foo (a) values (6)");
        execute("insert into foo (a) values (null)");

        assertResultSet(
            new String[] {
                "   5 ",
                "   6 ",
            },
            query("select a from foo where a >= 5")
        );
    }

    public void testSimpleIn() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (1, 1)");
        execute("insert into foo (a, b) values (2, 4)");
        execute("insert into foo (a, b) values (3, 9)");

        // OK, this one is where an SQL boolean needs to be true,false,null.
        // I think.
//        execute("insert into foo (a, b) values (null, -1)");

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
    
    public void testExpressions() throws Exception {
        execute("create table foo (a integer, b integer, c integer, description varchar(255))");
        execute("insert into foo(a, b, c, description) values (1, 1, 3, 'equals b')");
        execute("insert into foo(a, b, c, description) values (null, null, 3, 'is null')");
        execute("insert into foo(a, b, c, description) values (1, 2, 3, 'equals nothing')");
        
        assertResultSet(
            new String[] { " 'equals b' " },
            query("select description from foo where a in (b, c)")
        );
    }
    
    public void testNullLiteralOnRightHandSideOfIn() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a, b) values (null, 17)");
        String sql = "select b from foo where a in (null, 5)";
        if (dialect.disallowNullOnRightHandSideOfIn()) {
            expectQueryFailure(sql, 
                "To check for null, use IS NULL or IS NOT NULL, not a null literal");
        }
        else {
            // Hah!  Gotcha!  null = null evaluates to false.
            assertResultSet(new String[] { }, query(sql));
        }
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
            expectQueryFailure(booleanAsLeftSideOfIn, 
                "expected boolean expression but got non-boolean expression");
            
            // The message should identify what part of the expression is the problem.
            // For example, "expected boolean expression but got foo.a"
            // And/or by context, for example:
//          expectQueryFailure(booleanAsLeftSideOfIn, "operand of NOT must be a boolean expression");
        } else {
            assertResultSet(new String[] { }, query(booleanAsLeftSideOfIn));
        }
    }

    public void testInWithSubselect() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo (a, b) values (1, 1)");
        execute("insert into foo (a, b) values (2, 4)");
        execute("insert into foo (a, b) values (3, 9)");

        execute("create table bar (c integer)");
        execute("insert into bar (c) values (2)");
        execute("insert into bar (c) values (3)");

        String inWithSubselect = 
            "select b from foo where foo.a in (select c from bar)";
        if (dialect.wishThisWereTrue()) {
            assertResultSet(
                new String[] {
                    "   4 ",
                    "   9 ",
                },
                query(inWithSubselect)
            );
        }
        else {
            expectQueryFailure(inWithSubselect, 
                "expected primary but got SELECT");
        }
    }
    
    public void testReferToColumnAlias() throws Exception {
        execute("create table foo(a integer, b integer)");
        execute("insert into foo(a, b) values(3, 10)");
        execute("insert into foo(a, b) values(7, 20)");
        String sql = "select a + b as a_and_b from foo where a_and_b < 20";
        if (dialect.whereCanReferToColumnAlias()) {
            assertResultSet(
                new String[] { " 13 " }, 
                query(sql)
            );
        }
        else {
            expectQueryFailure(sql, "no column a_and_b");
        }
    }
    
    public void testLike() throws Exception {
        execute("create table foo(a varchar(255))");
        execute("insert into foo(a) values('cat')");
        execute("insert into foo(a) values('cut')");
        execute("insert into foo(a) values('category')");
        execute("insert into foo(a) values('tomcat')");
        execute("insert into foo(a) values('dog')");
        execute("insert into foo(a) values(null)");
        
        assertResultSet(new String[] { " 'cat' " },
            query("select a from foo where a like 'cat'"));
        assertResultSet(new String[] { " 'cat' ", " 'category' " },
            query("select a from foo where a like 'cat%'"));
        assertResultSet(new String[] { 
            " 'cat' ", " 'category' ", " 'tomcat' " },
            query("select a from foo where a like '%cat%'"));
        assertResultSet(new String[] { 
            " 'cat' ", " 'tomcat' " },
            query("select a from foo where a like '%cat'"));

        assertResultSet(new String[] { " 'cat' " , " 'cut' " },
            query("select a from foo where a like 'c_t'"));
    }

}
