package net.sourceforge.mayfly.ldbc.where;

import junit.framework.*;

import org.ldbc.parser.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.util.*;

public class InTest extends TestCase {
    
    public void testParse() throws Exception {
        Tree selectTree = Tree.parse("select * from foo where a in (1, 2)");
        Tree inTree = selectTree.children()
            .singleSubtreeOfType(SQLTokenTypes.CONDITION).children()
                .singleSubtreeOfType(SQLTokenTypes.LITERAL_in);
        assertEquals(
            new In(
                new SingleColumn("a"),
                new L()
	                .append(new MathematicalInt(1))
	                .append(new MathematicalInt(2))
                ),
            In.fromInTree(inTree, TreeConverters.forWhereTree())
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
        return new Row(new Tuple(new Column("a"), new Cell(new Long(aValue))));
    }

}
