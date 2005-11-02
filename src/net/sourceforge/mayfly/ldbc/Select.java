package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.util.*;

import org.ldbc.parser.*;

import java.sql.*;
import java.util.*;

public class Select extends ValueObject {

    public static Select fromTree(Tree selectTree) {

        int[] typesToIgnore = new int[]{SQLTokenTypes.COMMA};

        L converted =
            selectTree.children().convertUsing(TreeConverters.forSelectTree(), typesToIgnore);

        Where where =
            converted.selectObjectsThatAre(Where.class).size() > 0 ?
                    (Where) converted.selectObjectThatIs(Where.class) :
                    Where.EMPTY;

        return
            new Select(
                new What(converted.selectObjectsThatAre(WhatElement.class)),
                new From(converted.selectObjectsThatAre(FromElement.class)),
                where
            );
    }


    private What what;
    private From from;
    private Where where;

    public Select(What what, From from, Where where) {
        this.what = what;
        this.from = from;
        this.where = where;
    }

    public From from() {
        return from;
    }

    public ResultSet select(final DataStore store) throws SQLException {
        try {
            Columns columns = what.selectedColumns();
            checkColumns(store, columns);
            return new MyResultSet(columns, executeOn(store));
        } catch (MayflyException e) {
            throw e.asSqlException();
        }
    }

    private void checkColumns(final DataStore store, Columns columns) throws SQLException {
        Row row = dummyRow(store);

        for (Iterator iter = columns.iterator(); iter.hasNext();) {
            Column column = (Column) iter.next();
            row.cell(column);
        }

        new Rows(row).select(where);
    }

    private Row dummyRow(final DataStore store) throws SQLException {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = dummyRows(store, firstTable);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(dummyRows(store, table));
        }
        if (joinedRows.size() != 1) {
            throw new RuntimeException("internal error: got " + joinedRows.size());
        }
        return (Row) joinedRows.element(0);
    }

    public Rows executeOn(DataStore store) throws SQLException {
        Iterator iterator = from.iterator();

        FromElement firstTable = (FromElement) iterator.next();
        Rows joinedRows = tableContents(store, firstTable);
        while (iterator.hasNext()) {
            FromElement table = (FromElement) iterator.next();
            joinedRows = (Rows)joinedRows.cartesianJoin(tableContents(store, table));
        }

        return (Rows) joinedRows.select(where);
    }

    private Rows tableContents(DataStore store, FromElement table) throws SQLException {
        return applyAlias(table, store.table(table.tableName()).rows());
    }

    private Rows dummyRows(DataStore store, FromElement table) throws SQLException {
        return applyAlias(table, store.table(table.tableName()).dummyRows());
    }

    private Rows applyAlias(FromElement table, Rows storedRows) {
        if (table.alias() == null) {
            return storedRows;
        } else {
            return applyAlias(table.alias(), storedRows);
        }
    }

    private Rows applyAlias(String alias, Rows storedRows) {
        L rows = new L();
        for (Iterator iter = storedRows.iterator(); iter.hasNext();) {
            Row row = (Row) iter.next();
            rows.add(applyAlias(alias, row));
        }
        return new Rows(rows.asImmutable());
    }

    private Row applyAlias(String alias, Row row) {
        M columnToCell = new M();
        for (Iterator iter = row.iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Column column = (Column) entry.getKey();
            Cell cell = (Cell) entry.getValue();

            Column newColumn = new Column(alias, column.columnName());
            columnToCell.put(newColumn, cell);
        }
        return new Row(columnToCell.asImmutable());
    }

}
