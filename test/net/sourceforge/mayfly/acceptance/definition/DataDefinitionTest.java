package net.sourceforge.mayfly.acceptance.definition;

import net.sourceforge.mayfly.acceptance.ForeignKeyTest;
import net.sourceforge.mayfly.acceptance.SqlTestCase;

/**
 * Tests of creating and dropping tables.
 * Also see {@link net.sourceforge.mayfly.acceptance.definition.AddColumnTest}
 * and {@link net.sourceforge.mayfly.acceptance.definition.DropColumnTest}.
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
    // So that's what DROP TABLE tests are doing here.

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
    
    public void testDroppingTableDropsIndexes() throws Exception {
        execute("create table foo(x integer not null)");
        execute("create index x_index on foo(x)");
        execute("drop table foo");
    }
    
}
