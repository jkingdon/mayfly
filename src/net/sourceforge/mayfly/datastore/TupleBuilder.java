package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class TupleBuilder {
    
    private L elements = new L();

    public TupleBuilder append(TupleElement tuple) {
        elements.append(tuple);
        return this;
    }

    public TupleBuilder appendAll(Tuple elementsToAdd) {
        elements.addAll(elementsToAdd);
        return this;
    }

    public TupleBuilder appendColumnCellTuple(String tableName, String columnName, Object cellValue) {
        return append(new TupleElement(new Column(tableName, columnName), Cell.fromContents(cellValue)));
    }

    public TupleBuilder appendColumnCellTuple(String columnName, Object cellValue) {
        return append(new TupleElement(new Column(columnName), Cell.fromContents(cellValue)));
    }
    
    public Tuple asTuple() {
        return new Tuple(elements.asImmutable());
    }

}
