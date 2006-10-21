package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.util.ImmutableList;

public class RowTest extends TestCase {

    public void testCell() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
                .appendColumnCellContents("colC", "3")
        );

        assertEquals(new StringCell("2"), row.cell("colB"));
        assertEquals(new StringCell("2"), row.cell("COLb"));

        try {
            row.cell("colD");
            fail();
        } catch (MayflyException e) {
            assertEquals("no column colD", e.getMessage());
        }
    }

    public void testDuplicateColumnNames() throws Exception {
        TupleElement one = new TupleElement("colA", new LongCell(5));
        TupleElement two = new TupleElement("ColA", new LongCell(7));
        ImmutableList list = ImmutableList.fromArray(
            new TupleElement[] { one, two });
        try {
            new Row(list);
            fail();
        }
        catch (MayflyInternalException e) {
            assertEquals("duplicate column ColA", e.getMessage());
        }
    }

    public void testDropColumn() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 7)
            .appendColumnCellContents("b", 9)
            .asRow();
        
        Row newRow = row.dropColumn("B");
        
        assertEquals(1, newRow.columnCount());
        LongCell cell = (LongCell) newRow.cell("A");
        assertEquals(7, cell.asLong());
    }
    
    public void testDropColumnNonexistent() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 7)
            .asRow();
        
        try {
            row.dropColumn("B");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no column B", e.getMessage());
        }
    }
    
    public void testToString() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCell("a", new StringCell("hi"))
            .appendColumnCell("b", new LongCell(777))
            .appendColumnCell("c", NullCell.INSTANCE)
            .appendColumnCell("d", new BinaryCell((byte)7))
            .asRow();
        assertEquals(
            "Row(a=string 'hi', b=number 777, c=null, d=binary data)",
            row.toString());
    }
    
}
