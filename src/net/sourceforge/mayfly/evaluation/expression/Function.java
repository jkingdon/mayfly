package net.sourceforge.mayfly.evaluation.expression;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.MayflyInternalException;
import net.sourceforge.mayfly.Options;
import net.sourceforge.mayfly.UnimplementedException;
import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.ResultRows;
import net.sourceforge.mayfly.evaluation.select.Evaluator;
import net.sourceforge.mayfly.parser.Location;
import net.sourceforge.mayfly.util.ImmutableList;

public class Function extends Expression {

    public static Expression create(
        String name, ImmutableList<Expression> arguments, 
        Location location, Options options) {
        if (name.equalsIgnoreCase("concat")) {
            return concat(arguments, location, options);
        }
        else {
            return new Function(name, arguments, location, options);
        }
    }

    private static Expression concat(ImmutableList<Expression> arguments, 
        Location location, Options options) {
        if (arguments.isEmpty()) {
            throw new MayflyInternalException(
                "parser should require at least one argument");
        }
        else if (arguments.size() == 1) {
            return arguments.get(0);
        }
        else {
            /* Not sure associativity matters, but we turn concat(a, b, c)
               into (a || b) || c not a || (b || c) */
            Expression last = arguments.last();
            Expression allButLast = concat(
                arguments.allButLast(), location, options);
            return new Concatenate(allButLast, last);
        }
    }

    final String className;
    final String methodName;
    final Class classObject;

    public Function(String name, ImmutableList<Expression> arguments,
        Location location, Options options) {
        super(location);
        int lastPeriod = name.lastIndexOf('.');
        if (lastPeriod == -1) {
            /* The more likely reason is not that a period was intended,
               but that a built-in or aliased function is misspelled. */
            throw new MayflyException("no function " + name, location);
//            throw new MayflyException("function name " + name + 
//                " does not contain a period", location);
        }
        this.className = name.substring(0, lastPeriod);
        this.methodName = name.substring(lastPeriod + 1);

        try {
            this.classObject = Class.forName(className);
        } catch (ClassNotFoundException e) {
            /* Is there any reason to supply e as the cause?
             * Normally, I'm loath to potentially destroy information about
             * what happened.  But is there any interesting information to
             * destroy here?
             */
            throw new MayflyException(
                "function name specifies Java class " + 
                className + " which is not found", location);
        }
        
//        try {
//            classObject.getMethod(methodName, new Class[0]);
//        }
//        catch (NoSuchMethodException e) {
//            throw new MayflyException(
//                "function name specifies method " + methodName +
//                " which is not found in " + classObject.getName(),
//                location);
//        }
    }
    
    @Override
    public Cell aggregate(ResultRows rows) {
        throw new UnimplementedException();
    }

    @Override
    public Cell evaluate(ResultRow row, Evaluator evaluator) {
        throw new UnimplementedException();
    }

    @Override
    public boolean sameExpression(Expression other) {
        throw new UnimplementedException();
    }

    @Override
    public String displayName() {
        return "function call";
    }

}
