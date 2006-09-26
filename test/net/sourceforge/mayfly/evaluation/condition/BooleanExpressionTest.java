package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.ldbc.what.CountAll;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.what.WhatElement;
import net.sourceforge.mayfly.ldbc.where.And;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.ldbc.where.Equal;
import net.sourceforge.mayfly.ldbc.where.In;
import net.sourceforge.mayfly.ldbc.where.IsNull;
import net.sourceforge.mayfly.ldbc.where.Not;
import net.sourceforge.mayfly.ldbc.where.Or;
import net.sourceforge.mayfly.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;

public class BooleanExpressionTest extends TestCase {
    
    public void testSelect() throws Exception {
        BooleanExpression where = new Parser("name='steve'").parseWhere();

        Row row1 = new Row(new TupleElement(new Column("name"), new StringCell("steve")));
        Row row2 = new Row(new TupleElement(new Column("name"), new StringCell("bob")));

        assertTrue(where.evaluate(row1));
        assertFalse(where.evaluate(row2));
    }
    
    public void testNull() throws Exception {
        BooleanExpression where = new Equal(new SingleColumn("a"), new IntegerLiteral(5));
        Row fiveRow = new Row(new TupleElement(new Column("a"), new LongCell(5)));
        Row nullRow = new Row(new TupleElement(new Column("a"), NullCell.INSTANCE));
        
        assertTrue(where.evaluate(fiveRow));
        assertFalse(where.evaluate(nullRow));
    }

    public void testFirstAggregate() throws Exception {
        assertEquals(null, BooleanExpression.TRUE.firstAggregate());
        assertEquals(null, new Equal(new SingleColumn("x"), new SingleColumn("y")).firstAggregate());
        assertEquals("count(*)", new Equal(new CountAll("count"), new SingleColumn("y")).firstAggregate());
        assertEquals("count(*)", new IsNull(new CountAll("count")).firstAggregate());

        assertEquals(null, new In(new SingleColumn("x"), new ArrayList()).firstAggregate());
        assertEquals("count(*)", new In(new CountAll("count"), new ArrayList()).firstAggregate());
        assertEquals("count(*)",
            new In(
                new SingleColumn("x"), 
                Arrays.asList(new WhatElement[] { new CountAll("count") })
            ).firstAggregate()
        );

        assertEquals("count(*)", new Not(new IsNull(new CountAll("count"))).firstAggregate());
        assertEquals("count(*)", new Or(new IsNull(new CountAll("count")), BooleanExpression.TRUE).firstAggregate());
        assertEquals("count(*)", new And(BooleanExpression.TRUE, new IsNull(new CountAll("count"))).firstAggregate());
    }

}
