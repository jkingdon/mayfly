package net.sourceforge.mayfly.acceptance;

import java.sql.*;

public class JoinTest extends SqlTestCase {

    public void testImplicitInnerJoin() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (b integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (b) values (100)");
        execute("insert into bar (b) values (101)");

        assertResultSet(
            new String[] {
                "   4,  100 ",
                "   4,  101 ",
                "   5,  100 ",
                "   5,  101 ",
            },
            query("select foo.a, bar.b from foo, bar")
        );
    }

    public void testJoinSameNameTwice() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (100)");
        execute("insert into bar (a) values (101)");
        assertResultSet(
            new String[] {
                "   4,  100 ",
                "   4,  101 ",
                "   5,  100 ",
                "   5,  101 ",
            },
            query("select foo.a, bar.a from foo, bar")
        );
    }

    public void testWhereNeedsTableName() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into bar (a) values (100)");
        execute("insert into bar (a) values (101)");

        assertResultSet(
            new String[] { "4, 100" },
            query("select foo.a, bar.a from foo, bar where bar.a = 100")
        );
    }

    public void testColumnNameForWrongTable() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("CREATE TABLE bar (b INTEGER)");
        expectQueryFailure("select foo.b from foo, bar", "no column foo.b");

        expectQueryFailure("select a from foo, bar where bar.A = 5", "no column bar.A");

        execute("insert into foo (a) values (7)");
        execute("insert into bar (b) values (8)");
        expectQueryFailure("select a from foo, bar where bar.A = 5", "no column bar.A");
    }

    public void testAmbiguousColumnName() throws Exception {
        execute("CREATE TABLE FOO (A INTEGER)");
        execute("CREATE TABLE bar (a INTEGER)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (9)");
        
        String ambiguousColumnNameQuery = "select A from foo, bar";
        if (EXPECT_MAYFLY_BEHAVIOR) {
            expectQueryFailure(ambiguousColumnNameQuery, "ambiguous column A");
        } else {
            // This is the hypersonic behavior.  It seems too "guess what I meant"-ish
            // for mayfly.
            assertResultSet(new String[] {"5"}, query(ambiguousColumnNameQuery));
            assertResultSet(new String[] {"9"}, query("select A from bar, foo"));
        }
    }

    public void testAlias() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (10)");
        ResultSet results = query("select f.a from foo f where f.a = 4");
        assertTrue(results.next());

        assertEquals(4, results.getInt("a"));

        assertFalse(results.next());
    }

    public void testAliasResolvesToCorrectTable() throws Exception {
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into bar (a) values (100)");
        execute("insert into bar (a) values (101)");

        assertResultSet(
            new String[] { "4, 100" },
            query("select f.a, b.a from foo f, bar b where b.a = 100")
        );
    }
    
    public void testSelfJoin() throws Exception {
        execute("create table place (id integer, parent integer, name varchar(80))");
        execute("insert into place (id, parent, name) values (1, 0, 'India')");
        execute("insert into place (id, parent, name) values (10, 1, 'Karnataka')");
        execute("insert into place (id, parent, name) values (100, 10, 'Bangalore')");
        assertResultSet(
            new String[] {
                " 'Karnataka', 'India' ",
                " 'Bangalore', 'Karnataka' ",
            },
            query("select child.name, parent.name from place parent, place child " +
                "where parent.id = child.parent")
        );
    }

    public void testLeftJoin() throws Exception {
        execute("create table place (id integer, parent integer, name varchar(80))");
        execute("insert into place (id, parent, name) values (1, 0, 'India')");
        execute("insert into place (id, parent, name) values (10, 1, 'Karnataka')");
        execute("insert into place (id, parent, name) values (100, 10, 'Bangalore')");
        assertResultSet(
            new String[] {
                " 'India', null ",
                " 'Karnataka', 'India' ",
                " 'Bangalore', 'Karnataka' ",
            },
            query("select child.name, parent.name from place child left outer join place parent " +
                "on parent.id = child.parent")
        );
    }
    
    public void testWordOuterIsOptional() throws Exception {
        if (mayflyMissing()) {
            // The grammar, again.
            execute("create table foo (a integer)");
            assertNotNull(query("select * from foo left join foo on 1 = 1"));
        }
    }

    public void testCrossJoin() throws Exception {
        // Hypersonic, and to a certain extent MySQL, treat CROSS JOIN as being
        // just like INNER JOIN.  Mayfly, Oracle, and Postgres hew more closely
        // to the SQL standard: INNER JOIN must have ON and CROSS JOIN cannot have ON.

        if (!mayflyMissing()) {
            // The above describes the intended mayfly behavior, but I can't figure out
            // how to get ANTLR to parse CROSS JOIN.  Something dumb, I'm sure.
            return;
        }

        execute("create table foo (a integer)");
        execute("create table bar (b integer)");
        execute("insert into foo (a) values (4)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (b) values (100)");
        execute("insert into bar (b) values (101)");

        String[] fullCartesianProduct = new String[] {
            "   4,  100 ",
            "   4,  101 ",
            "   5,  100 ",
            "   5,  101 ",
        };

        String crossJoinNoOn = "select a, b from foo cross join bar";
        if (EXPECT_MAYFLY_BEHAVIOR) {
            assertResultSet(fullCartesianProduct, query(crossJoinNoOn));
        } else {
            expectQueryFailure(crossJoinNoOn, null);
        }
        
        String crossJoinWithOn = "select a, b from foo cross join bar on 1 = 1";
        if (EXPECT_MAYFLY_BEHAVIOR) {
            expectQueryFailure(crossJoinWithOn,
                "Specify INNER JOIN, not CROSS JOIN, if you want an ON condition");
        } else {
            assertResultSet(fullCartesianProduct, query(crossJoinWithOn));
        }

        expectQueryFailure("select a, b from foo inner join bar",
            "Specify CROSS JOIN, not INNER JOIN, if you want to omit an ON condition");
    }

    public void testExplicitJoin() throws Exception {
        execute("create table places (name varchar(80), type integer)");
        execute("create table types (type integer, name varchar(80))");
        execute("insert into places (name, type) values ('London', 1)");
        execute("insert into places (name, type) values ('France', 2)");
        execute("insert into places (name, type) values ('Erewhon', 0)");
        execute("insert into types (name, type) values ('City', 1)");
        execute("insert into types (name, type) values ('Country', 2)");

        assertResultSet(
            new String[] {
                " 'London',   'City'    ",
                " 'France',   'Country' ",
            },
            query("select places.name, types.name from places inner join types on places.type = types.type")
        );
    }
    
    public void testErrorInOnCondition() throws Exception {
        execute("create table places (name varchar(80), type integer)");
        execute("create table types (type integer, name varchar(80))");
        expectQueryFailure(
            "select places.name from places inner join types on type = types.type",
            "ambiguous column type");
    }

    public void testCombineExplicitAndImplicitJoins() throws Exception {
        // It is useful/common to have a query with both an explicit and
        // implicit join in it?

        // Another case if these can be made to work:
        // from foo, bar outer join baz  => the "left" is bar, not the result of foo cross bar

        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("create table types (type integer, name varchar(80))");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (9)");
        execute("insert into bar (a) values (10)");
        execute("insert into types (name, type) values ('City', 9)");
        
        // Illustrates setup but isn't the point of this test
        assertResultSet(
            new String[] {
                " 9 ",
            },
            query("select a from bar inner join types on a = type")
        );

        // Hypersonic says column A is ambiguous
        if (EXPECT_MAYFLY_BEHAVIOR) {
            assertResultSet(
                new String[] {
                    " 5, 9 ",
                },
                query("select foo.a, bar.a from foo, bar inner join types on a = type")
            );
        }

        // Hypersonic-friendly variant of above case
        assertResultSet(
            new String[] {
                " 5, 9 ",
            },
            query("select foo.a, bar.a from foo, bar inner join types on bar.a = type")
        );

        // Which raises the question of whether the ON is really any different from the WHERE.
        // Hypersonic seems to say no, at least in the following case:
        // (I would think mayfly should reject this kind of usage, but what does SQL92 and/or
        // common practice say?)
        String onReachesOutOfJoinedColumnsQuery = 
            "select foo.a, bar.a from bar, foo inner join types on bar.a = type";
        if (EXPECT_MAYFLY_BEHAVIOR) {
            expectQueryFailure(onReachesOutOfJoinedColumnsQuery, "no column bar.a");
        } else {
            assertResultSet(
                new String[] {
                    " 5, 9 ",
                },
                query(onReachesOutOfJoinedColumnsQuery)
            );
        }

        assertResultSet(
            new String[] {
                " 5, 9 ",
            },
            query("select foo.a, bar.a from bar inner join types on a = type, foo")
        );
    }
    
    public void testNestedJoins() throws Exception {
        execute("create table foo (f integer, name varchar(80))");
        execute("create table bar (b1 integer, b2 integer)");
        execute("create table quux (q integer, name varchar(80))");
        execute("insert into foo (f, name) values (5, 'FooVal')");
        execute("insert into foo (f, name) values (7, 'FooDecoy')");
        execute("insert into bar (b1, b2) values (5, 9)");
        execute("insert into bar (b1, b2) values (5, 10)");
        execute("insert into bar (b1, b2) values (4, 9)");
        execute("insert into quux (q, name) values (9, 'QuuxVal')");
        execute("insert into quux (q, name) values (8, 'QuuxDecoy')");
        
        // Neither LDBC nor Hypersonic parse this.  But the SQL92 grammar seems to allow it (I think)
//        assertResultSet(
//            new String[] {" 'FooVal', 'QuuxVal' " },
//            query("select foo.name, quux.name from foo inner join bar inner join quux on b2 = q on f = b1")
//        );

        /** We can't currently transform this 
         * {@link net.sourceforge.mayfly.ldbc.SelectTest#xtestNestedJoins()} */
        if (mayflyMissing()) {
            assertResultSet(
                new String[] {" 'FooVal', 'QuuxVal' " },
                query("select foo.name, quux.name from foo inner join bar on f = b1 inner join quux on b2 = q")
            );
        }
    }
    
}
