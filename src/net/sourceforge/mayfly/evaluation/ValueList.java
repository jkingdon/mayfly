package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValueList {

    public static ValueList singleton(Cell cell) {
        return new ValueList(ImmutableList.singleton(new Value(cell, Location.UNKNOWN)), Location.UNKNOWN);
    }

    public final ImmutableList values;
    public final Location location;

    public ValueList(ImmutableList values, Location location) {
        this.values = values;
        this.location = location;
    }
    
    public ValueList(Location start) {
        this(new ImmutableList(), start);
    }

    public ValueList with(Value newValue) {
        return new ValueList(values.with(newValue), this.location.combine(newValue.location));
    }
    
    public ValueList with(Location end) {
        return new ValueList(values, this.location.combine(end));
    }

    public ImmutableList asCells() {
        List cells = new ArrayList();
        for (Iterator iter = values.iterator(); iter.hasNext();) {
            Value value = (Value) iter.next();
            cells.add(value.value);
        }
        return new ImmutableList(cells);
    }

    public int size() {
        return values.size();
    }

    public Cell cell(int index) {
        return value(index).value;
    }

    public Location location(int index) {
        return value(index).location;
    }

    public Value value(int index) {
        return (Value)values.get(index);
    }
    
}
