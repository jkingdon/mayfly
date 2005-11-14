package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;

public class WhatTest extends TestCase {
    public void testApplyWhat_Simple() throws Exception {
        Row original = new Row(
            new TupleBuilder()
                .append(new Tuple(new Column("colA"), new Cell("1")))
                .append(new Tuple(new Column("colB"), new Cell("2")))
        );

        Row expected = new Row(
            new TupleBuilder()
                .append(new Tuple(new Column("colB"), new Cell("2")))
        );

        assertEquals(expected,
                     new What()
                        .add(new SingleColumn("colB"))
                        .applyTo(original)
        );
    }
}