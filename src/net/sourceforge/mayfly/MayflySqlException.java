package net.sourceforge.mayfly;

import java.sql.SQLException;

public class MayflySqlException extends SQLException {

    public MayflySqlException(String message) {
        super(message);
    }

    public MayflySqlException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public MayflySqlException(Throwable cause) {
        super(cause.getMessage());
        initCause(cause);
    }

    public int startLineNumber() {
        return -1;
    }

    public int startColumn() {
        return -1;
    }

    public int endLineNumber() {
        return -1;
    }

    public int endColumn() {
        return -1;
    }

}
