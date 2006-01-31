package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.parser.*;
import net.sourceforge.mayfly.util.*;

import java.util.*;

public abstract class Command extends ValueObject {

    public static Command fromSql(String sql) {
        return new Parser(sql).parse();
    }

    public static Command fromTokens(List tokens) {
        return new Parser(tokens).parse();
    }

    final public void substitute(Collection jdbcParameters) {
    }

    final public int parameterCount() {
        return 0;
    }
    
    abstract public DataStore update(DataStore store, String schema);

    abstract public int rowsAffected();

}
