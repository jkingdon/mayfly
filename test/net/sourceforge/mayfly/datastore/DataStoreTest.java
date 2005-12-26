package net.sourceforge.mayfly.datastore;

import junit.framework.*;

import java.util.*;

public class DataStoreTest extends TestCase {
    
    public void testAddRowWithSchemaAndColumnNames() throws Exception {
        DataStore store = new DataStore()
            .addSchema("mars", new Schema().createTable("foo", Collections.singletonList("x")));
        DataStore newStore = store.addRow(
            "mars", "foo", Collections.singletonList("x"), Collections.singletonList(new Long(5)));
        assertEquals(1, newStore.table("mars", "foo").rowCount());
    }

    public void testAddRowWithSchemaNoColumnNames() throws Exception {
        DataStore store = new DataStore()
            .addSchema("mars", new Schema().createTable("foo", Collections.singletonList("x")));
        DataStore newStore = store.addRow(
            "mars", "foo", Collections.singletonList(new Long(5)));
        assertEquals(1, newStore.table("mars", "foo").rowCount());
    }

}
