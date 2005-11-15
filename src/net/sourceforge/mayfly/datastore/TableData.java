package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.sql.*;
import java.util.*;

public class TableData {

    private final Columns columns;
    private final Rows rows;

    public TableData(Columns columns) {
        this(columns, new Rows());
    }
    
    private TableData(Columns columns, Rows rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public int getInt(String columnName, int rowIndex) throws SQLException {
        Row row = (Row) rows.element(rowIndex);

        Cell cell = row.cell(findColumn(columnName));
        return cell.asInt();
    }

    public TableData addRow(List columnNames, List values) throws SQLException {
        if (columnNames.size() != values.size()) {
            throw new IllegalArgumentException("Column names has " + columnNames.size() + 
                " elements but values has " + values.size());
        }

        TupleBuilder tuple = new TupleBuilder();

        for (int i = 0; i < columnNames.size(); ++i) {
            tuple.append(
                new TupleElement(
                    findColumn((String) columnNames.get(i)),
                    new Cell(values.get(i))
                )
            );
        }

        Row newRow = new Row(tuple);

        return new TableData(columns, (Rows) rows.with(newRow));
    }
    
    public Rows dummyRows() {

        TupleBuilder tuple = new TupleBuilder();
        for (int i = 0; i < columns.size(); ++i) {
            tuple.append(
                new TupleElement(
                    columns.get(i),
                    new Cell(new Long(0))
                )
            );
        }

        return new Rows(new Row(tuple));
    }

    public Column findColumn(String columnName) throws SQLException {
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

    public boolean hasColumn(String target) {
        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            if (column.matchesName(target)) {
                return true;
            }
        }
        return false;
    }

}