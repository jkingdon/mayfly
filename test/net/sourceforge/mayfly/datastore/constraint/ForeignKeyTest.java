package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.command.InsertTable;
import net.sourceforge.mayfly.util.ImmutableList;

public class ForeignKeyTest extends TestCase {

    public void testCheckInsert() throws Exception {
        InsertTable barTable = new InsertTable(DataStore.ANONYMOUS_SCHEMA_NAME, "bar");
        ForeignKey key = new ForeignKey(DataStore.ANONYMOUS_SCHEMA_NAME, "foo", "bar_id", barTable, "id");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", ImmutableList.singleton("bar_id"))

                    .createTable("bar", ImmutableList.singleton("id"))
                    .addRow("bar", ImmutableList.singleton("id"), ImmutableList.singleton(new LongCell(5)))
            );
        key.checkInsert(store, DataStore.ANONYMOUS_SCHEMA_NAME, "foo",
            store.table("foo").columns(), ImmutableList.singleton(new LongCell(5)));

        try {
            key.checkInsert(store, DataStore.ANONYMOUS_SCHEMA_NAME, "foo",
                store.table("foo").columns(), ImmutableList.singleton(new LongCell(55)));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("foreign key violation: bar has no id 55", e.getMessage());
        }
    }

    public void testCheckDelete() throws Exception {
        InsertTable barTable = new InsertTable(DataStore.ANONYMOUS_SCHEMA_NAME, "bar");
        ForeignKey key = new ForeignKey(DataStore.ANONYMOUS_SCHEMA_NAME, "foo", "bar_id", barTable, "id");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", ImmutableList.singleton("bar_id"))
                    .addRow("foo", ImmutableList.singleton("bar_id"), ImmutableList.singleton(new LongCell(5)))

                    .createTable("bar", ImmutableList.singleton("id"))
                    .addRow("bar", ImmutableList.singleton("id"), ImmutableList.singleton(new LongCell(5)))
                    .addRow("bar", ImmutableList.singleton("id"), ImmutableList.singleton(new LongCell(6)))
            );
        Row sixRow = new TupleBuilder().appendColumnCell("id", new LongCell(6)).asRow();
        key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, "bar", sixRow);

        Row fiveRow = new TupleBuilder().appendColumnCell("id", new LongCell(5)).asRow();
        try {
            key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, "bar", fiveRow);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("foreign key violation: table foo refers to id 5 in bar", e.getMessage());
        }
        
        key.checkDelete(store, "another_schema", "bar", fiveRow);
        key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, "another_table", fiveRow);
    }

}
