package net.sourceforge.mayfly.ldbc;

import java.util.*;

public class Dimensions extends ValueObject { //TODO: extends Enumerable?

    private List<Dimension> dimensions = new ArrayList<Dimension>();

    public Dimensions add(String name) {
        dimensions.add(new Dimension(name));
        return this;
    }
}
