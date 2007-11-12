package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.dump.SqlDumper;

public class CreateTableTest extends TestCase {
    
    public void testAssignForeignKeyNames() throws Exception {
        Database database = new Database();
        database.execute("create table refd(id integer primary key)");
        database.execute("create table foo(a integer, b integer," +
            "foreign key(a) references refd(id)," +
            "foreign key(b) references refd(id)" +
            ")");
        assertEquals(
            "CREATE TABLE refd(\n" +
            "  id INTEGER,\n" +
            "  PRIMARY KEY(id)\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE foo(\n" +
            "  a INTEGER,\n" +
            "  b INTEGER,\n" +
            "  CONSTRAINT foo_ibfk_1 FOREIGN KEY(a) REFERENCES refd(id),\n" +
            "  CONSTRAINT foo_ibfk_2 FOREIGN KEY(b) REFERENCES refd(id)\n" +
            ");\n" +
            "\n", 
            new SqlDumper().dump(database.dataStore()));
    }

}
