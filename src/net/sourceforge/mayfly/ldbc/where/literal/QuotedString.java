package net.sourceforge.mayfly.ldbc.where.literal;

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

    public Object valueForCellContentComparison() {
        return stringWithoutQuotes();
    }

    protected Cell valueAsCell() {
        return new StringCell(stringWithoutQuotes());
    }
    
    public String displayName() {
        return stringInQuotes;
    }

}
