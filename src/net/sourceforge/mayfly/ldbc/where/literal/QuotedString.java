package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class QuotedString extends Literal {
    private String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this.stringInQuotes = stringInQuotes;
    }

    public static net.sourceforge.mayfly.ldbc.where.literal.QuotedString fromQuotedStringTree(Tree tree) {
        return new net.sourceforge.mayfly.ldbc.where.literal.QuotedString(tree.getText());
    }

    public boolean matchesCell(Cell cell) {
        return stringWithoutQuotes().equals(cell.asString());
    }

    private String stringWithoutQuotes() {
        return stringInQuotes.substring(1, stringInQuotes.length()-1);
    }

    public Object valueForCellContentComparison() {
        return stringWithoutQuotes();
    }
}
