package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.ArrayList;
import java.util.List;

public class GroupByCells extends ValueObject {
    
    private List cells;
    
    public GroupByCells() {
        cells = new ArrayList();
    }

    public GroupByCells(Cell key) {
        this();
        cells.add(key);
    }

    public GroupByCells(Cell key1, Cell key2) {
        this();
        cells.add(key1);
        cells.add(key2);
    }

    public void add(Cell cell) {
        cells.add(cell);
    }

    public Cell firstKey() {
        return (Cell) cells.get(0);
    }

    public int size() {
        return cells.size();
    }

    public Cell get(int index) {
        return (Cell) cells.get(index);
    }

}
