package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

public class TupleBuilder {
    
    private L elements = new L();

    public TupleBuilder append(TupleElement tuple) {
        elements.append(tuple);
        return this;
    }

    public TupleBuilder append(Column column, Cell cell) {
        return append(new TupleElement(column, cell));
    }
    
    public TupleBuilder appendColumnCellContents(String columnName, String cellValue) {
        return appendColumnCell(columnName, new StringCell(cellValue));
    }

    public TupleBuilder appendColumnCellContents(String columnName, long cellValue) {
        return appendColumnCell(columnName, new LongCell(cellValue));
    }

    public TupleBuilder appendColumnCell(String columnName, Cell cell) {
        return append(new TupleElement(columnName, cell));
    }

    public ImmutableList asElements() {
        return elements.asImmutable();
    }
    
    public Row asRow() {
        return new Row(elements.asImmutable());
    }

}
