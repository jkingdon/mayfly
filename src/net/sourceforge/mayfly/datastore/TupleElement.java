package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.CaseInsensitiveString;

public class TupleElement {
    private final String column;
    private final Cell cell;

    public TupleElement(Column column, Cell cell) {
        this(column.columnName(), cell);
    }

    public TupleElement(String column, Cell cell) {
        this.column = column;
        this.cell = cell;
    }

    public Cell cell() {
        return cell;
    }

    public String columnName() {
        return column;
    }
    
    public CaseInsensitiveString columnNameCase() {
        return new CaseInsensitiveString(column);
    }

    boolean matchesName(String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException(
                "column name " + target + " should not contain a period");
        }

        return columnName().equalsIgnoreCase(target);
    }

}
