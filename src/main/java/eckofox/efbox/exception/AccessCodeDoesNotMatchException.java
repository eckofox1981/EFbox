package eckofox.efbox.exception;

public class AccessCodeDoesNotMatchException extends IllegalArgumentException {

    public AccessCodeDoesNotMatchException(String message) {
        super(message);
    }

    public AccessCodeDoesNotMatchException(Throwable cause) {
        super(cause);
    }

    public AccessCodeDoesNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
