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
    
    // test that two schemas each have their own tables
    // test two tables in a CREATE SCHEMA command
    // test mars.foo syntax (where is this legal?)
    // attempt to add a schema which already exists (also see MySql IF NOT EXISTS)
    // make sure that set schema really affects everything (maybe unit test for this...)
    // case insensitive on SET SCHEMA
    // case preserving on error messages, listing schemas, etc

}
