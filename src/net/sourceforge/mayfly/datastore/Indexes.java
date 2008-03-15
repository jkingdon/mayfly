package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.CaseInsensitiveString;
import net.sourceforge.mayfly.util.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Indexes implements Iterable<Index> {

    private final ImmutableList<Index> indexes;
    
    public Indexes() {
        this(new ImmutableList<Index>());
    }

    public Indexes(ImmutableList<Index> indexes) {
        checkDuplicates(indexes);
        this.indexes = indexes;
    }

    private static void checkDuplicates(ImmutableList<Index> proposed) {
        Set<CaseInsensitiveString> names = new HashSet<CaseInsensitiveString>();
        for (Index index : proposed) {
            if (index.hasName()) {
                boolean wasPresent = 
                    !names.add(new CaseInsensitiveString(index.name()));
                if (wasPresent) {
                    throw new MayflyException("duplicate index " + index.name());
                }
            }
        }
    }

    public Indexes with(Index index) {
        return new Indexes(indexes.with(index));
    }

    public Indexes without(String indexName) {
        boolean found = false;
        Indexes result = new Indexes();
        for (Index index : indexes) {
            if (indexName.equalsIgnoreCase(index.name())) {
                found = true;
            }
            else {
                result = result.with(index);
            }
        }
        if (!found) {
            throw new MayflyException("no index " + indexName);
        }
        return result;
    }

    public Iterator<Index> iterator() {
        return indexes.iterator();
    }

    public Indexes renameColumn(String oldName, String newName) {
        List result = new ArrayList();
        for (Index index : indexes) {
            result.add(index.renameColumn(oldName, newName));
        }
        return new Indexes(new ImmutableList(result));
    }

    public int indexCount() {
        return indexes.size();
    }

    public void check(Rows rows, Row newRow, TableReference table, Location location) {
        for (Index index : indexes) {
            index.check(rows, newRow, table, location);
        }
    }

}
