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
        this(new ImmutableList(columnNames), new Rows());
    }
    
    private TableData(ImmutableList columnNames, Rows rows) {
        this.columns = Columns.fromColumnNames(columnNames);
        this.rows = rows;
    }

    public int getInt(String columnName, int rowIndex) throws SQLException {
        Row row = (Row) rows.element(rowIndex);

        assertColumnKnown(columnName);

        Cell cell = row.cell(new Column(columnName));
        return cell.asInt();
    }

    public TableData addRow(List columnNames, List values) throws SQLException {
        L cols =
            new L(columnNames)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            String columnName = (String) from;
                            return new Column(columnName);
                        }
                    }
                );
        L cells =
            new L(values)
                .collect(
                    new Transformer() {
                        public Object transform(Object from) {
                            return new Cell(from);
                        }
                    }
                );

        M columnToCell = cols.zipper(cells);

        Row newRow = new Row(columnToCell.asImmutable());

        Columns testColumns = (Columns) newRow.columns();
        assertNoUnknownColumns(testColumns);

        return new TableData(columns.asNames().asImmutable(), (Rows) rows.with(newRow));
    }

    public String findColumn(String columnName) throws SQLException {
        assertColumnKnown(columnName);

        return columns.findColumnWithName(columnName).columnName();
    }

    private void assertColumnKnown(String columnName) throws SQLException {
        assertNoUnknownColumns(Columns.fromColumnNames(new L().append(columnName)));
    }

    private void assertNoUnknownColumns(Columns testColumns) throws SQLException {
        Columns extraColumns = (Columns) testColumns.subtract(columns);
        if (extraColumns.hasContents()) {
            throw new SQLException("no column " + extraColumns.asNames().element(0));
        }
    }

    public List columnNames() {
        return columns.asNames();
    }
    
    public int rowCount() {
        return rows.size();
    }

    public Rows rows() {
        return rows;
    }


}