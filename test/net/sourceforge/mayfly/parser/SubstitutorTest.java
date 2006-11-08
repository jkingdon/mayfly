package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;

import net.sourceforge.mayfly.util.ImmutableByteArray;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubstitutorTest extends TestCase {
    
    public void testEmpty() throws Exception {
        assertEquals(Collections.EMPTY_LIST, Substitutor.substitute(Collections.EMPTY_LIST, Collections.EMPTY_LIST));
    }
    
    public void testNoSubstitutions() throws Exception {
        List tokens = new Lexer("select foo.* from foo where x = 5").tokens();
        List expected = new ArrayList(tokens);
        assertEquals(expected, Substitutor.substitute(tokens, Collections.EMPTY_LIST));
    }

    public void testSubstitute() throws Exception {
        List tokens = Arrays.asList(new Token[] {
            makeToken(TokenType.PARAMETER), makeToken(TokenType.EQUAL), 
            new TextToken(TokenType.IDENTIFIER, "x", -1, -1, -1, -1)
        });
        LexerTest.check(
            new TokenType[] { 
                TokenType.NUMBER, TokenType.EQUAL, TokenType.IDENTIFIER },
            new String[] { "5", null, "x" },
            Substitutor.substitute(tokens, Collections.singletonList(new Long(5)))
        );
    }

    public void testSubstituteDecimal() throws Exception {
        List tokens = Arrays.asList(new Token[] {
            makeToken(TokenType.PARAMETER)
        });
        LexerTest.check(
            new TokenType[] { 
                TokenType.MINUS, TokenType.NUMBER, TokenType.PERIOD,
                TokenType.NUMBER },
            new String[] { "-", "73", ".", "45" },
            Substitutor.substitute(tokens, Collections.singletonList(
                new BigDecimal("-73.45")))
        );
    }

    private Token makeToken(TokenType tokenType) {
        return new TextToken(tokenType, null, Location.UNKNOWN);
    }
    
    public void testString() throws Exception {
        List tokens = Collections.singletonList(makeToken(TokenType.PARAMETER));
        LexerTest.check(
            new TokenType[] { TokenType.QUOTED_STRING },
            new String[] { "'can''t'" },
            Substitutor.substitute(tokens, Collections.singletonList("can't"))
        );
    }
    
    public void testBinary() throws Exception {
        List input = Collections.singletonList(
            new TextToken(TokenType.PARAMETER, "?", 4, 3, 4, 4));
        List output = Substitutor.substitute(input, 
            Collections.singletonList(new ImmutableByteArray( (byte)3 )));
        assertEquals(1, output.size());
        Token token = (Token) output.get(0);
        assertEquals(TokenType.BINARY, token.type);
    }
    
    public void testCount() throws Exception {
        List tokens = new Lexer("select ? + x from foo where x = ? or ? = y").tokens();
        assertEquals(3, Substitutor.parameterCount(tokens));
    }
    
    public void testLineNumbers() throws Exception {
        List unsubstituted = new Lexer("x = ?").tokens();
        List tokens = Substitutor.substitute(
            unsubstituted, Collections.singletonList(new Long(1234567)));
        assertEquals(4, tokens.size());
        Token number = (Token) tokens.get(2);
        assertEquals("1234567", number.getText());
        assertEquals(1, number.startLineNumber());
        assertEquals(5, number.startColumn());
        assertEquals(1, number.endLineNumber());
        assertEquals(6, number.endColumn());
    }

}
