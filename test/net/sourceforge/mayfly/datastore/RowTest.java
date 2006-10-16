package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;

public class RowTest extends TestCase {

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
                .appendColumnCellContents("foo", "colA", "1")
                .appendColumnCellContents("foo", "colB", "2")
        );

        Row row2 = new Row(
            new TupleBuilder()
                .appendColumnCellContents("bar", "colC", "3")
        );

        Row expected = new Row(
            new TupleBuilder()
                .appendColumnCellContents("foo", "colA", "1")
                .appendColumnCellContents("foo", "colB", "2")
                .appendColumnCellContents("bar", "colC", "3")
        );

        assertEquals(expected, row1.plus(row2));
        assertEquals(expected, row1.combine(row2));
    }
    
    public void testConflictingAliases() throws Exception {
        Row row1 = new Row(
            new TupleBuilder()
                .appendColumnCellContents("table1", "colA", "1")
                .appendColumnCellContents("table2", "colB", "2")
        );

        Row row2 = new Row(
            new TupleBuilder()
                .appendColumnCellContents("TABLE1", "colC", "3")
        );

        try {
            row1.combine(row2);
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate table name or alias TABLE1", e.getMessage());
        }
    }
    
   /**
     * Replaced by {@link net.sourceforge.mayfly.evaluation.ResultRowTest#testFindColumn()}
     */
    public void testFindColumn() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .append(new Column("x"), new LongCell(5))
                .append(new Column("foo", "z"), new StringCell("Chicago"))
                .append(new Column("bar", "z"), new StringCell("Chicago"))
                .append(new Column("y"), new StringCell("Chicago"))
        );
        
        assertEquals(new Column("y"), row.findColumn("y"));
        assertEquals(new Column("bar", "z"), row.findColumn("bar", "z"));
    }

    public void testHeaderIs() throws Exception {
        assertTrue(
            new Row.HeaderIs(new Column("colA"))
                .evaluate(new TupleElement(new Column("colA"), new StringCell("a")))
        );
        assertFalse(
            new Row.HeaderIs(new Column("colB"))
                .evaluate(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }

    public void testGetHeader() throws Exception {
        assertEquals(
            new Column("colA"),
            new Row.GetHeader().transform(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }

    public void testGetCell() throws Exception {
        assertEquals(
            new StringCell("a"), 
            new Row.GetCell().transform(new TupleElement(new Column("colA"), new StringCell("a")))
        );
    }
    
    public void testDropColumn() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 7)
            .appendColumnCellContents("b", 9)
            .asRow();
        
        Row newRow = row.dropColumn("B");
        
        assertEquals(1, newRow.size());
        LongCell cell = (LongCell) newRow.cell(new Column("A"));
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
    
}
