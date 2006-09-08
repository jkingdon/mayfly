package net.sourceforge.mayfly.parser;

public class EndOfFileToken extends Token {

    public EndOfFileToken(int line, int column) {
        super(TokenType.END_OF_FILE, line, column, line, column);
    }

}
