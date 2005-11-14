package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;

public class LiteralTest extends TestCase {
    public void testTransform() throws Exception {
        assertEquals(
            new Cell("foo"),
            new MyLiteral().transform("would be a row object but doesn't matter here")
        );
    }

    class MyLiteral extends Literal {

        public Object valueForCellContentComparison() {
            return "foo";
        }

    }

}
