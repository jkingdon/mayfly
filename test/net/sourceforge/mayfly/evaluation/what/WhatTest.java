package net.sourceforge.mayfly.evaluation.what;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.util.MayflyAssert;

public class WhatTest extends TestCase {

    public void testSelected() throws Exception {
        What original = new What()
            .add(new SingleColumn("a"))
            .add(new AllColumnsFromTable("foo"))
            .add(new SingleColumn("bar", "b"));
        ResultRow dummyRow = new ResultRow()
            .withColumn("bar", "a", NullCell.INSTANCE)
            .withColumn("bar", "b", NullCell.INSTANCE)
            .withColumn("foo", "x", NullCell.INSTANCE)
            .withColumn("foo", "y", NullCell.INSTANCE)
        ;
        Selected selected = original.selected(dummyRow);
        
        assertEquals(4, selected.size());
        MayflyAssert.assertColumn("bar", "a", selected.element(0)); // Or null, "a"
        MayflyAssert.assertColumn("foo", "x", selected.element(1));
        MayflyAssert.assertColumn("foo", "y", selected.element(2));
        MayflyAssert.assertColumn("bar", "b", selected.element(3));
    }
    
    public void testSelectedDegenerateCase() throws Exception {
        What original = new What().add(new IntegerLiteral(7));
        Selected expected = new Selected().add(new IntegerLiteral(7));
        assertEquals(expected, original.selected(new ResultRow()));
    }
    
    public void testSelectedAll() throws Exception {
        What original = new What()
            .add(new All());
        ResultRow dummyRow = new ResultRow()
            .withColumn("bar", "a", NullCell.INSTANCE)
            .withColumn("bar", "b", NullCell.INSTANCE)
            .withColumn("foo", "x", NullCell.INSTANCE)
            .withColumn("foo", "y", NullCell.INSTANCE)
        ;
        
        Selected expected = new Selected()
            .add(new SingleColumn("bar", "a"))
            .add(new SingleColumn("bar", "b"))
            .add(new SingleColumn("foo", "x"))
            .add(new SingleColumn("foo", "y"));
    
        assertEquals(expected, original.selected(dummyRow));
    }

}
