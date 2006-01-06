package net.sourceforge.mayfly.ldbc.what;

import junit.framework.*;

import net.sourceforge.mayfly.datastore.*;

public class SingleColumnTest extends TestCase {

    public void testRowTransform() throws Exception {
        Row row = new Row(
            new TupleBuilder()
                .appendColumnCellContents("colA", "1")
                .appendColumnCellContents("colB", "2")
        );

        assertEquals(new StringCell("1"), new SingleColumn("colA").evaluate(row));
        assertEquals(new StringCell("2"), new SingleColumn("colB").evaluate(row));
    }

}
