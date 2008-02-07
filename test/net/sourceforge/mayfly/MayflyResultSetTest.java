package net.sourceforge.mayfly;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.expression.CountAll;
import net.sourceforge.mayfly.evaluation.expression.Maximum;
import net.sourceforge.mayfly.evaluation.expression.Plus;
import net.sourceforge.mayfly.evaluation.expression.SingleColumn;
import net.sourceforge.mayfly.evaluation.what.Selected;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.MayflyAssert;

public class MayflyResultSetTest extends TestCase {
    
    public void testMoreThanOneExpression() throws Exception {
        notAScalar(
            "attempt to specify 2 expressions in a subselect", 
            new MayflyResultSet(
                new Selected(ImmutableList.fromArray(new Expression[] { 
                    new Maximum(new SingleColumn("a"), "max", false),
                    new CountAll("Count")
                })),
                new ResultRows()
            ));
    }

    /**
      Here there are two error conditions: the 2 expressions, and also about
      the number of rows.  Throw the exception about the 2 expressions,
      because that is an error with the subselect as written and doesn't
      depend at all on the data in the tables.  The developer will prefer
      to fix those before worrying about data-dependent bugs.
    */
    public void testMoreThanOneExpressionAndAlsoMultipleRows() throws Exception {
        Maximum maximum = new Maximum(new SingleColumn("a"), "max", false);
        notAScalar(
             "attempt to specify 2 expressions in a subselect", 
             new MayflyResultSet(
                 new Selected(ImmutableList.fromArray(new Expression[] { 
                     maximum,
                     new CountAll("Count")
                 })),
                 new ResultRows()
                     .with(new ResultRow().with(maximum, new LongCell(44)))
                     .with(new ResultRow().with(maximum, new LongCell(55)))
             ));
    }

    public void testNoRows() throws Exception {
        Cell scalar = new MayflyResultSet(
            new Selected(ImmutableList.fromArray(new Expression[] { 
                new Maximum(new SingleColumn("a"), "max", false)
            })),
            new ResultRows()
        ).scalar();
        ObjectAssert.assertInstanceOf(NullCell.class, scalar);
    }

    public void testGroupByOrOtherMultipleRowCase() throws Exception {
        Maximum maximum = new Maximum(new SingleColumn("a"), "max", false);
        notAScalar(
            "subselect expects one row but got 2", 
            new MayflyResultSet(
                new Selected(maximum),
                new ResultRows()
                    .with(new ResultRow().with(maximum, new LongCell(44)))
                    .with(new ResultRow().with(maximum, new LongCell(55)))
            ));
    }

    public void testSuccess() throws Exception {
        Maximum maximum = new Maximum(new SingleColumn("a"), "max", false);
        MayflyResultSet results =
            new MayflyResultSet(
                new Selected(maximum),
                new ResultRows(new ResultRow().with(maximum, new LongCell(44)))
            );
        LongCell result = (LongCell) results.scalar();
        assertEquals(44, result.asLong());
    }

    private void notAScalar(String expectedMessage, MayflyResultSet results) {
        try {
            results.scalar();
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                expectedMessage, 
                e.getMessage());
        }
    }
    
    public void testValues() throws Exception {
        SingleColumn a = new SingleColumn("a");
        SingleColumn b = new SingleColumn("b");
        MayflyResultSet results =
            new MayflyResultSet(
                new Selected(a, new Plus(a, b)),
                new ResultRows(
                    new ResultRow()
                        .with(a, new LongCell(44))
                        .with(b, new LongCell(55))
                    )
            );
        results.next();
        ValueList values = results.asValues(new Location(4, 5, 6, 7));
        assertEquals(2, values.size());
        MayflyAssert.assertLong(44, values.cell(0));
        MayflyAssert.assertLong(99, values.cell(1));
    }

}
