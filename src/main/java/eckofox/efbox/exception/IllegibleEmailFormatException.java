package eckofox.efbox.exception;

public class IllegibleEmailFormatException extends IllegalArgumentException {
    public IllegibleEmailFormatException(String message) {
        super(message);
    }

    public IllegibleEmailFormatException(Throwable cause) {
        super(cause);
    }

    public IllegibleEmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
