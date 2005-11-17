package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class TupleElement extends ValueObject {
    private final CellHeader header;
    private final Cell cell;

    public TupleElement(CellHeader header, Cell cell) {
        this.header = header;
        this.cell = cell;
    }

    public CellHeader header() {
        return header;
    }

    public Cell cell() {
        return cell;
    }

}