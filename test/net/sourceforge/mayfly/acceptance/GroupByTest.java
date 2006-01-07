package net.sourceforge.mayfly.acceptance;

public class GroupByTest extends SqlTestCase {
    
    public void testGroupByActsLikeDistinct() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Dickens', 'Bleak House')");
        execute("insert into books(author, title) values ('Dickens', 'A Tale of Two Cities')");
        
        assertResultList(new String[] { " 'Dickens' " }, query("select author from books group by author"));
        assertResultList(new String[] { " 'Dickens' ", " 'Dickens' " }, query("select author from books"));

        if (!mayflyMissing()) {
            return;
        }
        assertResultSet(new String[] { " 'Dickens' " }, query("select author as dude from books group by dude"));
    }
    
    public void testGroupByExpression() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table people (birthdate integer, age integer)");
        execute("insert into people(birthdate, age) values (1704, 43)");
        execute("insert into people(birthdate, age) values (1714, 33)");
        execute("insert into people(birthdate, age) values (1806, 22)");
        
        // Some databases don't allow an expression, according to 
        // The Practical SQL Handbook; Using Structured Query Language, 2nd edition.

        // TODO: Need assertResultListIgnoreOrder
        assertResultSet(new String[] { " 1747 ", " 1828 " }, 
            query("select birthdate + age from people group by birthdate + age"));
        assertResultSet(new String[] { " 1747 ", " 1828 " }, 
            query("select birthdate + age as deathdate from people group by deathdate"));
    }
    
    public void testGroupByNotInSelectList() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table books (author varchar(255), title varchar(255), edition integer)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 2)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 3)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Other Title', 1)");
        
        // Some databases don't allow something you aren't selecting for, according to 
        // The Practical SQL Handbook; Using Structured Query Language, 2nd edition.

        // TODO: Need assertResultListIgnoreOrder
        assertResultSet(
            new String[] { " 'Other Title' ", " 'Practical SQL' " },
            query("select title from books group by author, title")
        );
    }
    
    public void testGroupByAndAggregate() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        // TODO: Need assertResultListIgnoreOrder
        assertResultSet(
            new String[] { " 'Bowman', 2 ", " 'Gang Of Four', 1 " },
            query("select author, count(title) from books group by author")
        );
    }
    
    public void testMultipleGroupBy() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table books (author varchar(255), title varchar(255), edition integer)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 2)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Practical SQL', 3)");
        execute("insert into books(author, title, edition) values ('Bowman', 'Other Title', 4)");
        
        // TODO: Need assertResultListIgnoreOrder
        assertResultSet(
            new String[] {
                " 'Bowman', 'Practical SQL', 2 ", 
                " 'Bowman', 'Other Title', 1 "
            },
            query("select author, title, count(*) from books group by author, title")
        );
    }
    
    public void testSelectSomethingNotGrouped() throws Exception {
        if (!mayflyMissing()) {
            return;
        }

        execute("create table books (author varchar(255), title varchar(255))");
        execute("insert into books(author, title) values ('Bowman', 'Practical SQL')");
        execute("insert into books(author, title) values ('Bowman', 'Other Title')");
        execute("insert into books(author, title) values ('Gang Of Four', 'Design Patterns')");
        
        String notAggegateOrGrouped = "select author, title, count(*) from books group by author";

        if (dialect.errorIfNotAggregateOrGrouped()) {
            expectQueryFailure(notAggegateOrGrouped, null);
        } else {
            // MySQL seems to supply some random row for the title.
            // That seems fishy.
            assertResultSet(
                new String[] {
                    " 'Bowman', 'Practical SQL', 2 ", 
                    " 'Gang Of Four', 'Design Patterns', 1 "
                },
                query(notAggegateOrGrouped)
            );
        }
    }
    
    public void testGroupByAggregate() throws Exception {
        // select pub_id, sum(price) from titles group by pub_id, sum(price)
    }
    
    public void testGroupByAggregateViaAlias() throws Exception {
        // select pub_id, sum(price) as total from titles group by pub_id, total
    }
    
    public void testGroupByNull() throws Exception {
        // Null is like another value
        // Null is a separate group from zero or empty string
        // With count(type), where type is null, gets us 0 (do create the group, but get 0)
        // With count(*), where type is null, gets us 1 (or whatever the count was)
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
