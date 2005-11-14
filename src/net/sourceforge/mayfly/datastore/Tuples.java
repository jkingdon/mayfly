package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;
import net.sourceforge.mayfly.ldbc.what.*;

import java.util.*;

public class Tuples extends Aggregate {
    private L tuples;

    public Tuples() {
        this(new L());
    }

    public Tuples(Tuple tuple) {
        this(new L().append(tuple));
    }

    public Tuples(L tuples) {
        this.tuples = tuples;
    }

    protected Aggregate createNew(Iterable items) {
        return new Tuples(new L(items));
    }

    public Iterator iterator() {
        return tuples.iterator();
    }

    public Tuples append(Tuple tuple) {
        tuples.append(tuple);
        return this;
    }

    public Tuples appendAll(Tuples tuplesToAdd) {
        tuples.addAll(tuplesToAdd);
        return this;
    }

    public Tuples asImmutable() {
        return new Tuples(tuples.asUnmodifiable());
    }

    public Cell cellFor(CellHeader header) {
        return withHeader(header).cell();
    }

    public Tuple withHeader(CellHeader header) {
        return ((Tuple)find(new HeaderIs(header)));
    }

    public CellHeaders headers() {
        return new CellHeaders(collect(new GetHeader()));
    }

    public Cells cells() {
        return new Cells(collect(new GetCell()));
    }

    public Tuples appendColumnCellTuple(String tableName, String columnName, Object cellValue) {
        return append(new Tuple(new Column(tableName, columnName), new Cell(cellValue)));
    }

    public Tuples appendColumnCellTuple(String columnName, Object cellValue) {
        return append(new Tuple(new Column(columnName), new Cell(cellValue)));
    }




    public static class HeaderIs implements Selector {
        private CellHeader header;

        public HeaderIs(CellHeader header) {
            this.header = header;
        }

        public boolean evaluate(Object candidate) {
            return ((Tuple)candidate).header().equals(header);
        }

        public String toString() {
            return header.toString();
        }
    }

    public static class GetHeader implements Transformer {
        public Object transform(Object from) {
            return ((Tuple)from).header();
        }
    }

    public static class GetCell implements Transformer {
        public Object transform(Object from) {
            return ((Tuple)from).cell();
        }
    }
}
