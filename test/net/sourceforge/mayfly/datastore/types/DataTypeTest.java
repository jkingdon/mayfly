package net.sourceforge.mayfly.datastore.types;

import junit.framework.TestCase;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Value;

public class DataTypeTest extends TestCase {
    
    public void testErrorMessages() throws Exception {
        checkStoreString("integer", new IntegerDataType("integer"));
        checkStoreString("smallint", new IntegerDataType("smallint"));
        checkStoreInteger("date", new DateDataType());
        checkStoreInteger("timestamp", new TimestampDataType());
        checkStoreInteger("binary", new BinaryDataType());
        checkStoreInteger("string", new StringDataType("varchar(255)"));
    }

    private void checkStoreString(String whatKindOfColumn, DataType type) {
        try {
            type.coerce(new Value(new StringCell("hi")), "a");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("attempt to store string 'hi' into " +
                    whatKindOfColumn +
                    " column a",
                e.getMessage());
        }
    }
    
    private void checkStoreInteger(String whatKindOfColumn, DataType type) {
        try {
            type.coerce(new Value(new LongCell(77)), "a");
            fail();
        }
        catch (MayflyException e) {
            assertEquals("attempt to store number 77 into " +
                    whatKindOfColumn +
                    " column a",
                e.getMessage());
        }
    }
    
}
