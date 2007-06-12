package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.Schema;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class ForeignKeyTest extends TestCase {

    public void testCheckInsert() throws Exception {
        ForeignKey key = new ForeignKey("foo", "bar_id", "bar", "id");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", ImmutableList.singleton("bar_id"))

                    .createTable("bar", ImmutableList.singleton("id"))
                    .addRow("bar", ImmutableList.singleton("id"), ValueList.singleton(new LongCell(5)))
            );
        key.checkInsert(store, DataStore.ANONYMOUS_SCHEMA_NAME, "foo",
            singleColumnRow("bar_id", new LongCell(5)),
            Location.UNKNOWN
        );

        try {
            key.checkInsert(store, DataStore.ANONYMOUS_SCHEMA_NAME, "foo",
                singleColumnRow("bar_id", new LongCell(55)),
                Location.UNKNOWN
            );
            fail();
        }
        catch (MayflyException e) {
            assertEquals("foreign key violation: bar has no id 55", e.getMessage());
        }
    }

    private static Row singleColumnRow(String columnName, LongCell cell) {
        return new TupleBuilder()
            .appendColumnCell(columnName, cell).asRow();
    }

    public void testCheckDelete() throws Exception {
        ForeignKey key = new ForeignKey("foo", "bar_id", "bar", "id");
        DataStore store =
            new DataStore(
                new Schema()
                    .createTable("foo", ImmutableList.singleton("bar_id"))
                    .addRow("foo", ImmutableList.singleton("bar_id"), ValueList.singleton(new LongCell(5)))

                    .createTable("bar", ImmutableList.singleton("id"))
                    .addRow("bar", ImmutableList.singleton("id"), ValueList.singleton(new LongCell(5)))
                    .addRow("bar", ImmutableList.singleton("id"), ValueList.singleton(new LongCell(6)))
            );
        Row sixRow = singleColumnRow("id", new LongCell(6));
        key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, "bar", sixRow, null);

        Row fiveRow = singleColumnRow("id", new LongCell(5));
        try {
            key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, "bar", fiveRow, null);
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "foreign key violation: table foo refers to id 5 in bar", 
                e.getMessage());
        }
        
        key.checkDelete(store, "another_schema", "bar", fiveRow, null);
        key.checkDelete(store, DataStore.ANONYMOUS_SCHEMA_NAME, 
            "another_table", fiveRow, null);
    }
    
    public void testCanBeTarget() throws Exception {
        ForeignKey key = new ForeignKey(
            "bar", "foo_id", "foo", "id");
        assertTrue(key.canBeTargetOfForeignKey("foo_id"));
        assertFalse(key.canBeTargetOfForeignKey("id"));
        assertFalse(key.canBeTargetOfForeignKey("x"));
    }
    
    public void testRename() throws Exception {
        ForeignKey key = new ForeignKey(
            "bar", "fu_id", "foo", "id");
        ForeignKey result = (ForeignKey) key.renameColumn("fu_id", "foo_id");
        assertEquals("bar", result.referencerTable);
        assertEquals("foo_id", result.referencerColumn);
        assertEquals("foo", result.targetTable.tableName());
        assertEquals("id", result.targetColumn);
    }

}
