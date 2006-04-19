package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.L;

public class TupleBuilder {
    
    private L elements = new L();

    public TupleBuilder append(TupleElement tuple) {
        elements.append(tuple);
        return this;
    }

    public TupleBuilder append(CellHeader column, Cell cell) {
        return append(new TupleElement(column, cell));
    }
    
    public TupleBuilder appendAll(Tuple elementsToAdd) {
        elements.addAll(elementsToAdd);
        return this;
    }

    public TupleBuilder appendColumnCellContents(String tableName, String columnName, String cellValue) {
        return appendColumnCell(tableName, columnName, new StringCell(cellValue));
    }

    public TupleBuilder appendColumnCell(String tableName, String columnName, Cell cell) {
        return append(new Column(tableName, columnName), cell);
    }

    public TupleBuilder appendColumnCellContents(String columnName, String cellValue) {
        return appendColumnCell(columnName, new StringCell(cellValue));
    }

    public TupleBuilder appendColumnCellContents(String columnName, long cellValue) {
        return appendColumnCell(columnName, new LongCell(cellValue));
    }

    public TupleBuilder appendColumnCell(String columnName, Cell cell) {
        return append(new Column(columnName), cell);
    }

    public Tuple asTuple() {
        return new Tuple(elements.asImmutable());
    }

}
