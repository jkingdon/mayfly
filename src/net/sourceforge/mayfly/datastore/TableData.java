package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.util.*;

import java.sql.*;
import java.util.*;

public class TableData {

    private final Columns columns;
    private final Rows rows;

    public TableData(List columnNames) {
        this(Columns.fromColumnNames(columnNames), new Rows());
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

        M columnToCell = new M();
        for (int i = 0; i < columnNames.size(); ++i) {
            columnToCell.put(
                findColumn((String) columnNames.get(i)),
                new Cell(values.get(i))
            );
        }

        Row newRow = new Row(columnToCell.asImmutable());

        return new TableData(columns, (Rows) rows.with(newRow));
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