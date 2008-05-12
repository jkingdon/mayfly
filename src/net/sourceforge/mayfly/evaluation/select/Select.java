package net.sourceforge.mayfly.evaluation.select;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyResultSet;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.DataStore;
import net.sourceforge.mayfly.evaluation.Aggregator;
import net.sourceforge.mayfly.evaluation.GroupByKeys;
import net.sourceforge.mayfly.evaluation.command.Command;
import net.sourceforge.mayfly.evaluation.command.UpdateStore;
import net.sourceforge.mayfly.evaluation.condition.Condition;
import net.sourceforge.mayfly.evaluation.from.From;
import net.sourceforge.mayfly.evaluation.what.What;
import net.sourceforge.mayfly.parser.Location;

public class Select extends Command {

    private final What what;

    private final From from;

    private final Condition where;

    /**
     * Not immutable, because of {@link GroupByKeys}
     */
    private final Aggregator groupBy;

    private final Distinct distinct;

    private final OrderBy orderBy;

    private final Limit limit;

    public final Location location;

    public Select(What what, From from, Condition where, Aggregator groupBy, 
        boolean distinct, OrderBy orderBy, Limit limit, Location location) {
        this.what = what;
        this.from = from;
        this.where = where;
        this.groupBy = groupBy;
        this.distinct = distinct ? new IsDistinct() : new NotDistinct();
        this.orderBy = orderBy;
        this.limit = limit;
        this.location = location;
    }

    @Override
    public MayflyResultSet select(Evaluator evaluator, Cell lastIdentity) {
        return plan(evaluator).asResultSet();
    }
    
    @Override
    public UpdateStore update(DataStore store, String schema) {
        throw new MayflyException(
            "SELECT is only available with query, not update");
    }

    public OptimizedSelect plan(Evaluator evaluator) {
        return planner().plan(evaluator);
    }

    public Planner planner() {
        return new Planner(what, from, where, groupBy, distinct, orderBy, limit);
    }

}
