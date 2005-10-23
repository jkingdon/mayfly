package net.sourceforge.mayfly;

import junit.framework.*;

import net.sourceforge.mayfly.ldbc.*;

import java.sql.*;

public class PerformanceTest extends TestCase {
    
    private static final int COLUMNS_PER_TABLE = 15;
    private static final int TABLE_COUNT = 30;
    private static final int TABLES_TO_INSERT_INTO = 10;
    private static final int ROWS_TO_INSERT = 3;

    private Database database;
    private Connection connection;

    public void setUp() throws Exception {
//        database = new Database();
//        connection = database.openConnection();
        connection = MetaDataTest.openConnection();
    }

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
        //assertEquals(TABLE_COUNT, countTables());
    }

    private int countTables() {
        return database.tables().size();
    }
    
    private void execute(String sql) throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
//        CCJSqlParserManager parser = new CCJSqlParserManager();
//        parser.parse(new StringReader(sql));
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
        //assertEquals(0, countTables());
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
