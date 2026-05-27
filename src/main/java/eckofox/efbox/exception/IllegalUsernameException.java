package eckofox.efbox.exception;

public class IllegalUsernameException extends IllegalArgumentException {
    public IllegalUsernameException(String message){
        super(message);
    }

    public IllegalUsernameException(Throwable cause) {
        super(cause);
    }

    public IllegalUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}
