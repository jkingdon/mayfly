package net.sourceforge.mayfly.ldbc;

import net.sourceforge.mayfly.util.*;

public class Literal extends ValueObject {

    public static class QuotedString extends Literal {
        private String stringInQuotes;

        public QuotedString(String stringInQuotes) {
            this.stringInQuotes = stringInQuotes;
        }

        public static QuotedString fromTree(Tree tree) {
            return new QuotedString(tree.getText());
        }
    }
}
