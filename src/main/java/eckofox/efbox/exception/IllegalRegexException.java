package eckofox.efbox.exception;

public class IllegalRegexException extends IllegalArgumentException {
    public IllegalRegexException(String message){
        super(message);
    }

    public IllegalRegexException(Throwable cause) {
        super(cause);
    }

    public IllegalRegexException(String message, Throwable cause) {
        super(message, cause);
    }


}
