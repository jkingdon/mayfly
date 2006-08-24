package net.sourceforge.mayfly.parser;

import net.sourceforge.mayfly.MayflyInternalException;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Substitutor {

    public static List substitute(List tokens, List parameters) {
        List result = new ArrayList();
        Iterator parameterIterator = parameters.iterator();
        for (Iterator tokenIterator = tokens.iterator(); tokenIterator.hasNext();) {
            Token token = (Token) tokenIterator.next();
            if (token.getType() == TokenType.PARAMETER) {
                Object value = parameterIterator.next();
                result.add(tokenFromValue(value, token));
            }
            else {
                result.add(token);
            }
        }
        return result;
    }
    
    public static int parameterCount(List tokens) {
        int count = 0;
        for (Iterator iter = tokens.iterator(); iter.hasNext();) {
            Token token = (Token) iter.next();
            if (token.getType() == TokenType.PARAMETER) {
                ++count;
            }
        }
        return count;
    }

    private static Token tokenFromValue(Object value, Token oldToken) {
        if (value instanceof Number) {
            Number numberValue = (Number) value;
            return new Token(TokenType.NUMBER, numberValue.toString(), oldToken);
        }
        else if (value instanceof String) {
            String stringValue = (String) value;
            return new Token(TokenType.QUOTED_STRING, 
                "'" + StringEscapeUtils.escapeSql(stringValue) + "'",
                oldToken);
        }
        else if (value == null) {
            return new Token(TokenType.KEYWORD_null, "null", oldToken);
        }
        else {
            throw new MayflyInternalException("Don't know how to substitute a " + value.getClass().getName());
        }
    }

}
