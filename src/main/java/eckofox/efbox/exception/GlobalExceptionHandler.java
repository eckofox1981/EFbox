package eckofox.efbox.exception;

import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.security.bruteforceprotection.ExceptionBruteForceProtectionService;
import eckofox.efbox.user.User;
import eckofox.efbox.user.UserRepository;
import eckofox.efbox.user.UserRole;
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
import java.util.List;
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
    private final ExceptionBruteForceProtectionService bruteForceProtectionService;

    @ExceptionHandler(AccessCodeDoesNotExistsException.class)
    public ResponseEntity<String> handleAccessCodeDoesNotExistsException(AccessCodeDoesNotExistsException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING,
                        ExceptionType.ACCESS_CODE_DOES_NOT_EXIST_EXCEPTION,
                        403,
                        exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("You don't seem to have requested a code.");
    }

    @ExceptionHandler(AccessCodeDoesNotMatchException.class)
    public ResponseEntity<String> handleAccessCodeDoesNotMatchException(AccessCodeDoesNotMatchException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING,
                        ExceptionType.ACCESS_CODE_DOES_NOT_MATCH,
                        403,
                        exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Your code does not match your request.");
    }

    @ExceptionHandler(AccessException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.ACCESS_EXCEPTION, 403, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(EmailNotSentException.class)
    public ResponseEntity<String> handleEmailNotSentException(EmailNotSentException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.EMAIL_NOT_SENT_EXCEPTION, 400, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error while sending the email.");
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<String> handleFileValidationException(FileValidationException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.FILE_VALIDATION_EXCEPTION, 422, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("The file was not accepted.");
    }

    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleIllegalAccessException(IllegalAccessException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg =
                messageCreator(
                        LogEventType.WARNING, ExceptionType.ILLEGAL_ACCESS_EXCEPTION, 403, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error accessing target (may not exist).");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.ILLEGAL_ARGUMENT_EXCEPTION, 406, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("The data entered was not correct.");
    }

    @ExceptionHandler(IllegalRegexException.class)
    public ResponseEntity<String> handleIllegalRegexException(IllegalRegexException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.ILLEGAL_REGEX_EXCEPTION, 422, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("You have used forbidden characters (ex: <,>,:,?,=,...).");
    }

    @ExceptionHandler(IllegibleEmailFormatException.class)
    public ResponseEntity<String> handleIllegibleEmailFormatException(IllegibleEmailFormatException exception) throws EmailNotSentException {
        recordToExceptionCache(ExceptionType.ILLEGIBLE_EMAIL_FORMAT_EXCEPTION);
        //NOTE: not recorded
        return ResponseEntity.status(406).body("Your email is not a valid email. Please check.");
    }

    @ExceptionHandler(IllegiblePasswordException.class)
    public ResponseEntity<String> handleIllegiblePasswordException(IllegiblePasswordException exception) throws EmailNotSentException {

        recordToExceptionCache(ExceptionType.ILLEGIBLE_PASSWORD_EXCEPTION);
        //NOTE: exception not recorded.
        return ResponseEntity
                .status(406)
                .body("""
                        Password not eligible. Requirements:
                        - 8 to 64 characters,
                        - lower and uppercase characters,
                        - at least one special character (@, $, €, ¥, !, %, *, ?, &).
                        """);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIllegalIOException(IOException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.IO_EXCEPTION, 406, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(LoginException.class)
    public  ResponseEntity<String> handleLoginException(LoginException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.LOGIN_EXCEPTION, 401, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Login failed.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public  ResponseEntity<String> handleNoSuchElementException(NoSuchElementException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.NO_SUCH_ELEMENT_EXCEPTION, 404, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Not found.");
    }

    @ExceptionHandler(RepetitiveLoginAttemptsException.class)
    public  ResponseEntity<String> handleRepetitiveLoginAttemptsException(RepetitiveLoginAttemptsException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING,
                ExceptionType.REPETITIVE_LOGIN_ATTEMPTS_EXCEPTION,
                423,
                exception.getMessage()
        );

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode())
                .body("You are temporarily locked out of your account. Email sent to account owner.");
    }

    @ExceptionHandler(ServletException.class)
    public  ResponseEntity<String> handleServletException(ServletException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.SERVLET_EXCEPTION, 400, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error processing request.");
    }

    @ExceptionHandler(SQLException.class)
    public  ResponseEntity<String> handleSQLException(SQLException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.ERROR, ExceptionType.SQL_EXCEPTION, 400, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error accessing database.");
    }

    @ExceptionHandler(NoTokenFoundException.class)
    public  ResponseEntity<String> handleNoTokenFoundExceptionException(NoTokenFoundException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.ERROR, ExceptionType.NO_TOKEN_FOUND_EXCEPTION, 416, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error accessing database.");
    }

    @ExceptionHandler(Exception.class)
    public  ResponseEntity<String> handleUndefinedException(Exception exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.UNDEFINED_EXCEPTION, 400, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Something went wrong during the request.");
    }

    @ExceptionHandler(UnsafePasswordException.class)
    public ResponseEntity<String> handleUnsafePasswordException(UnsafePasswordException exception) throws EmailNotSentException {
        //NOTE: no recording since it doesn't affect the service

        recordToExceptionCache(ExceptionType.UNSAFE_PASSWORD_EXCEPTION);
        return ResponseEntity.status(406)
                .body("This password is in the list of common passwords tested by hackers.\n"
                        + "Your account would not be safe with this password. Please choose another one.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public  ResponseEntity<String> handleUserNotFoundException(UserNotFoundException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.WARNING, ExceptionType.USER_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Error accessing user.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public  ResponseEntity<String> usernameNotFoundException(UsernameNotFoundException exception) throws EmailNotSentException {
        EFBoxErrorMessage errMsg = messageCreator(
                LogEventType.ERROR, ExceptionType.USERNAME_NOT_FOUND_EXCEPTION, 404, exception.getMessage());

        recordToExceptionCache(errMsg.getExceptionType());
        return ResponseEntity.status(errMsg.getCode()).body("Authentication failed.");
    }

    private void recordToExceptionCache(ExceptionType exceptionType) throws EmailNotSentException {
        bruteForceProtectionService.exceptionTypeRecord(exceptionType);
    }

    private EFBoxErrorMessage messageCreator(
            LogEventType eventType, ExceptionType exceptionType, int code, String message
    ) {
        //TODO: regression testing remove at end of project
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = new User(
                null,
                "AnonymousUser",
                "-",
                "-",
                "-",
                "-",
                List.of(UserRole.ROLE_ANONYMOUS),
                List.of()
        );

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
