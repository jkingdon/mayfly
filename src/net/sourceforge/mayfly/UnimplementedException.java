package net.sourceforge.mayfly;

public class UnimplementedException extends RuntimeException {

    public UnimplementedException() {
        super();
    }

    public UnimplementedException(String message) {
        super(message);
    }

    public UnimplementedException(Throwable cause) {
        super(cause);
    }

    public UnimplementedException(String message, Throwable cause) {
        super(message, cause);
    }

}
