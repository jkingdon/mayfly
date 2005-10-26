package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;

public class LiteralTest extends TestCase {
    public void testTransform() throws Exception {
        assertEquals(new Cell("foo"), new MyLiteral().transform("IRL this is a row object but it actually doesnt matter"));
    }

    class MyLiteral extends Literal {
        public boolean matchesCell(Cell cell) {
            return false;
        }

        public Object valueForCellContentComparison() {
            return "foo";
        }
    }
}