package eckofox.EFbox.exception;

public class UnsafePasswordException extends IllegalArgumentException {
    public UnsafePasswordException(String message) {
        super(message);
    }

    public UnsafePasswordException(Throwable cause) {
        super(cause);
    }

    public UnsafePasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
