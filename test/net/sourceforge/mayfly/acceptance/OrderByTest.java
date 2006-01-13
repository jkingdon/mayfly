package net.sourceforge.mayfly.acceptance;

import java.sql.*;

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
            expectQueryFailure(expression, "expected end of file but got PLUS");
            expectQueryFailure(constantExpression, "expected end of file but got PLUS");
            
            expectQueryFailure(referenceToExpression, "ORDER BY 1 refers to an expression not a column");
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
        
        // This one blows up because NullCell doesn't compare to LongCell.
        // Worry about this later.
//        assertResultList(new String[] { "'C'", "'B'", "'A'" },
//            query(baseQuery + " order by parent.id")
//        );
        
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
