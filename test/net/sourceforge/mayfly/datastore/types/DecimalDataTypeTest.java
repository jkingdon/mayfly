package net.sourceforge.mayfly.datastore.types;

import junit.framework.TestCase;
import junitx.framework.ObjectAssert;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DecimalCell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.math.BigDecimal;

public class DecimalDataTypeTest extends TestCase {
    
    public void testCoerce() throws Exception {
        DecimalDataType type = new DecimalDataType(10, 2);
        check(2, 1350, "13.50", type);
        check(2, 1350, "13.5", type);

        try {
            check(2, 1351, "13.512", type);
            fail();
        }
        catch (ArithmeticException e) {
        }
        
        ObjectAssert.assertInstanceOf(NullCell.class, 
            MayflyAssert.coerce(type, NullCell.INSTANCE));
    }
    
    public void testFromInteger() throws Exception {
        DecimalDataType type = new DecimalDataType(10, 2);
        DecimalCell coerced = (DecimalCell) 
            MayflyAssert.coerce(type, new LongCell(2333444555L));
        assertEquals(2, coerced.asBigDecimal().scale());
        assertEquals("2333444555.00", 
            coerced.asBigDecimal().toString());
    }

    private void check(int expectedScale, int expectedDigits, 
        String in, DecimalDataType type) {
        check(expectedScale, expectedDigits, 
            new DecimalCell(new BigDecimal(in)), 
            type);
    }

    private void check(int expectedScale, int expectedDigits, 
        Cell inputCell, DecimalDataType type) {
        DecimalCell coerced = (DecimalCell) 
            MayflyAssert.coerce(type, inputCell);
        assertEquals(expectedScale, coerced.asBigDecimal().scale());
        assertEquals(expectedDigits, 
            coerced.asBigDecimal().movePointRight(2).intValue());
    }

}
