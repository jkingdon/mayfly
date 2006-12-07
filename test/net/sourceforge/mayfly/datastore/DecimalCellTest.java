package net.sourceforge.mayfly.datastore;

import junit.framework.TestCase;

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
        
        try {
            new DecimalCell("-9223372036854775809").asLong();
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "Value -9223372036854775809 does not fit in a long", 
                e.getMessage());
        }

        try {
            new DecimalCell("3.14").asLong();
            fail();
        }
        catch (MayflyException e) {
            assertEquals(
                "Value 3.14 does not fit in a long", 
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
    }
    
}
