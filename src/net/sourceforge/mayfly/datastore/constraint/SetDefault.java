package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class SetDefault extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, 
        String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        return setValue(oldValue, null, 
            store, referencerSchema, referencerTable, referencerColumn);
    }
    
}
