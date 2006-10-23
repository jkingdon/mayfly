package net.sourceforge.mayfly.parser;

public class EndOfFileToken extends Token {

    public EndOfFileToken(int line, int column, String command) {
        super(TokenType.END_OF_FILE, 
            new Location(line, column, line, column, command));
    }

}
