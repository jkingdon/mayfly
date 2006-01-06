package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;

public class LiteralTest extends TestCase {
    public void testTransform() throws Exception {
        assertEquals(
            new StringCell("foo"),
            new MyLiteral().evaluate(new Row(new TupleBuilder()))
        );
    }

    class MyLiteral extends Literal {

        public Object valueForCellContentComparison() {
            throw new UnimplementedException();
        }

        protected Cell valueAsCell() {
            return new StringCell("foo");
        }

    }

}
