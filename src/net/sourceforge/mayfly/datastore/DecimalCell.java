package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.parser.Location;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

public class DecimalCell extends Cell {
    
    public static BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    public static BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
 
    private final BigDecimal value;

    public DecimalCell(BigDecimal value) {
        this.value = value;
    }

    public DecimalCell(String string) {
        this(new BigDecimal(string));
    }

    public byte asByte() throws SQLException {
        try {
            return longToByte(asLong("a byte"));
        }
        catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public short asShort() throws SQLException {
        try {
            return longToShort(asLong("a short"));
        }
        catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public int asInt() throws SQLException {
        try {
            return longToInt(asLong("an int"));
        }
        catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    public long asLong() {
        /**
         * Could call {@link BigDecimal#longValueExact()} if we wanted to
         * assume Java 1.5.
         */
        return asLong("a long");
    }

    private long asLong(String intoWhat) {
        BigInteger integer = toInteger(intoWhat);
        if (integer.compareTo(LONG_MIN) < 0) {
            throw doesNotFit(intoWhat);
        }
        else if (integer.compareTo(LONG_MAX) > 0) {
            throw doesNotFit(intoWhat);
        }
        else {
            return integer.longValue();
        }
    }

    private BigInteger toInteger(String intoWhat) {
        BigInteger bigInteger = value.toBigInteger();
        BigDecimal integerPart = new BigDecimal(bigInteger);
        BigDecimal difference = value.subtract(integerPart);
        if (difference.signum() != 0) {
            throw doesNotFit(intoWhat);
        }
        return bigInteger;
    }

    private MayflyException doesNotFit(String intoWhat) {
        return new MayflyException(
            "Value " + value.toString() + " does not fit in " + intoWhat);
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
        return "decimal " + value.toString();
    }

}
