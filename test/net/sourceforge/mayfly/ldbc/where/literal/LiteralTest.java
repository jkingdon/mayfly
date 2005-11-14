package net.sourceforge.mayfly.ldbc.where.literal;

import junit.framework.TestCase;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

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

        public Tuples process(Tuples originalTuples, M aliasToTableName) {
            throw new RuntimeException();
        }
    }
}