package net.sourceforge.mayfly.acceptance;


public class SchemaTest extends SqlTestCase {

    public void testBasics() throws Exception {
        if (true) return;

        execute("create schema mars create table foo (x integer)");
        execute("set schema mars");
        execute("insert into foo(x) values (5)");
        assertResultSet(new String[] { " 5 " }, query("select x from foo"));
    }

}
