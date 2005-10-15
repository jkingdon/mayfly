package net.sourceforge.mayfly.ldbc;

public class Select extends ValueObject {
    public static Select fromSql(String sql) {
        return new Select();
    }
}
