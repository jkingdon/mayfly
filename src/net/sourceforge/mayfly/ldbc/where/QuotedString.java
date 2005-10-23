package net.sourceforge.mayfly.ldbc.where;

import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class QuotedString extends Literal {
    private String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this.stringInQuotes = stringInQuotes;
    }

    public static net.sourceforge.mayfly.ldbc.where.QuotedString fromTree(Tree tree) {
        return new net.sourceforge.mayfly.ldbc.where.QuotedString(tree.getText());
    }

    public boolean matchesCell(Cell cell) {
        return stringWithoutQuotes().equals(cell.asString());
    }

    private String stringWithoutQuotes() {
        return stringInQuotes.substring(1, stringInQuotes.length()-1);
    }
}
