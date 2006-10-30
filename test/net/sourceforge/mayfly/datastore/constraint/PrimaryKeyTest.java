package net.sourceforge.mayfly.datastore.constraint;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.parser.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrimaryKeyTest extends TestCase {

    public void testDescribeValues() throws Exception {
        List values = new ArrayList();
        values.add(NullCell.INSTANCE);
        values.add(new LongCell(7));
        values.add(new StringCell("foo"));

        assertEquals("null,7,foo", NotNullOrUnique.describeValues(values));
    }
    
    public void testSqlEquals() throws Exception {
        try {
            NotNullOrUnique.sqlEquals(
                Collections.EMPTY_LIST,
                Collections.singletonList(NullCell.INSTANCE),
                Location.UNKNOWN);
            fail();
        }
        catch (MayflyInternalException e) {
            assertEquals("meant to compare equal size lists but were 0 and 1",
                e.getMessage());
        }

        assertFalse(NotNullOrUnique.sqlEquals(
            Collections.singletonList(NullCell.INSTANCE),
            Collections.singletonList(NullCell.INSTANCE), 
            Location.UNKNOWN));
        assertTrue(NotNullOrUnique.sqlEquals(
            Collections.singletonList(new LongCell(7)),
            Collections.singletonList(new LongCell(7)), 
            Location.UNKNOWN));
    }

}
