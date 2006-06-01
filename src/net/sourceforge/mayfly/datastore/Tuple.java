package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.Aggregate;
import net.sourceforge.mayfly.util.ImmutableList;
import net.sourceforge.mayfly.util.Iterable;
import net.sourceforge.mayfly.util.L;
import net.sourceforge.mayfly.util.Selector;
import net.sourceforge.mayfly.util.Transformer;

import java.util.Iterator;

public class Tuple extends Aggregate {
    private final ImmutableList elements;

    public Tuple(ImmutableList elements) {
        this.elements = elements;
    }

    protected Aggregate createNew(Iterable items) {
        return new Tuple(ImmutableList.fromIterable(items));
    }

    public Iterator iterator() {
        return elements.iterator();
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

}
