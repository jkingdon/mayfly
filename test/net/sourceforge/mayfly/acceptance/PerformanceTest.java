package net.sourceforge.mayfly.acceptance;

import net.sourceforge.mayfly.util.StringBuilder;

import java.sql.ResultSet;

public class PerformanceTest extends SqlTestCase {
    
    private static final int COLUMNS_PER_TABLE = 15;
    private static final int TABLE_COUNT = 30;
    private static final int TABLES_TO_INSERT_INTO = 10;
    private static final int ROWS_TO_INSERT = 3;

    // this test is not part of the default build because it is a bit slow,
    // and more importantly because it contains few assertions, so it is
    // more interesting for manual than automated use.
    public void xtestTypicalUnitTest() throws Exception {
        long start = System.currentTimeMillis();
        createTables();
        insertSomeData();
        dropTables();
        long end = System.currentTimeMillis();
        if (false) {
            System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");
        }
    }
    
    public void xtestLotsOfRows() throws Exception {
        // At the moment, I'm getting 510 ms for MySQL; 2500 ms for Mayfly.
        // There must be some low-hanging fruit here...
        execute("create table foo(x integer, y integer, z integer, w varchar(50))");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            execute("insert into foo(x, y, w) values (" + 
                i + ", " + i % 100 + ", " + "'string " + i + "')");
        }
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");
    }
    
    public void xtestLotsOfRowsWithUnique() throws Exception {
        // Really slow for mayfly (15x slower than without unique).
        execute(
            "create table foo(x integer, y integer, z integer, w varchar(50)" +
            ",unique(x), unique(y)" +
            ")");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            execute("insert into foo(x, y, w) values (" + 
                i + ", " + (i + 100) + ", " + "'string " + i + "')");
        }
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");
    }
    
    public void xtestQueries() throws Exception {
        // Goal here is to test parsing, per-command overhead, etc, not the time evaluating
        // complicated joins and wheres.
        execute("create table foo (a integer, b integer, c integer, d integer, e integer)");
        execute("insert into foo (a, b, c, d, e) values (1, 2, 3, 4, 5)");
        execute("insert into foo (a, b, c, d, e) values (10, 20, 30, 40, 50)");
        execute("insert into foo (a, b, c, d, e) values (100, 200, 300, 400, 500)");
        long start = System.currentTimeMillis();
        int rows = 0;
        int sum = 0;
        for (int i = 0; i < 1000; ++i) {
            ResultSet results = query("select a, c, e from foo where e > " + i);
            while (results.next()) {
                rows += 1;
                sum += results.getInt(1);
            }
            results.close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");

        assertEquals(5 * 3 + 45 * 2 + 450 * 1 + 500 * 0, rows);
        assertEquals(5 * 111 + 45 * 110 + 450 * 100 + 500 * 0, sum);
    }

    private void createTables() throws Exception {
        for (int i = 0; i < TABLE_COUNT; ++i) {
            StringBuilder command = new StringBuilder();
            command.append("create table some_table_" + i + " (\n");
            for (int j = 0; j < COLUMNS_PER_TABLE; ++j) {
                command.append("  column_" + j + " integer");
                if (j < COLUMNS_PER_TABLE - 1) {
                    command.append(",");
                }
                command.append("\n");
            }
            command.append(")\n");
            execute(command.toString());
        }
        assertTableCount(TABLE_COUNT);
    }

    private void insertSomeData() throws Exception {
        for (int i = 0; i < TABLES_TO_INSERT_INTO; ++i) {
            for (int j = 0; j < ROWS_TO_INSERT; ++j) {
                insertRow("some_table_" + i);
            }
        }
    }

    private void insertRow(String table) throws Exception {
        // INSERT INTO table (COLUMN_0, COLUMN_1) VALUES (100, 101)

        StringBuilder command = new StringBuilder();
        command.append("insert into " + table + " (");
        for (int i = 0; i < COLUMNS_PER_TABLE; i++) {
            command.append("column_" + i);
            if (i < COLUMNS_PER_TABLE - 1) {
                command.append(",");
            }
        }
        command.append(") VALUES (");
        for (int i = 0; i < COLUMNS_PER_TABLE; i++) {
            command.append(100 + i);
            if (i < COLUMNS_PER_TABLE - 1) {
                command.append(",");
            }
        }
        command.append(")");

        execute(command.toString());
    }

    private void dropTables() throws Exception {
        for (int i = 0; i < TABLE_COUNT; ++i) {
            execute("drop table some_table_" + i + "\n");
        }
        assertTableCount(0);
    }
    
    public void testStringBuilder() throws Exception {
//        long start = System.currentTimeMillis();
        int appendCount = TABLE_COUNT * COLUMNS_PER_TABLE * 10;
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < appendCount; i++) {
            builder.append("string foo " + i);
        }
        assertTrue(builder.toString(), builder.length() > 100);
//        long end = System.currentTimeMillis();
//        System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");
    }

}
