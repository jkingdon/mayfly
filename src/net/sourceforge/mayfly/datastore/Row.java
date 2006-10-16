package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.Selector;
import net.sourceforge.mayfly.util.Transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Row extends Aggregate {

    private final ImmutableList elements;
    
    public Row() {
        this(new ImmutableList());
    }

    public Row(TupleElement element) {
        this(ImmutableList.singleton(element));
    }
    
    public Row(ImmutableList elements) {
        this.elements = elements;
    }

    public Row(TupleBuilder builder) {
        this(builder.asElements());
    }

    protected Aggregate createNew(Iterable items) {
        return new Row(ImmutableList.fromIterable(items));
    }

    public Iterator iterator() {
        return elements.iterator();
    }


    public Cell cell(Column column) {
        return cell(column.tableOrAlias(), column.columnName());
    }

    public Cell cell(String tableOrAlias, String column) {
        return cellFor(findColumn(tableOrAlias, column));
    }
    
    public Column findColumn(String columnName) {
        return findColumn(null, columnName);
    }

    public Column findColumn(String tableOrAlias, String columnName) {
        return headers().thatAreColumns().columnFromName(tableOrAlias, columnName);
    }

    /**
     * @internal
     * This method, and the whole concept of having rows refer to
     * columns, is broken.  We don't want to have to re-write all
     * the rows every time that we MODIFY COLUMN or change the
     * auto-increment value (currently stored in the column).
     * Instead, call {@link #columnNames()} and then look up
     * the columns with {@link TableData#findColumn(String)}.
     */
    public Columns columns() {
        return new Columns(ImmutableList.fromIterable(headers()));
    }
    
    public ImmutableList columnNames() {
        List found = new ArrayList();
        for (int i = 0; i < elements.size(); ++i) {
            TupleElement element = (TupleElement) elements.get(i);
            Column column = element.column();
            found.add(column.columnName());
        }
        return new ImmutableList(found);
    }

    public Columns columnsForTable(String aliasOrTable) {
        L found = new L();
        for (int i = 0; i < elements.size(); ++i) {
            TupleElement element = (TupleElement) elements.get(i);
            Column column = element.column();
            if (column.matchesAliasOrTable(aliasOrTable)) {
                found.add(column);
            }
        }
        return new Columns(new ImmutableList(found));
    }

    public Cell cellFor(CellHeader header) {
        return withHeader(header).cell();
    }

    public TupleElement withHeader(CellHeader header) {
        return ((TupleElement)findFirst(new HeaderIs(header)));
    }

    public CellHeaders headers() {
        return new CellHeaders(collect(new GetHeader()));
    }

    public Cells cells() {
        return new Cells(collect(new GetCell()));
    }


    public static class HeaderIs implements Selector {
        private CellHeader header;

        public HeaderIs(CellHeader header) {
            this.header = header;
        }

        public boolean evaluate(Object candidate) {
            return ((TupleElement)candidate).header().equals(header);
        }

        public String toString() {
            return header.toString();
        }
    }

    public static class GetHeader implements Transformer {
        public Object transform(Object from) {
            return ((TupleElement)from).header();
        }
    }

    public static class GetCell implements Transformer {
        public Object transform(Object from) {
            return ((TupleElement)from).cell();
        }
    }

    public String toString() {
        String columns = headers().toString();
        String cells = cells().toString();

        return "\n" +
               "Row:\n" +
               "\tcolumns:\t" + columns + "\n" +
               "\tcells:\t" + cells;
    }

    public Row addColumn(Column newColumn) {
        return new Row(elements.with(new TupleElement(newColumn, newColumn.newColumnValue())));
    }

    public Row dropColumn(String columnName) {
        boolean found = false;
        TupleBuilder newRow = new TupleBuilder();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            TupleElement element = (TupleElement) iter.next();
            if (element.column().matchesName(columnName)) {
                found = true;
            }
            else {
                newRow.append(element);
            }
        }
        if (found) {
            return newRow.asRow();
        }
        else {
            throw new MayflyException("no column " + columnName);
        }
    }

    /**
     * Return a new row which has all the columns from two input
     * rows.
     */
    public Row combine(Row right) {
        Set leftTableNames = new HashSet();

        L result = new L();
        Iterator leftIterator = this.iterator();
        while (leftIterator.hasNext()) {
            TupleElement element = (TupleElement) leftIterator.next();
            leftTableNames.add(new CaseInsensitiveString(
                element.column().tableOrAlias()));
            result.append(element);
        }

        Iterator rightIterator = right.iterator();
        while (rightIterator.hasNext()) {
            TupleElement element = (TupleElement) rightIterator.next();
            String tableOrAlias = element.column().tableOrAlias();
            if (leftTableNames.contains(new CaseInsensitiveString(tableOrAlias))) {
                throw new MayflyException(
                    "duplicate table name or alias " + tableOrAlias);
            }
            result.append(element);
        }

        return new Row(ImmutableList.fromIterable(result));
    }

}
