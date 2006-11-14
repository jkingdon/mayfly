package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.mayfly.UnimplementedException;
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

    public DataStore handleUpdate(Cell oldValue, Cell newValue, 
        DataStore store, String referencerSchema, 
        String referencerTable, 
        String referencerColumn, TableReference targetTable, String targetColumn) {
        throw new UnimplementedException();
    }
    
    public void dump(Writer out) throws IOException {
        out.write("SET DEFAULT");
    }

}
