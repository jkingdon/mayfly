package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.util.ImmutableByteArray;

public class BinaryToken extends Token {

    private final ImmutableByteArray bytes;

    public BinaryToken(ImmutableByteArray binaryValue, Token oldToken) {
        super(TokenType.BINARY, oldToken);
        this.bytes = binaryValue;
    }
    
    public ImmutableByteArray getBytes() {
        return bytes;
    }

}
