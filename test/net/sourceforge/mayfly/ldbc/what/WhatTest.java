package net.sourceforge.mayfly.ldbc.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.ldbc.where.literal.MathematicalInt;
import net.sourceforge.mayfly.util.MayflyAssert;

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
        Selected selected = original.selected(dummyRow);
        
        assertEquals(4, selected.size());
        MayflyAssert.assertColumn("bar", "a", (SingleColumn) selected.element(0)); // Or null, "a"
        MayflyAssert.assertColumn("foo", "x", (SingleColumn) selected.element(1));
        MayflyAssert.assertColumn("foo", "y", (SingleColumn) selected.element(2));
        MayflyAssert.assertColumn("bar", "b", (SingleColumn) selected.element(3));
    }
    
    public void testSelectedDegenerateCase() throws Exception {
        What original = new What().add(new MathematicalInt(7));
        Selected expected = new Selected().add(new MathematicalInt(7));
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
        
        Selected expected = new Selected()
            .add(new SingleColumn("bar", "a"))
            .add(new SingleColumn("bar", "b"))
            .add(new SingleColumn("foo", "x"))
            .add(new SingleColumn("foo", "y"));
    
        assertEquals(expected, original.selected(dummyRow));
    }

}
