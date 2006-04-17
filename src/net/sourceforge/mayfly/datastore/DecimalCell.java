package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.UnimplementedException;

import java.math.BigDecimal;
import java.sql.SQLException;

public class DecimalCell extends Cell {
 
    private final BigDecimal value;

    public DecimalCell(BigDecimal value) {
        this.value = value;
    }

    public byte asByte() throws SQLException {
        throw new UnimplementedException("Can't yet convert decimal to byte, even if the value is in range");
    }

    public short asShort() throws SQLException {
        throw new UnimplementedException("Can't yet convert decimal to short, even if the value is in range");
    }

    public int asInt() throws SQLException {
        throw new UnimplementedException("Can't yet convert decimal to int, even if the value is in range");
    }

    public long asLong() {
        throw new UnimplementedException("Can't yet convert decimal to long, even if the value is in range");
    }

    public String asString() {
        /** Is this what other databases do?  Seems consistent with {@link LongCell},
            but something makes me want to make sure I check this out and write a test. 
         */ 
        throw new UnimplementedException("Can't yet convert decimal to string");
    }

    public Object asObject() {
        return value;
    }
    
    public BigDecimal asBigDecimal() {
        return value;
    }

    public int compareTo(Cell otherCell) {
        // want test
        throw new UnimplementedException("Can't yet compare decimals");
    }

    public String displayName() {
        return value.toString();
    }

}
