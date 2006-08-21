package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.Equal;
import net.sourceforge.mayfly.ldbc.where.Where;

public class Cascade extends Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, 
        TableReference targetTable, String targetColumn) {
        UpdateStore update = store.delete(referencerSchema, referencerTable,
            new Where(
                new Equal(
                    new SingleColumn(referencerTable, referencerColumn),
                    new CellExpression(oldValue)
                )
            )
        );
        return update.store();
    }

}
