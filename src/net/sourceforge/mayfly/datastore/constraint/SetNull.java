package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.parser.Location;

public class SetNull extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, 
        String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        return setValue(oldValue, new NullExpression(Location.UNKNOWN), 
            store, referencerSchema, referencerTable, referencerColumn);
    }

    public DataStore handleUpdate(Cell oldValue, Cell newValue, 
        DataStore store, String referencerSchema, 
        String referencerTable, 
        String referencerColumn, TableReference targetTable, String targetColumn) {
        if (true) throw new UnimplementedException();

        // I don't know why this wasn't working
        return handleDelete(oldValue, store, 
            referencerSchema, referencerTable, referencerColumn, 
            targetTable, targetColumn);
    }
    
    public void dump(Writer out) throws IOException {
        out.write("SET NULL");
    }
    
}
