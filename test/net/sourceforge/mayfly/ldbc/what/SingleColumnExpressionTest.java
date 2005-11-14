package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;
import net.sourceforge.mayfly.datastore.*;

public class SingleColumnExpressionTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new Tuples()
                .appendColumnCellTuple("colA", "1")
                .appendColumnCellTuple("colB", "2")
        );

        assertEquals(new Cell("1"), new SingleColumnExpression("colA").transform(row));
        assertEquals(new Cell("2"), new SingleColumnExpression("colB").transform(row));
    }

    //TODO: name sucks
    public void testProcess_Simple() throws Exception {
        Tuples original = new Tuples()
            .append(new Tuple(new Column("colA"), new Cell("a")))
            .append(new Tuple(new Column("colB"), new Cell("b")));

        assertEquals(
            new Tuples()
                .append(new Tuple(new Column("colA"), new Cell("a"))),
            new SingleColumnExpression("colA").process(original)
        );
    }



}
