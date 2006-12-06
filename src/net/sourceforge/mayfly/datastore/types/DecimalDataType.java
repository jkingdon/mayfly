package net.sourceforge.mayfly.datastore.types;

import java.math.BigDecimal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DecimalCell;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.parser.TokenType;

public class DecimalDataType extends DataType {

    private final int precision;
    private final int scale;

    public DecimalDataType(int precision, int scale) {
        super();
        this.precision = precision;
        this.scale = scale;
    }
    
    public String dumpName() {
        return
            TokenType.KEYWORD_decimal.description() +
            "(" +
            precision +
            "," +
            scale +
            ")";
    }
    
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
        return genericCoerce(value, columnName, "decimal", null);
    }

}
