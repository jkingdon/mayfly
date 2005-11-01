package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.util.*;

public class SingleColumnExpressionTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new M()
                .entry(new Column("colA"), new Cell("1"))
                .entry(new Column("colB"), new Cell("2"))
                .asImmutable()
        );

        assertEquals(new Cell("1"), new SingleColumnExpression("colA").transform(row));
        assertEquals(new Cell("2"), new SingleColumnExpression("colB").transform(row));
    }

}
