package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public abstract class Command extends ValueObject {

    public static Command fromSql(String sql) {
        return new Parser(sql).parse();
    }

    abstract public void substitute(Collection jdbcParameters);

    abstract public int parameterCount();
    
    abstract public DataStore update(DataStore store, String schema);

    abstract public int rowsAffected();

}
