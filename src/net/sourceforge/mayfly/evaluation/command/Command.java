package net.sourceforge.mayfly.evaluation.command;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.expression.RealTimeSource;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Lexer;
import net.sourceforge.mayfly.parser.Parser;

import java.util.List;

public abstract class Command {

    public static Command fromSql(String sql) {
        return fromSql(sql, new Options());
    }

    public static Command fromSql(String sql, Options options) {
        return new Parser(
            new Lexer(sql).tokens(), false, new RealTimeSource(), options
        ).parse();
    }

    public static Command fromTokens(List tokens, Options options) {
        return new Parser(tokens, false, new RealTimeSource(), options).parse();
    }
    
    public UpdateStore update(Evaluator evaluator) {
        return update(evaluator.store(), evaluator.currentSchema());
    }

    abstract public UpdateStore update(DataStore store, String currentSchema);

    public MayflyResultSet select(Evaluator evaluator, Cell lastIdentity) {
        throw new MayflyException(
            "This command is only available with update, not query");
    }

}
