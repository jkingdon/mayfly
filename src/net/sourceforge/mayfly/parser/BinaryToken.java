package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.util.ImmutableByteArray;

public class BinaryToken extends Token {

    private final ImmutableByteArray bytes;

    public BinaryToken(ImmutableByteArray binaryValue, Location location) {
        super(TokenType.BINARY, location);
        this.bytes = binaryValue;
    }
    
    @Override
    public ImmutableByteArray getBytes() {
        return bytes;
    }

    @Override
    public Token withCommand(String command) {
        return new BinaryToken(bytes, location.withCommand(command));
    }

}
