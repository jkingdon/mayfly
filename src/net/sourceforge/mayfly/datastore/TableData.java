package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.ldbc.where.Where;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.M;
import net.sourceforge.mayfly.util.StringBuilder;
import net.sourceforge.mayfly.util.Transformer;

import java.util.Iterator;
import java.util.List;

public class TableData {

    private final Columns columns;
    private final Rows rows;
    private final Constraints constraints;

    public TableData(Columns columns, Constraints constraints) {
        this(columns, constraints, new Rows());
    }
    
    private TableData(Columns columns, Constraints constraints, Rows rows) {
        this.constraints = constraints;
        columns.checkForDuplicates();
        this.columns = columns;
        this.rows = rows;
    }

    public TableData addRow(Checker checker, List columnNames, List values) {
        Columns specified = findColumns(columnNames);
        specified.checkForDuplicates();
        
        return addRow(checker, specified, values);
    }

    public TableData addRow(Checker checker, List values) {
        return addRow(checker, columns, values);
    }

    private TableData addRow(Checker checker,
        Columns columnsToInsert, List values) {
        if (columnsToInsert.size() != values.size()) {
            if (values.size() > columnsToInsert.size()) {
                throw new MayflyException("Too many values.\n" + describeNamesAndValues(columnsToInsert, values));
            } else {
                throw new MayflyException("Too few values.\n" + describeNamesAndValues(columnsToInsert, values));
            }
        }
        
        M specifiedColumnToValue = columnsToInsert.zipper(new L(values));
        Columns newColumns = columns;
        
        TupleBuilder tuple = new TupleBuilder();
        Iterator iter = columns.iterator();
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            newColumns = setColumn(
                specifiedColumnToValue, newColumns, tuple, column);
        }
        Row newRow = new Row(tuple);
        constraints.check(rows, newRow);
        if (checker != null) {
            checker.checkInsert(constraints, newRow);
        }

        return new TableData(newColumns, constraints, (Rows) rows.with(newRow));
    }

    private Columns setColumn(M specifiedColumnToValue, Columns newColumns, 
        TupleBuilder tuple, Column column) {
        boolean isDefault;
        if (specifiedColumnToValue.containsKey(column)) {
            Cell value = (Cell) specifiedColumnToValue.get(column);
            isDefault = value == null;
            Cell cell = isDefault ? column.defaultValue() : column.coerce(value);
            tuple.append(new TupleElement(column, cell));
        } else {
            isDefault = true;
            tuple.append(new TupleElement(column, column.defaultValue()));
        }
        
        if (isDefault && column.isAutoIncrement()) {
            newColumns = newColumns.replace(column.afterAutoIncrement());
        }
        return newColumns;
    }

    public UpdateTable update(Checker checker, List setClauses, Where where) {
        Rows newRows = new Rows();
        int rowsAffected = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            
            if (where.evaluate(row)) {
                TupleMapper mapper = new TupleMapper(row);
                for (Iterator iterator = setClauses.iterator(); iterator.hasNext();) {
                    SetClause setClause = (SetClause) iterator.next();
                    Column column = setClause.column(columns);
                    mapper.put(column, setClause.value(row, column));
                }
                Row newRow = mapper.asRow();
                constraints.check(newRows, newRow);
                checker.checkInsert(constraints, newRow);
                checker.checkDelete(row, newRow);

                newRows = (Rows) newRows.with(newRow);
                ++rowsAffected;
            }
            else {
                constraints.check(newRows, row);
                newRows = (Rows) newRows.with(row);
            }

        }
        TableData newTable = new TableData(columns, constraints, newRows);
        return new UpdateTable(newTable, rowsAffected);
    }

    public UpdateTable delete(Where where, Checker checker) {
        Rows newRows = new Rows();
        int rowsAffected = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            
            if (where.evaluate(row)) {
                ++rowsAffected;
                checker.checkDelete(row, null);
            }
            else {
                newRows = (Rows) newRows.with(row);
            }

        }
        TableData newTable = new TableData(columns, constraints, newRows);
        return new UpdateTable(newTable, rowsAffected);
    }

    private String describeNamesAndValues(Columns columns, List values) {
        StringBuilder result = new StringBuilder();
        
        result.append("Columns and values were:\n");

        Iterator nameIterator = columns.iterator();
        Iterator valueIterator = values.iterator();
        while (nameIterator.hasNext() || valueIterator.hasNext()) {
            if (nameIterator.hasNext()) {
                Column column = (Column) nameIterator.next();
                result.append(column.columnName());
            } else {
                result.append("(none)");
            }

            result.append(' ');

            if (valueIterator.hasNext()) {
                Object value = valueIterator.next();
                result.append(value.toString());
            } else {
                result.append("(none)");
            }
            
            result.append('\n');
        }
        return result.toString();
    }

    public Columns findColumns(List columnNames) {
        L columnList =
            new L(columnNames)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            return findColumn((String) from);
                        }
                    }
                );

        Columns specified = new Columns(columnList.asImmutable());
        return specified;
    }
    
    public Rows dummyRows() {

        TupleBuilder tuple = new TupleBuilder();
        for (int i = 0; i < columns.size(); ++i) {
            tuple.append(
                new TupleElement(
                    columns.get(i),
                    NullCell.INSTANCE
                )
            );
        }

        return new Rows(new Row(tuple));
    }

    public Column findColumn(String columnName) {
        return columns.columnFromName(columnName);
    }

    public List columnNames() {
        return columns.asNames();
    }
    
    public Columns columns() {
        return columns;
    }
    
    public int rowCount() {
        return rows.size();
    }

    public Rows rows() {
        return rows;
    }

    public boolean hasValue(String column, Cell value) {
        Column foundColumn = columns.columnFromName(column);
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            if (row.cell(foundColumn).sqlEquals(value)) {
                return true;
            }
        }
        return false;
    }

    public DataStore checkDelete(
        DataStore store,
        String schema, String table, Row rowToDelete, Row replacementRow) {
        return constraints.checkDelete(store, schema, table, 
            rowToDelete, replacementRow);
    }

    public void checkDropTable(DataStore store, String schema, String table) {
        constraints.checkDropTable(store, schema, table);
    }

}
