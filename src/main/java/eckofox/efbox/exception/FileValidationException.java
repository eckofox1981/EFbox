package eckofox.efbox.exception;

public class FileValidationException extends IllegalArgumentException {
    public FileValidationException(String message){
        super(message);
    }

    public FileValidationException(Throwable cause) {
        super(cause);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
