package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;

public class LiteralTest extends TestCase {
    public void testTransform() throws Exception {
        assertEquals(
            new StringCell("foo"),
            new MyLiteral().transform("would be a row object but doesn't matter here")
        );
    }

    class MyLiteral extends Literal {

        public Cell evaluate(Row row) {
            throw new UnimplementedException();
        }

        public Object valueForCellContentComparison() {
            return "foo";
        }

    }

}
