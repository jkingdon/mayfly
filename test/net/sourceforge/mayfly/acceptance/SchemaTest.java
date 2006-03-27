package net.sourceforge.mayfly.acceptance;

public class SchemaTest extends SqlTestCase {

    public void testWithAuthorizationKeyword() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        String sql = "create schema mars authorization dba";
        if (dialect.authorizationAllowedInCreateSchema()) {
            assertEquals(0, execute(sql));
            assertEquals(0, execute("set schema mars"));
            assertEquals(0, execute("create table foo (x integer)"));
    
            assertEquals(1, execute("insert into foo(x) values (5)"));
            assertResultSet(new String[] { " 5 " }, query("select x from foo"));
        }
        else {
            expectExecuteFailure(sql, "expected end of file but got AUTHORIZATION");
        }
    }
    
    public void testWithAuthorizationOtherUser() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        if (!dialect.authorizationAllowedInCreateSchema()) {
            return;
        }

        // In hypersonic, the user has to be "dba".
        // Is this really any more sensible than ignoring the user?
        expectExecuteFailure("create schema mars authorization shivaji", 
            "Can only specify user dba in create schema but was shivaji");
    }
    
    public void testBasicSyntax() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }

        String sql = "create schema mars";
        if (dialect.authorizationRequiredInCreateSchema()) {
            expectExecuteFailure(sql, "expected AUTHORIZATION but got end of file");
        }
        else {
            assertEquals(0, execute(sql));
            assertEquals(0, execute("set schema mars"));
        }
    }
    
    public void xtestMySqlSyntax() throws Exception {
        // This turns out to be a headache.  I don't know how to give myself the right
        // permissions to create databases and use them in this way.

        //assertEquals(0, execute("create schema mars")); // MySQL 5.0.2+ allows this
        assertEquals(0, execute("create database mars")); // MySQL 4.x
        assertEquals(0, execute("create table mars.foo (x integer)"));
        assertEquals(0, execute("set schema mars"));
        assertEquals(1, execute("insert into foo(x) values (5)"));
        assertResultSet(new String[] { " 5 " }, query("select x from foo"));
        assertEquals(0, execute("drop database mars"));
    }
    
    public void testBadSetSchema() throws Exception {
        expectExecuteFailure("set schema nonexistent", "no schema nonexistent");
    }
    
    public void testTwoSchemasEachHaveTheirOwnTables() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        createEmptySchema("mars");
        assertEquals(0, execute("create table foo (x integer)"));
        createEmptySchema("venus");
        assertEquals(0, execute("create table bar (x integer)"));

        assertEquals(0, execute("set schema mars"));
        assertEquals(1, execute("insert into foo(x) values (5)"));
        // Or "no table mars.bar"?  But that might be noise where schemas aren't at issue.
        expectExecuteFailure("insert into bar(x) values (5)", "no table bar"); 
    }
    
    public void testSchemaAndTablesInSameStatementWithAuthorization() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }

        String sql = "create schema mars authorization dba create table foo (x integer) create table bar (x integer)";
        if (dialect.canCreateSchemaAndTablesInSameStatement() && dialect.authorizationAllowedInCreateSchema()) {
            execute(sql);
            execute("set schema mars");
            assertEquals(1, execute("insert into bar(x) values (5)"));
        }
        else {
            expectExecuteFailure(sql, "syntax error at CREATE TABLE");
        }
    }
    
    public void testSchemaAndTablesInSameStatementWithoutAuthorization() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }

        String sql = "create schema mars create table foo (x integer) create table bar (x integer)";
        if (dialect.canCreateSchemaAndTablesInSameStatement() && !dialect.authorizationRequiredInCreateSchema()) {
            execute(sql);
            execute("set schema mars");
            assertEquals(1, execute("insert into bar(x) values (5)"));
        }
        else {
            expectExecuteFailure(sql, "syntax error at CREATE TABLE");
        }
    }
    
    public void testSchemaAlreadyExists() throws Exception {
        if (dialect.schemasMissing()) {
            // Would like to test this case including the IF NOT EXISTS on CREATE DATABASE
            // (Meaning the MySQL case?)
            return;
        }
        execute(dialect.createEmptySchemaCommand("mars"));
        expectExecuteFailure(
            dialect.createEmptySchemaCommand("mars"),
            "schema mars already exists"
        );
    }
    
    public void testSetSchemaIsCaseInsensitive() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        createEmptySchema("mars");
        execute("create table foo (x integer)");
        execute("set schema MARS");
        assertResultSet(new String[] { }, query("select * from foo"));

        // If this message were to include the schema name, I think we'd want it to
        // say mars not MARS.
        expectQueryFailure("select * from nonexist", "no table nonexist");
        
        // Test that error message is case preserving
        expectExecuteFailure("set schema Venus", "no schema Venus");
    }
    
    public void testSchemaNameDotTable() throws Exception {
        if (dialect.schemasMissing()) {
            return;
        }
        
        createEmptySchema("mars");
        execute("create table foo (x integer)");
        execute("insert into mars.foo (x) values (7)");
        
        if (dialect.wishThisWereTrue()) {
            assertResultSet(new String[] { " 7 " }, query("select x from mars.foo"));
            assertResultSet(new String[] { " 7 " }, query("select foo.x from mars.foo"));
        }
    }
    
    // test mars.foo syntax (where is this legal?)
    // case insensitive on mars.foo
    // world?.col where ? is JDBC parameter (int or string)

}
