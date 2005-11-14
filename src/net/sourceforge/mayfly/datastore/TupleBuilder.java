package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

public class TupleBuilder {
    
    private L tuples = new L();

    public TupleBuilder append(Tuple tuple) {
        tuples.append(tuple);
        return this;
    }

    public TupleBuilder appendAll(Tuples tuplesToAdd) {
        tuples.addAll(tuplesToAdd);
        return this;
    }

    public TupleBuilder appendColumnCellTuple(String tableName, String columnName, Object cellValue) {
        return append(new Tuple(new Column(tableName, columnName), new Cell(cellValue)));
    }

    public TupleBuilder appendColumnCellTuple(String columnName, Object cellValue) {
        return append(new Tuple(new Column(columnName), new Cell(cellValue)));
    }
    
    public Tuples asTuple() {
        return new Tuples(tuples.asImmutable());
    }

}
