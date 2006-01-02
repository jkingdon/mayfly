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
            // I don't know how to get ANTLR to parse this.
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
    
    public void testSetSchemaIsCaseInsensitive() throws Exception {
        if (dialect instanceof MySqlDialect) {
            return;
        }
        
        execute("create schema mars authorization dba create table foo (x integer)");
        execute("set schema MARS");
        assertResultSet(new String[] { }, query("select * from foo"));

        // If this message were to include the schema name, I think we'd want it to
        // say mars not MARS.
        expectQueryFailure("select * from nonexist", "no table nonexist");
        
        // Test that error message is case preserving
        expectExecuteFailure("set schema Venus", "no schema Venus");
    }
    
    public void testSchemaNameDotTable() throws Exception {
        if (dialect instanceof MySqlDialect) {
            return;
        }
        
        execute("create schema mars authorization dba create table foo (x integer)");
        execute("insert into mars.foo (x) values (7)");
    }
    
    // test mars.foo syntax (where is this legal?)
    // case insensitive on mars.foo
    // world?.col where ? is JDBC parameter (int or string)

}
