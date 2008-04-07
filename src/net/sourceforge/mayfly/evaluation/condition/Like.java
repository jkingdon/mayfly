package net.sourceforge.mayfly.evaluation.condition;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.NullCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.evaluation.ResultRow;
import net.sourceforge.mayfly.evaluation.select.Evaluator;

import java.util.regex.Pattern;

public class Like extends RowExpression {

    public Like(Expression left, Expression pattern) {
        super(left, pattern);
    }

    @Override
    protected boolean compare(Cell left, Cell right) {
        if (left instanceof NullCell || right instanceof NullCell) {
            return false;
        }
        String candidate = left.asString();
        String pattern = right.asString();
        return compare(candidate, pattern);
    }

    static boolean compare(String candidate, String pattern) {
        String regex = 
            quote(pattern)
                .replaceAll("%", ".*")
                .replaceAll("_", ".")
                ;
        return Pattern.matches(regex, candidate);
    }

    static String quote(String in) {
        return in
            .replace("\\", "\\\\")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace(".", "\\.")
            .replace("$", "\\$")
            .replace("^", "\\^")
            .replace("?", "\\?")
            .replace("*", "\\*")
            .replace("+", "\\+")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("|", "\\|")
            .replace("(", "\\(")
            .replace(")", "\\)")
            ;
    }

    @Override
    public Condition resolve(ResultRow row, Evaluator evaluator) {
        Expression newLeftSide = leftSide.resolve(row, evaluator);
        Expression newRightSide = rightSide.resolve(row, evaluator);
        if (newLeftSide != leftSide || newRightSide != rightSide) {
            return new Like(newLeftSide, newRightSide);
        }
        else {
            return this;
        }
    }

}
