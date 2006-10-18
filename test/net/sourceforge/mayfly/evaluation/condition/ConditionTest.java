package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;

public class ConditionTest extends TestCase {
    
    public void testSelect() throws Exception {
        Condition where = new Parser("name='steve'").parseWhere();

        Row row1 = new Row(new TupleElement(new Column("name"), new StringCell("steve")));
        Row row2 = new Row(new TupleElement(new Column("name"), new StringCell("bob")));

        assertTrue(where.evaluate(row1));
        assertFalse(where.evaluate(row2));
    }
    
    public void testNull() throws Exception {
        Condition where = new Equal(new SingleColumn("a"), new IntegerLiteral(5));
        Row fiveRow = new Row(new TupleElement(new Column("a"), new LongCell(5)));
        Row nullRow = new Row(new TupleElement(new Column("a"), NullCell.INSTANCE));
        
        assertTrue(where.evaluate(fiveRow));
        assertFalse(where.evaluate(nullRow));
    }

    public void testFirstAggregate() throws Exception {
        assertEquals(null, Condition.TRUE.firstAggregate());
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
        assertEquals("count(*)", new Or(new IsNull(new CountAll("count")), Condition.TRUE).firstAggregate());
        assertEquals("count(*)", new And(Condition.TRUE, new IsNull(new CountAll("count"))).firstAggregate());
    }

    public void testCheck() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("foo", "a", NullCell.INSTANCE)
            .withColumn("bar", "a", NullCell.INSTANCE);

        check("foo.a = bar.a", row);
        check("foo.a = bar.a and (bar.a < 5 or foo.a <> bar.a)", row);

        assertNoBaz("baz.a = 7", row);

        assertNoBaz("foo.a = bar.a and (bar.a = 5 or baz.a = 7)", row);
        assertNoBaz("not baz.a = foo.a", row);

        assertNoBaz("baz.a in (3, 4)", row);
        assertNoBaz("bar.a in (3, baz.a)", row);
        assertNoBaz("baz.a is null", row);
        
        Condition.TRUE.check(row);
    }

    private void assertNoBaz(String sql, ResultRow row) {
        try {
            check(sql, row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("no column baz.a", e.getMessage());
        }
    }

    private void check(String expressionString, ResultRow row) {
        Parser parser = new Parser(expressionString);
        Condition condition = parser.parseCondition().asBoolean();
        assertEquals("", parser.remainingTokens());
        condition.check(row);
    }
    
}
