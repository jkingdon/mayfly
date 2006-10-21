package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.ValueObject;

public class TupleElement extends ValueObject {
    private final CellHeader header;
    private final Cell cell;

    public TupleElement(CellHeader header, Cell cell) {
        this.header = header;
        this.cell = cell;
    }

    public TupleElement(String column, Cell cell) {
        this(new Column(column), cell);
    }

    private Column column() {
        return (Column)header;
    }

    public Cell cell() {
        return cell;
    }

    public String columnName() {
        return column().columnName();
    }

    public String tableOrAlias() {
        return column().tableOrAlias();
    }

    boolean matchesName(String target) {
        if (target.indexOf('.') != -1) {
            throw new MayflyException(
                "column name " + target + " should not contain a period");
        }

        return columnName().equalsIgnoreCase(target);
    }

}
