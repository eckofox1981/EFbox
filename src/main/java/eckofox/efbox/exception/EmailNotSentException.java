package eckofox.efbox.exception;

public class EmailNotSentException extends Exception{
    public EmailNotSentException(String message){
        super(message);
    }

    public EmailNotSentException(Throwable cause) {
        super(cause);
    }

    public EmailNotSentException(String message, Throwable cause) {
        super(message, cause);
    }
}
