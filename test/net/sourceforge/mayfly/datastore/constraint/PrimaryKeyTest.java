package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyTest extends TestCase {

    public void testDescribeValues() throws Exception {
        List values = new ArrayList();
        values.add(NullCell.INSTANCE);
        values.add(new LongCell(7));
        values.add(new StringCell("foo"));

        assertEquals("null,7,foo", NotNullOrUnique.describeValues(values));
    }

}
