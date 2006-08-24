package net.sourceforge.mayfly.parser;

import junit.framework.TestCase;

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
            new Token(TokenType.IDENTIFIER, "x", -1, -1, -1, -1)
        });
        LexerTest.check(
            new TokenType[] { 
                TokenType.NUMBER, TokenType.EQUAL, TokenType.IDENTIFIER },
            new String[] { "5", null, "x" },
            Substitutor.substitute(tokens, Collections.singletonList(new Long(5)))
        );
    }

    private Token makeToken(TokenType tokenType) {
        return new Token(tokenType, null, -1, -1, -1 ,-1);
    }
    
    public void testString() throws Exception {
        List tokens = Collections.singletonList(makeToken(TokenType.PARAMETER));
        LexerTest.check(
            new TokenType[] { TokenType.QUOTED_STRING },
            new String[] { "'can''t'" },
            Substitutor.substitute(tokens, Collections.singletonList("can't"))
        );
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
