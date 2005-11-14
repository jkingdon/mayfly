package net.sourceforge.mayfly.datastore;

import junit.framework.*;
import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.what.*;

public class RowTest extends TestCase {


    //TODO: need to establish column order somehow

    public void testCell() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellTuple("colA", "1")
                .appendColumnCellTuple("colB", "2")
                .appendColumnCellTuple("colC", "3")
        );

        assertEquals(new Cell("2"), row.cell(new Column("colB")));
        assertEquals(new Cell("2"), row.cell(new Column("COLb")));
    }

    public void testCellByAliasAndColumn() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellTuple("Foo", "colA", "1")
                .appendColumnCellTuple("Foo", "colB", "2")
                .appendColumnCellTuple("Bar", "colA", "3")
        );

        assertEquals(new Cell("2"), row.cell(null, "colB"));
        assertEquals(new Cell("2"), row.cell(null, "COLb"));

        try {
            row.cell(null, "colA");
            fail();
        } catch (MayflyException e) {
            assertEquals("ambiguous column colA", e.getMessage());
        }

        assertEquals(new Cell("1"), row.cell("Foo", "colA"));
        assertEquals(new Cell("3"), row.cell("Bar", "colA"));
        assertEquals(new Cell("3"), row.cell("Bar", "COLa"));

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
                .appendColumnCellTuple("colA", "1")
                .appendColumnCellTuple("colB", "2")
        );

        Row row2 = new Row(
            new TupleBuilder()
                .appendColumnCellTuple("colC", "3")
        );

        Row expected = new Row(
            new TupleBuilder()
                .appendColumnCellTuple("colA", "1")
                .appendColumnCellTuple("colB", "2")
                .appendColumnCellTuple("colC", "3")
        );

        assertEquals(expected, row1.plus(row2));
    }

}
