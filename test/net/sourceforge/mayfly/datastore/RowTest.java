package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.evaluation.expression.PositionalHeader;

public class RowTest extends TestCase {


    //TODO: need to establish column order somehow

    public void testCell() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
                .appendColumnCellContents("colC", "3")
        );

        assertEquals(new StringCell("2"), row.cell(row.findColumn("colB")));
        assertEquals(new StringCell("2"), row.cell(row.findColumn("COLb")));
    }

    public void testCellByAliasAndColumn() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("Foo", "colA", "1")
                .appendColumnCellContents("Foo", "colB", "2")
                .appendColumnCellContents("Bar", "colA", "3")
        );

        assertEquals(new StringCell("2"), row.cell(null, "colB"));
        assertEquals(new StringCell("2"), row.cell(null, "COLb"));

        try {
            row.cell(null, "colA");
            fail();
        } catch (MayflyException e) {
            assertEquals("ambiguous column colA", e.getMessage());
        }

        assertEquals(new StringCell("1"), row.cell("Foo", "colA"));
        assertEquals(new StringCell("3"), row.cell("Bar", "colA"));
        assertEquals(new StringCell("3"), row.cell("Bar", "COLa"));

        try {
            row.cell("Bar", "colB");
            fail();
        } catch (MayflyException e) {
            assertEquals("no column Bar.colB", e.getMessage());
        }

        try {
            row.cell(null, "colC");
            fail();
        } catch (MayflyException e) {
            assertEquals("no column colC", e.getMessage());
        }
    }

    public void testPlus() throws Exception {
        Row row1 = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
        );

        Row row2 = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colC", "3")
        );

        Row expected = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
                .appendColumnCellContents("colC", "3")
        );

        assertEquals(expected, row1.plus(row2));
    }
    
    public void testByPosition() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .append(new Column("x"), new LongCell(5))
                .append(new PositionalHeader(43), new StringCell("hi"))
                .append(new Column("y"), new StringCell("Chicago"))
                .append(new PositionalHeader(7), new LongCell(77))
        );
        
        assertEquals(new StringCell("hi"), row.byPosition(43));
        assertEquals(new LongCell(77), row.byPosition(7));
        try {
            row.byPosition(3);
            fail();
        } catch (MayflyException e) {
            // Would be nice if this was a MayflyInternalException, but I guess
            // it isn't really important.
            assertEquals("positional header #3 not found", e.getMessage());
        }
    }
    
    public void testFindColumn() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .append(new Column("x"), new LongCell(5))
                .append(new PositionalHeader(43), new StringCell("hi"))
                .append(new Column("foo", "z"), new StringCell("Chicago"))
                .append(new Column("bar", "z"), new StringCell("Chicago"))
                .append(new Column("y"), new StringCell("Chicago"))
                .append(new PositionalHeader(7), new LongCell(77))
        );
        
        assertEquals(new Column("y"), row.findColumn("y"));
        assertEquals(new Column("bar", "z"), row.findColumn("bar", "z"));
    }

}
