package net.sourceforge.mayfly.evaluation.command;

import junit.framework.TestCase;

import net.sourceforge.mayfly.Database;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class ConstraintsBuilderTest extends TestCase {
    
    public void testFindNextNumber() throws Exception {
        Database database = new Database();
        database.execute("create table bar(id integer primary key)");
        database.execute(
            "create table foo(" +
                "a integer, b integer, c integer, d integer," +
                "e integer, f integer, " +
                "constraint foo_ibfk_22 foreign key(a) references bar(id)," +
                "constraint foo_ibfk_not_a_number foreign key(b) references bar(id)," +
                "constraint other_ibfk_23 foreign key(c) references bar(id)," +
                "constraint foo_ibfk_11 foreign key(d) references bar(id), " +
                "constraint foo_ibfk_ foreign key(e) references bar(id), " +
                "constraint foo_other_string foreign key(f) references bar(id)" +
                ")");
        int nextNumber = 
            ConstraintsBuilder.findNextForeignKeyNumber(
                database.dataStore(), 
                new TableReference(DataStore.ANONYMOUS_SCHEMA_NAME, "foo"));
        assertEquals(23, nextNumber);
    }

}
