package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new Tuples()
                .appendColumnCellTuple("colA", "1")
                .appendColumnCellTuple("colB", "2")
        );

        assertEquals(new Cell("1"), new SingleColumn("colA").transform(row));
        assertEquals(new Cell("2"), new SingleColumn("colB").transform(row));
    }

    public void testProcess_Simple() throws Exception {
        Tuples original = new Tuples()
            .append(new Tuple(new Column(new TableIdentifier("foo"), "colA"), new Cell("a")))
            .append(new Tuple(new Column(new TableIdentifier("bar"), "colB"), new Cell("b")));

        assertEquals(
            new Tuples()
                .append(new Tuple(new Column(new TableIdentifier("foo"), "colA"), new Cell("a"))),
            new SingleColumn("colA").process(original, new M())
        );
    }

    //TODO: name sucks
    public void testProcess_TableAlias() throws Exception {
        M tableAliases =
            new M()
                .entry("f", "fOO")
                .entry("B", "bar");

        Tuples original = new Tuples()
            .append(new Tuple(new Column(new TableIdentifier("foo"), "colA"), new Cell("a")))
            .append(new Tuple(new Column(new TableIdentifier("bar"), "colB"), new Cell("b")));

        assertEquals(
            new Tuples()
                .append(new Tuple(new Column(new TableIdentifier("foo"), "colA"), new Cell("a"))),
            new SingleColumn("F", "cola")
                .process(original, tableAliases)
        );

        assertEquals(
            new Tuples()
                .append(new Tuple(new Column(new TableIdentifier("bar"), "colB"), new Cell("b"))),
            new SingleColumn("b", "COLB")
                .process(original, tableAliases)
        );
    }

    public void testProcess_FailureScenarios() throws Exception {
        //cases:
        //- ambiguous col.
        //- col not found
        //- tbl alias unknown
        //- tbl not found
    }



}
