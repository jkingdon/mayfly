package net.sourceforge.mayfly.dump;

import junit.framework.TestCase;

import net.sourceforge.mayfly.Database;

import java.io.IOException;
import java.io.StringWriter;

public class DumperTest extends TestCase {
    
    private Database database;

    public void setUp() {
        database = new Database();
    }

    public void testSimple() throws Exception {
        database.execute("create table foo (a integer)");
        database.execute("insert into foo (a) values (5)");
        
        expectDump(
            "<foo>\n" +
            "  <a>5</a>\n" +
            "</foo>\n\n"
        );
    }
    
    public void testMultipleColumns() throws Exception {
        database.execute("create table foo (a integer, b integer)");
        database.execute("insert into foo (a, b) values (5, 8)");
        
        expectDump(
            "<foo>\n" +
            "  <a>5</a>\n" +
            "  <b>8</b>\n" +
            "</foo>\n\n"
        );
    }
    
    public void testMultipleRows() throws Exception {
        database.execute("create table foo (a integer)");
        database.execute("insert into foo (a) values (5)");
        database.execute("insert into foo (a) values (7)");
        
        String fiveRow = 
            "<foo>\n" +
            "  <a>5</a>\n" +
            "</foo>\n\n";
        String sevenRow = 
            "<foo>\n" +
            "  <a>7</a>\n" +
            "</foo>\n\n";

        String actual = dump();
        if (actual.equals(fiveRow + sevenRow)) {
            // pass
        } else if (actual.equals(sevenRow + fiveRow)) {
            // pass
        } else {
            fail("Didn't get expected dump; got " + actual);
        }
    }
    
    public void xtestQueryResult() throws Exception {
        // Haven't implemented this one yet.
        database.execute("create table foo (a integer, b integer)");
        database.execute("insert into foo (a, b) values (5, 25)");
        database.execute("insert into foo (a, b) values (6, 36)");
        
        assertEquals(
            "<result>\n" +
            "  <b>25</b>\n" +
            "</result>\n\n",
            dumpQuery("select b from foo where a = 5")
        );
    }

    public String dumpQuery(String sql) throws Exception {
        StringWriter out = new StringWriter();
        new Dumper().dump(database.dataStore(), sql, out);
        return out.toString();
    }

    private void expectDump(String expected) throws IOException {
        assertEquals(expected, dump());
    }

    private String dump() throws IOException {
        StringWriter out = new StringWriter();
        new Dumper().dump(database.dataStore(), out);
        return out.toString();
    }
    
}
