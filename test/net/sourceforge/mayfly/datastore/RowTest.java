package net.sourceforge.mayfly.datastore;

import junit.framework.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class RowTest extends TestCase {


    //TODO: need to establish column order somehow

    public void testCell() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .entry(new Column("colC"), new Cell("3"))
                .asImmutable()
        );

        assertEquals(new Cell("2"), row.cell(new Column("colB")));
        assertEquals(new Cell("2"), row.cell(new Column("COLb")));
    }

    public void testCellByAliasAndColumn() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("Foo", "colA"), new Cell("1"))
                .entry(new Column("Foo", "colB"), new Cell("2"))
                .entry(new Column("Bar", "colA"), new Cell("3"))
                .asImmutable()
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
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .asImmutable()
        );

        Row row2 = new Row(
            new M()
                .entry(new Column("colC"), new Cell("3"))
                .asImmutable()
        );

        Row expected = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .entry(new Column("colC"), new Cell("3"))
                .asImmutable()
        );


        assertEquals(expected, row1.plus(row2));
    }
}
