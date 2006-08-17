package net.sourceforge.mayfly.datastore.constraint;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.Column;
import net.sourceforge.mayfly.datastore.Columns;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.datastore.Row;
import net.sourceforge.mayfly.datastore.TableData;
import net.sourceforge.mayfly.evaluation.command.InsertTable;

import java.util.List;

public class ForeignKey {

    private final String referencerSchema;
    private final String referencerTable;
    private final String referencerColumn;

    private final InsertTable targetTable;
    private final String targetColumn;

    public ForeignKey(String referencerSchema, String referencerTable, String referencerColumn, 
        InsertTable targetTable, String targetColumn) {
        
        this.referencerSchema = referencerSchema;
        this.referencerTable = referencerTable;
        this.referencerColumn = referencerColumn;

        this.targetTable = targetTable;
        targetTable.assertSchemaIsResolved();
        this.targetColumn = targetColumn;
    }

    public void checkInsert(DataStore store, String schema, String table, Columns columns, List values) {
        if (referencerSchema.equalsIgnoreCase(schema)
            && referencerTable.equalsIgnoreCase(table)) {
            TableData foundTable = store.table(targetTable);
            Cell value = pickValue(columns, values);
            if (!foundTable.hasValue(targetColumn, value)) {
                throwInsertException(schema, value);
            }
        }
    }

    private void throwInsertException(String schema, Cell value) {
        StringBuilder targetTableName = new StringBuilder();
        if (!targetTable.schema().equalsIgnoreCase(schema)) {
            targetTableName.append(targetTable.schema());
            targetTableName.append(".");
        }
        targetTableName.append(targetTable.tableName());
        throw new MayflyException("foreign key violation: " + targetTableName.toString() + 
            " has no " +
            targetColumn +
            " " + value.asBriefString());
    }

    private Cell pickValue(Columns columns, List values) {
        for (int i = 0; i < columns.size(); ++i) {
            Column column = columns.get(i);
            if (column.matchesName(referencerColumn)) {
                return (Cell) values.get(i);
            }
        }
        throw new MayflyInternalException("Didn't find " + targetColumn + " in " + columns.toString());
    }

    public void checkDelete(DataStore store, String schema, String table, Row row) {
        if (targetTable.schema(schema).equalsIgnoreCase(schema)
            && targetTable.tableName().equalsIgnoreCase(table)) {
            Cell cell = row.cell(row.findColumn(targetColumn));
            if (store.table(referencerSchema, referencerTable).hasValue(referencerColumn, cell)) {
                throw new MayflyException("foreign key violation: table " + referencerTable + 
                    " refers to " + targetColumn + " " + cell.asBriefString() + " in " + targetTable.tableName());
            }
        }
    }

}
