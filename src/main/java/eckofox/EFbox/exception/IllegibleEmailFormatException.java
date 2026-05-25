package eckofox.EFbox.exception;

public class IllegibleEmailFormatException extends IllegiblePasswordException {
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
