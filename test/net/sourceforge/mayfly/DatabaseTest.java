package net.sourceforge.mayfly;

import junit.framework.TestCase;

import net.sourceforge.mayfly.acceptance.SqlTestCase;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.dump.SqlDumper;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class DatabaseTest extends TestCase {

    private Database database;

    public void setUp() throws Exception {
        database = new Database();
    }

    public void testCreateAndDrop() throws Exception {
        database.execute("CREATE TABLE FOO (A integer)");
        assertEquals(Collections.singleton("FOO"), database.tables());

        database.execute("DROP TABLE Foo");
        assertEquals(Collections.EMPTY_SET, database.tables());
    }
    
    public void testCreateWithOneColumn() throws Exception {
        database.execute("CREATE TABLE Foo (A integer)");
        assertEquals(Collections.singleton("Foo"), database.tables());
        assertEquals(Collections.singletonList("A"), database.columnNames("fOo"));
    }
    
    public void testInsert() throws Exception {
        database.execute("CREATE TABLE FOO (A integer)");
        assertEquals(0, database.rowCount("foo"));
        database.execute("INSERT INTO FOO (A) values (5)");
        assertEquals(1, database.rowCount("foo"));
    }
    
    public void testSnapshot() throws Exception {
        Database original = new Database();
        original.execute("create table foo (a integer)");
        original.execute("insert into foo(a) values(6)");
        DataStore dataStore = original.dataStore();

        Database snapshot = new Database(dataStore);
        snapshot.execute("insert into foo (a) values (70)");
        snapshot.execute("create table bar (b integer)");
        
        original.execute("create table foo2 (c integer)");

        assertEquals(new TreeSet(Arrays.asList(new String[] {"foo", "bar"})), snapshot.tables());
        assertEquals(2, snapshot.rowCount("Foo"));

        assertEquals(new TreeSet(Arrays.asList(new String[] {"foo", "foo2"})), original.tables());
        assertEquals(1, original.rowCount("Foo"));
    }
    
    public void testTables() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (x integer)");
        assertEquals(Collections.singleton("inAnonymousSchema"), database.tables());
        database.execute("set schema mars");
        assertEquals(Collections.singleton("foo"), database.tables());
    }
    
    public void testColumnNames() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (y integer)");
        assertEquals(Collections.singletonList("x"), database.columnNames("inAnonymousSchema"));
        database.execute("set schema mars");
        assertEquals(Collections.singletonList("y"), database.columnNames("foo"));
        
        try {
            database.columnNames("nosuch");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no table nosuch", e.getMessage());
        }
    }
    
    public void testQueryAndSchema() throws Exception {
        database.execute("create table inAnonymousSchema (x integer)");
        database.execute("create schema mars authorization dba create table foo (y integer)");
        String fooQuery = "select * from foo";
        try {
            database.query(fooQuery);
            fail();
        } catch (MayflyException e) {
            assertEquals("no table foo", e.getMessage());
        }
        database.execute("set schema mars");
        SqlTestCase.assertResultSet(new String[] { }, database.query(fooQuery));
    }
    
    public void testSchemas() throws Exception {
        database.execute("create schema MARS authorization dba create table foo (x integer)");
        database.execute("create schema Venus authorization dba create table foo (x integer)");
        Set expected = new TreeSet();
        expected.add("MARS");
        expected.add("Venus");
        assertEquals(expected, database.schemas());
    }
    
    public void testScript() throws Exception {
        Reader script = new StringReader(
            "create table foo (x integer); insert into foo(x) values(5)" +
            ";insert into foo(x)values(7)"
        );
        database.executeScript(script);
        SqlTestCase.assertResultSet(
            new String[] { "5", "7" },
            database.query("select x from foo")
        );
    }
    
    public void testScriptError() throws Exception {
        String command = "create table foo\n" +
            "   (x integer,\n" +
            "    y not)";
        Reader script = new StringReader(
            command + ";" +
            "create table otherTable(a integer);");
        try {
            database.executeScript(script);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("expected data type but got NOT", e.getMessage());
            assertEquals(3, e.startLineNumber());
            assertEquals(7, e.startColumn());
            assertEquals(3, e.endLineNumber());
            assertEquals(10, e.endColumn());
            assertEquals(command, e.failingCommand());
        }
    }
    
    public void testFailingCommandFromDatabase() throws Exception {
        database.execute("create table foo(x integer not null)");
        try {
            database.execute("insert into foo(x) values(null)");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("column x cannot be null", e.getMessage());
            assertEquals("insert into foo(x) values(null)", e.failingCommand());
        }
    }
    
    public void testRenameColumn() throws Exception {
        Database database = new Database();
        database.execute(
            "create table foo(a integer, b integer, c integer, " +
            "primary key(a, b))");
        database.execute("create index an_index on foo(c)");
        database.execute("alter table foo change column a aa integer");
        database.execute("alter table foo change column c cc integer");

        String dump = new SqlDumper().dump(database.dataStore());
        assertEquals("CREATE TABLE foo(\n" +
            "  aa INTEGER,\n" +
            "  b INTEGER,\n" +
            "  cc INTEGER,\n" +
            "  PRIMARY KEY(aa, b)\n" +
            ");\n" +
            "CREATE INDEX an_index ON foo(cc);\n\n",
            dump);
    }

    public void testRenameForeignKeyTarget() throws Exception {
        Database database = new Database();
        database.execute("create table foo(id integer primary key)");
        database.execute("create table bar(foo_id integer," +
            "foreign key(foo_id) references foo(id))");
        String rename = 
            "alter table foo change column id identifier integer";
        if (false) {
            database.execute(rename);
    
            String dump = new SqlDumper().dump(database.dataStore());
            assertEquals("CREATE TABLE foo(\n" +
                "  identifier INTEGER,\n" +
                "  PRIMARY KEY(identifier)\n" +
                ");\n\n" +
                "CREATE TABLE bar(\n" +
                "  foo_id INTEGER,\n" +
                "  FOREIGN KEY(foo_id) REFERENCES foo(identifier)\n" +
                ");\n\n",
                dump);
        }
        else {
            /* We wish we were able to just rename the identifier
               in the foreign key.  But at a minimum, throw an
               exception rather than leave the foreign key dangling
               and pointing at a nonexistent column. */
            try {
                database.execute(rename);
                fail();
            }
            catch (MayflyException e) {
                assertEquals(
                    "the column id is referenced by " +
                    "a foreign key in table bar, column foo_id", 
                    e.getMessage());
            }
        }
    }

}
