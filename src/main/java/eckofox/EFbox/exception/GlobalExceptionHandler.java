package eckofox.EFbox.exception;

import jakarta.servlet.ServletException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/** inspired by https://www.ignek.com/blog/centralized-exception-handling-in-spring-boot
 * https://medium.com/@AlexanderObregon/spring-boot-global-exception-handling-with-restcontrolleradvice-676c5b0b74ea
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
 */
@RestControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(ExceptionType.ACCESS_DENIED_EXCEPTION, 403, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<String> handleFileValidationException(FileValidationException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(ExceptionType.FILE_VALIDATION_EXCEPTION, 422, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleIllegalAccessException(IllegalAccessException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(ExceptionType.ILLEGAL_ACCESS_EXCEPTION, 403, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error accessing target (may not exist).");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The data entered was not correct.");
    }

    @ExceptionHandler(IllegalRegexException.class)
    public ResponseEntity<String> handleIllegalRegexException(IllegalRegexException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.ILLEGAL_REGEX_EXCEPTION, 422, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("You have used forbidden characters (ex: <,>,:,?,=,...).");
    }

    @ExceptionHandler(IllegiblePasswordException.class)
    public ResponseEntity<String> handleIllegiblePasswordException(IllegiblePasswordException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.ILLEGIBLE_PASSWORD_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity
                .status(errMsg.getCode())
                .body("Password not eligible. Requirements: 5 letters minimum, lower and uppercase characters and at least one digit.");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIllegalIOException(IOException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.IO_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(LoginException.class)
    public  ResponseEntity<String> handleLoginException(LoginException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.LOGIN_EXCEPTION, 401, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Login failed.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public  ResponseEntity<String> handleNoSuchElementException(NoSuchElementException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.NO_SUCH_ELEMENT_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Not found.");
    }

    @ExceptionHandler(ServletException.class)
    public  ResponseEntity<String> handleServletException(ServletException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.SERVLET_EXCEPTION, 400, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error processing request.");
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<String> handleUndefinedException(Exception exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.UNDEFINED_EXCEPTION, 400, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public  ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.USER_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error accessing user.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public  ResponseEntity<String> usernameNotFoundException(UsernameNotFoundException exception) {
        EFBoxErrorMessage errMsg = messageCreator(ExceptionType.USERNAME_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Authentication failed.");
    }

    private EFBoxErrorMessage messageCreator(ExceptionType exceptionType, int code, String message) {
        EFBoxErrorMessage errMsg = new EFBoxErrorMessage(
                exceptionType,
                LocalDateTime.now().toString(),
                HttpStatusCode.valueOf(code),
                message
        );
        //TODO: Add logg (for logging-exception branch), printline for now
        System.out.println("| " + errMsg.getTimestamp() + " | " + errMsg.getCode() + " | " +
                errMsg.getExceptionType().getDescription() + " | " + errMsg.getLogMessage() + " |");

        return errMsg;
    }
}
