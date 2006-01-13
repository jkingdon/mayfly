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
        // With count(type), where type is null, gets us 0 (do create the group, but get 0)
        // With count(*), where type is null, gets us 1 (or whatever the count was)
    }
    
    public void testGroupByInteger() throws Exception {
        if (!mayflyMissing()) {
            // ORDER BY doesn't yet do null
            return;
        }

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
    
    public void testWhereIsAppliedBeforeGroupBy() throws Exception {
        
    }

    public void testHavingIsAppliedAfterGroupBy() throws Exception {
        
    }
    
    public void testHavingIsDisallowedOnUnaggregated() throws Exception {
        // select pub-id, avg(price) group by pub_id having price > 5
        // (price is bogus)
    }
    
    public void testGroupByAndOrderBy() throws Exception {
        // For example, select type, avg(price) order by avg(price)
    }

}
