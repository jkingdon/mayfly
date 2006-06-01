package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.M;

import java.util.Iterator;
import java.util.Map;

public class TupleMapper {
    
    M columnToCell;

    public TupleMapper(Row initial) {
        columnToCell = new M();
        for (Iterator iter = initial.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            columnToCell.put(element.column(), element.cell());
        }
    }

    public void put(Column column, Cell cell) {
        columnToCell.put(column, cell);
    }

    public Row asRow() {
        TupleBuilder builder = new TupleBuilder();
        for (Iterator iter = columnToCell.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Column column = (Column) entry.getKey();
            Cell cell = (Cell) entry.getValue();
            builder.append(column, cell);
        }
        return new Row(builder);
    }

}
