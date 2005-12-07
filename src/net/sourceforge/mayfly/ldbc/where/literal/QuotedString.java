package net.sourceforge.mayfly.ldbc.where.literal;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.datastore.*;
import net.sourceforge.mayfly.ldbc.*;

public class QuotedString extends Literal {
    private String stringInQuotes;

    public QuotedString(String stringInQuotes) {
        this.stringInQuotes = stringInQuotes;
    }

    public static QuotedString fromQuotedStringTree(Tree tree) {
        return new QuotedString(tree.getText());
    }

    private String stringWithoutQuotes() {
        String withoutQuotes = stringInQuotes.substring(1, stringInQuotes.length()-1);
        return withoutQuotes.replaceAll("''", "'");
    }

    public Cell evaluate(Row row) {
        throw new UnimplementedException();
    }

    public Object valueForCellContentComparison() {
        return stringWithoutQuotes();
    }

}
