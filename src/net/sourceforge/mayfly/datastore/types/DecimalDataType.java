package net.sourceforge.mayfly.datastore.types;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DecimalCell;
import net.sourceforge.mayfly.datastore.LongCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.TokenType;

import java.math.BigDecimal;

public class DecimalDataType extends DataType {

    private final int precision;
    private final int scale;

    public DecimalDataType(int precision, int scale) {
        super();
        this.precision = precision;
        this.scale = scale;
    }
    
    @Override
    public String dumpName() {
        return
            TokenType.KEYWORD_decimal.description() +
            "(" +
            precision +
            "," +
            scale +
            ")";
    }
    
    @Override
    public Cell coerce(Value value, String columnName) {
        if (value.value instanceof DecimalCell) {
            DecimalCell decimal = (DecimalCell) value.value;
            BigDecimal bigDecimal = decimal.asBigDecimal();
            if (scale != bigDecimal.scale()) {
                return new DecimalCell(bigDecimal.setScale(scale));
            }
            else {
                return decimal;
            }
        }
        else if (value.value instanceof LongCell) {
            LongCell integer = (LongCell) value.value;
            return new DecimalCell(
                new BigDecimal(integer.asLong())
                    .setScale(scale)
            );
        }
        else {
            return genericCoerce(value, columnName, "decimal", null);
        }
    }

}
