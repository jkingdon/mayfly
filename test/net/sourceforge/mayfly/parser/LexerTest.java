package net.sourceforge.mayfly.parser;

import junit.framework.*;

import net.sourceforge.mayfly.MayflyException;

import java.util.*;

public class LexerTest extends TestCase {
    
    public void testEmpty() throws Exception {
        check(new TokenType[] { TokenType.END_OF_FILE }, new String[] { null }, "");
    }

    public void testSingleCharacterTokens() throws Exception {
        check(
            new TokenType[] {
                TokenType.PERIOD,
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.OPEN_PAREN,
                TokenType.CLOSE_PAREN,
                TokenType.COMMA,
                TokenType.PARAMETER,
                TokenType.END_OF_FILE,
            },
            new String[] { ".", "+", "-", "(", ")", ",", "?", null},
            ".+-(),?"
        );
    }
    
    public void testSlashAndStar() throws Exception {
        // At least for now, we don't recognize /* */ style comments,
        // so this is simple
        check(
            new TokenType[] {
                TokenType.DIVIDE,
                TokenType.ASTERISK,
                TokenType.END_OF_FILE,
            },
            new String[] { "/", "*", null},
            "/ *"
        );
    }
    
    public void testLessThanAndGreaterThan() throws Exception {
        check(
            new TokenType[] {
                TokenType.LESS,
                TokenType.LESS_GREATER,
                TokenType.LESS_EQUAL,
                TokenType.LESS,
                TokenType.PLUS,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.END_OF_FILE
            },
            new String[] { "<", "<>", "<=", "<", "+", ">", ">=", null},
            "<<><=<+>>="
        );
    }
    
    public void testEquals() throws Exception {
        check(
            new TokenType[] {
                TokenType.EQUAL,
                TokenType.BANG_EQUAL,
                TokenType.EQUAL,
                TokenType.END_OF_FILE
            },
            new String[] { "=", "!=", "=", null},
            "=!=="
        );
    }
    
