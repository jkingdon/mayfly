package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.M;

import java.util.Iterator;
import java.util.Map;

public class TupleMapper {
    
    private M columnToCell;

    public TupleMapper(Row initial) {
        columnToCell = new M();
        for (Iterator iter = initial.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            columnToCell.put(element.columnName(), element.cell());
        }
    }

    public void put(Column column, Cell cell) {
        put(column.columnName(), cell);
    }

    public void put(String columnName, Cell cell) {
        columnToCell.put(columnName, cell);
    }

    public Row asRow() {
        TupleBuilder builder = new TupleBuilder();
        for (Iterator iter = columnToCell.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String column = (String) entry.getKey();
            Cell cell = (Cell) entry.getValue();
            builder.appendColumnCell(column, cell);
        }
        return new Row(builder);
    }

}
