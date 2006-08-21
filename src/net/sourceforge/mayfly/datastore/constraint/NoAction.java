package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class NoAction extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, TableReference targetTable, String targetColumn) {
        throw new MayflyException(
            "foreign key violation: table " + 
            referencerTable + " refers to " + 
            targetColumn + " " + oldValue.asBriefString() + 
            " in " + targetTable.tableName());
    }

}
