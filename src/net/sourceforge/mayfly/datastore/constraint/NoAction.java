package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;

public class NoAction extends Action {

    @Override
    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        throw new MayflyException(
            "foreign key violation: table " + 
            referencerTable + " refers to " + 
            targetColumn + " " + oldValue.asBriefString() + 
            " in " + targetTable.tableName());
    }

    @Override
    public DataStore handleUpdate(Cell oldValue, Cell newValue, 
        DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        return handleDelete(oldValue, store, 
            referencerSchema, referencerTable, referencerColumn, 
            targetTable, targetColumn);
    }
    
    @Override
    public void dump(Writer out) throws IOException {
        out.write("NO ACTION");
    }
    
}
