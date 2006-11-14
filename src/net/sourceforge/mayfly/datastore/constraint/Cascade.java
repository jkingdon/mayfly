package net.sourceforge.mayfly.datastore.constraint;

import java.io.IOException;
import java.io.Writer;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.util.ImmutableList;

public class Cascade extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        UpdateStore update = store.delete(referencerSchema, referencerTable,
            where(oldValue, referencerTable, referencerColumn)
        );
        return update.store();
    }

    public DataStore handleUpdate(Cell oldValue, Cell newValue, 
        DataStore store, String referencerSchema, 
        String referencerTable, 
        String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        if (true) throw new UnimplementedException();

        // This update will fail, because we haven't yet changed
        // the target table
        
        // Could call setValue here...
        UpdateStore update = store.update(referencerSchema, referencerTable, 
            ImmutableList.singleton(
                new SetClause(referencerColumn, new CellExpression(newValue))
            ), 
            where(oldValue, referencerTable, referencerColumn));
        throw new UnimplementedException();
    }
    
    public void dump(Writer out) throws IOException {
        out.write("CASCADE");
    }

}
