package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.parser.Parser;
import net.sourceforge.mayfly.util.ValueObject;

import java.util.List;

public abstract class Command extends ValueObject {

    public static Command fromSql(String sql) {
        return new Parser(sql).parse();
    }

    public static Command fromTokens(List tokens) {
        return new Parser(tokens).parse();
    }

    abstract public DataStore update(DataStore store, String currentSchema);

    abstract public int rowsAffected();

}
