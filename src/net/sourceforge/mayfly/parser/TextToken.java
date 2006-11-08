package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;

public class TextToken extends Token {

    private final String text;

    public TextToken(TokenType type, String text, Token oldToken) {
        this(type, text, oldToken.location);
    }

    public TextToken(TokenType type, String text, 
        int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
        this(type, text, 
            new Location(startLineNumber, startColumn, endLineNumber, endColumn,
                null));
    }

    public TextToken(TokenType type, String text, Location location) {
        super(type, location);
        if (type == TokenType.BINARY || type == TokenType.END_OF_FILE) {
            throw new MayflyInternalException(
                "Can't have a text token of type " + type);
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Token withCommand(String command) {
        return new TextToken(type, text, location.withCommand(command));
    }
    
}
