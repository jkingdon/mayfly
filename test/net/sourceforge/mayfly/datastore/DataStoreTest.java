package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.evaluation.ValueList;

import java.util.Collections;

public class DataStoreTest extends TestCase {
    
    public void testAddRowWithSchemaAndColumnNames() throws Exception {
        DataStore store = new DataStore()
            .addSchema("mars", new Schema().createTable("foo", Collections.singletonList("x")));
        DataStore newStore = store.addRow(
            new TableReference("mars", "foo"), 
            Collections.singletonList("x"), ValueList.singleton(new LongCell(5)),
            new NullChecker());
        assertEquals(1, newStore.table("mars", "foo").rowCount());
    }

    public void testAddRowWithSchemaNoColumnNames() throws Exception {
        DataStore store = new DataStore()
            .addSchema("mars", new Schema().createTable("foo", Collections.singletonList("x")));
        DataStore newStore = store.addRow(
            new TableReference("mars", "foo"), 
            ValueList.singleton(new LongCell(5)), new NullChecker());
        assertEquals(1, newStore.table("mars", "foo").rowCount());
    }

}
