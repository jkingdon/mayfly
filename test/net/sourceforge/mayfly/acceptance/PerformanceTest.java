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
        // At the moment, I'm getting 360 ms for hypersonic;
        // 111 s for MySQL (4.1.21 on Windows); 
        // 953 ms for Mayfly.
        // (try MySQL 5.x on Linux; I think it is faster).
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
        // Really slow for mayfly (15x slower than without unique
        // on 10,000 rows),
        // once we get to more than about 1000 rows (or so).
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
    
    public void xtestDozensOfColumns() throws Exception {
        // 1000 rows: 188 ms hypersonic, 422 ms mayfly.
        // 10000 rows: 0.891 s hypersonic, 3.14 s mayfly
        execute("create table foo(a integer, b integer, c integer, " +
            "d integer, e integer, f integer, g integer, h integer," +
            "i integer, j integer, k integer, l integer, m integer," +
            "n integer, o integer, p integer, q integer, r integer," +
            "s integer, t integer, u integer, v integer, w integer," +
            "x integer, y integer, z integer," +
            "alpha varchar(50), beta varchar(50), gamma varchar(50)," +
            "delta varchar(50), epsilon varchar(50), zeta varchar(50), " +
            "eta varchar(50), theta varchar(50), " +
            "iota varchar(50), kappa varchar(50), lambda varchar(50), " +
            "mu varchar(50), nu varchar(50), xi varchar(50), " +
            "omicron varchar(50), " +
            "pi varchar(50), rho varchar(50), sigma varchar(50), " +
            "tau varchar(50), upsilon varchar(50), phi varchar(50), " +
            "chi varchar(50), psi varchar(50), omega varchar(50) )");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            execute("insert into foo(omega, chi, upsilon, sigma, pi," +
                "xi, mu, kappa, theta, zeta, delta, beta," +
                "z, x, v, t, r, p, n, l, j, h, f, d, b) " +
                "values ('xcvz', 'jkweflwe', 'uiiwecjewc', 'jksjfkasdf'," +
                "'skdfskjdf'," +
                "'jskdlkjsdf', 'wjef', 'ijeif', 'jksldkf'," +
                "'jkwefjk', 'jweifje', 'jiowejif'," +
                "789, 6897, 34, 235, 2783, 2378, 1234, 7474, 8293," +
                "235, 0, 1, " + i + ")");
        }
        long end = System.currentTimeMillis();
        if (true) {
            System.out.println(
                "Elapsed time = " + (end - start) / 1000.0 + " s");
        }
    }
    
    public void xtestQueries() throws Exception {
        // Goal here is to test parsing, per-command overhead, etc, not the time evaluating
        // complicated joins and wheres.
        // hypersonic 125 ms, mayfly 125 ms
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
