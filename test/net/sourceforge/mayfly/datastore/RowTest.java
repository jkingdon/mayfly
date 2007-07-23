package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;
import junitx.framework.StringAssert;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RowTest extends TestCase {

    public void testCell() throws Exception {
        Row row =
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
                .appendColumnCellContents("colC", "3")
                .asRow();

        MayflyAssert.assertString("2", row.cell("colB"));
        MayflyAssert.assertString("2", row.cell("COLb"));

        try {
            row.cell("colD");
            fail();
        } catch (MayflyException e) {
            assertEquals("no column colD", e.getMessage());
        }
    }

    public void testDropColumn() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 7)
            .appendColumnCellContents("b", 9)
            .asRow();
        
        Row newRow = row.dropColumn("B");
        
        assertEquals(1, newRow.columnCount());
        MayflyAssert.assertLong(7, newRow.cell("A"));
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
    
    public void testRenameColumn() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 5)
            .appendColumnCellContents("c", 7)
            .asRow();
        
        Row newRow = row.renameColumn("C", "b");
        
        assertEquals(2, newRow.columnCount());
        MayflyAssert.assertLong(5, newRow.cell("A"));
        MayflyAssert.assertLong(7, newRow.cell("B"));
    }
    
    public void testRenameColumnNonexistent() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 7)
            .asRow();
        
        try {
            row.renameColumn("AA", "b");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("no column AA", e.getMessage());
        }
    }
    
    public void testRenameToConflict() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCellContents("a", 5)
            .appendColumnCellContents("c", 7)
            .asRow();

        try {
            row.renameColumn("C", "A");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate column A", e.getMessage());
        }
    }
    
    public void testAdd() throws Exception {
        Row row = new Row().addColumn(new Column("a"));
        assertEquals(1, row.columnCount());
        assertEquals(NullCell.INSTANCE, row.cell("A"));
    }
    
    public void testAddDuplicate() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCell("a", new StringCell("hi"))
            .asRow();
        
        try {
            row.addColumn(new Column("A"));
            fail();
        }
        catch (MayflyException e) {
            assertEquals("duplicate column A", e.getMessage());
        }
    }
    
    public void testToString() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCell("a", new StringCell("hi"))
            .appendColumnCell("b", new LongCell(777))
            .appendColumnCell("c", NullCell.INSTANCE)
            .appendColumnCell("d", new BinaryCell((byte)7))
            .asRow();
        String debug = row.toString();
        StringAssert.assertStartsWith("Row(", debug);
        StringAssert.assertContains("a=string 'hi'", debug);
        StringAssert.assertContains("b=number 777", debug);
        StringAssert.assertContains("c=null", debug);
        StringAssert.assertContains("d=binary data", debug);
        StringAssert.assertEndsWith(")", debug);
    }
    
    public void testColumnNames() throws Exception {
        Row row = new TupleBuilder()
            .appendColumnCell("a", new StringCell("hi"))
            .appendColumnCell("b", new LongCell(777))
            .appendColumnCell("c", NullCell.INSTANCE)
            .appendColumnCell("d", new BinaryCell((byte)7))
            .asRow();
        Iterator names = row.columnNames();
        Set results = new HashSet();
        results.add(names.next());
        results.add(names.next());
        results.add(names.next());
        results.add(names.next());
        assertFalse(names.hasNext());
        
        assertEquals(
            new HashSet(Arrays.asList(new CaseInsensitiveString[] {
                new CaseInsensitiveString("d"),
                new CaseInsensitiveString("c"),
                new CaseInsensitiveString("b"),
                new CaseInsensitiveString("a")
            } )),
            results
        );
    }
    
}
