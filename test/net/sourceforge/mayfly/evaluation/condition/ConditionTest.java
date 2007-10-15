package net.sourceforge.mayfly.evaluation.condition;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.DateCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleBuilder;
import net.sourceforge.mayfly.evaluation.NoColumn;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.ScalarSubselect;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.select.Select;
import net.sourceforge.mayfly.evaluation.what.WhatElement;
import net.sourceforge.mayfly.parser.Parser;

import java.util.ArrayList;
import java.util.Arrays;

public class ConditionTest extends TestCase {
    
    public void testSelect() throws Exception {
        Condition where = new Parser("name='steve'").parseWhere();

        Row row1 = new TupleBuilder()
            .append("name", "steve").asRow();
        Row row2 = new TupleBuilder()
            .append("name", "bob").asRow();

        assertTrue(where.evaluate(row1, "table1"));
        assertFalse(where.evaluate(row2, "table1"));
    }
    
    public void testNull() throws Exception {
        Condition where = new Equal(new SingleColumn("a"), new IntegerLiteral(5));
        Row fiveRow = new TupleBuilder().append("a", 5).asRow();
        Row nullRow = new TupleBuilder()
            .append("a", NullCell.INSTANCE).asRow();
        
        assertTrue(where.evaluate(fiveRow, "table1"));
        assertFalse(where.evaluate(nullRow, "table1"));
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
    
    public void testSubselectsAndFirstAggregate() throws Exception {
        Select select = (Select) Command.fromSql("select max(x) from foo");
        assertEquals(null, 
            new Equal(new ScalarSubselect(select), new IntegerLiteral(5))
            .firstAggregate());
        assertEquals(null, 
            new SubselectedIn(new SingleColumn("x"), select)
            .firstAggregate());
        assertEquals("count(*)", 
            new SubselectedIn(new CountAll("count"), select)
            .firstAggregate());
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
    
    public void testSubselectAndCheck() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("foo", "a", NullCell.INSTANCE)
            .withColumn("bar", "a", NullCell.INSTANCE);

        assertNoBaz("baz.a in (select a from bar)", row);
        try {
            check("foo.a in (select a from bar where baz.a = bar.a)", row);
            fail();
        }
        catch (NoColumn e) {
            assertEquals("The query optimizer shouldn't try to move subselects", 
                e.getMessage());
            // This would also be acceptable:
//            assertEquals("no column baz.a", e.getMessage());
        }

        try {
            check("foo.a in (select max(a) from bar)", row);
            /* I think we might be able to not throw an exception here
               without damage to the optimizer. */
            fail();
        }
        catch (NoColumn e) {
            assertEquals("The query optimizer shouldn't try to move subselects", 
                e.getMessage());
        }

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
    
    public void testResolve() throws Exception {
        
    }
    
    public void testCompareDates() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("foo", "a", new DateCell(2001, 9, 11))
            .withColumn("foo", "b", new DateCell(2004, 11, 2));
        assertTrue(new Greater(
            new SingleColumn("b"), 
            new SingleColumn("a")).evaluate(row));
        assertFalse(new LessEqual(
            new SingleColumn("b"), 
            new SingleColumn("a")).evaluate(row));
    }
    
}
