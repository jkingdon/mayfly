package net.sourceforge.mayfly.acceptance.expression;

import net.sourceforge.mayfly.acceptance.SqlTestCase;

/**
 * @internal
 * See also {@link WhereTest#testLikePrecedence()} although that is about
 * parsing/precedence as much as LIKE.
 */
public class LikeTest extends SqlTestCase {
    
    public void testBasics() throws Exception {
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
    
    public void testCaseInsensitive() throws Exception {
        execute("create table foo(a varchar(255))");
        execute("insert into foo(a) values('CAP')");
        execute("insert into foo(a) values('cap')");
        execute("insert into foo(a) values('Cap')");
        
        if (dialect.likeIsCaseSensitive()) {
            assertResultSet(new String[] { " 'cap' " }, 
                query("select a from foo where a like 'cap'"));
            assertResultSet(new String[] { " 'CAP' "}, 
                query("select a from foo where a like 'CAP'"));
            assertResultSet(new String[] { }, 
                query("select a from foo where a like 'caP'"));
        }
        else {
            assertResultSet(new String[] { " 'cap' ", " 'Cap' ", " 'CAP' " }, 
                query("select a from foo where a like 'cap'"));
            assertResultSet(new String[] { " 'cap' ", " 'Cap' ", " 'CAP' " }, 
                query("select a from foo where a like 'CAP'"));
            assertResultSet(new String[] { " 'cap' ", " 'Cap' ", " 'CAP' " }, 
                query("select a from foo where a like 'caP'"));
        }
    }
    
}
