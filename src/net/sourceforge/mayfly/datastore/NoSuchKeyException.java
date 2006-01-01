package net.sourceforge.mayfly.datastore;

//TODO: Move to util package.
public class NoSuchKeyException extends RuntimeException {

    public NoSuchKeyException() {
        super();
    }

    public NoSuchKeyException(String message) {
        super(message);
    }

    public NoSuchKeyException(Throwable cause) {
        super(cause);
    }

    public NoSuchKeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
