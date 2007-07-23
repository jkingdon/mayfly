package net.sourceforge.mayfly.acceptance;

/**
 * Tests of creating and dropping tables.
 * Also see {@link net.sourceforge.mayfly.acceptance.AddColumnTest}
 * and {@link net.sourceforge.mayfly.acceptance.DropColumnTest}.
 */
public class DataDefinitionTest extends SqlTestCase {

    public void testDuplicateColumnName() throws Exception {
        expectExecuteFailure("create table foo (Id integer, Id integer)", 
            "duplicate column Id");
    }
    
    public void testTableAlreadyExists() throws Exception {
        execute("create table foo (x integer)");
        expectExecuteFailure("create table foo (y integer)", "table foo already exists");
        if (!dialect.tableNamesMightBeCaseSensitive()) {
            expectExecuteFailure("create table FOO (y integer)", "table foo already exists");
        }
    }

    // In our book, "data definition" includes "data undefinition".
    // So DROP TABLE tests belong in this class.

    // Maybe there's a motto here:
    // Mayfly, the most powerful data undefinition language around,
    // featuring not just DROP but also the ability to let your Database
    // be garbage collected or call JdbcDriver.shutdown().

    /**
     * @internal
     * Also see {@link ForeignKeyTest#testDropTable()} and
     * {@link ForeignKeyTest#testDropTableWithSchemas()}.
     */
    
    public void testDropNonexisting() throws Exception {
        expectExecuteFailure("DROP TABLE FOO", "no table FOO");
    }

    public void testDropNonexistingIfExists() throws Exception {
        if (dialect.haveDropTableFooIfExists()) {
            execute("DROP TABLE foo IF EXISTS");
        }
    }

    public void testDropIfExistsNonexisting() throws Exception {
        if (dialect.haveDropTableIfExistsFoo()) {
            execute("DROP TABLE IF EXISTS foo");
        }
    }

    public void testDropExistingIfExists() throws Exception {
        if (dialect.haveDropTableFooIfExists()) {
            execute("create table foo (x integer)");
            execute("DROP TABLE foo IF EXISTS");
            // Now check that it is gone:
            execute("create table foo (x integer)");
        }
    }
    
}
