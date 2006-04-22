package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.command.InsertTable;

import java.util.List;

public class ForeignKey {

    private final Column referencer;

    // When should this get resolved against the default schema?
    // Probably should be when we set up the constraint, I would think.
    private final InsertTable targetTable;

    private final String targetColumn;

    public ForeignKey(Column referencer, InsertTable targetTable, String targetColumn) {
        this.referencer = referencer;
        this.targetTable = targetTable;
        this.targetColumn = targetColumn;
    }

    public void checkInsert(DataStore store, String schema, String table, Columns columns, List values) {
        if (!schema.equals(DataStore.ANONYMOUS_SCHEMA_NAME)) {
            throw new UnimplementedException("Not yet doing foreign keys with schemas");
        }
        
        if (referencer.tableOrAlias().equalsIgnoreCase(table)) {
            TableData foundTable = store.table(targetTable);
            Cell value = pickValue(columns, values);
            if (!foundTable.hasValue(targetColumn, value)) {
                throw new MayflyException("foreign key violation");
            }
        }
        
    }

    private Cell pickValue(Columns columns, List values) {
        for (int i = 0; i < columns.size(); ++i) {
            Column column = columns.get(i);
            if (column.matchesName(targetColumn)) {
                return (Cell) values.get(i);
            }
        }
        throw new MayflyInternalException("I'm confused about columns and values");
    }

}
