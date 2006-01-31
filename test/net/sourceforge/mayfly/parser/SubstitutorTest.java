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
            new Token(TokenType.PARAMETER), new Token(TokenType.EQUAL), new Token(TokenType.IDENTIFIER, "x")
        });
        LexerTest.check(
            new TokenType[] { TokenType.NUMBER, TokenType.EQUAL, TokenType.IDENTIFIER },
            new String[] { "5", null, "x" },
            Substitutor.substitute(tokens, Collections.singletonList(new Long(5)))
        );
    }
    
    public void testString() throws Exception {
        List tokens = Collections.singletonList(new Token(TokenType.PARAMETER));
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

}
