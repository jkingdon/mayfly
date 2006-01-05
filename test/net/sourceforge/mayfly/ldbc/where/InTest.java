package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

public class InTest extends TestCase {
    
    public void testParse() throws Exception {
        assertEquals(
            new In(
                new SingleColumn("a"),
                new L()
	                .append(new MathematicalInt(1))
	                .append(new MathematicalInt(2))
                ),
                new Parser("a in (1, 2)").parseCondition()
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
