package net.sourceforge.mayfly.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 Example subclass of {@link net.sourceforge.mayfly.util.Aggregate}
 for tests.
 */
public class Strings extends Aggregate {
    private Collection strings;

    public Strings() {
        this(new String[0]);
    }

    public Strings(String first) {
        this(new String[]{first});
    }

    public Strings(String first, String second) {
        this(new String[]{first, second});
    }

    public Strings(String first, String second, String third) {
        this(new String[]{first, second, third});
    }

    private Strings(String[] strings) {
        this.strings = Arrays.asList(strings);
    }



    public Iterator iterator() {
        return strings.iterator();
    }

    protected Aggregate createNew(Iterable items) {
        L list = new L().addAll(items);
        return new Strings((String[]) list.toArray(new String[list.size()]));
    }

}