package net.sourceforge.mayfly.evaluation;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.parser.Location;

/**
   @internal
   We record location information during expression evaluation.  However, we don't
   store the line numbers of where the data was inserted or updated
   in the {@link net.sourceforge.mayfly.datastore.DataStore} itself,
   because they wouldn't make any sense without also including a stack trace of
   where they were created (and perhaps other information like filenames), 
   and it is far from clear that
   the information would be particularly helpful.
 */
public class Value {
    public final Cell value;
    public final Location location;

    public Value(Cell value, Location location) {
        this.value = value;
        this.location = location;
    }
}
