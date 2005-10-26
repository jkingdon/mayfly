package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.util.*;

public class StringStartsWith implements Selector {
    private String prefix;

    public StringStartsWith(String prefix) {
        this.prefix = prefix;
    }

    public boolean evaluate(Object candidate) {
        return candidate.toString().startsWith(prefix);
    }
}
