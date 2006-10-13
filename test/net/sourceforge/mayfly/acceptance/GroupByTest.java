package net.sourceforge.mayfly.acceptance;

import java.sql.ResultSet;

public class GroupByTest extends SqlTestCase {
    
    public void testGroupByActsLikeDistinct() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Dickens', 'Bleak House')");
        execute("insert into books(author, title) values ('Dickens', 'A Tale of Two Cities')");
        
        assertResultList(new String[] { " 'Dickens' " }, query("select author from books group by author"));
        assertResultList(new String[] { " 'Dickens' ", " 'Dickens' " }, query("select author from books"));

        if (!dialect.canGroupByColumnAlias()) {
            return;
        }
        assertResultList(new String[] { " 'Dickens' " }, 
            query("select author as dude from books group by dude"));
    }
    
    public void testGroupByExpression() throws Exception {
        execute("create table people (birthdate integer, age integer)");
        execute("insert into people(birthdate, age) values (1704, 43)");
        execute("insert into people(birthdate, age) values (1714, 33)");
        
        String sql = "select birthdate + age from people group by birthdate + age";
        if (!dialect.canGroupByExpression()) {
            expectQueryFailure(sql, 
                "GROUP BY expression (as opposed to column) is not implemented");
            return;
        }
        assertResultList(new String[] { " 1747 " }, query(sql));

        String needsSmartExpressionComparator = 
            "select birthdate + age + 0 from people group by birthdate + age";
        if (dialect.expectMayflyBehavior()) {
            expectQueryFailure(needsSmartExpressionComparator, 
                "expression is not aggregate or mentioned in GROUP BY");
        }
        else {
            assertResultList(new String[] { " 1747 " }, 
                query(needsSmartExpressionComparator));
        }

        if (!dialect.canGroupByColumnAlias()) {
            return;
        }
        assertResultList(new String[] { " 1747 " }, 
            query("select birthdate + age as deathdate " +
                "from people group by deathdate"));
    }

    public void testGroupByExpressionError() throws Exception {
        if (!dialect.canGroupByExpression()) {
            return;
        }

        execute("create table people (birthdate integer, age integer)");
        
        String selectColumnNotGrouped = 
            "select age from people group by birthdate + age";

        String expressionWhichMakesNoSense = 
            "select birthdate - age from people group by birthdate + age";

        if (dialect.errorIfNotAggregateOrGroupedWhenGroupByExpression()) {
            expectQueryFailure(
                selectColumnNotGrouped, 
                "age is not aggregate or mentioned in GROUP BY"
            );
            expectQueryFailure(expressionWhichMakesNoSense,
                "expression is not aggregate or mentioned in GROUP BY"
            );
        }
        else {
            // This only gets worse if there is data - these databases return
            // various flavours of garbage.  But for this test, the point is that
            // they don't throw an exception.
            assertResultSet(new String[] { }, query(selectColumnNotGrouped));
            assertResultSet(new String[] { }, query(expressionWhichMakesNoSense));
        }

        String expressionWhichCouldMakeSense = 
            "select birthdate + age + 0 from people group by birthdate + age";
        if (dialect.expectMayflyBehavior()) {
            expectQueryFailure(expressionWhichCouldMakeSense,
                "expression is not aggregate or mentioned in GROUP BY"
            );
        }
        else {
            assertResultSet(new String[] { }, query(expressionWhichCouldMakeSense));
        }
    }
    
    public void testGroupByNotInSelectList() throws Exception {
        execute("create table books (author varchar(255), title varchar(255), edition integer)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 2)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 3)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Other Title', 1)");
        
        // Some databases don't allow something you aren't selecting for, according to 
        // The Practical SQL Handbook; Using Structured Query Language, 2nd edition.

        assertResultList(
            new String[] { " 'Other Title' ", " 'Practical SQL' " },
            query("select title from books group by author, title order by title")
        );
    }
    
    public void testGroupByAndAggregate() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) " +
            "values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) " +
            "values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) " +
            "values ('Gang Of Four', 'Design Patterns')");
        
        assertResultList(
            new String[] { " 'Bowman', 2 ", " 'Gang Of Four', 1 " },
            query("select author, count(title) from books " +
                "group by author order by author")
        );
    }
    
    public void testMultipleGroupBy() throws Exception {
        execute("create table books (author varchar(255), title varchar(255), edition integer)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 2)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 3)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Other Title', 4)");
        
        assertResultList(
            new String[] {
                " 'Bowman', 'Other Title', 1 ",
                " 'Bowman', 'Practical SQL', 2 ", 
            },
            query("select author, title, count(*) from books " +
                "group by author, title order by title")
        );

        assertResultList(
            new String[] {
                " 'Bowman', 'Other Title', 1 ",
                " 'Bowman', 'Practical SQL', 2 ", 
            },
            query("select author, title, count(author) from books " +
                "group by author, title order by title")
        );
    }
    
    public void testSelectSomethingNotGrouped() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        String notAggegateOrGrouped = 
            "select author, title, count(*) from books group by author order by author";

        if (dialect.errorIfNotAggregateOrGrouped()) {
            expectQueryFailure(notAggegateOrGrouped, 
                "title is not aggregate or mentioned in GROUP BY");
        } else {
            // MySQL seems to supply some random row for the title.
            // That seems fishy.
            assertResultList(
                new String[] {
                    " 'Bowman', 'Practical SQL', 2 ", 
                    " 'Gang Of Four', 'Design Patterns', 1 "
                },
                query(notAggegateOrGrouped)
            );
        }
    }
    
    public void testSelectSomethingNotGroupedNoRows() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        
        String notAggegateOrGrouped = "select author, title, count(*) from books group by author";

        if (dialect.errorIfNotAggregateOrGrouped()) {
            expectQueryFailure(notAggegateOrGrouped, "title is not aggregate or mentioned in GROUP BY");
        } else {
            assertResultList(new String[] {}, query(notAggegateOrGrouped));
        }
    }
    
    public void xtestGroupByAggregate() throws Exception {
        // Might have to go back to 
        // The Practical SQL Handbook; Using Structured Query Language, 2nd edition.
        // to remember why you'd group by an aggregate.
        // This particular example isn't making sense.
        execute("create table books (pub_id integer, price integer)");
        execute("insert into books (pub_id, price) values (1, 1995)");
        execute("insert into books (pub_id, price) values (1, 2995)");
        execute("insert into books (pub_id, price) values (2, 2195)");
        assertResultSet(new String[] { "1 4990", "2 2195" }, 
            query("select pub_id, sum(price) " +
                "from books group by pub_id, sum(price)"));
    }
    
    public void xtestGroupByAggregateViaAlias() throws Exception {
        // select pub_id, sum(price) as total from titles group by pub_id, total
        // see above
    }
    
    public void testGroupByNull() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        // Null is like another value (it creates a group - one group for all nulls)
        execute("insert into books(author, title) values (null, 'Epic of Gilgamesh')");
        execute("insert into books(author, title) values (null, 'Ramayana')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        // Null is a separate group from zero or empty string
        execute("insert into books(author, title) values ('', 'The Pearl')");
        
        assertResultList(
            dialect.nullSortsLower() ?
                new String[] { " null, 2 ", " '', 1", " 'Gang Of Four', 1 "} :
                new String[] { " '', 1", " 'Gang Of Four', 1 ", " null, 2 " },
            query("select author, count(title) from books group by author order by author")
        );
    }
    
    public void testGroupByInteger() throws Exception {
        execute("create table foo (aKey integer, value integer)");
        execute("insert into foo(aKey, value) values (5, 40)");
        // Null is a separate group from zero or empty string
        execute("insert into foo(aKey, value) values (0, 30)");
        execute("insert into foo(aKey, value) values (null, 20)");
        execute("insert into foo(aKey, value) values (5, 60)");

        assertResultList(
            dialect.nullSortsLower() ?
            new String[] { " null, 20 ", " 0, 30 ", " 5, 50 " } :
            new String[] { " 0, 30 ", " 5, 50 ", " null, 20 " },
            query("select aKey, avg(value) from foo group by aKey order by aKey")
        );
    }
    
    public void testCountOnNullKey() throws Exception {
        // Kind of an obvious combination of GROUP BY and COUNT,
        // but it was enough for the authors of The Practical SQL Handbook
        // to mention specifically.
        execute("create table foo (aKey integer, value integer)");
        execute("insert into foo(aKey, value) values (null, 30)");
        execute("insert into foo(aKey, value) values (null, 20)");
        
        assertResultList(
            new String[] { " null, 2 " },
            query("select aKey, count(*) from foo group by aKey")
        );

        assertResultList(
            new String[] { " null, 0 " },
            query("select aKey, count(aKey) from foo group by aKey")
        );
    }

    public void testWhereIsAppliedBeforeGroupBy() throws Exception {
        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 300)");
        execute("insert into foo(x, y, z) values (9, 10, 400)");
        
        assertResultList(new String[] { " 2 " }, query("select avg(x) from foo where z < 400 group by y"));
    }

    public void testHavingIsAppliedAfterGroupBy() throws Exception {
        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 300)");
        execute("insert into foo(x, y, z) values (8, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        // First try a query which is easier than the one which doesn't select y:
        assertResultList(new String[] { " 2, 10 " },
            query("select avg(x), y from foo group by y having y < 20"));

        String groupByYHavingY = "select avg(x) from foo group by y having y < 20";
        if (dialect.columnInHavingMustAlsoBeInSelect()) {
            expectQueryFailure(groupByYHavingY, "no column y");
        }
        else {
            assertResultList(new String[] { " 2 " }, query(groupByYHavingY));            
        }
    }
    
    public void testHavingIsSelectedExpression() throws Exception {
        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 300)");
        execute("insert into foo(x, y, z) values (7, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        String sql = "select avg(x) from foo group by y having avg(x) < 5";
        if (dialect.wishThisWereTrue()) {
            assertResultList(new String[] { " 2 " }, query(sql));
        }
        else {
            // buggy mayfly behavior...
            assertResultList(new String[] { " 2 ", " 8 " }, query(sql));
        }
    }
    
    public void testHavingIsKeyExpression() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            return;
        }

        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 200)");
        execute("insert into foo(x, y, z) values (8, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        String groupByYHavingY = "select avg(x) from foo group by y, z having (y + z / 10) < 60";
        if (dialect.columnInHavingMustAlsoBeInSelect()) {
            expectQueryFailure(groupByYHavingY, null);
        }
        else {
            assertResultList(new String[] { " 2 " }, query(groupByYHavingY));
        }
    }
    
    public void testHavingIsDisallowedOnUnaggregated() throws Exception {
        if (!dialect.wishThisWereTrue()) {
            return;
        }

        execute("create table foo (x integer, y integer)");
        expectQueryFailure("select avg(x) from foo group by y having x < 5", 
            "x is not aggregate or mentioned in GROUP BY");
    }
    
    public void testHavingWithoutGroupBy() throws Exception {
        execute("create table foo (x integer, y integer)");
        String havingWithoutGroupBy = "select x from foo having x < 5";
        if (dialect.canHaveHavingWithoutGroupBy()) {
            assertResultList(new String[] { }, query(havingWithoutGroupBy));
            
            execute("insert into foo(x, y) values (3, 17)");
            execute("insert into foo(x, y) values (7, 26)");
            assertResultList(new String[] { "3" }, query(havingWithoutGroupBy));
        }
        else {
            expectQueryFailure(havingWithoutGroupBy, 
                "can't specify HAVING without GROUP BY");
        }
    }
    
    public void testGroupByAndOrderBy() throws Exception {
        // For example, select type, avg(price) order by avg(price)
        // or the error case: select type group by type order by price
        if (!dialect.wishThisWereTrue()) {
            return;
        }
    }

    public void testGroupByAndAsterisk() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        assertResultList(
            new String[] {
                " 'Bowman', 'Other Title' ", 
                " 'Bowman', 'Practical SQL' ", 
                " 'Gang Of Four', 'Design Patterns' "
            },
            query("select books.* from books group by author, title order by author, title")
        );

        assertResultList(
            new String[] {
                " 'Bowman', 'Other Title' ", 
                " 'Bowman', 'Practical SQL' ", 
                " 'Gang Of Four', 'Design Patterns' "
            },
            query("select * from books group by author, title order by author, title")
        );
        
        String selectAll = "select * from books group by author";
        String selectAllFromTable = "select books.* from books group by author";
        if (dialect.errorIfNotAggregateOrGrouped()) {
            expectQueryFailure(selectAll, "books.title is not aggregate or mentioned in GROUP BY");
            expectQueryFailure(selectAllFromTable, "books.title is not aggregate or mentioned in GROUP BY");
        }
        else {
            assertResultSet(new String[] { " 'Bowman' ", " 'Gang Of Four' " }, query(selectAll));
            assertResultSet(new String[] { " 'Bowman' ", " 'Gang Of Four' " }, query(selectAllFromTable));
        }
    }
    
    public void testSelectClauseAndAliases() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values(6)");
        execute("insert into foo(a) values(10)");
        
        String selectOk = "select f.*, avg(g.a) from foo f, foo g group by f.a order by f.a";
        String notAggregateOrGrouped = "select g.* from foo f, foo g group by f.a order by f.a";
        
        // Just for illustration:
        assertResultSet(new String[] {
            "6, 6",
            "6, 10",
            "10, 6",
            "10, 10"
        }, query("select f.*, g.* from foo f, foo g")
        );
        
        assertResultSet(new String[] {
            "6, 8",
            "10, 8"
        }, query(selectOk)
        );

        if (dialect.errorIfNotAggregateOrGroupedWhenGroupByExpression()) {
            expectQueryFailure(notAggregateOrGrouped, "g.a is not aggregate or mentioned in GROUP BY");
        }
        else {
            // Probably 6, 6 or 10, 10.  Doesn't really matter which bogus answer.
            ResultSet results = query(notAggregateOrGrouped);
            results.close();
        }
    }
    
    public void testAliasesAndHaving() throws Exception {
        execute("create table foo (a integer)");
        execute("insert into foo(a) values(6)");
        execute("insert into foo(a) values(10)");
        
        String selectOk = "select f.*, avg(g.a) from foo f, foo g " +
            "group by f.a having f.a < 10 order by f.a";
        
        assertResultSet(new String[] { "6, 8" }, query(selectOk));
    }
    
    public void testBadColumnInHaving() throws Exception {
        execute("create table foo(a integer)");
        if (dialect.wishThisWereTrue()) {
            expectQueryFailure(
                "select * from foo group by a having b < 10", 
                "no column b",
                1, 37, 1, 38);
        }
        execute("insert into foo(a) values(6)");
        expectQueryFailure(
            "select * from foo group by a having b < 10", 
            "no column b",
            1, 37, 1, 38);
    }

    public void testAliasesAndExpression() throws Exception {
        if (!dialect.canGroupByExpression()) {
            return;
        }

        execute("create table foo (a integer)");
        execute("insert into foo(a) values(6)");
        execute("insert into foo(a) values(10)");
        
        String selectOk = "select 5+f.a, avg(g.a) from foo f, foo g " +
            "group by 5 + f.a";
        
        assertResultSet(new String[] {
            "11, 8",
            "15, 8",
        }, query(selectOk)
        );
    }

}
