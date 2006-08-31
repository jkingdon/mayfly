package net.sourceforge.mayfly.datastore;

import net.sourceforge.mayfly.MayflyException;
import net.sourceforge.mayfly.UnimplementedException;

import java.sql.SQLException;

public class UnimplementedCell extends Cell {

    private final String expression;

    public UnimplementedCell(String expression) {
        this.expression = expression;
    }

    public byte asByte() throws SQLException {
        throw makeException();
    }

    public double asDouble() throws SQLException {
        throw makeException();
    }

    public int asInt() throws SQLException {
        throw makeException();
    }

    public long asLong() throws MayflyException {
        throw makeException();
    }

    public Object asObject() {
        throw makeException();
    }

    public short asShort() throws SQLException {
        throw makeException();
    }

    public String asString() {
        throw makeException();
    }

    public int compareTo(Cell otherCell) {
        throw makeException();
    }

    public String displayName() {
        throw makeException();
    }

    private UnimplementedException makeException() {
        return new UnimplementedException(expression + " is not implemented");
    }

}
