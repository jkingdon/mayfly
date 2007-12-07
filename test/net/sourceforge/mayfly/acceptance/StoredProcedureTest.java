package net.sourceforge.mayfly.acceptance;

import java.sql.SQLException;


public class StoredProcedureTest extends SqlTestCase {
    
    public void testJavaFunctionNoAlias() throws Exception {
        execute("create table foo(a integer)");
        execute("insert into foo(a) values(-7)");
        execute("insert into foo(a) values(5)");
        String query = "select \"java.lang.Math.abs\"(a) from foo";
        if (dialect.callJavaMethodAsStoredProcedure()) {
            assertResultSet(
                new String[] { " 5 ", " 7 " }, 
                query(query));
        }
        else {
            expectQueryFailure(query, 
                "This feature is not yet implemented in Mayfly");
        }
    }
    
    public void testWithAlias() throws Exception {
        String createAlias = "create alias sample for " +
            "\"" +
            getClass().getName() +
            ".sampleProcedure\"";
        if (dialect.callJavaMethodAsStoredProcedure()) {
            execute(createAlias);
            execute("create table foo(a integer, b integer)");
            execute("insert into foo(a, b) values(2, 3)");
            execute("insert into foo(a, b) values(20, -1)");
            assertResultSet(
                new String[] { " 13 ", " 401 " }, 
                query("select sample(a, b) from foo"));
        }
        else {
            expectExecuteFailure(createAlias, 
                "expected create command but got alias");
        }
    }
    
    static public int sampleProcedure(int a, int b) {
        return a * a + b * b;
    }
    
    public void testOverloadedOnArgumentCount() throws Exception {
        if (!dialect.callJavaMethodAsStoredProcedure()) {
            return;
        }

        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        createAlias("overloaded");
        String query = "select overloaded(a) from foo";
        if (dialect.complainAboutDubiousStoredProcedure()) {
            expectQueryFailure(query, 
                "multiple methods found for stored procedure.\n" +
                "class: " + getClass().getName() + "\n" +
                "method: overloaded");
        }
        else {
            /* I'm not really sure how hypersonic is picking a
               method or how it is behaving in general. So I'm
               not going to assert on what it returns. */
            query(query);
        }
    }

    static public int overloaded(int a) {
        return a;
    }
    
    static public int overloaded(int a, int b) {
        return a + b;
    }
    
    public void testOverloadedOnArgumentType() throws Exception {
        if (!dialect.callJavaMethodAsStoredProcedure()) {
            return;
        }

        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        createAlias("onType");
        String query = "select onType(a) from foo";
        if (dialect.complainAboutDubiousStoredProcedure()) {
            expectQueryFailure(query, 
                "multiple methods found for stored procedure.\n" +
                "class: " + getClass().getName() + "\n" +
                "method: onType");
        }
        else {
            /* I'm not really sure how hypersonic is picking a
               method or how it is behaving in general. So I'm
               not going to assert on what it returns. */
            query(query);
        }
    }
    
    static public int onType(int a) {
        return a;
    }
    
    static public int onType(double a) {
        return 1234;
    }

    public void testWrongNumberOfArguments() throws Exception {
        if (!dialect.callJavaMethodAsStoredProcedure()) {
            return;
        }

        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        createAlias("twoArguments");
        String query = "select twoArguments(a) from foo";
        if (dialect.complainAboutDubiousStoredProcedure()) {
            expectQueryFailure(query, 
                "stored procedure expected 2 arguments but got 1.\n" +
                "class: " + getClass().getName() + "\n" +
                "method: onType");
        }
        else {
            /* I'm not really sure how hypersonic is picking a
               method or how it is behaving in general. So I'm
               not going to assert on what it returns. */
            query(query);
        }
    }
    
    static public int twoArguments(int a, int b) {
        return a + b;
    }
    
    public void testMethodNotStatic() throws Exception {
        if (!dialect.callJavaMethodAsStoredProcedure()) {
            return;
        }

        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        createAlias("notStatic");
        String query = "select notStatic(a) from foo";
        expectQueryFailure(query, 
            "stored procedure method must be static.\n" +
            "class: " + getClass().getName() + "\n" +
            "method: notStatic");
    }
    
    public int notStatic(int a) {
        return a;
    }
    
    public void testMethodNotPublic() throws Exception {
        if (!dialect.callJavaMethodAsStoredProcedure()) {
            return;
        }

        execute("create table foo(a integer)");
        execute("insert into foo(a) values(5)");
        createAlias("notPublic");
        String query = "select notPublic(a) from foo";
        // Might be too much trouble to give a message other than "not found (maybe not public?)"
        expectQueryFailure(query, 
            "stored procedure method must be public.\n" +
            "class: " + getClass().getName() + "\n" +
            "method: notPublic");
    }
    
    int notPublic(int a) {
        return a;
    }
    
    private void createAlias(String methodAndAliasName) throws SQLException {
        execute("create alias " +
            methodAndAliasName +
            " for " +
            "\"" +
            getClass().getName() +
            "." +
            methodAndAliasName +
            "\"");
    }
    
    // types on arguments (int vs float vs string vs ?)
    // types on return type
    // in WHERE, not just in select
    // case-sensitive on method (I think that is right; it is in quotes, after all)
    // case-insensitive on alias (for analogous reasons)

}
