package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.CaseInsensitiveString;

public class TupleBuilder {
    
    private TupleMapper mapper = new TupleMapper();

    public TupleBuilder append(Column column, Cell cell) {
        return append(column.columnName, cell);
    }
    
    public TupleBuilder append(String columnName, String cellValue) {
        return append(columnName, new StringCell(cellValue));
    }

    public TupleBuilder append(String columnName, long cellValue) {
        return append(columnName, new LongCell(cellValue));
    }

    public TupleBuilder append(
        CaseInsensitiveString columnName, Cell cell) {
        mapper.add(columnName, cell);
        return this;
    }

    public TupleBuilder append(String columnName, Cell cell) {
        return append(new CaseInsensitiveString(columnName), cell);
    }

    public Row asRow() {
        return mapper.asRow();
    }

}
