package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
        );

        assertEquals(new StringCell("1"), new SingleColumn("colA").evaluate(row));
        assertEquals(new StringCell("2"), new SingleColumn("colB").evaluate(row));
    }

    public void testProcess_Simple() throws Exception {
        Tuple original = new TupleBuilder()
            .append(new TupleElement(new Column(new TableIdentifier("foo"), "colA"), new StringCell("a")))
            .append(new TupleElement(new Column(new TableIdentifier("bar"), "colB"), new StringCell("b")))
            .asTuple();

        assertEquals(
            new Tuple(new TupleElement(new Column(new TableIdentifier("foo"), "colA"), new StringCell("a"))),
            new SingleColumn("colA").process(original, new M())
        );
    }

    //TODO: name sucks
    public void testProcess_TableAlias() throws Exception {
        M tableAliases =
            new M()
                .entry("f", "fOO")
                .entry("B", "bar");

        Tuple original = new TupleBuilder()
            .append(new TupleElement(new Column(new TableIdentifier("foo"), "colA"), new StringCell("a")))
            .append(new TupleElement(new Column(new TableIdentifier("bar"), "colB"), new StringCell("b")))
            .asTuple();

        assertEquals(
            new Tuple(new TupleElement(new Column(new TableIdentifier("foo"), "colA"), new StringCell("a"))),
            new SingleColumn("F", "cola")
                .process(original, tableAliases)
        );

        assertEquals(
            new Tuple(new TupleElement(new Column(new TableIdentifier("bar"), "colB"), new StringCell("b"))),
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
