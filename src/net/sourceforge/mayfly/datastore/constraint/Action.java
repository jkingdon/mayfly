package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableReference;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.expression.NullExpression;
import net.sourceforge.mayfly.evaluation.expression.literal.CellExpression;
import net.sourceforge.mayfly.ldbc.what.SingleColumn;
import net.sourceforge.mayfly.ldbc.where.Equal;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.util.ImmutableList;

public abstract class Action {

    public DataStore handleDelete(Cell oldValue, DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn, TableReference targetTable, String targetColumn) {
        throw new UnimplementedException(
        "ON DELETE actions not implemented");
    }

    protected DataStore setValue(Cell oldValue, NullExpression valueToAssign, 
        DataStore store, 
        String referencerSchema, String referencerTable, String referencerColumn) {
        UpdateStore update = store.update(referencerSchema, referencerTable,
            ImmutableList.singleton(
                new SetClause(referencerColumn, valueToAssign)), 
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
