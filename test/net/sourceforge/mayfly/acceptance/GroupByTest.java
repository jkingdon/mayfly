package net.sourceforge.mayfly.acceptance;

public class GroupByTest extends SqlTestCase {
    
    public void testGroupByActsLikeDistinct() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Dickens', 'Bleak House')");
        execute("insert into books(author, title) values ('Dickens', 'A Tale of Two Cities')");
        
        assertResultList(new String[] { " 'Dickens' " }, query("select author from books group by author"));
        assertResultList(new String[] { " 'Dickens' ", " 'Dickens' " }, query("select author from books"));

        if (!mayflyMissing()) {
            return;
        }
        assertResultList(new String[] { " 'Dickens' " }, query("select author as dude from books group by dude"));
    }
    
    public void testGroupByExpression() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table people (birthdate integer, age integer)");
        execute("insert into people(birthdate, age) values (1704, 43)");
        execute("insert into people(birthdate, age) values (1714, 33)");
        
        // Some databases don't allow an expression, according to 
        // The Practical SQL Handbook; Using Structured Query Language, 2nd edition.

        assertResultList(new String[] { " 1747 " }, 
            query("select birthdate + age from people group by birthdate + age"));
        assertResultList(new String[] { " 1747 " }, 
            query("select birthdate + age as deathdate from people group by deathdate"));
    }
    
    public void testGroupByExpressionError() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table people (birthdate integer, age integer)");
        String selectColumnNotGrouped = "select age from people group by birthdate + age";
        if (dialect.errorIfNotAggregateOrGroupedWhenGroupByExpression()) {
            expectQueryFailure(
                selectColumnNotGrouped, 
                "age is not aggregate or mentioned in GROUP BY"
            );
        }
        else {
            assertResultSet(new String[] { }, query(selectColumnNotGrouped));
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
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        assertResultList(
            new String[] { " 'Bowman', 2 ", " 'Gang Of Four', 1 " },
            query("select author, count(title) from books group by author order by author")
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
            query("select author, title, count(*) from books group by author, title order by title")
        );
    }
    
    public void testSelectSomethingNotGrouped() throws Exception {
        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        String notAggegateOrGrouped = "select author, title, count(*) from books group by author order by author";

        if (dialect.errorIfNotAggregateOrGrouped()) {
            expectQueryFailure(notAggegateOrGrouped, "title is not aggregate or mentioned in GROUP BY");
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
    
    public void testGroupByAggregate() throws Exception {
        // select pub_id, sum(price) from titles group by pub_id, sum(price)
    }
    
    public void testGroupByAggregateViaAlias() throws Exception {
        // select pub_id, sum(price) as total from titles group by pub_id, total
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
            new String[] { " null, 2 ", " '', 1", " 'Gang Of Four', 1 "},
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
            new String[] { " null, 20 ", " 0, 30 ", " 5, 50 " },
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
        if (!mayflyMissing()) {
            return;
        }

        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 300)");
        execute("insert into foo(x, y, z) values (8, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        String groupByYHavingY = "select avg(x) from foo group by y having y < 20";
        if (dialect.canApplyHavingToKey()) {
            assertResultList(new String[] { " 2 " }, query(groupByYHavingY));
        }
        else {
            expectQueryFailure(groupByYHavingY, null);
        }
    }
    
    public void testHavingIsSelectedExpression() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 300)");
        execute("insert into foo(x, y, z) values (7, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        assertResultList(new String[] { " 2 " }, query("select avg(x) from foo group by y having avg(x) < 5"));
    }
    
    public void testHavingIsKeyExpression() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table foo (x integer, y integer, z integer)");
        execute("insert into foo(x, y, z) values (1, 10, 200)");
        execute("insert into foo(x, y, z) values (3, 10, 200)");
        execute("insert into foo(x, y, z) values (8, 20, 400)");
        execute("insert into foo(x, y, z) values (9, 20, 400)");
        
        String groupByYHavingY = "select avg(x) from foo group by y, z having (y + z / 10) < 60";
        if (dialect.canApplyHavingToKey()) {
            assertResultList(new String[] { " 2 " }, query(groupByYHavingY));
        }
        else {
            expectQueryFailure(groupByYHavingY, null);
        }
    }
    
    public void testHavingIsDisallowedOnUnaggregated() throws Exception {
        if (!mayflyMissing()) {
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
            expectQueryFailure(havingWithoutGroupBy, "can't specify HAVING without GROUP BY");
        }
    }
    
    public void testGroupByAndOrderBy() throws Exception {
        // For example, select type, avg(price) order by avg(price)
    }

}
