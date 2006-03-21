package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.datastore.constraint.PrimaryKey;
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
    private final PrimaryKey constraints;

    public TableData(Columns columns, PrimaryKey constraints) {
        this(columns, constraints, new Rows());
    }
    
    private TableData(Columns columns, PrimaryKey constraints, Rows rows) {
        this.constraints = constraints;
        columns.checkForDuplicates();
        this.columns = columns;
        this.rows = rows;
    }

    public TableData addRow(List columnNames, List values) {
        Columns specified = findColumns(columnNames);
        specified.checkForDuplicates();
        
        return addRow(specified, values);
    }

    public TableData addRow(List values) {
        return addRow(columns, values);
    }

    private TableData addRow(Columns columnsToInsert, List values) {
        if (columnsToInsert.size() != values.size()) {
            if (values.size() > columnsToInsert.size()) {
                throw new MayflyException("Too many values.\n" + describeNamesAndValues(columnsToInsert, values));
            } else {
                throw new MayflyException("Too few values.\n" + describeNamesAndValues(columnsToInsert, values));
            }
        }
        
        M specifiedColumnToValue = columnsToInsert.zipper(new L(values));
        
        TupleBuilder tuple = new TupleBuilder();
        Iterator iter = columns.iterator();
        while (iter.hasNext()) {
            Column column = (Column) iter.next();
            if (specifiedColumnToValue.containsKey(column)) {
                Cell cell = Cell.fromContents(specifiedColumnToValue.get(column));
                tuple.append(new TupleElement(column, cell));
            } else {
                tuple.append(new TupleElement(column, NullCell.INSTANCE));
            }
        }
        Row newRow = new Row(tuple);
        constraints.check(rows, newRow);

        return new TableData(columns, constraints, (Rows) rows.with(newRow));
    }

    public UpdateTable update(List setClauses, Where where) {
        Rows newRows = new Rows();
        int rowsAffected = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            
            if (where.evaluate(row)) {
                TupleMapper mapper = new TupleMapper(row.tuple());
                for (Iterator iterator = setClauses.iterator(); iterator.hasNext();) {
                    SetClause setClause = (SetClause) iterator.next();
                    Cell cell = setClause.value().evaluate(row);
                    mapper.put(findColumn(setClause.column()), cell);
                }
                Row newRow = new Row(mapper.asTuple());
                constraints.check(newRows, newRow);
                newRows = (Rows) newRows.with(newRow);
                ++rowsAffected;
            }
            else {
                newRows = (Rows) newRows.with(row);
            }

        }
        return new UpdateTable(new TableData(columns, constraints, newRows), rowsAffected);
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

    private Columns findColumns(List columnNames) {
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

}