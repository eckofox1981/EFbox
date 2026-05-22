package eckofox.EFbox.exception;

import eckofox.EFbox.logger.LogEventType;
import eckofox.EFbox.logger.LoggerService;
import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.LoginException;
import java.io.IOException;

import java.rmi.AccessException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/** inspired by https://www.ignek.com/blog/centralized-exception-handling-in-spring-boot
 * https://medium.com/@AlexanderObregon/spring-boot-global-exception-handling-with-restcontrolleradvice-676c5b0b74ea
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final LoggerService loggerService;
    private final UserRepository userRepository;

    @ExceptionHandler(AccessException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.ACCESS_EXCEPTION, 403, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<String> handleFileValidationException(FileValidationException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.FILE_VALIDATION_EXCEPTION, 422, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleIllegalAccessException(IllegalAccessException exception) {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.ILLEGAL_ACCESS_EXCEPTION, 403, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error accessing target (may not exist).");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("The data entered was not correct.");
    }

    @ExceptionHandler(IllegalRegexException.class)
    public ResponseEntity<String> handleIllegalRegexException(IllegalRegexException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.ILLEGAL_REGEX_EXCEPTION, 422, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("You have used forbidden characters (ex: <,>,:,?,=,...).");
    }

    @ExceptionHandler(IllegiblePasswordException.class)
    public ResponseEntity<String> handleIllegiblePasswordException(IllegiblePasswordException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.ILLEGIBLE_PASSWORD_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity
                .status(errMsg.getCode())
                .body("Password not eligible. Requirements: 5 letters minimum, lower and uppercase characters and at least one digit.");
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIllegalIOException(IOException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.IO_EXCEPTION, 406, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(LoginException.class)
    public  ResponseEntity<String> handleLoginException(LoginException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.LOGIN_EXCEPTION, 401, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Login failed.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public  ResponseEntity<String> handleNoSuchElementException(NoSuchElementException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.NO_SUCH_ELEMENT_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Not found.");
    }

    @ExceptionHandler(ServletException.class)
    public  ResponseEntity<String> handleServletException(ServletException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.SERVLET_EXCEPTION, 400, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error processing request.");
    }

    @ExceptionHandler(SQLException.class)
    public  ResponseEntity<String> handleSQLException(SQLException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.ERROR, ExceptionType.SQL_EXCEPTION, 400, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error accessing database.");
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<String> handleUndefinedException(Exception exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.UNDEFINED_EXCEPTION, 400, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public  ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.USER_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Error accessing user.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public  ResponseEntity<String> usernameNotFoundException(UsernameNotFoundException exception) {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.ERROR, ExceptionType.USERNAME_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        return ResponseEntity.status(errMsg.getCode()).body("Authentication failed.");
    }

    private EFBoxErrorMessage messageCreator(
            LogEventType eventType, ExceptionType exceptionType, int code, String message
    ) {
        //TODO: regression testing remove at end of project
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = new User(null, "AnonymousUser", "-", "-", "-");

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            user = userRepository.findByUsername(currentUserName)
                    .orElse(user);
        }

        message = message.length() > 255 ? message.substring(0, 255) : message;

        EFBoxErrorMessage errMsg = new EFBoxErrorMessage(
                UUID.randomUUID(),
                eventType,
                LocalDateTime.now(),
                message,
                user,
                exceptionType,
                code
        );

        loggerService.saveInfoLogg(errMsg);

        return errMsg;
    }
}
