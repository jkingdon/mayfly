package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.*;

public class Tuple extends ValueObject {
    private CellHeader header;
    private Cell cell;

    public Tuple(CellHeader header, Cell cell) {
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
