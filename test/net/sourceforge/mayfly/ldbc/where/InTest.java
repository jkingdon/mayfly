package net.sourceforge.mayfly.ldbc.where;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TupleElement;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.literal.MathematicalInt;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.L;

public class InTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new In(
                new SingleColumn("a"),
                new L()
	                .append(new MathematicalInt(1))
	                .append(new MathematicalInt(2))
                ),
                new Parser("a in (1, 2)").parseCondition().asBoolean()
        );
    }
    
    public void testEvaluate() throws Exception {
        In in = new In(
            new SingleColumn("a"),
            new L()
	            .append(new MathematicalInt(1))
				.append(new MathematicalInt(3)));
        assertFalse(in.evaluate(row(2)));
        assertTrue(in.evaluate(row(3)));
	}

    private Row row(long aValue) {
        return new Row(new TupleElement(new Column("a"), new LongCell(aValue)));
    }

}
