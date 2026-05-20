package eckofox.EFbox.exception;

public class IllegiblePasswordException extends IllegalArgumentException {
    public IllegiblePasswordException(String message){
        super(message);
    }

    public IllegiblePasswordException(Throwable cause) {
        super(cause);
    }

    public IllegiblePasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
