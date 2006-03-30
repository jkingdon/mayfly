package net.sourceforge.mayfly.acceptance;

public class DataDefinitionTest extends SqlTestCase {

    public void testDuplicateColumnName() throws Exception {
        expectExecuteFailure("create table foo (Id integer, Id integer)", "duplicate column Id");
    }
    
    public void testTableAlreadyExists() throws Exception {
        execute("create table foo (x integer)");
        expectExecuteFailure("create table foo (y integer)", "table foo already exists");
        expectExecuteFailure("create table FOO (y integer)", "table foo already exists");
    }

}
