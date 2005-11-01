package net.sourceforge.mayfly.datastore;

import junit.framework.*;
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
