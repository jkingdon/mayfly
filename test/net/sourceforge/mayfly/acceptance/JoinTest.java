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

        execute("insert into FOO (a) values (7)");
        execute("insert into bar (b) values (8)");
        expectQueryFailure("select a from foo, bar where bar.A = 5", "no column bar.A");
    }

    public void testAmbiguousColumnName() throws Exception {
        execute("CREATE TABLE foo (A INTEGER)");
        execute("CREATE TABLE bar (a INTEGER)");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (9)");
        
        String ambiguousColumnNameQuery = "select A from foo, bar";
        if (dialect.detectsAmbiguousColumns()) {
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
        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        assertNotNull(query("select * from foo left join bar on 1 = 1"));
    }

    public void testOuterSelfJoin() throws Exception {
        execute("create table foo (Id integer, parent integer)");
        assertResultSet(new String[] { }, 
            query("select * from foo child left outer join foo parent on child.parent = parent.id"));
    }
    
    public void xtestAmbiguousColumnViaJoin() throws Exception {
        // So the case here is that there are two copies of foo.a.
        // and the "*" picks them up, or something?  This might be worth
        // looking into more.  I'm not really sure what is going on,
        // whether it should be an error, and what error message makes
        // sense.
        execute("create table foo (a integer)");
        expectQueryFailure("select * from foo left outer join foo on 1 = 1", "ambiguous column a");
    }

    public void testCrossJoin() throws Exception {
        // Hypersonic, and to a certain extent MySQL, treat CROSS JOIN as being
        // just like INNER JOIN.  Mayfly, Oracle, and Postgres hew more closely
        // to the SQL standard: INNER JOIN must have ON and CROSS JOIN cannot have ON.

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
        if (dialect.crossJoinRequiresOn()) {
            expectQueryFailure(crossJoinNoOn, null);
        } else {
            assertResultSet(fullCartesianProduct, query(crossJoinNoOn));
        }
        
        String crossJoinWithOn = "select a, b from foo cross join bar on 1 = 1";
        if (dialect.crossJoinCanHaveOn()) {
            assertResultSet(fullCartesianProduct, query(crossJoinWithOn));
        } else {
            expectQueryFailure(crossJoinWithOn,
                // This message might be worthwhile, but I'm not sure whether the
                // parser should be trying to guess that an ON goes with a CROSS JOIN.
                // Especially in a dangling ON situation that might create other problems.
//                "Specify INNER JOIN, not CROSS JOIN, if you want an ON condition"
                
                "expected end of file but got ON"
            );
        }

        String innerJoinNoOn = "select a, b from foo inner join bar";
        if (dialect.innerJoinRequiresOn()) {
            expectQueryFailure(innerJoinNoOn, 
                // Might not be too hard to produce this error message but would it
                // really be right? In "FOO INNER JOIN BAR BAZ QUUX ON A = B" is
                // the ON omitted or is QUUX just an extraneous token?
//                "Specify CROSS JOIN, not INNER JOIN, if you want to omit an ON condition"

                "expected ON but got end of file"
            );
        } else {
            assertResultSet(fullCartesianProduct, query(innerJoinNoOn));
        }
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
        // implicit join in it?  (It is common if one is an outer join...)

        // Another case if these can be made to work:
        // from foo, bar outer join baz  => the "left" is bar, not the result of foo cross bar
        //   (or is it?)

        execute("create table foo (a integer)");
        execute("create table bar (a integer)");
        execute("create table types (type integer, name varchar(80))");
        execute("insert into foo (a) values (5)");
        execute("insert into bar (a) values (9)");
        execute("insert into bar (a) values (10)");
        execute("insert into types (name, type) values ('City', 9)");
        
        // Illustrates setup but isn't the point of this test
        assertResultSet(
            new String[] { " 9 " },
            query("select a from bar inner join types on a = type")
        );

        // Hypersonic/MySQL say column A is ambiguous
        String ambiguousIfReachesOutOfJoin = "select foo.a, bar.a from foo, bar inner join types on a = type";
        if (dialect.onIsRestrictedToJoinsTables()) {
            assertResultSet(
                new String[] { " 5, 9 " },
                query(ambiguousIfReachesOutOfJoin)
            );
        } else {
            expectQueryFailure(ambiguousIfReachesOutOfJoin, "ambiguous column a");
        }

        // Hypersonic-friendly variant of above case
        assertResultSet(
            new String[] { " 5, 9 " },
            query("select foo.a, bar.a from foo, bar inner join types on bar.a = type")
        );

        // Which raises the question of whether the ON is really any different from the WHERE.
        // Hypersonic seems to say no, at least in the following case:
        // (I would think mayfly should reject this kind of usage, but what does SQL92 and/or
        // common practice say? - I think they say it should work ;-()
        String onReachesOutOfJoinedColumnsQuery = 
            "select foo.a, bar.a from bar, foo inner join types on bar.a = type";
        if (dialect.onIsRestrictedToJoinsTables()) {
            expectQueryFailure(onReachesOutOfJoinedColumnsQuery, "no column bar.a");
        } else {
            assertResultSet(
                new String[] { " 5, 9 " },
                query(onReachesOutOfJoinedColumnsQuery)
            );
        }

        String ambiguousIfOneConsidersTablesMentionedAfterJoin =
            "select foo.a, bar.a from bar inner join types on a = type, foo";
        if (dialect.considerTablesMentionedAfterJoin()) {
            expectQueryFailure(ambiguousIfOneConsidersTablesMentionedAfterJoin, "ambiguous column a");
        } else {
            assertResultSet(
                new String[] { " 5, 9 " },
                query(ambiguousIfOneConsidersTablesMentionedAfterJoin)
            );
        }
        // Next would be the case just like that but where the ON explicitly says "foo.a"

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
        
        String onsAtEnd = "select foo.name, quux.name from foo inner join bar inner join quux on b2 = q on f = b1";
        if (dialect.rightHandArgumentToJoinCanBeJoin()) {
            assertResultSet(
                new String[] {" 'FooVal', 'QuuxVal' " },
                query(onsAtEnd)
            );
        } else {
            expectQueryFailure(onsAtEnd, null);
        }

        String parenthesizedQuery = 
            "select foo.name, quux.name from foo inner join (bar inner join quux on b2 = q) on f = b1";
        if (dialect.rightHandArgumentToJoinCanBeJoin()) {
            assertResultSet(
                new String[] {" 'FooVal', 'QuuxVal' " },
                query(parenthesizedQuery)
            );
        } else {
            expectQueryFailure(parenthesizedQuery, null);
        }

        assertResultSet(
            new String[] {" 'FooVal', 'QuuxVal' " },
            query("select foo.name, quux.name from foo inner join bar on f = b1 inner join quux on b2 = q")
        );
    }
    
}