    public void testBangWithoutEqualsAtEnd() throws Exception {
        try {
            lex("!");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '=' but got end of file",
                e.getMessage());
        }
    }

    public void testBangWithoutEqualsInMiddle() throws Exception {
        try {
            lex("!!");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '=' but got '!'",
                e.getMessage());
        }
    }

    public void testVerticalBars() throws Exception {
        check(
            new TokenType[] {
                TokenType.CONCATENATE,
                TokenType.CONCATENATE,
                TokenType.END_OF_FILE
            },
            new String[] { "||", "||", null},
            "||||"
        );
    }

    public void testSingleVerticalBar() throws Exception {
        try {
            lex("|||");
            fail();
        } catch (MayflyException e) {
            assertEquals("expected '|' but got end of file",
                e.getMessage());
        }
    }

    
    public void testUnexpectedCharacter() throws Exception {
        try {
            lex("{");
            fail();
        } catch (MayflyException e) {
            assertEquals("unexpected character '{'", e.getMessage());
        }
    }

    public void testDescribeCharacter() throws Exception {
        try {
            lex("\u0000");
            fail();
        } catch (MayflyException e) {
            assertEquals("unexpected character 0x0", e.getMessage());
        }
        
        assertEquals("0x0", new Lexer().describeCharacter(0x0));
        assertEquals("0x1", new Lexer().describeCharacter(0x1));
        assertEquals("0x1f", new Lexer().describeCharacter(0x1f));
        assertEquals("' '", new Lexer().describeCharacter(0x20));
        assertEquals("'~'", new Lexer().describeCharacter(0x7e));
        assertEquals("0x7f", new Lexer().describeCharacter(0x7f));
        assertEquals("0x80", new Lexer().describeCharacter(0x80));
        assertEquals("0x9f", new Lexer().describeCharacter(0x9f));
        assertEquals("0xa0", new Lexer().describeCharacter(0xa0)); // non breaking space
        
        // I think our default behavior has to be to hope that whoever
        // is looking at the message (an IDE, a console, etc) has some
        // way of showing the character.  Certainly if it makes sense to put,
        // say, Russian text in string constants or quoted identifiers,
        // we shouldn't just
        // start displaying messages in hex if there is a quote character
        // missing or the like.
        
        // The flip side is that if the character doesn't show up in
        // a way which lets us identify which character it is, we
        // aren't being very informative.
        assertEquals("'\u00a1'", new Lexer().describeCharacter(0xa1)); // inverted exclamation point
        
        // What about other unicode characters which won't show up clearly (various kinds of spaces,
        // byte-order-mark, line/paragraph separator, etc)?
    }

    public void testIdentifierToEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", null },
            "Foo"
        );
    }

    public void testIdentifierToCharacter() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.COMMA, TokenType.END_OF_FILE },
            new String[] { "Foo", ",", null },
            "Foo,"
        );
    }

    public void testNumberToEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.NUMBER, TokenType.END_OF_FILE },
            new String[] { "42", null },
            "42"
        );
    }

    public void testNumberToCharacter() throws Exception {
        check(new TokenType[] { TokenType.NUMBER, TokenType.COMMA, TokenType.END_OF_FILE },
            new String[] { "42", ",", null },
            "42,"
        );
    }

    public void testIdentifierCharacters() throws Exception {
        check(new TokenType[] {
                TokenType.IDENTIFIER, TokenType.MINUS, TokenType.IDENTIFIER, TokenType.END_OF_FILE
            },
            new String[] { "abcdefghijklmnopqrstuvwxyz", "-", "ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789", null },
            "abcdefghijklmnopqrstuvwxyz-ABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789"
        );
    }

    public void testIdentifierToSpace() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", null },
            "Foo "
        );
    }

    public void testKeywords() throws Exception {
        check(new TokenType[] {
                TokenType.KEYWORD_all,
                TokenType.KEYWORD_and,
                TokenType.KEYWORD_asc,
                TokenType.KEYWORD_authorization,
                TokenType.KEYWORD_avg,
                TokenType.KEYWORD_by,
                TokenType.KEYWORD_count,
                TokenType.KEYWORD_create,
                TokenType.KEYWORD_cross,
                TokenType.KEYWORD_desc,
                TokenType.KEYWORD_distinct,
                TokenType.KEYWORD_drop,
                TokenType.KEYWORD_from,
                TokenType.KEYWORD_group,
                TokenType.KEYWORD_in,
                TokenType.KEYWORD_inner,
                TokenType.KEYWORD_insert,
                TokenType.KEYWORD_integer,
                TokenType.KEYWORD_into,
                TokenType.KEYWORD_is,
                TokenType.KEYWORD_join,
                TokenType.KEYWORD_left,
                TokenType.KEYWORD_limit,
                TokenType.KEYWORD_max,
                TokenType.KEYWORD_min,
                TokenType.KEYWORD_not,
                TokenType.KEYWORD_null,
                TokenType.KEYWORD_offset,
                TokenType.KEYWORD_on,
                TokenType.KEYWORD_or,
                TokenType.KEYWORD_order,
                TokenType.KEYWORD_outer,
                TokenType.KEYWORD_schema,
                TokenType.KEYWORD_select,
                TokenType.KEYWORD_set,
                TokenType.KEYWORD_sum,
                TokenType.KEYWORD_table,
                TokenType.KEYWORD_values,
                TokenType.KEYWORD_varchar,
                TokenType.KEYWORD_where,
                TokenType.END_OF_FILE
            },
            new String[] {
                "ALL",
                "AND",
                "ASC",
                "AUTHORIZATION",
                "AVG",
                "BY",
                "COUNT",
                "CREATE",
                "CROSS",
                "DESC",
                "DISTINCT",
                "DROP",
                "FROM",
                "GROUP",
                "IN",
                "INNER",
                "INSERT",
                "INTEGER",
                "INTO",
                "IS",
                "JOIN",
                "LEFT",
                "LIMIT",
                "MAX",
                "MIN",
                "NOT",
                "NULL",
                "OFFSET",
                "ON",
                "OR",
                "ORDER",
                "OUTER",
                "SCHEMA",
                "SELECT",
                "SET",
                "SUM",
                "TABLE",
                "VALUES",
                "VARCHAR",
                "WHERE",
                null
            },
            "ALL AND ASC AUTHORIZATION AVG BY COUNT CREATE CROSS DESC " +
            "DISTINCT DROP FROM GROUP IN INNER INSERT INTEGER INTO " +
            "IS JOIN LEFT LIMIT MAX MIN NOT NULL OFFSET ON OR ORDER " +
            "OUTER SCHEMA SELECT SET SUM TABLE VALUES VARCHAR WHERE"
        );
    }

    public void testQuotedIdentifier() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { " !#$%^&*',+-/*", null },
            " \" !#$%^&*',+-/*\" "
        );
    }

    public void testQuotedIdentifierEndsAtEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "foo", null },
            "\"foo\""
        );
    }

    public void testUnterminatedQuotedIdentifier() throws Exception {
        try {
            lex(" \"foo ");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated quoted identifier", e.getMessage());
        }
    }

    public void testString() throws Exception {
        check(new TokenType[] { 
                TokenType.QUOTED_STRING, 
                TokenType.QUOTED_STRING, 
                TokenType.COMMA, 
                TokenType.QUOTED_STRING, 
                TokenType.END_OF_FILE
            },
            new String[] { "'don''t'", "'a'''", ",", "'b''''c'", null },
            " 'don''t' 'a''','b''''c'"
        );
    }

    public void testStringEndsAtEndOfFile() throws Exception {
        check(new TokenType[] { TokenType.QUOTED_STRING, TokenType.END_OF_FILE },
            new String[] { "'he said \"hi\"'", null },
            "'he said \"hi\"'"
        );
    }

    public void testUnterminatedString() throws Exception {
        try {
            lex(" 'foo ");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated string", e.getMessage());
        }
    }

    public void testUnterminatedStringAfterEscape() throws Exception {
        try {
            lex(" 'foo''");
            fail();
        } catch (MayflyException e) {
            assertEquals("unterminated string", e.getMessage());
        }
    }

    public void testSpace() throws Exception {
        check(new TokenType[] { TokenType.IDENTIFIER, TokenType.IDENTIFIER, TokenType.END_OF_FILE },
            new String[] { "Foo", "BAR", null },
            "\tFoo BAR\n\r\n\n"
        );
    }
    
    private void check(TokenType[] expectedTypes, String[] expectedTexts, String input) {
        assertEquals(expectedTexts.length, expectedTypes.length);
        List actual = lex(input);
        List actualTexts = new ArrayList(actual.size());
        List actualTypes = new ArrayList(actual.size());
        for (int i = 0; i < actual.size(); ++i) {
            Token token = (Token) actual.get(i);
            actualTypes.add(token.getType());
            actualTexts.add(token.getText());
        }
        
        assertEquals(Arrays.asList(expectedTypes), actualTypes);
        assertEquals(Arrays.asList(expectedTexts), actualTexts);
    }
    
    private List lex(String sql) {
        return new Lexer(sql).tokens();
    }

}
