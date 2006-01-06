package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;

public class WhatTest extends TestCase {

    public void testSelected() throws Exception {
        What original = new What()
            .add(new SingleColumn("a"))
            .add(new AllColumnsFromTable("foo"))
            .add(new SingleColumn("bar", "b"));
        Row dummyRow = new Row(new TupleBuilder()
            .append(new TupleElement(new Column("bar", "a"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("bar", "b"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("foo", "x"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("foo", "y"), NullCell.INSTANCE))
        );
        
        What expected = new What()
            .add(new SingleColumn("a")) // Or "bar", "a"
            .add(new SingleColumn("foo", "x"))
            .add(new SingleColumn("foo", "y"))
            .add(new SingleColumn("bar", "b"));

        assertEquals(expected, original.selected(dummyRow));
    }
    
    public void testSelectedDegenerateCase() throws Exception {
        What original = new What().add(new MathematicalInt(7));
        What expected = new What().add(new MathematicalInt(7));
        assertEquals(expected, original.selected(null));
    }
    
    public void testSelectedAll() throws Exception {
        What original = new What()
            .add(new All());
        Row dummyRow = new Row(new TupleBuilder()
            .append(new TupleElement(new Column("bar", "a"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("bar", "b"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("foo", "x"), NullCell.INSTANCE))
            .append(new TupleElement(new Column("foo", "y"), NullCell.INSTANCE))
        );
        
        What expected = new What()
            .add(new SingleColumn("bar", "a"))
            .add(new SingleColumn("bar", "b"))
            .add(new SingleColumn("foo", "x"))
            .add(new SingleColumn("foo", "y"));
    
        assertEquals(expected, original.selected(dummyRow));
    }

}
