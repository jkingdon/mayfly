package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;

public class SetNull extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, 
        String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        return setValue(oldValue, new NullExpression(), 
            store, referencerSchema, referencerTable, referencerColumn);
    }
    
}
