package net.sourceforge.mayfly.evaluation.expression;

import junit.framework.TestCase;

import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.expression.literal.IntegerLiteral;
import net.sourceforge.mayfly.evaluation.select.AliasEvaluator;
import net.sourceforge.mayfly.evaluation.what.AliasedExpression;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.MayflyAssert;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        ResultRow row = new ResultRow()
            .withColumn("t", "colA", "1")
            .withColumn("t", "colB", "2")
        ;

        MayflyAssert.assertString("1", new SingleColumn("colA").evaluate(row));
        MayflyAssert.assertString("2", new SingleColumn("colB").evaluate(row));
    }

    public void testSameColumn() throws Exception {
        Expression one = (Expression) new Parser("foo.x").parseWhatElement();
        Expression two = (Expression) new Parser("Foo . X").parseWhatElement();

        assertTrue(one.sameExpression(two));
        assertTrue(two.sameExpression(one));

        assertFalse(new SingleColumn("x").sameExpression(one));
        
        assertFalse(new SingleColumn("x").sameExpression(new IntegerLiteral(5)));
        
        assertFalse(one.sameExpression(new SingleColumn("foo", "y")));
     }
     
    public void testPossiblyNullEquals() throws Exception {
        SingleColumn column = new SingleColumn("irrelevant");
        assertTrue(column.possiblyNullTablesEqual("x", "X"));
        assertFalse(column.possiblyNullTablesEqual("x", "xy"));
        assertFalse(column.possiblyNullTablesEqual("x", null));
        assertFalse(column.possiblyNullTablesEqual(null, "X"));
        assertTrue(column.possiblyNullTablesEqual(null, null));
    }
    
    public void testResolve() throws Exception {
        ResultRow row = new ResultRow().withColumn("foo", "x", new LongCell(7));
        SingleColumn unresolved = new SingleColumn(
            "x", new Location(5, 20, 7, 24), new Options());

        SingleColumn resolved = (SingleColumn) unresolved.resolve(row);
        assertEquals("foo", resolved.tableOrAlias());
        assertEquals("x", resolved.columnName());
        assertEquals(20, resolved.location.startColumn);
    }
    
    public void testResolveWithColumnAlias() throws Exception {
        ResultRow row = new ResultRow().withColumn("foo", "x", new LongCell(7));
        SingleColumn unresolved = new SingleColumn(
            "adjusted", new Location(5, 20, 7, 24), new Options());
        unresolved.resolve(
            row, 
            new AliasEvaluator(new What(
                new AliasedExpression("adjusted", 
                    new Plus(new SingleColumn("x"), new IntegerLiteral(5))
                )
            ))
        );
    }
    
    public void testCaseSensitiveSameExression() throws Exception {
        SingleColumn secondColumn = new SingleColumn("fOO", "x");
        assertTrue(
            new SingleColumn("foo", "x", new Options())
            .sameExpression(secondColumn));
        assertFalse(
            new SingleColumn("foo", "x", new Options(true))
            .sameExpression(secondColumn));
    }

}
