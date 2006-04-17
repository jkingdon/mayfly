package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Expression;

public class QuotedString extends Literal {
    private final String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this.stringInQuotes = stringInQuotes;
    }

    private String stringWithoutQuotes() {
        String withoutQuotes = stringInQuotes.substring(1, stringInQuotes.length()-1);
        return withoutQuotes.replaceAll("''", "'");
    }

    protected Cell valueAsCell() {
        return new StringCell(stringWithoutQuotes());
    }
    
    public String displayName() {
        return stringInQuotes;
    }

    public boolean sameExpression(Expression other) {
        if (other instanceof QuotedString) {
            QuotedString string = (QuotedString) other;
            return stringInQuotes.equals(string.stringInQuotes);
        }
        else {
            return false;
        }
    }

}
