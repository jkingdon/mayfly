package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;

import java.util.*;

public class SetSchema extends Command {

    private final String name;

    public static SetSchema setSchemaFromTree(Tree tree) {
        String name = tree.getFirstChild().getText();
        return new SetSchema(name);
    }

    public SetSchema(String name) {
        this.name = name;
    }

    public void substitute(Collection jdbcParameters) {
        throw new UnimplementedException();
    }

    public int parameterCount() {
        throw new UnimplementedException();
    }

    public DataStore update(DataStore store, String schema) {
        throw new MayflyInternalException("set schema doesn't operate on a data store");
    }

    public int rowsAffected() {
        return 0;
    }

    public String name() {
        return name;
    }

}
