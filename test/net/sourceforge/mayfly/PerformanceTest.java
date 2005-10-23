package net.sourceforge.mayfly;

import net.sourceforge.mayfly.ldbc.*;

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
        System.out.println("Elapsed time = " + (end - start) / 1000.0 + " s");
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
