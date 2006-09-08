package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;

public class TextToken extends Token {

    private final String text;

    public TextToken(TokenType type, String text, Token oldToken) {
        this(type, text, 
            oldToken.startLineNumber(), oldToken.startColumn(), 
            oldToken.endLineNumber(), oldToken.endColumn());
    }

    public TextToken(TokenType type, String text, 
        int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
        super(type, startLineNumber, startColumn, endLineNumber, endColumn);
        if (type == TokenType.BINARY || type == TokenType.END_OF_FILE) {
            throw new MayflyInternalException(
                "Can't have a text token of type " + type);
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }
    
}
