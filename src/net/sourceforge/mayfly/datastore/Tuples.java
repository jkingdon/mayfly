package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

import java.util.*;

public class Tuples extends Aggregate {
    private final ImmutableList tuples;

    public Tuples() {
        this(new ImmutableList());
    }

    public Tuples(Tuple tuple) {
        this(ImmutableList.singleton(tuple));
    }

    public Tuples(ImmutableList tuples) {
        this.tuples = tuples;
    }

    protected Aggregate createNew(Iterable items) {
        return new Tuples(ImmutableList.fromIterable(items));
    }

    public Iterator iterator() {
        return tuples.iterator();
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
