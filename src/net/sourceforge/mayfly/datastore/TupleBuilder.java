package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.L;

public class TupleBuilder {
    
//    private L elements = new L();
    private TupleMapper mapper = new TupleMapper();

    public TupleBuilder append(Column column, Cell cell) {
        return append(column.columnName, cell);
    }
    
    public TupleBuilder appendColumnCellContents(String columnName, String cellValue) {
        return appendColumnCell(columnName, new StringCell(cellValue));
    }

    public TupleBuilder appendColumnCellContents(String columnName, long cellValue) {
        return appendColumnCell(columnName, new LongCell(cellValue));
    }

    public TupleBuilder append(
        CaseInsensitiveString columnName, Cell cell) {
        mapper.add(columnName, cell);
        return this;
    }

    public TupleBuilder appendColumnCell(String columnName, Cell cell) {
        return append(new CaseInsensitiveString(columnName), cell);
    }

    public Row asRow() {
        return mapper.asRow();
    }

}
