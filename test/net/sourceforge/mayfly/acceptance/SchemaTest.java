package net.sourceforge.mayfly.acceptance;


public class SchemaTest extends SqlTestCase {

    public void testHypersonicSyntax() throws Exception {
        if (dialect instanceof MySqlDialect) {
            return;
        }
        assertEquals(0, execute("create schema mars authorization dba create table foo (x integer)"));
        assertEquals(0, execute("set schema mars"));
        assertEquals(1, execute("insert into foo(x) values (5)"));
        assertResultSet(new String[] { " 5 " }, query("select x from foo"));
        
        expectExecuteFailure("set schema nonexistent", "no schema nonexistent");
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
    
    public void testTwoSchemasEachHaveTheirOwnTables() throws Exception {
        if (dialect instanceof MySqlDialect) {
            return;
        }
        assertEquals(0, execute("create schema mars authorization dba create table foo (x integer)"));
        assertEquals(0, execute("create schema venus authorization dba create table bar (x integer)"));
        assertEquals(0, execute("set schema mars"));
        assertEquals(1, execute("insert into foo(x) values (5)"));
        // Or "no table mars.bar"?  But that might be noise where schemas aren't at issue.
        expectExecuteFailure("insert into bar(x) values (5)", "no table bar"); 
    }
    
    public void testTwoTablesInCreateSchema() throws Exception {
        if (dialect instanceof MySqlDialect) {
            return;
        }
        if (dialect instanceof MayflyDialect) {
            // I don't know how to get ANTLR to parser this.
            // I'm not sure what ANTLR is complaining about...
            return;
        }
        execute("create schema mars authorization dba create table foo (x integer) create table bar (x integer)");
        execute("set schema mars");
        assertEquals(1, execute("insert into bar(x) values (5)"));
    }
    
    public void testSchemaAlreadyExists() throws Exception {
        if (dialect instanceof MySqlDialect) {
            // Would like to test this case including the IF NOT EXISTS on CREATE DATABASE
            return;
        }
        execute("create schema mars authorization dba create table foo (x integer)");
        expectExecuteFailure(
            "create schema mars authorization dba create table bar (x integer)",
            "schema mars already exists"
        );
    }
    
    // test mars.foo syntax (where is this legal?)
    // make sure that set schema really affects everything (maybe unit test for this...)
    // case insensitive on SET SCHEMA
    // case preserving on error messages, listing schemas, etc
    // world?.col where ? is JDBC parameter (int or string)

}
