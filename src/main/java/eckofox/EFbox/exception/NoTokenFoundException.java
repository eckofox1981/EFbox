package eckofox.EFbox.exception;

public class NoTokenFoundException extends Exception {
    public NoTokenFoundException(String message) {
        super(message);
    }

    public NoTokenFoundException(Throwable cause) {
        super(cause);
    }

    public NoTokenFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
