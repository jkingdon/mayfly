package net.sourceforge.mayfly.ldbc;

public class Literal extends ValueObject {

    public static class QuotedString extends Literal {
        private String stringInQuotes;

        public QuotedString(String stringInQuotes) {
            this.stringInQuotes = stringInQuotes;
        }
    }
}
