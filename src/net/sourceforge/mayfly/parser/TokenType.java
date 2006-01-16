package net.sourceforge.mayfly.parser;

import java.util.HashMap;
import java.util.Map;

public class TokenType {
    
    private static Map keywords = new HashMap();

    private static TokenType register(String description) {
        return new TokenType(description);
    }

    private static TokenType registerKeyword(String lowercaseKeyword) {
        TokenType type = new TokenType(lowercaseKeyword.toUpperCase());
        keywords.put(lowercaseKeyword, type);
        return type;
    }

    public static final TokenType END_OF_FILE = register("end of file");
    public static final TokenType IDENTIFIER = register("identifier");
    public static final TokenType QUOTED_STRING = register("string");
    public static final TokenType NUMBER = register("number");
    public static final TokenType PERIOD = register("'.'");
    public static final TokenType COMMA = register("','");
    public static final TokenType CONCATENATE = register("||");
    public static final TokenType PLUS = register("'+'");
    public static final TokenType MINUS = register("'-'");
    public static final TokenType DIVIDE = register("'/'");
    public static final TokenType ASTERISK = register("'*'");
    public static final TokenType OPEN_PAREN = register("'('");
    public static final TokenType CLOSE_PAREN = register("')'");
    public static final TokenType PARAMETER = register("'?'");
    public static final TokenType LESS = register("'<'");
    public static final TokenType LESS_EQUAL = register("'<='");
    public static final TokenType GREATER = register("'>'");
    public static final TokenType GREATER_EQUAL = register("'>='");
    public static final TokenType EQUAL = register("'='");
    public static final TokenType LESS_GREATER = register("'<>'");
    public static final TokenType BANG_EQUAL = register("'!='");

    public static final TokenType KEYWORD_all = registerKeyword("all");
    public static final TokenType KEYWORD_and = registerKeyword("and");
    public static final TokenType KEYWORD_asc = registerKeyword("asc");
    public static final TokenType KEYWORD_authorization = registerKeyword("authorization");
    public static final TokenType KEYWORD_avg = registerKeyword("avg");
    public static final TokenType KEYWORD_by = registerKeyword("by");
    public static final TokenType KEYWORD_count = registerKeyword("count");
    public static final TokenType KEYWORD_create = registerKeyword("create");
    public static final TokenType KEYWORD_cross = registerKeyword("cross");
    public static final TokenType KEYWORD_desc = registerKeyword("desc");
    public static final TokenType KEYWORD_distinct = registerKeyword("distinct");
    public static final TokenType KEYWORD_drop = registerKeyword("drop");
    public static final TokenType KEYWORD_from = registerKeyword("from");
    public static final TokenType KEYWORD_group = registerKeyword("group");
    public static final TokenType KEYWORD_in = registerKeyword("in");
    public static final TokenType KEYWORD_inner = registerKeyword("inner");
    public static final TokenType KEYWORD_insert = registerKeyword("insert");
    public static final TokenType KEYWORD_integer = registerKeyword("integer");
    public static final TokenType KEYWORD_into = registerKeyword("into");
    public static final TokenType KEYWORD_is = registerKeyword("is");
    public static final TokenType KEYWORD_join = registerKeyword("join");
    public static final TokenType KEYWORD_left = registerKeyword("left");
    public static final TokenType KEYWORD_limit = registerKeyword("limit");
    public static final TokenType KEYWORD_max = registerKeyword("max");
    public static final TokenType KEYWORD_min = registerKeyword("min");
    public static final TokenType KEYWORD_not = registerKeyword("not");
    public static final TokenType KEYWORD_null = registerKeyword("null");
    public static final TokenType KEYWORD_offset = registerKeyword("offset");
    public static final TokenType KEYWORD_on = registerKeyword("on");
    public static final TokenType KEYWORD_or = registerKeyword("or");
    public static final TokenType KEYWORD_order = registerKeyword("order");
    public static final TokenType KEYWORD_outer = registerKeyword("outer");
    public static final TokenType KEYWORD_schema = registerKeyword("schema");
    public static final TokenType KEYWORD_select = registerKeyword("select");
    public static final TokenType KEYWORD_set = registerKeyword("set");
    public static final TokenType KEYWORD_sum = registerKeyword("sum");
    public static final TokenType KEYWORD_table = registerKeyword("table");
    public static final TokenType KEYWORD_values = registerKeyword("values");
    public static final TokenType KEYWORD_varchar = registerKeyword("varchar");
    public static final TokenType KEYWORD_where = registerKeyword("where");

    public static TokenType lookupKeyword(String text) {
        return (TokenType) keywords.get(text.toLowerCase());
    }

    private final String description;

    public TokenType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
    
    public String toString() {
        return description();
    }

}
