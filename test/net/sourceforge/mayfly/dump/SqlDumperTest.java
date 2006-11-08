package net.sourceforge.mayfly.dump;

import junit.framework.TestCase;

import java.io.StringWriter;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.datastore.DataStore;

public class SqlDumperTest extends TestCase {
    
    private Database database;

    protected void setUp() throws Exception {
        database = new Database();
    }

    public void testEmpty() throws Exception {
        assertEquals("", new SqlDumper().dump(new Database().dataStore()));
    }
    
    public void testWriter() throws Exception {
        StringWriter out = new StringWriter();
        new SqlDumper().dump(new DataStore(), out);
        assertEquals("", out.toString());
    }

    public void testTable() throws Exception {
        database.execute("create table foo(a integer)");
        assertEquals("CREATE TABLE foo(\n  a INTEGER\n);\n\n", 
            dump());
    }

    public void testTwoColumns() throws Exception {
        database.execute("create table foo(a integer, B Integer)");
        assertEquals("CREATE TABLE foo(\n" +
                "  a INTEGER,\n" +
                "  B INTEGER\n" +
                ");\n\n", 
            dump());
    }
    
    public void testTwoTables() throws Exception {
        database.execute("create table foo(a integer)");
        database.execute("create table bar(b integer)");
        assertEquals(
            "CREATE TABLE foo(\n" +
            "  a INTEGER\n" +
            ");\n" + 
            "\n" +
            "CREATE TABLE bar(\n" +
            "  b INTEGER\n" +
            ");\n\n", 
            dump());
    }
    
    public void testDataTypes() throws Exception {
        database.execute("create table Foo(" +
            "b varchar ( 0243 ) ," +
            "c timestamp," +
            "d date," +
            "e text," +
            "f decimal ( 7 , 5 ), " +
            "g   blob ( 32800)," +
            "h blob" +
            ")");
        assertEquals(
            "CREATE TABLE Foo(\n" +
            "  b VARCHAR(243),\n" +
            "  c TIMESTAMP,\n" +
            "  d DATE,\n" +
            "  e TEXT,\n" +
            "  f DECIMAL(7,5),\n" +
            "  g BLOB(32800),\n" +
            "  h BLOB\n" +
            ");\n\n", 
            dump()
        );
    }

    public void testIntegerDataTypes() throws Exception {
        database.execute("create table Foo(" +
            "a integer," +
            "b int ," +
            "c tinyint," +
            "d smallint," +
            "e bigint," +
            "f identity," +
            "g serial" +
            ")");
        assertEquals(
            "CREATE TABLE Foo(\n" +
            "  a INTEGER,\n" +
            // The prevailing concept here mostly seems to be to canonicalize.
            "  b INTEGER,\n" +
            "  c TINYINT,\n" +
            "  d SMALLINT,\n" +
            "  e BIGINT,\n" +
            // Probably should be f INTEGER AUTO_INCREMENT NOT NULL or some such
            "  f IDENTITY,\n" +
            "  g IDENTITY\n" +
            ");\n\n", 
            dump()
        );
    }
    
    public void testRow() throws Exception {
        database.execute("create table foo(a integer)");
        database.execute("insert into foo(a) values(5)");
        assertEquals("CREATE TABLE foo(\n  a INTEGER\n);\n\n" +
            "INSERT INTO foo(a) VALUES(5);\n\n",
            dump());
    }
    
    public void testSeveralColumns() throws Exception {
        database.execute("create table foo(a integer, b integer)");
        database.execute("insert into foo(a, b) values(5, 8)");
        assertEquals("CREATE TABLE foo(\n  a INTEGER,\n  b INTEGER\n);\n\n" +
            "INSERT INTO foo(a, b) VALUES(5, 8);\n\n",
            dump());
    }
    
    public void testSeveralRows() throws Exception {
        database.execute("create table foo(a integer)");
        database.execute("insert into foo(a) values(5)");
        database.execute("insert into foo(a) values(6)");
        assertEquals("CREATE TABLE foo(\n  a INTEGER\n);\n\n" +
            "INSERT INTO foo(a) VALUES(5);\n" +
            "INSERT INTO foo(a) VALUES(6);\n\n",
            dump());
    }
    
    public void testRowsForSeveralTables() throws Exception {
        database.execute("create table foo(a integer)");
        database.execute("create table empty(a integer)");
        database.execute("create table bar(a integer)");
        database.execute("insert into foo(a) values(5)");
        database.execute("insert into bar(a) values(51)");
        database.execute("insert into bar(a) values(52)");
        assertEquals(
            "CREATE TABLE foo(\n  a INTEGER\n);\n\n" +
            "CREATE TABLE empty(\n  a INTEGER\n);\n\n" +
            "CREATE TABLE bar(\n  a INTEGER\n);\n" +
            "\n" +
            "INSERT INTO foo(a) VALUES(5);\n" +
            "\n" +
            "INSERT INTO bar(a) VALUES(51);\n" +
            "INSERT INTO bar(a) VALUES(52);\n" +
            "\n",
            dump());
    }
    
    // null and default values
    
    // constraints
    
    // round-trip test

    private String dump() {
        return new SqlDumper().dump(database.dataStore());
    }
    
}
