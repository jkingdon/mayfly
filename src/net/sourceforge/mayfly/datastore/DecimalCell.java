package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.parser.Location;

import java.math.BigDecimal;
import java.sql.SQLException;

public class DecimalCell extends Cell {
 
    private final BigDecimal value;

    public DecimalCell(BigDecimal value) {
        this.value = value;
    }

    public DecimalCell(String string) {
        this(new BigDecimal(string));
    }

    public byte asByte() throws SQLException {
        try {
            return value.byteValueExact();
        }
        catch (ArithmeticException e) {
            throw new SQLException(
                "Value " + value.toString() + " does not fit in a byte");
        }
    }

    public short asShort() throws SQLException {
        try {
            return value.shortValueExact();
        }
        catch (ArithmeticException e) {
            throw new SQLException(
                "Value " + value.toString() + " does not fit in a short");
        }
    }

    public int asInt() throws SQLException {
        try {
            return value.intValueExact();
        }
        catch (ArithmeticException e) {
            throw new SQLException(
                "Value " + value.toString() + " does not fit in an int");
        }
    }

    public long asLong() {
        try {
            return value.longValueExact();
        }
        catch (ArithmeticException e) {
            throw new MayflyException(
                "Value " + value.toString() + " does not fit in a long");
        }
    }

    public String asString() {
        /** What do other databases do?  I guess do it like {@link LongCell},
            but something makes me want to make sure I check this out and write a test. 
         */ 
        throw new UnimplementedException("Can't yet convert decimal to string");
    }

    public Object asObject() {
        return value;
    }
    
    public String asSql() {
        return value.toString();
    }
    
    public BigDecimal asBigDecimal() {
        return value;
    }

    public double asDouble() {
        return value.doubleValue();
    }

    public int compareTo(Cell otherCell, Location location) {
        if (otherCell instanceof DecimalCell) {
            return compareDecimals(value, ((DecimalCell) otherCell).value);
        }
        else if (otherCell instanceof NullCell) {
            return 1;
        }
        else {
            throw cannotCompare(otherCell, location);
        }
    }

    private int compareDecimals(BigDecimal value2, BigDecimal value3) {
        return value2.compareTo(value3);
    }

    public String displayName() {
        return value.toString();
    }

}
