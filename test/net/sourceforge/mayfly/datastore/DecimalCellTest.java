package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;
import junitx.framework.StringAssert;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.MayflyAssert;

import java.math.BigDecimal;
import java.sql.SQLException;

public class DecimalCellTest extends TestCase {

    public void testDecimalCompare() throws Exception {
        MayflyAssert.assertLessThan(
            new DecimalCell(new BigDecimal("5.01")), 
            new DecimalCell(new BigDecimal("5.1")));
        MayflyAssert.assertComparesSqlEqual(
            new DecimalCell(new BigDecimal("5.0")), 
            new DecimalCell(new BigDecimal("5.00")));
        MayflyAssert.assertLessThan(NullCell.INSTANCE, 
            new DecimalCell(new BigDecimal("0.0")));
    }
    
    public void testAsLong() throws Exception {
        assertEquals(Long.MIN_VALUE, 
            new DecimalCell("-9223372036854775808").asLong());
        assertEquals(Long.MAX_VALUE, 
            new DecimalCell("9223372036854775807").asLong());
        
        assertDoesNotFitInLong("-9223372036854775809");
        assertDoesNotFitInLong("9223372036854775808");
    }

    public void testLongAndDecimals() throws Exception {
        assertDoesNotFitInLong("3.14");

        assertEquals(0, new DecimalCell("0.0").asLong());
        assertEquals(0, new DecimalCell("-0.0").asLong());
        assertEquals(1, new DecimalCell("1.0").asLong());
        assertDoesNotFitInLong("0.0001");

        try {
            new DecimalCell("0.0000000000000000000000000000000000000000001")
                .asLong();
            fail();
        }
        catch (MayflyException e) {
            // Sun Java 1.5 says "1E-43".  libgcj gives the decimal.
            StringAssert.assertContains(" does not fit in a long", 
                e.getMessage());
        }

        assertDoesNotFitInLong(
            "9223372036854775800." +
                "0000000000000000000000000000000000000000001");
    }

    private void assertDoesNotFitInLong(String value) {
        try {
            new DecimalCell(value).asLong();
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "Value " +
                value +
                " does not fit in a long", 
                e.getMessage());
        }
    }
    
    public void testAsByte() throws Exception {
        assertEquals(-128, new DecimalCell("-128").asByte());
        
        try {
            new DecimalCell("128").asByte();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value 128 does not fit in a byte", e.getMessage());
        }

        try {
            new DecimalCell("1.1").asByte();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value 1.1 does not fit in a byte", e.getMessage());
        }
    }
    
    public void testAsShort() throws Exception {
        assertEquals(32767, 
            new DecimalCell("32767").asShort());
        
        try {
            new DecimalCell("32768").asShort();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value 32768 does not fit in a short", 
                e.getMessage());
        }

        try {
            new DecimalCell("1.1").asShort();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value 1.1 does not fit in a short", 
                e.getMessage());
        }
    }
    
    public void testAsInt() throws Exception {
        assertEquals(Integer.MIN_VALUE, 
            new DecimalCell("-2147483648").asInt());
        
        try {
            new DecimalCell("-2147483649").asInt();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value -2147483649 does not fit in an int", 
                e.getMessage());
        }

        try {
            new DecimalCell("-5.1").asInt();
            fail();
        }
        catch (SQLException e) {
            assertEquals("Value -5.1 does not fit in an int", 
                e.getMessage());
        }
    }
    
}
