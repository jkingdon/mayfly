package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.List;

public class ValueList {
    private final ImmutableList values;
    public final Location location;

    public ValueList(List values, Location location) {
        this.values = new ImmutableList(values);
        this.location = location;
    }
    
    public ValueList(Location start) {
        this(new ImmutableList(), start);
    }

    public ValueList with(Value newValue) {
        return new ValueList(values.with(newValue.value), this.location.combine(newValue.location));
    }
    
    public ValueList with(Location end) {
        return new ValueList(values, this.location.combine(end));
    }

    public ImmutableList asCells() {
        return values;
    }
}
