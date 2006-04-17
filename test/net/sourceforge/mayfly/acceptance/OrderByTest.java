package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderByTest extends SqlTestCase {

    public void testOrderByDoesNotCountAsWhat() throws Exception {
        execute("create table vehicles (name varchar(255), wheels integer)");
        execute("insert into vehicles (name, wheels) values ('bicycle', 2)");
        ResultSet results = query("select name from vehicles order by wheels");
        assertTrue(results.next());
        assertEquals("bicycle", results.getString(1));
        if (!dialect.orderByCountsAsWhat()) {
            try {
                results.getInt(2);
                fail();
            } catch (SQLException e) {
                assertMessage("no column 2", e);
            }
        } else {
            // Is this just a hypersonic quirk or do other databases do this?
            assertEquals(2, results.getInt(2));
        }

        results.close();
    }

    public void testOrderBy() throws Exception {
        execute("create table vehicles (name varchar(255), wheels integer, speed integer)");
        execute("insert into vehicles (name, wheels, speed) values ('bicycle', 2, 15)");
        execute("insert into vehicles (name, wheels, speed) values ('car', 4, 100)");
        execute("insert into vehicles (name, wheels, speed) values ('tricycle', 3, 5)");
        assertResultList(new String[] { "'bicycle'", "'tricycle'", "'car'" },
            query("select name from vehicles order by wheels asc")
        );
        assertResultList(new String[] { "'car'", "'tricycle'", "'bicycle'" },
            query("select name from vehicles order by wheels desc")
        );
        assertResultList(new String[] { "'tricycle'", "'bicycle'", "'car'" },
            query("select name from vehicles order by speed")
        );
    }
    
    public void testNullSortOrder() throws Exception {
        execute("create table foo (a varchar(255))");
        execute("insert into foo (a) values ('one')");
        execute("insert into foo (a) values (null)");
        execute("insert into foo (a) values ('')");
        assertResultList(
            dialect.nullSortsLower() ?
                new String[] { " null ", " '' ", " 'one' " } :
                new String[] { " '' ", " 'one' ", " null " },
            query("select a from foo order by a")
        );
    }
    
    public void testOrderByExpression() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a, b) values (5, 30)");
        execute("insert into foo(a, b) values (8, 40)");
        execute("insert into foo(a, b) values (3, 50)");
        execute("insert into foo(a, b) values (4, 60)");
        execute("insert into foo(a, b) values (2, 70)");

        String expression = "select a from foo order by a + b";
        // So here's the evil part: an integer is not an expression, it is a special case
        String reference = "select a from foo order by 1, b";
        String referenceDescending = "select a from foo order by 1 desc, b";
        // But this one is an expression
        String constantExpression = "select a from foo order by 1 + 0, b";

        // This one isn't quite so strange; maybe this is worth supporting
        String referenceToExpression = "select a + b from foo order by 1, b";

        assertResultList(new String[] { "2", "3", "4", "5", "8" }, query(reference));
        assertResultList(new String[] { "8", "5", "4", "3", "2" }, query(referenceDescending));
        if (dialect.canOrderByExpression()) {
            assertResultList(new String[] { "5", "8", "3", "4", "2" }, query(expression));
            // Evil!  We can at the very least give an error on a constant expression, I hope
            assertResultList(new String[] { "5", "8", "3", "4", "2" }, query(constantExpression));

            assertResultList(new String[] { "35", "48", "53", "64", "72" }, query(referenceToExpression));
        }
        else {
            expectQueryFailure(expression, "expected end of file but got '+'");
            expectQueryFailure(constantExpression, "expected end of file but got '+'");
            
            expectQueryFailure(referenceToExpression, "ORDER BY 1 refers to an expression not a column");
        }
    }
    
    public void testOrderByNumericReference() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a, b) values (5, 30)");
        execute("insert into foo(a, b) values (8, 40)");
        execute("insert into foo(a, b) values (3, 50)");
        execute("insert into foo(a, b) values (4, 60)");
        execute("insert into foo(a, b) values (2, 70)");

        assertResultList(new String[] { "2", "3", "4", "5", "8" }, query("select a from foo order by 1"));
        assertResultList(new String[] { "8", "5", "4", "3", "2" }, query("select a from foo order by 1 desc"));
        expectQueryFailure("select a from foo order by 0", "ORDER BY 0 must be in range 1 to 1");

        // Does negative mean something?
        //        expectQueryFailure("select a from foo order by -1", "ORDER BY -1 must be in range 1 to 1");
        //expectQueryFailure("select a from foo order by -1", "expected identifier but got '-'");

        expectQueryFailure("select a from foo order by 2", "ORDER BY 2 must be in range 1 to 1");
    }
    
    public void testOrderByNumericReferenceWithAsterisks() throws Exception {
        execute("create table foo (a integer, b integer)");
        execute("insert into foo(a, b) values (5, 30)");
        execute("insert into foo(a, b) values (8, 40)");
        execute("insert into foo(a, b) values (3, 50)");
        execute("insert into foo(a, b) values (4, 60)");
        execute("insert into foo(a, b) values (2, 70)");

        String orderByB = "select foo.*, a from foo order by 2";
        String orderBySecondA = "select foo.*, a from foo order by 3";

        assertResultList(new String[] { "2", "3", "4", "5", "8" }, query("select a, foo.* from foo order by 1"));
        String orderBySecondA2 = "select a, foo.* from foo order by 2";

        if (dialect.expectMayflyBehavior()) {
            // Currently, mayfly is written so the numbers refer to the position
            // in the select clause before foo.* is expanded.  That seems questionable,
            // in that mayfly seems to be the only database tested which does that.
            // 
            // On the other hand, it seems a bit questionable to add a column and
            // thus cause the ORDER BY to refer to something different.
            assertResultList(new String[] { "2", "3", "4", "5", "8" }, query(orderByB));
            expectQueryFailure(orderBySecondA, "ORDER BY 3 must be in range 1 to 2");
            expectQueryFailure(orderBySecondA2, "ORDER BY 2 refers to an expression not a column");
        }
        else {
            assertResultList(new String[] { "5", "8", "3", "4", "2" }, query(orderByB));
            assertResultList(new String[] { "2", "3", "4", "5", "8" }, query(orderBySecondA));
            assertResultList(new String[] { "2", "3", "4", "5", "8" }, query(orderBySecondA2));
        }
    }
    
    public void testOrderByWithAlias() throws Exception {
        execute("create table places (id integer, parent integer, name varchar(255))");
        execute("insert into places(id, parent, name) values(10, 1, 'B')");
        execute("insert into places(id, parent, name) values(1, 20, 'A')");
        execute("insert into places(id, parent, name) values(20, 0, 'C')");
        String baseQuery = "select child.name from " +
                "places child LEFT OUTER JOIN places parent " +
                "on child.parent = parent.id";
        assertResultList(new String[] { "'A'", "'B'", "'C'" },
            query(baseQuery + " order by child.id")
        );

        assertResultList(new String[] { "'C'", "'B'", "'A'" },
            query(baseQuery + " order by child.parent")
        );
        
        assertResultList(
            dialect.nullSortsLower() ? 
                new String[] { "'C'", "'B'", "'A'" } :
                new String[] { "'B'", "'A'", "'C'" },
            query(baseQuery + " order by parent.id")
        );
        
    }

    public void testOrderBySeveralColumns() throws Exception {
        execute("create table foo (name varchar(255), major integer, minor integer)");
        execute("insert into foo (name, major, minor) values ('E', 8, 2)");
        execute("insert into foo (name, major, minor) values ('C', 6, 6)");
        execute("insert into foo (name, major, minor) values ('A', 4, 99)");
        execute("insert into foo (name, major, minor) values ('B', 6, 3)");
        execute("insert into foo (name, major, minor) values ('D', 6, 9)");

        assertResultList(new String[] { "'A'", "'B'", "'C'", "'D'", "'E'" },
            query("select name from foo order by major, minor")
        );
    }

    public void testOrderByAmbiguous() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("CREATE TABLE bar (A INTEGER)");
        String sql = "select foo.a, bar.a from foo, bar order by a";
        if (dialect.detectsAmbiguousColumns()) {
            expectQueryFailure(sql, "ambiguous column a");
        } else {
            assertResultSet(new String[] { }, query(sql));
        }
    }

    // TODO: order by a   -- where a is in several columns, only one of which survives after the joins
    // TODO: what other cases involving resolving column names?
    
}
