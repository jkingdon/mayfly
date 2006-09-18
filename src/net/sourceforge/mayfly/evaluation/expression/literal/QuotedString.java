package net.sourceforge.mayfly.evaluation.expression.literal;

import net.sourceforge.mayfly.datastore.Cell;
import net.sourceforge.mayfly.datastore.StringCell;
import net.sourceforge.mayfly.evaluation.Expression;
import net.sourceforge.mayfly.parser.Location;

public class QuotedString extends Literal {
    private final String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this(stringInQuotes, Location.UNKNOWN);
    }

    public QuotedString(String stringInQuotes, Location location) {
        super(location);
        this.stringInQuotes = stringInQuotes;
    }

    public String stringWithoutQuotes() {
        String withoutQuotes = stringInQuotes.substring(1, stringInQuotes.length()-1);
        return withoutQuotes.replaceAll("''", "'");
    }

    public Cell valueAsCell() {
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
    
    public int hashCode() {
        final int PRIME = 31;
        int result = super.hashCode();
        result = PRIME * result + ((stringInQuotes == null) ? 0 : stringInQuotes.hashCode());
        return result;
    }

    /**
     * For tests which want to check the string but don't
     * want the test to have to contain the expected location.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final QuotedString other = (QuotedString) obj;
        if (stringInQuotes == null) {
            if (other.stringInQuotes != null)
                return false;
        }
        else if (!stringInQuotes.equals(other.stringInQuotes))
            return false;
        return true;
    }

}
