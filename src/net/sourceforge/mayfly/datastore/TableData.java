package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.constraint.Constraints;
import net.sourceforge.mayfly.evaluation.Checker;
import net.sourceforge.mayfly.evaluation.Value;
import net.sourceforge.mayfly.evaluation.ValueList;
import net.sourceforge.mayfly.evaluation.command.SetClause;
import net.sourceforge.mayfly.evaluation.command.UpdateTable;
import net.sourceforge.mayfly.ldbc.where.BooleanExpression;
import net.sourceforge.mayfly.parser.Location;
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
    
    TableData(Columns columns, Constraints constraints, Rows rows) {
        this.constraints = constraints;
        if (constraints == null) {
            throw new NullPointerException("constraints is required");
        }
        columns.checkForDuplicates();
        this.columns = columns;
        this.rows = rows;
    }

    public TableData addRow(Checker checker, List columnNames, ValueList values) {
        Columns specified = findColumns(columnNames);
        specified.checkForDuplicates();
        
        return addRow(checker, specified, values);
    }

    public TableData addRow(Checker checker, ValueList values) {
        return addRow(checker, columns, values);
    }

    private TableData addRow(Checker checker,
        Columns columnsToInsert, ValueList values) {
        if (columnsToInsert.size() != values.size()) {
            if (values.size() > columnsToInsert.size()) {
                throw makeException("Too many values.\n", columnsToInsert, values);
            } else {
                throw makeException("Too few values.\n", columnsToInsert, values);
            }
        }
        
        M specifiedColumnToValue = columnsToInsert.zipper(new L(values.values));
        Columns newColumns = columns;
        
        TupleBuilder tuple = new TupleBuilder();
        Iterator iter = columns.iterator();
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            newColumns = setColumn(
                specifiedColumnToValue, newColumns, tuple, column, 
                values.location);
        }
        Row newRow = new Row(tuple);
        constraints.check(rows, newRow, values.location);
        checker.checkInsert(constraints, newRow);

        return new TableData(newColumns, constraints, (Rows) rows.with(newRow));
    }

    private MayflyException makeException(String message, Columns columnsToInsert, ValueList values) {
        return new MayflyException(
            message + describeNamesAndValues(columnsToInsert, values.asCells()),
            values.location);
    }

    private Columns setColumn(M specifiedColumnToValue, Columns newColumns, 
        TupleBuilder tuple, Column column, Location location) {
        boolean isDefault;
        Cell cell;
        if (specifiedColumnToValue.containsKey(column)) {
            Value value = (Value) specifiedColumnToValue.get(column);
            isDefault = value.value == null;
            cell = column.coerce(
                isDefault ? column.defaultValue() : value.value, 
                value.location);
        } else {
            isDefault = true;
            cell = column.coerce(column.defaultValue(), location);
        }
        
        tuple.append(new TupleElement(column, cell));

        if (isDefault && column.isAutoIncrement()) {
            newColumns = newColumns.replace(column.afterAutoIncrement());
        }
        return newColumns;
    }

    public UpdateTable update(Checker checker, List setClauses, 
        BooleanExpression where) {
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
                setOnUpdateColumns(mapper);
                Row newRow = mapper.asRow();
                constraints.check(newRows, newRow, Location.UNKNOWN);
                checker.checkInsert(constraints, newRow);
                checker.checkDelete(row, newRow);

                newRows = (Rows) newRows.with(newRow);
                ++rowsAffected;
            }
            else {
                constraints.check(newRows, row, Location.UNKNOWN);
                newRows = (Rows) newRows.with(row);
            }

        }
        TableData newTable = new TableData(columns, constraints, newRows);
        return new UpdateTable(newTable, rowsAffected);
    }

    private void setOnUpdateColumns(TupleMapper mapper) {
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (column.hasOnUpdateValue()) {
                mapper.put(column, column.getOnUpdateValue());
            }
        }
    }

    public UpdateTable delete(BooleanExpression where, Checker checker) {
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
        return hasValue(columns.columnFromName(column), value);
    }

    private boolean hasValue(Column column, Cell value) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            if (row.cell(column).sqlEquals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNull(Column column) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            if (row.cell(column) instanceof NullCell) {
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

    public TableData addColumn(Column newColumn) {
        if (columns.hasColumn(newColumn.columnName())) {
            throw new MayflyException(
                "column " + newColumn.columnName() + " already exists");
        }
        return new TableData(
            (Columns) columns.with(newColumn), 
            constraints,
            rows.addColumn(newColumn)
        );
    }

    public TableData dropColumn(TableReference table, String column) {
        return new TableData(
            columns.without(column),
            constraints.dropColumn(table, column),
            rows.dropColumn(column)
        );
    }

    /** 
     * @internal
     * Check for whether this table has any foreign keys which
     * reference the other column specified by the parameters.
     * If so, throw an exception.
     */
    public void checkDropColumn(TableReference table, String column) {
        constraints.checkDropColumn(table, column);
    }

    public TableData modifyColumn(Column newColumn) {
        Column oldColumn = columns.columnFromName(newColumn.columnName());
        if (!oldColumn.isNotNull && newColumn.isNotNull) {
            // Check the constraint that we're adding
            if (hasNull(oldColumn)) {
                throw new MayflyException(
                    "cannot make column " + newColumn.columnName() + 
                    " NOT NULL because it contains null values");
            }
        }
        return new TableData(
            columns.replace(newColumn),
            constraints,
            rows
        );
    }

}
