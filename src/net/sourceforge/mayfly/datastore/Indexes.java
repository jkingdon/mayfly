package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Indexes {

    private final ImmutableList indexes;

    public Indexes(ImmutableList indexes) {
        this.indexes = indexes;
    }

    public Indexes with(Index index) {
        return new Indexes(indexes.with(index));
    }

    public Iterator iterator() {
        return indexes.iterator();
    }

    public Indexes renameColumn(String oldName, String newName) {
        List result = new ArrayList();
        for (Iterator iter = indexes.iterator(); iter.hasNext();) {
            Index index = (Index) iter.next();
            result.add(index.renameColumn(oldName, newName));
        }
        return new Indexes(new ImmutableList(result));
    }

}
